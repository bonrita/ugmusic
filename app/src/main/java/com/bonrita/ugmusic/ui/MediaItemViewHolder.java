package com.bonrita.ugmusic.ui;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonrita.ugmusic.R;
import com.bonrita.ugmusic.utils.MediaIDHelper;

class MediaItemViewHolder {

    private ImageView mImageView;
    private TextView mTitle;
    private TextView mDescription;

    private static ColorStateList sColorStatePlaying;
    private static ColorStateList sColorStateNotPlaying;

    public static View setupListView(Activity context, View convertView, ViewGroup parent, MediaBrowserCompat.MediaItem item) {

        if (sColorStatePlaying == null || sColorStateNotPlaying == null) {
            initializeColorStateLists(context);
        }

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

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
        MediaMetadataCompat metadata = controller.getMetadata();

        if (metadata != null) {
            String currentMediaId = metadata.getDescription().getMediaId();
            String viewHolderOriginalId = MediaIDHelper.getOriginalMediaId(item.getMediaId());
            int state = controller.getPlaybackState().getState();

            if (state == PlaybackStateCompat.STATE_PLAYING && currentMediaId.equals(viewHolderOriginalId)) {
                AnimationDrawable animation = (AnimationDrawable)
                        ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp);
                DrawableCompat.setTintList(animation, sColorStatePlaying);
                animation.start();
                holder.mImageView.setImageDrawable(animation);
            } else if (state == PlaybackStateCompat.STATE_PAUSED && currentMediaId.equals(viewHolderOriginalId)) {
                Drawable playDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_equalizer1_white_36dp);
                DrawableCompat.setTintList(playDrawable, sColorStateNotPlaying);
                holder.mImageView.setImageDrawable(playDrawable);
            } else {
                holder.mImageView.setImageResource(R.drawable.ic_play_arrow_black_36dp);
            }
        }

        return convertView;
    }

    private static void initializeColorStateLists(Context ctx) {
        sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources()
                .getColor(R.color.media_item_icon_not_playing));
        sColorStatePlaying = ColorStateList.valueOf(ctx.getResources()
                .getColor(R.color.media_item_icon_playing));
    }


}
