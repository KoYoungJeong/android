package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.libraries.advancerecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.AbstractExpandableItemAdapter;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.ExpandableViewUtils;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicFolderViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicItemViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicJoinButtonViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.views.listeners.OnExpandableChildItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnExpandableChildItemLongClickListener;
import com.tosslab.jandi.app.views.listeners.OnExpandableGroupItemClickListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

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

    private long selectedEntity = -1;
    private AnimStatus animStatus = AnimStatus.READY;
    private ValueAnimator colorAnimator;

    public ExpandableTopicAdapter(TopicFolderListDataProvider dataProvider) {
        this.provider = dataProvider;
        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public TopicFolderListDataProvider getProvider() {
        return provider;
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
        TopicFolderData groupItem = provider.getGroupItem(groupPosition);
        return groupItem != null ? groupItem.getGroupId() : -1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        TopicItemData childItem = provider.getChildItem(groupPosition, childPosition);
        return childItem != null ? childItem.getChildId() : -1;
    }

    @Nullable
    public TopicItemData getTopicItemData(int groupPosition, int childPosition) {
        return provider.getChildItem(groupPosition, childPosition);
    }

    public List<TopicItemData> getAllTopicItemData() {
        List<TopicItemData> topicItemDatas = new ArrayList<>();

        for (int i = 0; i < getGroupCount(); i++) {
            for (int j = 0; j < getChildCount(i); j++) {
                TopicItemData topicItemData = getTopicItemData(i, j);
                if (topicItemData != null) {
                    topicItemDatas.add(topicItemData);
                }
            }
        }

        // 마지막에 들어가 있는 더미 아이템을 빼주기 위해서
        if (topicItemDatas.size() > 0) {
            if (topicItemDatas.get(topicItemDatas.size() - 1).getEntityId() <= 0) {
                topicItemDatas.remove(topicItemDatas.size() - 1);
            }
        }

        return topicItemDatas;
    }

    @Nullable
    public TopicFolderData getTopicFolderData(int groupPosition) {
        return provider.getGroupItem(groupPosition);
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
        TopicItemData topic = getTopicItemData(groupPosition, childPosition);
        // 더미인 경우에만 처리하도록 함
        if (topic != null && topic.getEntityId() <= 0 && groupPosition == getGroupCount() - 1 &&
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // 리스트의 마지막에 Join Topic Button 추가
        if (viewType == TYPE_FOR_JOIN_TOPIC_BUTTON) {
            View v = inflater.inflate(R.layout.item_join_topic, parent, false);
            return new TopicJoinButtonViewHolder(v);
        }

        View v = inflater.inflate(R.layout.item_topic_list, parent, false);
        return new TopicItemViewHolder(v, true);
    }

    @Override
    public void onBindGroupViewHolder(TopicFolderViewHolder holder, int groupPosition, int viewType) {
        final TopicFolderData item = getTopicFolderData(groupPosition);

        if (item == null) {
            return;
        }

        holder.container.setVisibility(View.VISIBLE);
        holder.tvTitle.setText(item.getTitle());
        holder.tvChildCount.setText(String.format(" (%d)", item.getItemCount()));

        holder.itemView.setClickable(true);

        if (viewType == TYPE_NO_GROUP) {
            holder.container.setVisibility(View.GONE);
        }

        final int expandState = holder.getExpandStateFlags();

        if ((item.getItemCount() > 0) && (expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
            holder.container.setBackgroundResource(R.drawable.bg_list_innerfolder_item);
            holder.ivDefaultUnderline.setVisibility(View.GONE);
            holder.tvChildBadgeCnt.setVisibility(View.GONE);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_list_item);
            holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
            if (item.getChildBadgeCnt() > 0) {
                holder.tvChildBadgeCnt.setVisibility(View.VISIBLE);
                if (item.getChildBadgeCnt() > 999) {
                    holder.tvChildBadgeCnt.setText(String.valueOf(999));
                } else {
                    holder.tvChildBadgeCnt.setText(String.valueOf(item.getChildBadgeCnt()));
                }
            } else {
                holder.tvChildBadgeCnt.setVisibility(View.GONE);
            }
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

        TopicItemData item = provider.getChildItem(groupPosition, childPosition);
        if (item == null) {
            return;
        }

        if (getGroupItemViewType(groupPosition) != TYPE_NO_GROUP) {
            holder.container.setBackgroundResource(R.drawable.bg_list_innerfolder_item);
            if (childPosition != getChildCount(groupPosition) - 1) {
                holder.ivFolderItemUnderline.setVisibility(View.VISIBLE);
                holder.ivDefaultUnderline.setVisibility(View.GONE);
            } else {
                holder.ivFolderItemUnderline.setVisibility(View.GONE);
                holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
            }
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_list_item);
            holder.ivFolderItemUnderline.setVisibility(View.GONE);
            holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
        }

        holder.itemView.setClickable(true);
        holder.tvTopicName.setText(item.getName());

        if (item.getUnreadCount() > 0) {
            holder.tvTopicBadge.setVisibility(View.VISIBLE);
            if (item.getUnreadCount() > 999) {
                holder.tvTopicBadge.setText(String.valueOf(999));
            } else {
                holder.tvTopicBadge.setText(String.valueOf(item.getUnreadCount()));
            }
        } else {
            holder.tvTopicBadge.setVisibility(View.GONE);
        }

        if (item.getUnreadCount() <= 0 && !item.isReadOnly()) {
            holder.vgTopicBadge.setVisibility(View.GONE);
        } else {
            holder.vgTopicBadge.setVisibility(View.VISIBLE);
        }

        holder.tvTopicUserCnt.setText("(" + item.getMemberCount() + ")");
        if (!TextUtils.isEmpty(item.getDescription())) {
            holder.vgTopicDescription.setVisibility(View.VISIBLE);
            holder.tvTopicDescription.setVisibility(View.VISIBLE);
            holder.tvTopicDescription.setText(item.getDescription());
        } else {
            holder.vgTopicDescription.setVisibility(View.GONE);
            holder.tvTopicDescription.setVisibility(View.GONE);
        }

        if (item.isReadOnly()) {
            if (holder.vgTopicDescription.getVisibility() == View.GONE) {
                holder.vgTopicDescription.setVisibility(View.VISIBLE);
            }
            holder.vReadOnly.setVisibility(View.VISIBLE);
        } else {
            holder.vReadOnly.setVisibility(View.GONE);
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

        boolean isSelectedEntity = item.getEntityId() == selectedEntity;
        if (isSelectedEntity && animStatus == AnimStatus.READY) {
            animateForSelectedEntity(holder.vAnimator);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
    }

    private void animateForSelectedEntity(final View targetView) {
        Context context = targetView.getContext();

        animStatus = AnimStatus.IN_ANIM;
        int colorFrom = Color.TRANSPARENT;
        int colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);

        colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimator.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
        colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimator.setRepeatCount(1);
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animStatus = AnimStatus.FINISH;
                targetView.setBackgroundColor(Color.TRANSPARENT);
                colorAnimator.removeAllListeners();
            }
        });
        colorAnimator.addUpdateListener(animation ->
                targetView.setBackgroundColor((Integer) animation.getAnimatedValue()));
        colorAnimator.start();
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(TopicFolderViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        TopicFolderData topicFolderData = getTopicFolderData(groupPosition);
        if (topicFolderData == null || topicFolderData.getItemCount() == 0) {
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

    public long getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(long selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    public boolean isIdleOfAnim() {
        return animStatus == AnimStatus.IDLE;
    }

    private enum AnimStatus {
        READY, IN_ANIM, FINISH, IDLE
    }

}