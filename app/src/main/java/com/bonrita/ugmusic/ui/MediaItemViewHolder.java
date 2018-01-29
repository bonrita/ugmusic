package com.bonrita.ugmusic.ui;


import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonrita.ugmusic.R;

class MediaItemViewHolder {

    private ImageView mImageView;
    private TextView mTitle;
    private TextView mDescription;

    public static View setupListView(Activity context, View convertView, ViewGroup parent, MediaBrowserCompat.MediaItem item) {

        MediaItemViewHolder holder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.media_item_list, parent, false);
            holder = new MediaItemViewHolder();
            holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
            holder.mTitle = (TextView) convertView.findViewById(R.id.title);
            holder.mDescription = (TextView) convertView.findViewById(R.id.description);

            convertView.setTag(holder);
        } else {
            holder = (MediaItemViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(item.getDescription().getTitle());
        holder.mDescription.setText(item.getDescription().getSubtitle());

        if (!item.isPlayable()) {
            holder.mImageView.setVisibility(View.GONE);
        }

        return convertView;
    }


}
