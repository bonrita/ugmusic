package com.bonrita.ugmusic.adapter;


import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bonrita.ugmusic.R;

import java.util.List;

public class VerticalRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<MediaBrowserCompat.MediaItem> mList;
    private HorizontalRecyclerAdapter.OnItemClickListener mItemClickListener;
    private SparseIntArray mListPosition = new SparseIntArray();

    public VerticalRecyclerAdapter(List<MediaBrowserCompat.MediaItem> list) {
        mList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,


                                                      int viewType) {
        View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_item_vertical, parent, false);
        return new CellViewHolder(v1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CellViewHolder cellViewHolder = (CellViewHolder) holder;

        cellViewHolder.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        cellViewHolder.mRecyclerView.setLayoutManager(layoutManager);


        HorizontalRecyclerAdapter adapter = new HorizontalRecyclerAdapter(mList.get(position), mContext.getResources());
        cellViewHolder.mRecyclerView.setAdapter(adapter);
        adapter.SetOnItemClickListener(mItemClickListener);

        int lastSeenFirstPosition = mListPosition.get(position, 0);
        if (lastSeenFirstPosition >= 0) {
            cellViewHolder.mRecyclerView.scrollToPosition(lastSeenFirstPosition);
        }

    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {

        final int position = holder.getAdapterPosition();
        CellViewHolder cellViewHolder = (CellViewHolder) holder;
        LinearLayoutManager layoutManager = ((LinearLayoutManager) cellViewHolder.mRecyclerView.getLayoutManager());
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        mListPosition.put(position, firstVisiblePosition);

        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }

    private class CellViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView mRecyclerView;

        public CellViewHolder(View itemView) {
            super(itemView);

            mRecyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
        }
    }

    // for both short and long click
    public void SetOnItemClickListener(final HorizontalRecyclerAdapter.OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

}
