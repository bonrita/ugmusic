package com.bonrita.ugmusic.ui;


import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bonrita.ugmusic.MusicService;
import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
public class FullScreenPlayerActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerActivity.class);

    private static final int MAX_ART_WIDTH = 800;  // pixels
    private static final int MAX_ART_HEIGHT = 480;  // pixels
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;

    private ImageView mSkipPrev;
    private ImageView mSkipNext;
    private ImageView mPlayPause;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mLine3;
    private ProgressBar mLoading;
    private View mControllers;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private ImageView mBackgroundImage;
    private MediaBrowserCompat mMediaBrowser;

    PlaybackStateCompat mLastPlaybackState;

    private String mArtUrl;

    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        mBackgroundImage = (ImageView) findViewById(R.id.background_image);
        mPlayPause = (ImageView) findViewById(R.id.play_pause);
        mSkipNext = (ImageView) findViewById(R.id.next);
        mSkipPrev = (ImageView) findViewById(R.id.prev);
        mStart = (TextView) findViewById(R.id.startText);
        mEnd = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mLine1 = (TextView) findViewById(R.id.line1);
        mLine2 = (TextView) findViewById(R.id.line2);
        mLine3 = (TextView) findViewById(R.id.line3);
        mLoading = (ProgressBar) findViewById(R.id.progressBar1);
        mControllers = findViewById(R.id.controllers);

        setPlayPauseButtonAction();
        setPlayPreviousButtonAction();
        setPlayNextButtonAction();
        setSeekBarListener();

        // Connect to the MediaBrowserService.
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);

        if (controller != null) {
            controller.unregisterCallback(mControllerCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(mUpdateProgressTask);
                }
            }, PROGRESS_UPDATE_INITIAL_INTERVAL, PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void setPlayPauseButtonAction() {
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackStateCompat stateCompat = MediaControllerCompat
                        .getMediaController(FullScreenPlayerActivity.this).getPlaybackState();

                if (stateCompat != null) {
                    MediaControllerCompat.TransportControls controls = MediaControllerCompat
                            .getMediaController(FullScreenPlayerActivity.this).getTransportControls();

                    switch (stateCompat.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controls.pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            controls.play();
                            scheduleSeekbarUpdate();
                            break;
                    }
                }

            }
        });
    }

    protected void setPlayNextButtonAction() {
        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls = MediaControllerCompat
                        .getMediaController(FullScreenPlayerActivity.this).getTransportControls();
                controls.skipToNext();
            }
        });
    }

    protected void setPlayPreviousButtonAction() {
        mSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls = MediaControllerCompat
                        .getMediaController(FullScreenPlayerActivity.this).getTransportControls();
                controls.skipToPrevious();
            }
        });
    }

    protected void setSeekBarListener() {
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this)
                        .getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });
    }

    private void connectToSession(MediaSessionCompat.Token sessionToken) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(FullScreenPlayerActivity.this, sessionToken);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata == null) {
            finish();
            return;
        }

        MediaControllerCompat.setMediaController(FullScreenPlayerActivity.this, mediaController);
        mediaController.registerCallback(mControllerCallback);

        PlaybackStateCompat state = mediaController.getPlaybackState();
        updateUserInterface(metadata.getDescription());

        updatePlaybackControls(state);
        updateProgress();
        updateDuration(metadata);

        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }

    }

    protected void updateUserInterface(MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
        mLine1.setText(description.getTitle());
        mLine2.setText(description.getSubtitle());

        if (description.getIconUri() != null) {
            String artUrl = description.getIconUri().toString();

            if (!TextUtils.equals(artUrl, mArtUrl)) {
                RequestOptions requestOptions = new RequestOptions()
                        .override(MAX_ART_WIDTH, MAX_ART_HEIGHT);

                mArtUrl = artUrl;
                Glide.with(FullScreenPlayerActivity.this)
                        .load(artUrl)
                        .apply(requestOptions)
                        .into(mBackgroundImage);
            }

        }

    }

    protected void updatePlaybackControls(PlaybackStateCompat stateCompat) {

        if (stateCompat == null) {
            return;
        }
        mLastPlaybackState = stateCompat;

        switch (stateCompat.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
                mControllers.setVisibility(View.VISIBLE);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mControllers.setVisibility(View.VISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLine3.setText(R.string.loading);
                stopSeekbarUpdate();
                break;
        }

        boolean skipNext = (stateCompat.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0;
        mSkipNext.setVisibility(skipNext ? View.INVISIBLE : View.VISIBLE);

        boolean skipPrev = (stateCompat.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0;
        mSkipPrev.setVisibility(skipPrev ? View.INVISIBLE : View.VISIBLE);

    }

    public void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }

        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            long timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta + mLastPlaybackState.getPlaybackSpeed();
        }

        mSeekbar.setProgress((int) currentPosition);
    }

    protected void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }

        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mSeekbar.setMax(duration);
        mEnd.setText(DateUtils.formatElapsedTime(duration / 1000));

    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }
    };

    private final android.support.v4.media.session.MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            updateUserInterface(metadata.getDescription());
            updateDuration(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updatePlaybackControls(state);
        }


    };

}
