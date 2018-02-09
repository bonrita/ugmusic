package com.bonrita.ugmusic.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bonrita.ugmusic.MusicService;
import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.NetworkHelper;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class BaseActivity extends AppCompatActivity implements MediaBrowserProvider {
    private static final String TAG = LogHelper.makeLogTag(BaseActivity.class);

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;

    long mCurrentQueueItemId = -1;
    int mCurrentPlaybackState;
    boolean mControllPlaybackVisibility;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.d(TAG, "Activity onCreate");

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class),
                mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogHelper.d(TAG, "Activity onStart");

        mControlsFragment = (PlaybackControlsFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);

        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();

        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogHelper.d(TAG, "Activity onStop");
        mMediaBrowser.disconnect();

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);

//        if (shouldShowControls()) {
//            showPlaybackControls();
//        } else {
//            hidePlaybackControls();
//        }

        onMediaControllerConnected();
    }

    protected void onMediaControllerConnected() {
        // Empty implementation, can be overridden by clients.
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);

        if (controller == null || controller.getMetadata() == null
                || controller.getPlaybackState() == null) {
            return false;
        }

        switch (controller.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_ERROR:
                return false;
            default:
                return true;
        }
    }

    protected void hidePlaybackControls() {
        getFragmentManager().beginTransaction().hide(mControlsFragment).commit();
    }

    protected void showPlaybackControls() {
        if (NetworkHelper.isOnline(this)) {
            getFragmentManager().beginTransaction()
                    .show(mControlsFragment).commit();
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            LogHelper.d(TAG, "onConnected");
            Log.i(TAG, "mConnectionCallback: onConnected");
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }

    };

    private final android.support.v4.media.session.MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    if (!mControllPlaybackVisibility) {

                        if (shouldShowControls()) {
                            mControllPlaybackVisibility = true;
                            showPlaybackControls();
                        } else {
                            hidePlaybackControls();
                        }
                    }
                }
            };

}
