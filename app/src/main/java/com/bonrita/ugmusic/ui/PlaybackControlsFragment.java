package com.bonrita.ugmusic.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.LogHelper;

public class PlaybackControlsFragment extends android.app.Fragment {
    private static final String TAG = LogHelper.makeLogTag(PlaybackControlsFragment.class);

    private ImageView mAlbumArt;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mExtraInfo;
    private ImageButton mPlayPause;
    private boolean mControllerConnected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        mAlbumArt = (ImageView) rootView.findViewById(R.id.album_art);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mArtist = (TextView) rootView.findViewById(R.id.artist);
        mExtraInfo = (TextView) rootView.findViewById(R.id.extra_info);

        mPlayPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        mPlayPause.setOnClickListener(mButtonListener);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        if (controller != null) {
            controller.unregisterCallback(mControllerCallback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onAttachFragment(android.app.Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        if (controller != null && !mControllerConnected) {
            onConnected();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void onConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        onPlaybackStateChanged(controller.getPlaybackState());
        onMetadataChanged(controller.getMetadata());
        controller.registerCallback(mControllerCallback);
        mControllerConnected = true;
    }

    private void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPause.setImageResource(R.drawable.ic_pause_black_36dp);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayPause.setImageResource(R.drawable.ic_play_arrow_black_36dp);
                break;
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        mTitle.setText(metadata.getDescription().getTitle());
        mArtist.setText(metadata.getDescription().getSubtitle());
    }

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            PlaybackStateCompat playBackState = controller.getPlaybackState();

            switch (v.getId()) {
                case R.id.play_pause:
                    if (playBackState != null) {
                        onMetadataChanged(controller.getMetadata());
                        if (playBackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                            controller.getTransportControls().pause();
                        } else if (playBackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
                            controller.getTransportControls().play();
                        }
                    }
                    break;
            }
        }
    };

    private final android.support.v4.media.session.MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            PlaybackControlsFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            PlaybackControlsFragment.this.onMetadataChanged(metadata);
        }
    };


}
