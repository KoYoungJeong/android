package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.DefaultProfileChangeEvent;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 1. 7..
 */
public class ProfileSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MODE_CHARACTER_LIST = 0x00;
    public static final int MODE_COLOR_LIST = 0x01;

    private List<?> items = new ArrayList<Integer>();
    private int selectedPosition = 0;
    private int mode;

    private ProfileSelectorAdapter() {
    }

    public ProfileSelectorAdapter(int mode) {
        this.mode = mode;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_selector, parent, false);
        return new ItemViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        if (mode == MODE_COLOR_LIST) {
            ImageView ivImageBox = viewHolder.ivImageBox;
            int color = (Integer) items.get(position);
            ivImageBox.setImageDrawable(new ColorDrawable(color));
        } else {
            SimpleDraweeView ivImageBox = viewHolder.ivImageBox;
            String characterUrl = (String) items.get(position);
            ImageUtil.loadProfileImageWithoutRounding(ivImageBox, Uri.parse(characterUrl), 0);
        }

        if (selectedPosition == position) {
            viewHolder.ivSelectBox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSelectBox.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            EventBus.getDefault().post(new DefaultProfileChangeEvent(mode, getSelectedItem()));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<?> items) {
        if ((mode == MODE_COLOR_LIST && items.get(0) instanceof Integer)
                || (mode == MODE_CHARACTER_LIST && items.get(0) instanceof String)) {
            this.items = items;
        } else {
            throw new IllegalArgumentException("Illegal argument MODE_COLOR_LIST -> List<Integer>" +
                    "MODE_CHARACTER_LIST -> List<String> ");
        }
    }

    public Object getSelectedItem() {
        return items.get(selectedPosition);
    }

    public final static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelectBox;
        SimpleDraweeView ivImageBox;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ivSelectBox = (ImageView) itemView.findViewById(R.id.iv_select_box);
            ivImageBox = (SimpleDraweeView) itemView.findViewById(R.id.iv_image_box);
        }
    }

}
