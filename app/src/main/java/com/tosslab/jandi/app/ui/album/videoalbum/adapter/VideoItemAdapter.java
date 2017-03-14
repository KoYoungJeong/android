package com.tosslab.jandi.app.ui.album.videoalbum.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.videoalbum.model.VideoAlbumModel;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoItem;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.List;

public class VideoItemAdapter extends RecyclerView.Adapter {
    public static final int VIDEO_VIEW_TYPE = 0;
    public static final int PROGRESS_VIEW_TYPE = 1;
    private static final int LOAD_MORE_OFFSET = 3;
    private final Context context;
    private final List<VideoItem> videoList;

    private OnRecyclerItemClickListener onRecyclerItemImageClickListener;

    private OnLoadMoreCallback onLoadMoreCallback;
    private int enqueueLoadingImageId;

    private int column;
    private boolean needProgress = true;

    public VideoItemAdapter(Context context, List<VideoItem> videoList, int column) {
        this.context = context;
        this.videoList = videoList;
        this.column = column;
        if (this.videoList != null && videoList.size() < VideoAlbumModel.LIMIT) {
            needProgress = false;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIDEO_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image_picture, parent, false);
            VideoViewHolder viewHolder = new VideoViewHolder(view);
            viewHolder.ivVideo = (ImageView) view.findViewById(R.id.iv_item_image_picture_thumb);
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
        VideoItem item = getItem(position);

        VideoViewHolder viewHolder = (VideoViewHolder) holder;

        viewHolder.ivSelected.setVisibility(View.GONE);
        viewHolder.ivSelector.setVisibility(View.GONE);

        final Uri uri = UriUtil.getFileUri(item.getThumbnailPath());

        ImageLoader.newInstance()
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .uri(uri)
                .into(viewHolder.ivVideo);

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemImageClickListener != null) {
                onRecyclerItemImageClickListener.onItemClick(v, VideoItemAdapter.this, position);
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
            VideoItem lastItem = needProgress ? getItem(itemCount - 2) : getItem(itemCount - 1);
            int imageId = lastItem.get_id();

            if (imageId == enqueueLoadingImageId) {
                return;
            }

            enqueueLoadingImageId = imageId;
            onLoadMoreCallback.onLoadMore(imageId);
        }
    }

    public VideoItem getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public int getItemCount() {
        if (videoList == null) {
            return 0;
        }
        return needProgress ? videoList.size() + 1 : videoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return needProgress
                ? (position == getItemCount() - 1 ? PROGRESS_VIEW_TYPE : VIDEO_VIEW_TYPE)
                : VIDEO_VIEW_TYPE;
    }

    public void addVideoList(List<VideoItem> videoList) {
        if (this.videoList == null) {
            return;
        }

        if (videoList.size() < VideoAlbumModel.LIMIT) {
            needProgress = false;
        }

        this.videoList.addAll(videoList);
    }

    public void setOnRecyclerItemImageClickListener(OnRecyclerItemClickListener onRecyclerItemImageClickListener) {
        this.onRecyclerItemImageClickListener = onRecyclerItemImageClickListener;
    }

    public void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback) {
        this.onLoadMoreCallback = onLoadMoreCallback;
    }

    public interface OnLoadMoreCallback {
        void onLoadMore(int imageId);
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVideo;
        ImageView ivSelector;
        ImageView ivSelected;

        public VideoViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View itemView) {
            super(itemView);
        }
    }

}
