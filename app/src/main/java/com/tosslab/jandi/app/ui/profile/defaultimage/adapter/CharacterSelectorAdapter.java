package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.List;

/**
 * Created by tee on 16. 1. 14..
 */
public class CharacterSelectorAdapter<String> extends ProfileSelectorAdapter<String> {
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        SimpleDraweeView ivImageBox = viewHolder.ivImageBox;
        java.lang.String characterUrl = (java.lang.String) items.get(position);
        ImageUtil.loadProfileImageWithoutRounding(ivImageBox, Uri.parse(characterUrl), 0);
        ivImageBox.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            onRecyclerItemClickListener.onItemClick(ivImageBox, CharacterSelectorAdapter.this, position);
        });
        super.onBindViewHolder(holder, position, payloads);
    }
}
