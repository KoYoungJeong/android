package com.tosslab.jandi.app.ui.selector.room.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 9. 30..
 */
public class RoomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int TYPE_FOLDER = 1;
    final int TYPE_ROOM = 2;

    private final Context context;
    private List<ExpandRoomData> roomDatas;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public RoomRecyclerAdapter(Context context) {
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
            viewHolder.vgLine = itemView.findViewById(R.id.v_line_use_for_first_no_folder_item);
            viewHolder.vgContent = (LinearLayout) itemView.findViewById(R.id.vg_room_selector_content);
            return viewHolder;
        } else if (viewType == TYPE_FOLDER) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select_folder, parent, false);
            FolderViewHolder viewHolder = new FolderViewHolder(itemView);
            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewHolder.viewLine = itemView.findViewById(R.id.view_line);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ExpandRoomData item = getItem(position);
        if (getItemViewType(position) == TYPE_FOLDER) {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            folderViewHolder.tvName.setText(item.getName());
            folderViewHolder.itemView.setClickable(false);
            // 폴더 가장 위에는 divider line이 없도록 한다.
            if (position == 0) {
                folderViewHolder.viewLine.setVisibility(View.GONE);
            } else {
                folderViewHolder.viewLine.setVisibility(View.VISIBLE);
            }
            return;
        }

        RoomViewHolder roomholder = (RoomViewHolder) holder;

        // 폴더가 없는 첫번째 폴더는 상단에 라인이 그려져야 함.
        if (item.isFirstAmongNoFolderItem()) {
            roomholder.vgLine.setVisibility(View.VISIBLE);
        } else {
            roomholder.vgLine.setVisibility(View.GONE);
        }

        if (!item.isUser()) {
            roomholder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        if (item.getType() == FormattedEntity.TYPE_EVERYWHERE) {
            roomholder.ivIcon.setImageResource(R.drawable.icon_search_all_rooms);
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
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener
                        .onItemClick(v, RoomRecyclerAdapter.this, position);
            }
        });

    }

    public ExpandRoomData getItem(int position) {
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

    public void addAll(List<ExpandRoomData> categorizableEntities) {
        roomDatas.clear();
        roomDatas.addAll(categorizableEntities);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivIcon;
        private View vgLine;
        private LinearLayout vgContent;

        public RoomViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FolderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private View viewLine;

        public FolderViewHolder(View itemView) {
            super(itemView);
        }
    }

}
