package com.bonrita.ugmusic.adapter;

import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.model.MusicProvider;

import java.util.List;


public class HorizontalRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private MusicProvider mMusicProvider;
    MediaBrowserCompat.MediaItem mItem;
    private OnItemClickListener mItemClickListener;
    private List<MediaBrowserCompat.MediaItem> mList;


    public HorizontalRecyclerAdapter(MediaBrowserCompat.MediaItem item, Resources resources) {
        mItem = item;
        String mediaId = item.getDescription().getMediaId();
        mMusicProvider = MusicProvider.getInstance();
        mList = mMusicProvider.getChildren(mediaId, resources);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_item_type_title, parent, false);
        return new CellViewHolder(v1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CellViewHolder cellViewHolder = (CellViewHolder) holder;
        cellViewHolder.textView.setText(mList.get(position).getDescription().getTitle());
    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    // for both short and long click
    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    private class CellViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView textView;

        public CellViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemLongClick(v, getLayoutPosition());
                return true;
            }
            return false;
        }
    }

}
