package com.bonrita.ugmusic.playback;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {

    private static final String TAG = LogHelper.makeLogTag(QueueManager.class);

    private MusicProvider mMusicProvider;
    private Resources mResources;
    private MetadataUpdateListener mListener;

    // "Now Playing" queue:
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;
    private String mBaseCategory;
    private String mSecondaryCategory;

    public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
        this.mMusicProvider = musicProvider;
        this.mListener = listener;
        this.mResources = resources;

        mPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex = 0;
    }

    /**
     * Update queue with the new almost going to be played mediaId.
     *
     * @param mediaId
     */
    public void setQueueFromMediaId(String mediaId) {
        boolean queueSet = false;
        if (isSameBrowsingCategories(mediaId)) {
            int mediaIdIndex = getIndexFromMediaId(mediaId);
            if (mediaIdIndex > -1) {
                mCurrentIndex = mediaIdIndex;
                queueSet = true;
            }
        }

        if (!queueSet) {
            setCurrentPlayingQueue(mediaId);
        }

        metaDataChanged();
    }

    private void metaDataChanged() {
        if (isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
            MediaSessionCompat.QueueItem item = mPlayingQueue.get(mCurrentIndex);
            String mediaId = item.getDescription().getMediaId();
            String originalMediaId = MediaIDHelper.getOriginalMediaId(mediaId);
            MediaMetadataCompat music = mMusicProvider.getMusic(originalMediaId);
            mListener.onMetadataChanged(music);
        }
    }

    /**
     * Set current playing queue.
     * <p>
     * This will also set the current playing index.
     *
     * @param mediaId
     */
    protected void setCurrentPlayingQueue(String mediaId) {
        mPlayingQueue.clear();
        String categoryString = getCategoryString(mediaId);

        List<MediaBrowserCompat.MediaItem> mediaItems = mMusicProvider.getChildren(categoryString, mResources);

        Iterator<MediaBrowserCompat.MediaItem> iterator = mediaItems.iterator();

        int i = 0;
        while (iterator.hasNext()) {
            MediaDescriptionCompat description = iterator.next().getDescription();
            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, i);
            mPlayingQueue.add(i, queueItem);
            if (description.getMediaId().equals(mediaId)) {
                mCurrentIndex = i;
            }
            i++;
        }
    }

    @NonNull
    protected String getCategoryString(String mediaId) {
        String cat = "";
        String[] newMediaCategories = getMediaIdAssociatedCategories(mediaId);
        for (int i = 0; i < newMediaCategories.length; i++) {
            if (i < newMediaCategories.length - 1) {
                cat += newMediaCategories[i] + String.valueOf(MediaIDHelper.CATEGORY_SEPARATOR);
            } else {
                cat += newMediaCategories[i];
            }
        }
        return cat;
    }

    private int getIndexFromMediaId(String mediaId) {
        Iterator<MediaSessionCompat.QueueItem> iterator = mPlayingQueue.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            MediaSessionCompat.QueueItem queueItem = iterator.next();

            if (queueItem.getDescription().getMediaId().equals(mediaId)) {
                return i;
            }

            i++;
        }

        return -1;
    }

    private boolean isSameBrowsingCategories(String mediaId) {
        String[] newMediaCategories = getMediaIdAssociatedCategories(mediaId);
        MediaSessionCompat.QueueItem current = getCurrentQueueItem();

        if (current == null) {
            return false;
        }

        String[] currentMediaCategories = getMediaIdAssociatedCategories(current.getDescription().getMediaId());

        return Arrays.equals(currentMediaCategories, newMediaCategories);
    }

    protected boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        if (queue == null || queue.isEmpty() || index < 0 || index > queue.size()) {
            return false;
        }
        return true;
    }

    private String[] getMediaIdAssociatedCategories(String mediaId) {
        int musicSeparatorPosition = mediaId.indexOf(MediaIDHelper.MUSIC_SEPARATOR);
        if (musicSeparatorPosition > 0) {
            mediaId = mediaId.substring(0, musicSeparatorPosition);
        }

        return mediaId.split(String.valueOf(MediaIDHelper.CATEGORY_SEPARATOR));
    }

    public MediaSessionCompat.QueueItem getCurrentQueueItem() {
        if (!isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
            return null;
        }
        return mPlayingQueue.get(mCurrentIndex);
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
