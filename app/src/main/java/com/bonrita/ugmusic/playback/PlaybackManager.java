package com.bonrita.ugmusic.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.model.MusicProviderSource;
import com.bonrita.ugmusic.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayerFactory;

/**
 * Manage the interactions among the container service, the queue manager and the actual playback.
 */
public class PlaybackManager implements Playback.Callback {

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);

    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.bonrita.ugmusic.THUMBS_UP";

    private PlaybackServiceCallback mServiceCallback;
    private Resources mResources;
    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private Playback mPlayback;
    private MediaSessionCallback mMediaSessionCallback;

    public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                           MusicProvider musicProvider, QueueManager queueManager,
                           Playback playback) {
        mMusicProvider = musicProvider;
        mServiceCallback = serviceCallback;
        mResources = resources;
        mQueueManager = queueManager;
        mPlayback = playback;
        mPlayback.setCallback(this);
        mMediaSessionCallback = new MediaSessionCallback();
    }


    @Override
    public void onCompletion() {

    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(state);
    }

    public void updatePlaybackState(Integer state) {

        long position = mPlayback.getCurrentStreamPosition();
        int currentState = mPlayback.getState(state);

        PlaybackStateCompat.Builder newState = new PlaybackStateCompat.Builder()
                .setState(currentState, position, 1.0f);
        mServiceCallback.onPlaybackStateUpdated(newState.build());
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    public void handleStopRequest() {
        mServiceCallback.onPlaybackStop();
        mPlayback.stop(true);
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;

        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mServiceCallback.onPlaybackStart();
            MediaMetadataCompat track = mMusicProvider.getMusic(mediaId);
            mPlayback.play(track);
        }

        @Override
        public void onStop() {
            handleStopRequest();
        }
    }
}
