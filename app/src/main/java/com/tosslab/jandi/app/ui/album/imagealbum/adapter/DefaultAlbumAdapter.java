package com.tosslab.jandi.app.ui.album.imagealbum.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumFragment;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class DefaultAlbumAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final List<ImageAlbum> defaultAlbumList;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public DefaultAlbumAdapter(Context context, List<ImageAlbum> defaultAlbumList) {
        this.context = context;
        this.defaultAlbumList = defaultAlbumList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_default_album, parent, false);
        AlbumViewHolder albumViewHolder = new AlbumViewHolder(view);
        albumViewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_item_default_album_title);
        albumViewHolder.tvSelectCount = (TextView) view.findViewById(R.id.tv_item_default_album_count);
        albumViewHolder.ivSample = (ImageView) view.findViewById(R.id.iv_item_default_album_sample);

        return albumViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;

        ImageAlbum item = getItem(position);

        String buckerName = item.getBuckerName();
        int bucketCount = item.getCount();

        int countOfBucket;

        if (item.getBucketId() != ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM) {
            countOfBucket = SelectPictures.getSelectPictures().getCountOfBucket(item.getBucketId());
        } else {
            countOfBucket = 0;
        }
        viewHolder.tvSelectCount.setText(String.valueOf(countOfBucket));
        if (countOfBucket > 0) {
            viewHolder.tvSelectCount.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvSelectCount.setVisibility(View.GONE);
        }

        if (item.getBucketId() != -1) {

            viewHolder.tvTitle.setText(String.format("%s (%s)", buckerName, bucketCount));
        } else {
            buckerName = context.getString(R.string.jandi_view_all);
            viewHolder.tvTitle.setText(String.format("%s (%s)", buckerName, bucketCount));
        }

        Uri uri = UriUtil.getContentUri(item.get_id());

        ImageLoader.newInstance()
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .uri(uri)
                .into(viewHolder.ivSample);

        viewHolder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(v, DefaultAlbumAdapter.this, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (defaultAlbumList == null) {
            return 0;
        }
        return defaultAlbumList.size();
    }

    public ImageAlbum getItem(int position) {
        return defaultAlbumList.get(position);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    private static class AlbumViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvSelectCount;
        ImageView ivSample;

        public AlbumViewHolder(View itemView) {
            super(itemView);
        }
    }

}
