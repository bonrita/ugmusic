package com.bonrita.ugmusic;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.playback.LocalPlayback;
import com.bonrita.ugmusic.playback.PlaybackManager;
import com.bonrita.ugmusic.playback.QueueManager;
import com.bonrita.ugmusic.ui.NowPlayingActivity;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.List;
// https://github.com/googlesamples/android-MediaBrowserService
public class MusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {

    private static final String TAG = LogHelper.makeLogTag(MusicService.class);
    private MediaSessionCompat mSession;
    private MusicProvider mMusicProvider;
    private PlaybackManager mPlayBackManager;

    /**
     * Called to get the root information for browsing by a particular client.
     * <p>
     * The implementation should verify that the client package has permission
     * to access browse media information before returning the root id; it
     * should return null if the client is not allowed to access this
     * information.
     * </p>
     *
     * @param clientPackageName The package name of the application which is
     *                          requesting access to browse media.
     * @param clientUid         The uid of the application which is requesting access to
     *                          browse media.
     * @param rootHints         An optional bundle of service-specific arguments to send
     *                          to the media browse service when connecting and retrieving the
     *                          root id for browsing, or null if none. The contents of this
     *                          bundle may affect the information returned when browsing.
     * @return The {@link BrowserRoot} for accessing this app's content or null.
     * @see BrowserRoot#EXTRA_RECENT
     * @see BrowserRoot#EXTRA_OFFLINE
     * @see BrowserRoot#EXTRA_SUGGESTED
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.i(TAG, "onGetRoot");
        return new BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
    }

    /**
     * Called to get information about the children of a media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}
     * with the list of children. If loading the children will be an expensive
     * operation that should be performed on another thread,
     * {@link Result#detach result.detach} may be called before returning from
     * this function, and then {@link Result#sendResult result.sendResult}
     * called when the loading is complete.
     * </p><p>
     * In case the media item does not have any children, call {@link Result#sendResult}
     * with an empty list. When the given {@code parentId} is invalid, implementations must
     * call {@link Result#sendResult result.sendResult} with {@code null}, which will invoke
     * {@link MediaBrowserCompat.SubscriptionCallback#onError}.
     * </p>
     *
     * @param parentId The id of the parent media item whose children are to be
     *                 queried.
     * @param result   The Result to send the list of children to.
     */
    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.i(TAG, "onLoadChildren music service parentId = " + parentId);
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            // Detach this message (result) from the current thread and allow the
            // {@link #sendResult} call to be tried again.
            result.detach();
            Log.i(TAG, "onLoadChildren: MusicProvider is not initialized. parentId = " + parentId);
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    if (success) {
                        Log.i(TAG, "Try to retrieve music the 2nd time.");
                        result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
                    }
                }
            });
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.d(TAG, "onCreate");


        mMusicProvider = MusicProvider.getInstance();

        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method.
        // {@link #onLoadChildren()}.
        mMusicProvider.retrieveMediaAsync(null);

        QueueManager queueManager = new QueueManager(mMusicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {

                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {

                    }

                    @Override
                    public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {

                    }
                });

        LocalPlayback playback = new LocalPlayback(this, mMusicProvider);
        mPlayBackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager, playback);

        // Start a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setCallback(mPlayBackManager.getMediaSessionCallback());

        // Start an activity.
        Context context = getApplicationContext();
        Intent intent = new Intent(context, NowPlayingActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mPlayBackManager.updatePlaybackState(null);
    }

    @Override
    public void onDestroy() {
        mPlayBackManager.handleStopRequest();
        mSession.release();
    }

    @Override
    public void onPlaybackStart() {
        mSession.setActive(true);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onNotificationRequired() {

    }

    @Override
    public void onPlaybackStop() {
        mSession.setActive(false);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }
}
