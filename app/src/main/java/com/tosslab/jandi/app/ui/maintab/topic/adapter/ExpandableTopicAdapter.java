package com.tosslab.jandi.app.ui.maintab.topic.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.utils.AbstractExpandableItemAdapter;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.utils.ExpandableViewUtils;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.viewholder.TopicFolderViewHolder;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.viewholder.TopicItemViewHolder;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.viewholder.TopicJoinButtonViewHolder;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.views.listeners.OnExpandableChildItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnExpandableChildItemLongClickListener;
import com.tosslab.jandi.app.views.listeners.OnExpandableGroupItemClickListener;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 8. 26..
 */
public class ExpandableTopicAdapter
        extends AbstractExpandableItemAdapter<TopicFolderViewHolder, TopicItemViewHolder> {

    // 그룹이 없는 리스트를 포함하기 위한 더미 그룹 타입
    public static final int TYPE_NO_GROUP = 1;
    // 하단의 TOPIC JOIN BUTTON 을 삽입하기 위한 타입
    public static final int TYPE_FOR_JOIN_TOPIC_BUTTON = 2;
    private static final int TYPE_HAS_GROUP = 0;
    private OnExpandableChildItemClickListener onExpandableChildItemClickListener;
    private OnExpandableChildItemLongClickListener onExpandableChildItemLongClickListener;
    private OnExpandableGroupItemClickListener onExpandableGroupItemClickListener;

    private TopicFolderListDataProvider provider;

    private int selectedEntity;
    private AnimStatus animStatus = AnimStatus.READY;


    public ExpandableTopicAdapter(TopicFolderListDataProvider dataProvider) {
        this.provider = dataProvider;
        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public void setProvider(TopicFolderListDataProvider dataProvider) {
        this.provider = dataProvider;
    }

    @Override
    public int getGroupCount() {
        return provider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return provider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return provider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return provider.getChildItem(groupPosition, childPosition).getChildId();
    }

    public TopicItemData getTopicItemData(int groupPosition, int childPosition) {
        return (TopicItemData) provider.getChildItem(groupPosition, childPosition);
    }

    public List<TopicItemData> getAllTopicItemData() {

        List<TopicItemData> topicItemDatas = new ArrayList<>();

        for (int i = 0; i < getGroupCount(); i++) {
            for (int j = 0; j < getChildCount(i); j++)
                topicItemDatas.add(getTopicItemData(i, j));
        }

        // 마지막에 들어가 있는 더미 아이템을 빼주기 위해서
        topicItemDatas.remove(topicItemDatas.size() - 1);

        return topicItemDatas;

    }

    public TopicFolderData getTopicFolderData(int groupPosition) {
        return (TopicFolderData) provider.getGroupItem(groupPosition);
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        if (groupPosition == getGroupCount() - 1) {
            return TYPE_NO_GROUP;
        }
        return TYPE_HAS_GROUP;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        if (groupPosition == getGroupCount() - 1 &&
                childPosition == getChildCount(groupPosition) - 1) {
            return TYPE_FOR_JOIN_TOPIC_BUTTON;
        }
        return 0;
    }

    @Override
    public TopicFolderViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_topic_folder_list, parent, false);
        return new TopicFolderViewHolder(v);
    }

    @Override
    public TopicItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // 리스트의 마지막에 Join Topic Button 추가
        if (viewType == TYPE_FOR_JOIN_TOPIC_BUTTON) {
            final View v = inflater.inflate(R.layout.item_join_topic, parent, false);
            return new TopicJoinButtonViewHolder(v);
        }

        final View v = inflater.inflate(R.layout.item_topic_list, parent, false);
        return new TopicItemViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(TopicFolderViewHolder holder, int groupPosition, int viewType) {
        final TopicFolderData item = getTopicFolderData(groupPosition);

        holder.container.setVisibility(View.VISIBLE);
        holder.tvTitle.setText(item.getTitle());
        holder.tvTopicCnt.setText(String.valueOf(item.getItemCount()));

        holder.itemView.setClickable(true);
        if (viewType == TYPE_NO_GROUP) {
            holder.container.setVisibility(View.GONE);
        }

        final int expandState = holder.getExpandStateFlags();

        if ((item.getItemCount() > 0) && (expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
            holder.tvTopicCnt.setBackgroundResource(R.drawable.topiclist_icon_folder_open);
            holder.tvTopicCnt.setTextColor(0xff154a67);
            holder.tvTitle.setTextColor(0xff154a67);
            holder.vgChildBadgeCnt.setVisibility(View.GONE);
            holder.ivDefaultUnderline.setVisibility(View.GONE);
        } else {
            holder.tvTopicCnt.setBackgroundResource(R.drawable.topiclist_icon_folder);
            holder.tvTopicCnt.setTextColor(0xffa6a6a6);
            holder.tvTitle.setTextColor(0xffa6a6a6);
            if (item.getChildBadgeCnt() > 0) {
                holder.vgChildBadgeCnt.setVisibility(View.VISIBLE);
                holder.tvChildBadgeCnt.setText(String.valueOf(item.getChildBadgeCnt()));
            } else {
                holder.vgChildBadgeCnt.setVisibility(View.GONE);
            }
            holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
        }

        holder.vgFolderSetting.setClickable(true);
        holder.vgFolderSetting.setOnClickListener(v -> {
            if (onExpandableGroupItemClickListener != null) {
                onExpandableGroupItemClickListener.onItemClick(holder.vgFolderSetting,
                        ExpandableTopicAdapter.this, groupPosition);
            }
        });


    }

    @Override
    public void onBindChildViewHolder(TopicItemViewHolder holder, int groupPosition, int childPosition, int viewType) {

        // 리스트의 마지막에 Join Topic Button 추가
        if (viewType == TYPE_FOR_JOIN_TOPIC_BUTTON) {
            holder.itemView.setOnClickListener(v -> {
                EventBus.getDefault().post(new JoinableTopicCallEvent());
            });
            return;
        }

        final TopicItemData item = (TopicItemData) provider.getChildItem(groupPosition, childPosition);

        if (getGroupItemViewType(groupPosition) != TYPE_NO_GROUP) {
            holder.container.setBackgroundResource(R.drawable.bg_list_innerfolder_item);
            if (childPosition != getChildCount(groupPosition) - 1) {
                holder.ivFolderItemUnderline.setVisibility(View.VISIBLE);
                holder.ivDefaultUnderline.setVisibility(View.GONE);
            } else {
                holder.ivFolderItemUnderline.setVisibility(View.GONE);
                holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
            }
            if (childPosition == 0) {
                holder.ivShadowUnderline.setVisibility(View.VISIBLE);
            } else {
                holder.ivShadowUnderline.setVisibility(View.GONE);
            }
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_list_item);
            holder.ivFolderItemUnderline.setVisibility(View.GONE);
            holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
            holder.ivShadowUnderline.setVisibility(View.GONE);
        }

        holder.itemView.setClickable(true);
        holder.tvTopicName.setText(item.getName());

        if (item.getUnreadCount() > 0) {
            holder.vgTopicBadge.setVisibility(View.VISIBLE);
            holder.tvTopicBadge.setText(item.getUnreadCount() + "");
        } else {
            holder.vgTopicBadge.setVisibility(View.GONE);
        }
        holder.tvTopicUserCnt.setText("(" + item.getMemberCount() + ")");
        if (!TextUtils.isEmpty(item.getDescription())) {
            holder.tvTopicDescription.setVisibility(View.VISIBLE);
            holder.tvTopicDescription.setText(item.getDescription());
        } else {
            holder.tvTopicDescription.setVisibility(View.GONE);
        }

        if (item.isPublic()) {
            if (item.isStarred()) {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
        } else {
            if (item.isStarred()) {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
        }

        if (!item.isPushOn()) {
            holder.vPushOff.setVisibility(View.VISIBLE);
        } else {
            holder.vPushOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onExpandableChildItemClickListener != null) {
                onExpandableChildItemClickListener.onItemClick(holder.itemView,
                        ExpandableTopicAdapter.this, groupPosition, childPosition);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onExpandableChildItemLongClickListener != null) {
                onExpandableChildItemLongClickListener.onItemClick(holder.itemView,
                        ExpandableTopicAdapter.this, groupPosition, childPosition);
                return true;
            }
            return false;
        });

        if (item.getEntityId() == selectedEntity && animStatus == AnimStatus.READY) {

            Context context = holder.itemView.getContext();
            animStatus = AnimStatus.IN_ANIM;
            Integer colorFrom;
            if (getGroupItemViewType(groupPosition) != TYPE_NO_GROUP) {
                colorFrom = context.getResources().getColor(
                        R.color.jandi_list_item_background_inner_folder);
            } else {
                colorFrom = context.getResources().getColor(R.color.white);
            }
            Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_50);

            final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(1);
            colorAnimation.addUpdateListener(animator -> holder.container.setBackgroundColor(
                    (Integer) animator.getAnimatedValue()));

            colorAnimation.addListener(new SimpleEndAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animStatus = AnimStatus.FINISH;
                }
            });

            colorAnimation.start();
        }

    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(TopicFolderViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        if (getTopicFolderData(groupPosition).getItemCount() == 0) {
            return false;
        }
        // check the item is *not* pinned
        if (getTopicFolderData(groupPosition).isPinnedToSwipeLeft()) {
            // return false to raise View.OnClickListener#onClick() event
            return false;
        }
        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        final View containerView = holder.container;
        final View settingView = holder.vgFolderSetting;


        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return !ExpandableViewUtils.hitTest(settingView, x - offsetX, y - offsetY);
    }

    public void setOnChildItemClickListener(OnExpandableChildItemClickListener onExpandableChildItemClickListener) {
        this.onExpandableChildItemClickListener = onExpandableChildItemClickListener;
    }

    public void setOnChildItemLongClickListener(OnExpandableChildItemLongClickListener onExpandableChildItemLongClickListener) {
        this.onExpandableChildItemLongClickListener = onExpandableChildItemLongClickListener;
    }

    public void setOnGroupItemClickListener(OnExpandableGroupItemClickListener onExpandableGroupItemClickListener) {
        this.onExpandableGroupItemClickListener = onExpandableGroupItemClickListener;
    }

    public void updateGroupBadgeCount() {
        int groupCount = getGroupCount();
        for (int groupIdx = 0; groupIdx < groupCount; groupIdx++) {
            int childCount = getChildCount(groupIdx);
            int badgeCount = 0;
            for (int childIdx = 0; childIdx < childCount; childIdx++) {
                TopicItemData topicItemData = getTopicItemData(groupIdx, childIdx);
                badgeCount += topicItemData.getUnreadCount();
            }
            getTopicFolderData(groupIdx).setChildBadgeCnt(badgeCount);
        }
    }

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    public int findGroupIdOfChildEntity(int entityId) {
        if (entityId <= 0) {
            return -1;
        }
        int groupCount = getGroupCount();
        for (int groupIdx = 0; groupIdx < groupCount; groupIdx++) {
            int childCount = getChildCount(groupIdx);
            for (int childIdx = 0; childIdx < childCount; childIdx++) {
                if (getTopicItemData(groupIdx, childIdx).getEntityId() == entityId) {
                    return getTopicFolderData(groupIdx).getFolderId();
                }
            }
        }
        return -1;
    }

    private enum AnimStatus {
        READY, IN_ANIM, FINISH, IDLE
    }

}