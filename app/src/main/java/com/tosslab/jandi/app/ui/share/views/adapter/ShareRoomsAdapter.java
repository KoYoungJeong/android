package com.tosslab.jandi.app.ui.share.views.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 9. 15..
 */
public class ShareRoomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int TYPE_FOLDER = 1;
    final int TYPE_ROOM = 2;

    private final Context context;
    private List<ExpandRoomData> roomDatas;
    private OnItemClickListener onItemClickListener;
    private boolean hasFolder = false;

    public ShareRoomsAdapter(Context context) {
        this.context = context;
        roomDatas = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null;

        if (viewType == TYPE_ROOM) {

            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select, parent, false);

            RoomViewHolder viewHolder = new RoomViewHolder(itemView);

            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewHolder.ivIcon =
                    (SimpleDraweeView) itemView.findViewById(R.id.iv_room_selector_item_icon);
            viewHolder.vLine = itemView.findViewById(R.id.v_line_use_for_first_no_folder_item);

            return viewHolder;

        } else if (viewType == TYPE_FOLDER) {

            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select_folder, parent, false);
            FolderViewHolder viewHolder = new FolderViewHolder(itemView);
            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewHolder.vLine = itemView.findViewById(R.id.view_line);

            return viewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ExpandRoomData item = getItem(position);

        LogUtil.e("item", item.toString());

        if (getItemViewType(position) == TYPE_FOLDER) {
            hasFolder = true;
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            folderViewHolder.tvName.setText(item.getName().toString());
            folderViewHolder.itemView.setClickable(false);
            if (position == 0) {
                folderViewHolder.vLine.setVisibility(View.INVISIBLE);
            } else {
                folderViewHolder.vLine.setVisibility(View.VISIBLE);
            }
            return;
        }

        RoomViewHolder roomholder = (RoomViewHolder) holder;

        // 폴더가 없는 첫번째 폴더는 상단에 라인이 그려져야 함.
        if (item.isFirstAmongNoFolderItem() && hasFolder) {
            roomholder.vLine.setVisibility(View.VISIBLE);
        } else {
            roomholder.vLine.setVisibility(View.GONE);
        }

        SimpleDraweeView ivIcon = roomholder.ivIcon;
        if (item.getType() == FormattedEntity.TYPE_EVERYWHERE) {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.icon_search_all, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.icon_search_all))
                    .into(ivIcon);
            roomholder.tvName.setText(R.string.jandi_file_category_everywhere);
        } else if (item.isUser()) {
            String fileUrl = ImageUtil.getImageFileUrl(item.getProfileUrl());
            ImageUtil.loadProfileImage(ivIcon, fileUrl, R.drawable.profile_img_comment);
            roomholder.tvName.setText(item.getName());
        } else if (item.isPublicTopic()) {
            int resId = R.drawable.topiclist_icon_topic_fav;
            if (!item.isStarred()) {
                resId = R.drawable.topiclist_icon_topic;
            }
            ImageLoader.newBuilder()
                    .placeHolder(resId, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(resId))
                    .into(ivIcon);
            roomholder.tvName.setText(item.getName());
        } else {
            int resId = R.drawable.topiclist_icon_topic_private_fav;
            if (!item.isStarred()) {
                resId = R.drawable.topiclist_icon_topic_private;
            }
            ImageLoader.newBuilder()
                    .placeHolder(resId, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(resId))
                    .into(ivIcon);
            roomholder.tvName.setText(item.getName());
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int roomId = item.getEntityId();
                String roomName = item.getName();
                int type;
                if (item.isPublicTopic()) {
                    type = JandiConstants.TYPE_PUBLIC_TOPIC;
                } else if (item.isUser()) {
                    type = JandiConstants.TYPE_DIRECT_MESSAGE;
                } else {
                    type = JandiConstants.TYPE_PRIVATE_TOPIC;
                }
                onItemClickListener.onItemClick(roomId, roomName, type);
            }
        });
    }

    private ExpandRoomData getItem(int position) {
        return roomDatas.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isFolder()) {
            return TYPE_FOLDER;
        } else {
            return TYPE_ROOM;
        }
    }

    @Override
    public int getItemCount() {
        return roomDatas.size();
    }

    public void addAll(List<ExpandRoomData> roomDatas) {
        this.roomDatas.clear();
        this.roomDatas.addAll(roomDatas);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int roomId, String roomName, int roomType);
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private SimpleDraweeView ivIcon;
        private View vLine;

        public RoomViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private View vLine;

        public FolderViewHolder(View itemView) {
            super(itemView);
        }

    }

}


