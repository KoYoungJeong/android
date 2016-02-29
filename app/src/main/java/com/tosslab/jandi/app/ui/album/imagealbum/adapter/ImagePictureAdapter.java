package com.tosslab.jandi.app.ui.album.imagealbum.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class ImagePictureAdapter extends RecyclerView.Adapter {
    private static final int LOAD_MORE_OFFSET = 3;
    public static final int IMAGE_VIEW_TYPE = 0;
    public static final int PROGRESS_VIEW_TYPE = 1;

    private final Context context;
    private final List<ImagePicture> photoList;

    private OnRecyclerItemClickListener onRecyclerItemImageClickListener;

    private OnLoadMoreCallback onLoadMoreCallback;
    private int enqueueLoadingImageId;

    private int mode;
    private int column;
    private boolean needProgress = true;

    public ImagePictureAdapter(Context context, List<ImagePicture> photoList, int column) {
        this.context = context;
        this.photoList = photoList;
        this.column = column;
        if (this.photoList != null && photoList.size() < ImageAlbumModel.LIMIT) {
            needProgress = false;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IMAGE_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image_picture, parent, false);
            PictureViewHolder viewHolder = new PictureViewHolder(view);
            viewHolder.ivPicture = (SimpleDraweeView) view.findViewById(R.id.iv_item_image_picture_thumb);
            viewHolder.ivSelector = (ImageView) view.findViewById(R.id.iv_item_image_picture_selector);
            viewHolder.ivSelected = (ImageView) view.findViewById(R.id.iv_item_image_picture_selected);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_progress, parent, false);
            return new ProgressViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (needProgress && position == getItemCount() - 1) {
            return;
        }
        ImagePicture item = getItem(position);

        PictureViewHolder viewHolder = (PictureViewHolder) holder;

        String imagePath = item.getImagePath();

        if (SelectPictures.getSelectPictures().contains(imagePath)) {
            viewHolder.ivSelector.setSelected(true);
            viewHolder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSelector.setSelected(false);
            viewHolder.ivSelected.setVisibility(View.GONE);
        }

        if (mode != ImageAlbumActivity.EXTRA_MODE_UPLOAD) {
            viewHolder.ivSelector.setVisibility(View.GONE);
        }

        final SimpleDraweeView ivPicture = viewHolder.ivPicture;

        final Uri uri = UriFactory.getContentUri(item.get_id());

        setImage(ivPicture, uri);

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemImageClickListener != null) {
                onRecyclerItemImageClickListener.onItemClick(v, ImagePictureAdapter.this, position);
            }
        });

        loadMoreIfNeed(position);
    }

    private void loadMoreIfNeed(int position) {
        if (onLoadMoreCallback == null) {
            return;
        }

        int itemCount = getItemCount();

        if (position == itemCount - (LOAD_MORE_OFFSET * column)) {
            ImagePicture lastItem = needProgress ? getItem(itemCount - 2) : getItem(itemCount - 1);
            int imageId = lastItem.get_id();

            if (imageId == enqueueLoadingImageId) {
                return;
            }

            enqueueLoadingImageId = imageId;
            onLoadMoreCallback.onLoadMore(imageId);
        }
    }

    private void setImage(SimpleDraweeView ivPicture, Uri uri) {
        int size = ApplicationUtil.getDisplaySize(false) / column;
        ImageLoader.newBuilder()
                .aspectRatio(1.0f)
                .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .resize(size, size)
                .load(uri)
                .into(ivPicture);
    }

    public ImagePicture getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public int getItemCount() {
        if (photoList == null) {
            return 0;
        }
        return needProgress ? photoList.size() + 1 : photoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return needProgress
                ? (position == getItemCount() - 1 ? PROGRESS_VIEW_TYPE : IMAGE_VIEW_TYPE)
                : IMAGE_VIEW_TYPE;
    }

    public void addPhotoList(List<ImagePicture> photoList) {
        if (this.photoList == null) {
            return;
        }

        if (photoList.size() < ImageAlbumModel.LIMIT) {
            needProgress = false;
        }

        this.photoList.addAll(photoList);
    }

    public void setOnRecyclerItemImageClickListener(OnRecyclerItemClickListener onRecyclerItemImageClickListener) {
        this.onRecyclerItemImageClickListener = onRecyclerItemImageClickListener;
    }

    public void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback) {
        this.onLoadMoreCallback = onLoadMoreCallback;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private static class PictureViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView ivPicture;
        ImageView ivSelector;
        ImageView ivSelected;

        public PictureViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnLoadMoreCallback {
        void onLoadMore(int imageId);
    }
}
