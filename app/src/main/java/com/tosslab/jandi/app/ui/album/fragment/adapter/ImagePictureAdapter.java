package com.tosslab.jandi.app.ui.album.fragment.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.fragment.vo.SelectPictures;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class ImagePictureAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final List<ImagePicture> photoList;
    private OnRecyclerItemClickListener onRecyclerItemImageClickListener;
    private OnRecyclerItemClickListener onRecyclerItemCheckClickListener;

    public ImagePictureAdapter(Context context, List<ImagePicture> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_image_picture, parent, false);
        PictureViewHolder viewHolder = new PictureViewHolder(view);
        viewHolder.ivPicture = (ImageView) view.findViewById(R.id.iv_item_image_picture_thumb);
        viewHolder.ivSelector = (ImageView) view.findViewById(R.id.iv_item_image_picture_selector);
        viewHolder.ivSelected = (ImageView) view.findViewById(R.id.iv_item_image_picture_selected);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ImagePicture item = getItem(position);

        PictureViewHolder viewHolder = (PictureViewHolder) holder;

        if (SelectPictures.getSelectPictures().contains(item.getImagePath())) {
            viewHolder.ivSelector.setSelected(true);
            viewHolder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSelector.setSelected(false);
            viewHolder.ivSelected.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(item.getImagePath())
                .placeholder(new ColorDrawable(Color.TRANSPARENT))
                .centerCrop()
                .into(viewHolder.ivPicture);

        viewHolder.ivPicture.setOnClickListener(v -> {
            if (onRecyclerItemImageClickListener != null) {
                onRecyclerItemImageClickListener.onItemClick(v, ImagePictureAdapter.this, position);
            }
        });

        viewHolder.ivSelector.setOnClickListener(v -> {
            if (onRecyclerItemCheckClickListener != null) {
                onRecyclerItemCheckClickListener.onItemClick(v, ImagePictureAdapter.this, position);
            }
        });

    }

    public ImagePicture getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public int getItemCount() {
        if (photoList == null) {
            return 0;
        }
        return photoList.size();
    }

    public void setOnRecyclerItemImageClickListener(OnRecyclerItemClickListener onRecyclerItemImageClickListener) {
        this.onRecyclerItemImageClickListener = onRecyclerItemImageClickListener;
    }

    public ImagePictureAdapter setOnRecyclerItemCheckClickListener(OnRecyclerItemClickListener onRecyclerItemCheckClickListener) {
        this.onRecyclerItemCheckClickListener = onRecyclerItemCheckClickListener;
        return this;
    }

    private static class PictureViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPicture;
        ImageView ivSelector;
        ImageView ivSelected;


        public PictureViewHolder(View itemView) {
            super(itemView);
        }
    }
}
