package com.bonrita.ugmusic.ui;

import android.animation.Animator;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.AnimatorRes;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.LogHelper;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances.
 * It will create a MediaBrowser.
 * When it is created and connected/disconnect on start/stop.
 * Thus, a MediaBrowser will be always connected while this activity is running.
 */
public class MusicPlayerActivity extends BaseActivity {

    private static final String TAG = LogHelper.makeLogTag(MusicPlayerActivity.class);
    private static final String FRAGMENT_TAG = "ugmp_list_container";
    private Bundle mVoiceSearchParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);

        initializeFromParams(savedInstanceState, getIntent());
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        // Check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        }

        String mediaId = null;
        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {
        FragmentManager fragmentManager = getFragmentManager();
        MediaBrowserFragment fragment = new MediaBrowserFragment();

        fragmentManager.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
//                .setCustomAnimations(R.animator.slide_in_from_right).commit();
    }

    @Override
    protected void onMediaControllerConnected() {
        if (mVoiceSearchParams != null) {
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            String query = mVoiceSearchParams.getString(SearchManager.QUERY);
            MediaControllerCompat.getMediaController(MusicPlayerActivity.this).getTransportControls()
                    .playFromSearch(query, mVoiceSearchParams);
        }

        getBrowseFragment().onConnected();

    }

    private MediaBrowserFragment getBrowseFragment() {
        return (MediaBrowserFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
}
