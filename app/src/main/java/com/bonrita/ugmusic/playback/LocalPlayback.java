package com.bonrita.ugmusic.playback;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.model.MusicProviderSource;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.MediaIDHelper;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class LocalPlayback implements Playback {
    private final static String TAG = LogHelper.makeLogTag(LocalPlayback.class);
    private final Context mContext;
    private final MusicProvider mMusicProvider;
    private SimpleExoPlayer mExoplayer;
    private Playback.Callback mPlaybackCallback;

    private String mCurrentMediaId;
    private ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        mContext = context;
        mMusicProvider = musicProvider;
    }

    /**
     * Start/setup the playback.
     * Resources/listeners would be allocated by implementations.
     */
    @Override
    public void start() {

    }

    /**
     * Stop the playback. All resources can be de-allocated by implementations here.
     *
     * @param notifyListeners if true and a callback has been set by setCallback,
     *                        callback.onPlaybackStatusChanged will be called after changing
     *                        the state.
     */
    @Override
    public void stop(boolean notifyListeners) {
        if (mExoplayer != null) {
            mExoplayer.release();
            mExoplayer = null;
        }
    }

    /**
     * Set the latest playback state as determined by the caller.
     *
     * @param state
     */
    @Override
    public void setState(int state) {

    }

    /**
     * Get the current {@link PlaybackState#getState()}
     */
    @Override
    public int getState(@Nullable Integer state) {
        int playbackState;

        if (mExoplayer == null) {
            return PlaybackStateCompat.STATE_NONE;
        }

        if (state == null) {
            state = mExoplayer.getPlaybackState();
        }

        switch (state) {
            case Player.STATE_READY:
                if (mExoplayer.getPlayWhenReady()) {
                    playbackState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    playbackState = PlaybackStateCompat.STATE_PAUSED;
                }
                break;

            default:
                playbackState = PlaybackStateCompat.STATE_NONE;
                break;
        }

        return playbackState;
    }

    /**
     * @return boolean that indicates that this is ready to be used.
     */
    @Override
    public boolean isConnected() {
        return false;
    }

    /**
     * @return boolean indicating whether the player is playing or is supposed to be
     * playing when we gain audio focus.
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * @return pos if currently playing an item
     */
    @Override
    public long getCurrentStreamPosition() {
        return mExoplayer != null ? mExoplayer.getCurrentPosition() : 0;
    }

    /**
     * Queries the underlying stream and update the internal last known stream position.
     */
    @Override
    public void updateLastKnownStreamPosition() {

    }

    @Override
    public void play(MediaSessionCompat.QueueItem item) {
        String mediaId = item.getDescription().getMediaId();

        if (mExoplayer == null) {
            mExoplayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
            mExoplayer.addListener(mEventListener);
        }

        if (!TextUtils.equals(mediaId, mCurrentMediaId)) {
            mCurrentMediaId = mediaId;
            String originalMediaId = MediaIDHelper.getOriginalMediaId(mediaId);
            MediaMetadataCompat track = mMusicProvider.getMusic(originalMediaId);
            String source = track.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE);
            if (source != null) {
                // Remove spaces.
                source = source.replaceAll(" ", "%20");
            }

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "ugmusic"));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(source), dataSourceFactory, extractorsFactory, null, null);
            mExoplayer.prepare(mediaSource);

        }

        if (mCurrentMediaId != null) {
            mExoplayer.setPlayWhenReady(true);
        }

    }

    @Override
    public void pause() {
        if (mExoplayer != null && getState(null) == PlaybackStateCompat.STATE_PLAYING) {
            mExoplayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    @Override
    public String getCurrentMediaId() {
        return null;
    }

    @Override
    public void setCallback(Callback callback) {
        mPlaybackCallback = callback;
    }

    private class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            mPlaybackCallback.onPlaybackStatusChanged(playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onSeekProcessed() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    }
}
