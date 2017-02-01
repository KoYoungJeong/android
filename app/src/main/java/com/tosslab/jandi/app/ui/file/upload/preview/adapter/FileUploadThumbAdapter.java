package com.tosslab.jandi.app.ui.file.upload.preview.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FileUploadThumbAdapter extends RecyclerView.Adapter<FileUploadThumbAdapter.ThumbViewHolder> {

    private List<FileThumbInfo> files;

    private OnRecyclerItemClickListener itemClickListener;

    public FileUploadThumbAdapter() {
        this.files = new ArrayList<>();
    }

    @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_upload_preview_thumb, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ThumbViewHolder holder, int position) {
        FileThumbInfo item = getItem(position);
        if (item.fileThumbs > 0) {
            holder.ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.ivThumb.setImageResource(item.fileThumbs);
        } else {
            ImageLoader.newInstance().
                    actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .uri(UriUtil.getFileUri(item.filePath))
                    .into(holder.ivThumb);
        }

        holder.vSelector.setSelected(item.selected);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(holder.itemView, FileUploadThumbAdapter.this, position);
            }
        });
    }

    public FileThumbInfo getItem(int position) {
        return files.get(position);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setFileThumbInfo(List<FileThumbInfo> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_file_upload_preview_thumb)
        ImageView ivThumb;
        @Bind(R.id.v_file_upload_preview_thumb_selector)
        View vSelector;

        ThumbViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class FileThumbInfo {
        private final String filePath;
        private final int fileThumbs;
        private boolean selected;

        public FileThumbInfo(String filePath, int fileThumbs) {
            this.filePath = filePath;
            this.fileThumbs = fileThumbs;
        }

        public static FileThumbInfo create(String filePath) {
            FileExtensionsUtil.Extensions extensions = FileExtensionsUtil.getExtensions(filePath);
            int fileThumbWithBG;
            if (extensions != FileExtensionsUtil.Extensions.IMAGE) {
                fileThumbWithBG = FileExtensionsUtil.getFileThumbByExtWithBG(extensions);
            } else {
                fileThumbWithBG = -1;
            }
            return new FileUploadThumbAdapter.FileThumbInfo(filePath, fileThumbWithBG);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
