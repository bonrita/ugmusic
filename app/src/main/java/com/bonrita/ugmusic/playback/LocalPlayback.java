package com.bonrita.ugmusic.playback;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.bonrita.ugmusic.MusicService;
import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.model.MusicProviderSource;
import com.bonrita.ugmusic.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class LocalPlayback implements Playback {
    private final static String TAG = LogHelper.makeLogTag(LocalPlayback.class);
    private final Context mContext;
    private final MusicProvider mMusicProvider;
    private SimpleExoPlayer mExoplayer;

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        mContext = context;
        mMusicProvider = musicProvider;
    }

    public void play(MediaMetadataCompat track) {
        String source = track.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE);
        if (source != null) {
            // Remove spaces.
            source = source.replaceAll(" ", "%20");
        }

        if (mExoplayer == null) {
            mExoplayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "ugmusic"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(source), dataSourceFactory, extractorsFactory, null, null);
        mExoplayer.prepare(mediaSource);
        mExoplayer.setPlayWhenReady(true);

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
    public int getState() {
        return 0;
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
        return 0;
    }

    /**
     * Queries the underlying stream and update the internal last known stream position.
     */
    @Override
    public void updateLastKnownStreamPosition() {

    }

    @Override
    public void play(MediaSessionCompat.QueueItem item) {

    }

    @Override
    public void pause() {

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

    }
}
