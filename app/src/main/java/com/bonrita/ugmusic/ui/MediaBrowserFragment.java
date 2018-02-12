package com.bonrita.ugmusic.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.adapter.VerticalRecyclerAdapter;
import com.bonrita.ugmusic.model.MusicProvider;
import com.bonrita.ugmusic.utils.LogHelper;
import com.bonrita.ugmusic.utils.MediaIDHelper;
import com.bonrita.ugmusic.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that lists all the various browsable queues available from a
 * {@link android.service.media.MediaBrowserService}
 * <p/>
 * It uses a {@link android.support.v4.media.MediaBrowserCompat} to connect to the
 * {@link com.bonrita.ugmusic.MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link android.support.v4.media.MediaBrowserCompat.MediaItem}'s that can be browsed are shown
 * in a ListView.
 */
public class MediaBrowserFragment extends Fragment {
    private static final String TAG = LogHelper.makeLogTag(MediaBrowserFragment.class);

    private static final String ARG_MEDIA_ID = "media_id";

    private String mMediaId;

    private TextView mErrorMessage;
    private View mErrorView;

    protected ListView mListView;
    RecyclerView mRecyclerView;
    View mRootView;

    private ArrayAdapter<MediaBrowserCompat.MediaItem> mBrowserAdapter;

    private MediaBrowserProvider mMediaBrowserProvider;
    private MediaFragmentListener mMediaFragmentListener;
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {

        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Log.i(TAG, "Number of items loaded = " + children.size() + " from parentId = " + parentId);
            checkForUserVisibleErrors(children.isEmpty());

            if (parentId.equals(MediaIDHelper.MEDIA_ID_ROOT)) {
                mListView.setVisibility(View.GONE);

                VerticalRecyclerAdapter adapter = new VerticalRecyclerAdapter(children);
                mRecyclerView.setAdapter(adapter);

                mRecyclerView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();

            } else {
                mRecyclerView.setVisibility(View.GONE);
                mBrowserAdapter.clear();
                for (MediaBrowserCompat.MediaItem item : children) {
                    mBrowserAdapter.add(item);
                }
                mBrowserAdapter.notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
            }

        }

        /**
         * Called when the id doesn't exist or other errors in subscribing.
         * <p>
         * If this is called, the subscription remains until {@link MediaBrowserCompat#unsubscribe}
         * called, because some errors may heal themselves.
         * </p>
         *s
         * @param parentId The media id of the parent media item whose children could not be loaded.
         */
        @Override
        public void onError(@NonNull String parentId) {
            Log.i(TAG, "onError: children could not be loaded of the parent media id = " + parentId);
            LogHelper.e(TAG, "browse fragment subscription onError, id=" + parentId);
            Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
            checkForUserVisibleErrors(true);
        }
    };
    private android.support.v4.media.session.MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    mBrowserAdapter.notifyDataSetChanged();
                }
            };


    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView called");
        mRootView = inflater.inflate(R.layout.fragment_list, container, false);

        mErrorView = mRootView.findViewById(R.id.playback_error);
        mErrorMessage = mErrorView.findViewById(R.id.error_message);

        checkForUserVisibleErrors(false);

        mBrowserAdapter = new BrowserAdapter(getActivity());
//        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        mListView = (ListView) mRootView.findViewById(R.id.list_view);
        mListView.setAdapter(mBrowserAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item = (MediaBrowserCompat.MediaItem) parent.getItemAtPosition(position);
                mMediaFragmentListener.onMediaItemSelected(item);
            }
        });

        mListView.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setVisibility(View.GONE);

        return mRootView;
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "OnAttach called");
        mMediaFragmentListener = (MediaFragmentListener) context;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart called");
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();

        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "OnStop called");
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            Log.i(TAG, "Un-subscribing Media ID = " + mMediaId);
            mediaBrowser.unsubscribe(mMediaId);
        }

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        controller.unregisterCallback(mMediaControllerCallback);
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach called");
        super.onDetach();
        mMediaFragmentListener = null;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onConnected() {
        mMediaId = getMediaId();

        if (mMediaId == null) {
            mMediaId = mMediaFragmentListener.getMediaBrowser().getRoot();
        }

        Log.i(TAG, "Media ID = " + mMediaId);
        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaId);
        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        controller.registerCallback(mMediaControllerCallback);
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = false;

        if (!NetworkHelper.isOnline(getActivity())) {
            showError = true;
            mErrorMessage.setText(R.string.error_no_connection);
        } else if (forceError) {
            showError = true;
            // If the caller requested to show error, show a generic message:
            mErrorMessage.setText(R.string.error_loading_media);
        }

        mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }

    // Save the current media ID so that we can track it in the application.
    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(MediaBrowserFragment.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    public String getMediaId() {
        Bundle args = getArguments();

        if (args != null) {
            return args.getString(MediaBrowserFragment.ARG_MEDIA_ID);
        }
        return null;
    }

    private class BrowserAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {
        /**
         * Constructor
         *
         * @param context The current context.
         */
        public BrowserAdapter(@NonNull Context context) {
            super(context, R.layout.media_item_list, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MediaBrowserCompat.MediaItem mediaItem = getItem(position);
            return MediaItemViewHolder.setupListView((Activity) getContext(), convertView, parent, mediaItem);
        }
    }

    public interface MediaFragmentListener extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    }
}
