package com.bonrita.ugmusic.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bonrita.ugmusic.MusicService;
import com.bonrita.ugmusic.utils.LogHelper;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class BaseActivity extends AppCompatActivity implements MediaBrowserProvider {
    private static final String TAG = LogHelper.makeLogTag(BaseActivity.class);
    private MediaBrowserCompat mMediaBrowser;

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
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogHelper.d(TAG, "Activity onStop");
        mMediaBrowser.disconnect();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);

        onMediaControllerConnected();
    }

    protected void onMediaControllerConnected() {
        // Empty implementation, can be overridden by clients.
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

}
