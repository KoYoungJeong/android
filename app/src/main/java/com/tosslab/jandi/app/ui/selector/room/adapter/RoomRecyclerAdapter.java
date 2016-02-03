package com.tosslab.jandi.app.ui.selector.room.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 9. 30..
 */
public class RoomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int TYPE_FOLDER = 1;
    final int TYPE_ROOM = 2;
    final int TYPE_DUMMY_DISABLE = 3;

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

        SimpleDraweeView ivIcon = roomholder.ivIcon;
        if (!item.isUser()) {
            GenericDraweeHierarchy hierarchy = ivIcon.getHierarchy();
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
        }

        if (item.getType() == FormattedEntity.TYPE_EVERYWHERE) {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.icon_search_all_rooms, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.icon_search_all_rooms))
                    .into(ivIcon);
            roomholder.tvName.setText(R.string.jandi_file_category_everywhere);

        } else if (item.isUser()) {
            String fileUrl = ImageUtil.getImageFileUrl(item.getProfileUrl());
            boolean jandiBot = EntityManager.getInstance().isBot(item.getEntityId());

            ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
            if (!jandiBot) {
                layoutParams.height = layoutParams.width;
            } else {
                layoutParams.height = layoutParams.width * 5 / 4;
            }
            ivIcon.setLayoutParams(layoutParams);

            SpannableStringBuilder name = new SpannableStringBuilder();
            if (jandiBot) {
                ImageLoader.newBuilder()
                        .placeHolder(R.drawable.bot_32x40, ScalingUtils.ScaleType.CENTER_INSIDE)
                        .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .load(UriFactory.getResourceUri(R.drawable.bot_32x40))
                        .into(ivIcon);
                name.append(item.getName());
            } else {
                ImageUtil.loadProfileImage(ivIcon, fileUrl, R.drawable.profile_img_comment);
                name.append(item.getName());

                if (!item.isEnabled()) {
                    roomholder.vDisableCover.setVisibility(View.VISIBLE);
                    roomholder.vLineThrough.setVisibility(View.VISIBLE);
                }
            }
            roomholder.tvName.setText(name);
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

    public void addAll(List<ExpandRoomData> categorizableEntities) {
        roomDatas.clear();
        roomDatas.addAll(categorizableEntities);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private SimpleDraweeView ivIcon;
        private View vgLine;
        private View vLineThrough;
        private View vDisableCover;

        public RoomViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            ivIcon = (SimpleDraweeView) itemView.findViewById(R.id.iv_room_selector_item_icon);
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
        private TextView tvName;
        private SimpleDraweeView ivIcon;

        public DisabledGroupDummyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            ivIcon = (SimpleDraweeView) itemView.findViewById(R.id.iv_room_selector_item_icon);
        }

        public void setUp(ExpandRoomData item) {
            ExpandRoomData.DummyDisabledRoomData dummy = (ExpandRoomData.DummyDisabledRoomData) item;
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.icon_disabled_members, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.icon_disabled_members))
                    .into(ivIcon);

            SpannableStringBuilder name = new SpannableStringBuilder();
            name.append(item.getName())
                    .append(" ");

            int start = name.length();
            name.append(tvName.getResources().getString(R.string.jandi_count_with_brace, dummy.getCount()));
            int end = name.length();
            name.setSpan(new HighlightSpannable(Color.TRANSPARENT, 0xFFACACAC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvName.setText(name);
        }
    }
}
