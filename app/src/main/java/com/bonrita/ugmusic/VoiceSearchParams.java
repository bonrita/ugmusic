package com.bonrita.ugmusic;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * For more information about voice search parameters,
 * check https://developer.android.com/guide/components/intents-common.html#PlaySearch
 */
public final class VoiceSearchParams {
    public final String mQuery;
    public boolean isAny;
    public boolean isUnstructured;
    public boolean isGenreFocus;
    public boolean isArtistFocus;
    public boolean isAlbumFocus;
    public boolean isSongFocus;
    public String genre;
    public String artist;
    public String album;
    public String song;

    /**
     * Creates a simple object describing the search criteria from the query and extras.
     * @param query the query parameter from a voice search
     * @param extras the extras parameter from a voice search
     */
    public VoiceSearchParams(String query, Bundle extras) {
        mQuery = query;

        if (TextUtils.isEmpty(query)) {
            isAny = true;
        } else {
            if (extras == null) {
                isUnstructured = true;
            } else {
                String genreKey;
                if (Build.VERSION.SDK_INT >= 21) {
                    genreKey = MediaStore.EXTRA_MEDIA_GENRE;
                } else {
                    genreKey = "android.intent.extra.genre";
                }

                String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
                if (TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
                    // For a Genre focused search, only genre is set:
                    isGenreFocus = true;
                    genre = extras.getString(genreKey);
                    if (TextUtils.isEmpty(genre)) {
                        // Because of a bug on the platform, genre is only sent as a query, not as
                        // the semantic-aware extras. This check makes it future-proof when the
                        // bug is fixed.
                        genre = query;
                    }
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                    // For an artist focused search, both artist and genre are set:
                    isArtistFocus = true;
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                    // For an album focused search, album, artist and genre are set:
                    isAlbumFocus = true;
                    genre = extras.getString(genreKey);
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)) {
                    // For a song focused search, title, album, artist and genre are set:
                    isSongFocus = true;
                    song = extras.getString(MediaStore.EXTRA_MEDIA_TITLE);
                    genre = extras.getString(genreKey);
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else {
                    // If we don't know the focus, we treat it as an unstructured query.
                    isUnstructured = true;
                }
            }
        }

    }


    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "query=" + mQuery
                + " isAny=" + isAny
                + " isUnstructured=" + isUnstructured
                + " isGenreFocus=" + isGenreFocus
                + " isArtistFocus=" + isArtistFocus
                + " isAlbumFocus=" + isAlbumFocus
                + " isSongFocus=" + isSongFocus
                + " genre=" + genre
                + " artist=" + artist
                + " album=" + album
                + " song=" + song;
    }
}
