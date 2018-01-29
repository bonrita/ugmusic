package com.bonrita.ugmusic.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

/**
 * Holder class that encapsulates a MediaMetadata and allows the actual metadata to be modified
 * without requiring to rebuild the collections the metadata is in.
 */
class MutableMediaMetadata {

    public MediaMetadataCompat metaData;
    public final String trackId;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat metaData) {
        this.metaData = metaData;
        this.trackId = trackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        MutableMediaMetadata that = (MutableMediaMetadata) o;

        return TextUtils.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }
}
