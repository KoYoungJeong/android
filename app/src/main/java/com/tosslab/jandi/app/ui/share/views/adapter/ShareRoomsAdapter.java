package com.tosslab.jandi.app.ui.share.views.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
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
            viewHolder.ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_selector_item_icon);
            viewHolder.vgLine = (LinearLayout) itemView.findViewById(R.id.ll_line_use_for_first_no_folder_item);

            return viewHolder;

        } else if (viewType == TYPE_FOLDER) {

            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select_folder, parent, false);
            FolderViewHolder viewHolder = new FolderViewHolder(itemView);
            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);

            return viewHolder;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ExpandRoomData item = getItem(position);

        LogUtil.e("item", item.toString());

        if (getItemViewType(position) == TYPE_FOLDER) {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            folderViewHolder.tvName.setText(item.getName().toString());
            folderViewHolder.itemView.setClickable(false);
            return;
        }

        RoomViewHolder roomholder = (RoomViewHolder) holder;

        // 폴더가 없는 첫번째 폴더는 상단에 라인이 그려져야 함.
        if (item.isFirstAmongNoFolderItem()) {
            roomholder.vgLine.setVisibility(View.VISIBLE);
        } else {
            roomholder.vgLine.setVisibility(View.GONE);
        }

        if (item.getType() == FormattedEntity.TYPE_EVERYWHERE) {
            roomholder.ivIcon.setImageResource(R.drawable.icon_search_all);
            roomholder.tvName.setText(R.string.jandi_file_category_everywhere);
        } else if (item.isUser()) {
            Ion.with(roomholder.ivIcon)
                    .placeholder(R.drawable.profile_img_comment)
                    .error(R.drawable.profile_img_comment)
                    .fitCenter()
                    .transform(new IonCircleTransform())
                    .crossfade(true)
                    .load(BitmapUtil.getFileUrl(item.getProfileUrl()));

            roomholder.tvName.setText(item.getName());
        } else if (item.isPublicTopic()) {
            if (item.isStarred()) {
                roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
            roomholder.tvName.setText(item.getName());
        } else {
            if (item.isStarred()) {
                roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
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

    class RoomViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private ImageView ivIcon;
        private LinearLayout vgLine;

        public RoomViewHolder(View itemView) {
            super(itemView);
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;

        public FolderViewHolder(View itemView) {
            super(itemView);
        }

    }

}


