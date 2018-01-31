package com.bonrita.ugmusic.ui;

import android.animation.Animator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.AnimatorRes;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
public class MusicPlayerActivity extends BaseActivity implements MediaBrowserFragment.MediaFragmentListener {

    private static final String TAG = LogHelper.makeLogTag(MusicPlayerActivity.class);
    private static final String FRAGMENT_TAG = "ugmp_list_container";
    private static final String SAVED_MEDIA_ID = "com.bonrita.ugmusic.MEDIA_ID";
    private Bundle mVoiceSearchParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");

        setContentView(R.layout.activity_player);

        initializeFromParams(savedInstanceState, getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment != null) {
            String mediaId = fragment.getMediaId();
            if(mediaId != null) {
                outState.putString(SAVED_MEDIA_ID, mediaId);
            }
        }

        super.onSaveInstanceState(outState);
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        // Check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else if (savedInstanceState != null){
            mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
        }

        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {
        MediaBrowserFragment fragment = getBrowseFragment();

        if (fragment == null || !TextUtils.equals(mediaId, fragment.getMediaId())) {

            fragment = new MediaBrowserFragment();

            // Save the media ID so that we can trace it in the application.
            fragment.setMediaId(mediaId);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, FRAGMENT_TAG)
                    .setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_in_from_left
                            , R.animator.slide_in_from_right, R.animator.slide_in_from_left);

            if (mediaId != null) {
                // This back stack is managed by the activity and allows the user to return
                // to the previous fragment state, by pressing the Back button.
                // By calling addToBackStack(), the replace transaction is saved to the back stack
                // so the user can reverse the transaction and bring back the previous
                // fragment by pressing the Back button.
                // https://developer.android.com/guide/components/fragments.html
                transaction.addToBackStack(null);
            }

            transaction.commit();
        }

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

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        int gg = 0;
        if (item != null) {
            Log.i(TAG, "Clicked media ID = " + item.getMediaId());

            if (item.isBrowsable()) {
                navigateToBrowser(item.getMediaId());
            }
        }
    }
}
