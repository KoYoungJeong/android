package com.tosslab.jandi.app.ui.selector.room.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class RoomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int FROM_ROOM_SELECTOR = 0x01;
    public static final int FROM_USER_SELECTOR = 0x02;
    private static final int TYPE_FOLDER = 1;
    private static final int TYPE_ROOM = 2;
    private static final int TYPE_DUMMY_DISABLE = 3;
    private final Context context;
    private final int from;
    private List<ExpandRoomData> roomDatas;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public RoomRecyclerAdapter(Context context, int from) {
        this.context = context;
        this.from = from;
        roomDatas = new ArrayList<>();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == TYPE_ROOM) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select, parent, false);
            return new RoomViewHolder(itemView);
        } else if (viewType == TYPE_FOLDER) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select_folder, parent, false);
            return new FolderViewHolder(itemView);
        } else if (viewType == TYPE_DUMMY_DISABLE) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select_disable_group_dummy, parent, false);
            return new DisabledGroupDummyViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        ExpandRoomData item = getItem(position);
        if (itemViewType == TYPE_FOLDER) {
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
        } else if (itemViewType == TYPE_DUMMY_DISABLE) {
            DisabledGroupDummyViewHolder disabledGroupDummyViewHolder = (DisabledGroupDummyViewHolder) holder;
            disabledGroupDummyViewHolder.setUp(item);

            holder.itemView.setOnClickListener(v -> {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener
                            .onItemClick(v, RoomRecyclerAdapter.this, position);
                }
            });
            return;
        }

        RoomViewHolder roomholder = (RoomViewHolder) holder;

        roomholder.vLineThrough.setVisibility(View.GONE);
        roomholder.vDisableCover.setVisibility(View.GONE);

        // 폴더가 없는 첫번째 폴더는 상단에 라인이 그려져야 함.
        if (item.isFirstAmongNoFolderItem()) {
            roomholder.vgLine.setVisibility(View.VISIBLE);
        } else {
            roomholder.vgLine.setVisibility(View.GONE);
        }

        ImageView ivIcon = roomholder.ivIcon;
        if (!item.isUser()) {
            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        int iconVisible = View.VISIBLE;
        if (item.getType() == JandiConstants.Entity.TYPE_JOINED_ROOM) {
            iconVisible = View.GONE;
            ivIcon.setVisibility(iconVisible);
            roomholder.tvName.setText(R.string.jandi_joined_room);
            roomholder.tvName.setTypeface(Typeface.create(roomholder.tvName.getTypeface(), Typeface.BOLD));
        } else if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
            if (from == FROM_ROOM_SELECTOR) {
                iconVisible = View.GONE;
                ivIcon.setVisibility(iconVisible);
                roomholder.tvName.setText(R.string.jandi_search_category_everywhere);
            } else {
                ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivIcon, R.drawable.icon_search_all_members);

                roomholder.tvName.setText(R.string.jandi_search_category_everyone);
            }

            roomholder.tvName.setTypeface(Typeface.create(roomholder.tvName.getTypeface(), Typeface.BOLD));
        } else if (item.isUser()) {
            roomholder.tvName.setTypeface(Typeface.create(roomholder.tvName.getTypeface(), Typeface.NORMAL));
            String fileUrl = ImageUtil.getImageFileUrl(item.getProfileUrl());
            boolean jandiBot = TeamInfoLoader.getInstance().isJandiBot(item.getEntityId());

            SpannableStringBuilder name = new SpannableStringBuilder();
            if (jandiBot) {
                ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivIcon, R.drawable.logotype_80);
                name.append(item.getName());
            } else {

                if (!item.isInactive()) {
                    ImageUtil.loadProfileImage(ivIcon, fileUrl, R.drawable.profile_img);
                    name.append(item.getName());
                } else {
                    ImageLoader.loadFromResources(ivIcon, R.drawable.profile_img_dummyaccount_32);
                    name.append(item.getEmail());
                }

                if (!item.isEnabled()) {
                    roomholder.vDisableCover.setVisibility(View.VISIBLE);
                    roomholder.vLineThrough.setVisibility(View.VISIBLE);
                }
            }
            roomholder.tvName.setText(name);
        } else if (item.isPublicTopic()) {
            roomholder.tvName.setTypeface(Typeface.create(roomholder.tvName.getTypeface(), Typeface.NORMAL));
            int resId = R.drawable.topiclist_icon_topic_fav;
            if (!item.isStarred()) {
                resId = R.drawable.topiclist_icon_topic;
            }

            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivIcon, resId);

            roomholder.tvName.setText(item.getName());
        } else {
            roomholder.tvName.setTypeface(Typeface.create(roomholder.tvName.getTypeface(), Typeface.NORMAL));
            int resId = R.drawable.topiclist_icon_topic_private_fav;
            if (!item.isStarred()) {
                resId = R.drawable.topiclist_icon_topic_private;
            }

            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivIcon, resId);

            roomholder.tvName.setText(item.getName());
        }

        if (ivIcon.getVisibility() != iconVisible) {
            ivIcon.setVisibility(iconVisible);
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
        ExpandRoomData item = getItem(position);
        if (item.isFolder()) {
            return TYPE_FOLDER;
        } else {
            if (item instanceof ExpandRoomData.DummyDisabledRoomData) {
                return TYPE_DUMMY_DISABLE;
            } else {
                return TYPE_ROOM;
            }
        }
    }

    @Override
    public int getItemCount() {
        for (int idx = roomDatas.size() - 1; idx >= 0; idx--) {
            ExpandRoomData roomData = roomDatas.get(idx);
            if (roomData.isEnabled()) {
                return roomDatas.size();
            } else {
                if (roomData instanceof ExpandRoomData.DummyDisabledRoomData) {
                    if (((ExpandRoomData.DummyDisabledRoomData) roomData).isExpanded()) {
                        return roomDatas.size();
                    } else {
                        return idx + 1;
                    }
                }
            }
        }
        return roomDatas.size();
    }

    public void clear() {
        roomDatas.clear();
    }

    public void addAll(List<ExpandRoomData> categorizableEntities) {
        roomDatas.addAll(categorizableEntities);
    }

    public void add(int position, ExpandRoomData expandRoomData) {
        roomDatas.add(position, expandRoomData);
    }


    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivIcon;
        private View vgLine;
        private View vLineThrough;
        private View vDisableCover;

        public RoomViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_selector_item_icon);
            vgLine = itemView.findViewById(R.id.v_line_use_for_first_no_folder_item);
            vLineThrough = itemView.findViewById(R.id.iv_room_selector_item_name_line_through);
            vDisableCover = itemView.findViewById(R.id.v_room_selector_disabled_warning);
        }
    }

    private static class FolderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private View viewLine;

        public FolderViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewLine = itemView.findViewById(R.id.view_line);
        }
    }

    private static class DisabledGroupDummyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivArrow;
        private TextView tvName;
        private ImageView ivIcon;

        public DisabledGroupDummyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_selector_item_icon);
            ivArrow = ((ImageView) itemView.findViewById(R.id.iv_room_selector_item_arrow));
        }

        public void setUp(ExpandRoomData item) {
            ExpandRoomData.DummyDisabledRoomData dummy = (ExpandRoomData.DummyDisabledRoomData) item;

            ivIcon.setImageResource(R.drawable.icon_disabled_members);

            if (dummy.isExpanded()) {
                ivArrow.setImageResource(R.drawable.icon_arrow_up_disabled_members);
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.jandi_transparent_white_90p));
            } else {
                ivArrow.setImageResource(R.drawable.icon_arrow_disabled_members);
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.jandi_more_bg));
            }


            SpannableStringBuilder name = new SpannableStringBuilder();
            name.append(item.getName())
                    .append(" ")
                    .append(tvName.getResources().getString(R.string.jandi_count_with_brace, dummy.getCount()));
            tvName.setText(name);
        }
    }
}
