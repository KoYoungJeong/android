package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.MainTopicViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicFolderViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicItemViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicJoinButtonViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.IMarkerTopicFolderItem;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicJoinButtonData;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 2017. 2. 10..
 */

public class TopicFolderAdapter extends RecyclerView.Adapter<MainTopicViewHolder> {

    private final int VIEW_TYPE_FOLDER = 0x01;
    private final int VIEW_TYPE_ITEM = 0x02;
    private final int VIEW_TYPE_JOIN_BUTTON = 0x03;

    private List<IMarkerTopicFolderItem> items = new ArrayList<>();

    private List<IMarkerTopicFolderItem> cloneItems = new ArrayList<>();

    private Set<String> closedFolderSet = new HashSet<>();

    private OnFolderSettingClickListener onFolderSettingClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemClickListener;

    private AnimStatus animStatus = AnimStatus.READY;
    private ValueAnimator colorAnimator;

    private long selectedEntity = -1;

    public void setItems(List<IMarkerTopicFolderItem> items) {
        this.items = items;
        closedFolderSet = JandiPreference.getFolderClosedStatus();
        setCloneItems();
    }

    @Override
    public MainTopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;

        if (viewType == VIEW_TYPE_FOLDER) {
            v = inflater.inflate(R.layout.item_topic_folder_list, parent, false);
            return new TopicFolderViewHolder(v);
        } else if (viewType == VIEW_TYPE_ITEM) {
            v = inflater.inflate(R.layout.item_topic_list, parent, false);
            return new TopicItemViewHolder(v);
        } else {
            v = inflater.inflate(R.layout.item_join_topic, parent, false);
            return new TopicJoinButtonViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(MainTopicViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FOLDER) {
            final TopicFolderData topicFolderData = ((TopicFolderData) cloneItems.get(position));
            holder.setOnItemClickListener(() -> {
                long folderId = topicFolderData.getFolderId();
                changeFolderStatus(folderId);
            });
            ((TopicFolderViewHolder) holder).setOnFolderSettingClickListener(() -> {
                if (onFolderSettingClickListener != null) {
                    onFolderSettingClickListener.onClick(
                            topicFolderData.getFolderId(),
                            topicFolderData.getTitle(),
                            topicFolderData.getSeq());
                }
            });
        } else if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            final TopicItemData topicItemData = ((TopicItemData) cloneItems.get(position));
            holder.setOnItemClickListener(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(topicItemData);
                }
            });
            TopicItemViewHolder topicItemViewHolder = (TopicItemViewHolder) holder;
            topicItemViewHolder.setOnItemLongClickListener(() -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onLongClick(topicItemData);
                }
            });
            boolean isSelectedEntity = topicItemData.getEntityId() == selectedEntity;
            if (isSelectedEntity && animStatus == AnimStatus.READY) {
                animateForSelectedEntity(holder.itemView);
            }
        }

        if (getItemViewType(position) != VIEW_TYPE_JOIN_BUTTON) {
            holder.bind(cloneItems.get(position));
        } else {
            holder.itemView.setOnClickListener(v -> {
                EventBus.getDefault().post(new JoinableTopicCallEvent());
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (cloneItems.get(position) instanceof TopicFolderData) {
            return VIEW_TYPE_FOLDER;
        } else if (cloneItems.get(position) instanceof TopicItemData) {
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_JOIN_BUTTON;
        }
    }

    @Override
    public int getItemCount() {
        return cloneItems.size();
    }

    private void setFolderStatus(long folderId, boolean isOpened) {
        if (isOpened) {
            if (isFolderClosed(folderId)) {
                closedFolderSet.remove(String.valueOf(folderId));
            }
        } else {
            closedFolderSet.add(String.valueOf(folderId));
        }
        JandiPreference.setFolderClosedStatus(closedFolderSet);
    }

    private boolean isFolderClosed(long folderId) {
        return closedFolderSet.contains(String.valueOf(folderId));
    }

    private void setCloneItems() {
        cloneItems.clear();
        cloneItems.addAll(items);
        for (int i = cloneItems.size() - 1; i >= 0; i--) {
            final IMarkerTopicFolderItem item = cloneItems.get(i);

            if (item instanceof TopicFolderData) {
                TopicFolderData topicFolderData = ((TopicFolderData) item);
                boolean isClosed = isFolderClosed(topicFolderData.getFolderId());
                topicFolderData.setOpened(!isClosed);
            }

            if (item instanceof TopicItemData) {
                TopicItemData topicItemData = ((TopicItemData) item);
                boolean isClosed = isFolderClosed(topicItemData.getParentId());
                if (isClosed) {
                    cloneItems.remove(i);
                }
            }
        }
        cloneItems.add(new TopicJoinButtonData());
    }

    private void changeFolderStatus(long folderId) {
        int folderIndex = -1;
        int itemStartIndex = -1;
        int itemCnt = -1;
        boolean isOpened = false;

        // 폴더 오픈 정보 변경
        for (int i = 0; i < cloneItems.size(); i++) {
            final IMarkerTopicFolderItem item = cloneItems.get(i);
            if (item instanceof TopicFolderData) {
                TopicFolderData folderData = ((TopicFolderData) item);
                if (folderData.getFolderId() == folderId) {
                    boolean isPreviousOpened = !isFolderClosed(folderId);
                    isOpened = !isPreviousOpened;
                    setFolderStatus(folderId, isOpened);
                    folderData.setOpened(isOpened);
                    break;
                }
            }
        }

        // 데이터 셋 변경
        setCloneItems();

        // 리스트 뷰 부분 변경을 위한 변수 셋팅
        for (int i = 0; i < cloneItems.size(); i++) {
            final IMarkerTopicFolderItem item = cloneItems.get(i);
            if (item instanceof TopicFolderData) {
                TopicFolderData folderData = ((TopicFolderData) item);
                if (folderData.getFolderId() == folderId) {
                    folderIndex = i;
                    itemStartIndex = i + 1;
                    itemCnt = folderData.getItemCount();
                    break;
                }
            }
        }

        // 리스트 뷰 변경
        notifyItemChanged(folderIndex);

        if (isOpened) {
            notifyItemRangeInserted(itemStartIndex, itemCnt);
        } else {
            notifyItemRangeRemoved(itemStartIndex, itemCnt);
        }
    }

    public void setOnFolderSettingClickListener(
            OnFolderSettingClickListener onFolderSettingClickListener) {
        this.onFolderSettingClickListener = onFolderSettingClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    public long getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(long selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void stopAnimation() {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
    }

    public int indexOfEntity(long entityId) {
        int itemCount = getItemCount();
        for (int idx = 0; idx < itemCount; idx++) {
            IMarkerTopicFolderItem item = items.get(idx);
            if (item != null
                    && (item instanceof TopicItemData)
                    && (((TopicItemData) item).getEntityId() == entityId)) {
                return idx;
            }
        }
        return -1;
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

    private enum AnimStatus {
        READY, IN_ANIM, FINISH, IDLE
    }

    public interface OnFolderSettingClickListener {
        void onClick(long folderId, String folderName, int folderSeq);
    }

    public interface OnItemClickListener {
        void onClick(TopicItemData topicItemData);
    }

    public interface OnItemLongClickListener {
        void onLongClick(TopicItemData topicItemData);
    }

}