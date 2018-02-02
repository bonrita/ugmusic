package com.bonrita.ugmusic.model;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.bonrita.ugmusic.utils.MediaIDHelper.CATEGORY_SEPARATOR;
import static com.bonrita.ugmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST;
import static com.bonrita.ugmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.bonrita.ugmusic.utils.MediaIDHelper.MEDIA_ID_ROOT;
import static com.bonrita.ugmusic.utils.MediaIDHelper.createMediaID;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {
    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    // Categorised caches for music track data:
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    private ConcurrentMap<String, MutableMediaMetadata> mMusicListById;

    private Set<String> mFavoriteTracks;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByArtist;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MusicProvider() {
        this(new RemoteJSONSource());
    }

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public MediaMetadataCompat getMusic(String mediaId) {
        return mMusicListById.containsKey(mediaId) ? mMusicListById.get(mediaId).metaData : null;
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String parentId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        String genre_path = MEDIA_ID_MUSICS_BY_GENRE + MediaIDHelper.CATEGORY_SEPARATOR;

        if (parentId.equals(MEDIA_ID_ROOT)) {
            Log.i(TAG, "Get children for root is ready. parentId = " + parentId);
            mediaItems.add(createBrowsableParentMediaItemForGenres(resources));
            mediaItems.add(createBrowsableParentMediaItemForArtists(resources));
        } else if (parentId.equals(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)) {
            Set<String> genres = mMusicListByGenre.keySet();
            Iterator<String> iterator = genres.iterator();
            while (iterator.hasNext()) {
                mediaItems.add(createBrowsableMediaItemForGenre(iterator.next(), resources));
            }
        } else if (parentId.equals(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST)) {
            Set<String> artists = mMusicListByArtist.keySet();
            Iterator<String> iterator = artists.iterator();
            while (iterator.hasNext()) {
                mediaItems.add(createBrowsableMediaItemForArtists(iterator.next(), resources));
            }
        } else if (parentId.startsWith(genre_path)) {

            int separator = parentId.indexOf(CATEGORY_SEPARATOR);
            String genre = parentId.substring(separator + 1);

            if (mMusicListByGenre.containsKey(genre)) {
                List<MediaMetadataCompat> metadata_list = mMusicListByGenre.get(genre);
                Iterator<MediaMetadataCompat> iterator = metadata_list.iterator();

                while (iterator.hasNext()) {
                    MediaMetadataCompat metadata = iterator.next();
                    mediaItems.add(createPlayableMediaItems(metadata.getDescription()));
                }
            }
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItems(MediaDescriptionCompat description) {
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForArtists(String artist, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_ARTIST, artist))
                .setTitle(artist)
                .setSubtitle(resources.getString(R.string.browse_musics_by_artist_subtitle, artist))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, MEDIA_ID_MUSICS_BY_GENRE, genre))
                .setTitle(genre)
                .setSubtitle(resources.getString(R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableParentMediaItemForGenres(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_BY_GENRE)
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableParentMediaItemForArtists(@NonNull Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_BY_ARTIST)
                .setTitle(resources.getString(R.string.browse_by_artists))
                .setSubtitle(resources.getString(R.string.browse_artists_subtitle))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metaData.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);

            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metaData);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                Iterator<MediaMetadataCompat> tracks = mSource.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }
                buildListsByGenre();
                buildListByArtists();
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private synchronized void buildListByArtists() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByArtists = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String artist = m.metaData.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            List<MediaMetadataCompat> list = newMusicListByArtists.get(artist);

            if (list == null) {
                list = new ArrayList<>();
                newMusicListByArtists.put(artist, list);
            }

            list.add(m.metaData);
        }
        mMusicListByArtist = newMusicListByArtists;
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

}
