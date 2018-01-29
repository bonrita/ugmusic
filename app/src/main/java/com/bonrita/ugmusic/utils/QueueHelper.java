package com.bonrita.ugmusic.utils;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Utility class to help on queue related tasks.
 */
public class QueueHelper {
    private static final String TAG = LogHelper.makeLogTag(QueueHelper.class);

    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    /**
     * Get the position of the the given music in the queue.
     * When provided with the music media ID
     * Where the music stands in the queue. It's position.
     *
     * @param queue
     * @param mediaId
     * @return
     */
    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, String mediaId) {

        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }

        return MediaSessionCompat.QueueItem.UNKNOWN_ID;
    }

    /**
     * Get the music index in the queue given the queue id.
     *
     * @param queue
     * @param queueId
     * @return
     */
    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, long queueId) {

        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }

        return MediaSessionCompat.QueueItem.UNKNOWN_ID;
    }

}
