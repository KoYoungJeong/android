package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.TypeUtil;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.greenrobot.event.EventBus;

public class MainMessageListAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder>
        implements MessageListHeaderAdapter.MessageItemDate, MessageListAdapterView, MessageListAdapterModel {

    Context context;
    long lastMarker = -1;
    AnimState markerAnimState = AnimState.Idle;
    boolean moreFromNew;
    MoreState oldMoreState;
    MoreState newMoreState;
    MainMessageListAdapter.OnItemClickListener onItemClickListener;
    MainMessageListAdapter.OnItemLongClickListener onItemLongClickListener;
    long teamId;
    long roomId = -1;
    long entityId;
    List<ResMessages.Link> links;
    private MessagePointer messagePointer;

    private Map<ResMessages.Link, Integer> itemTypes;

    public MainMessageListAdapter(Context context) {
        this.context = context;
        oldMoreState = MoreState.Idle;
        links = new ArrayList<>();
        setHasStableIds(true);
        itemTypes = new WeakHashMap<>();

    }

    @Override
    public RecyclerBodyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BodyViewHolder viewHolder = BodyViewFactory.createViewHolder(viewType);
        View convertView = LayoutInflater.from(context).inflate(viewHolder.getLayoutId(), parent, false);
        viewHolder.initView(convertView);

        return new RecyclerBodyViewHolder(convertView, viewHolder);
    }

    @Override
    public void onBindViewHolder(RecyclerBodyViewHolder viewHolder, int position) {
        ResMessages.Link item = getItem(position);
        BodyViewHolder bodyViewHolder = viewHolder.getViewHolder();
        bodyViewHolder.bindData(item, teamId, roomId, entityId);

        if (item.id == lastMarker) {
            if (markerAnimState == MainMessageListAdapter.AnimState.Idle) {
                final View view = viewHolder.itemView;
                Integer colorFrom = context.getResources().getColor(R.color.jandi_transparent_white_1f);
                Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);
                final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(1);
                colorAnimation.addUpdateListener(animator -> view.setBackgroundColor((Integer) animator.getAnimatedValue()));

                colorAnimation.addListener(new SimpleEndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        markerAnimState = MainMessageListAdapter.AnimState.End;
                    }
                });
                colorAnimation.start();
                markerAnimState = MainMessageListAdapter.AnimState.Loading;
            }
        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, messagePointer.getLastReadLinkId());
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position <= 2 && oldMoreState == MoreState.Idle) {
            oldMoreState = MainMessageListAdapter.MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        } else if (moreFromNew && position == getItemCount() - 1
                && newMoreState == MainMessageListAdapter.MoreState.Idle) {
            newMoreState = MainMessageListAdapter.MoreState.Loading;
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MainMessageListAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MainMessageListAdapter.this, position);
            }
            return false;
        });

    }

    @Override
    public int getItemViewType(int position) {
        ResMessages.Link currentLink = getItem(position);
        ResMessages.Link previousLink = null;
        ResMessages.Link nextLink = null;
        if (position > 0) {
            previousLink = getItem(position - 1);
        }

        if (position < getItemCount() - 1) {
            nextLink = getItem(position + 1);
        }

        if (!itemTypes.containsKey(currentLink)) {
            int contentType = BodyViewFactory.getContentType(previousLink, currentLink, nextLink);
            itemTypes.put(currentLink, contentType);
            return contentType;
        } else {
            return itemTypes.get(currentLink);
        }
    }

    @Override
    public void addAll(int position, List<ResMessages.Link> links) {

        if (links == null || links.isEmpty()) {
            return;
        }

        int minPosition = Math.min(position, getItemCount());

        List<ResMessages.Link> copyList = new ArrayList<>(links);
        if (minPosition != 0) {
            for (int idx = copyList.size() - 1; idx >= 0; idx--) {
                ResMessages.Link link = copyList.get(idx);
                if (TextUtils.equals(link.status, "archived")) {
                    copyList.remove(idx);
                    int searchedPosition = indexByMessageId(link.messageId);
                    if (searchedPosition < 0) {
                        continue;
                    }

                    if (TextUtils.equals(link.message.contentType, "file")) {
                        ResMessages.Link originLink = MainMessageListAdapter.this.getItem(searchedPosition);
                        originLink.message = link.message;
                        originLink.status = "archived";
                        itemTypes.remove(originLink);
                    } else {
                        MainMessageListAdapter.this.remove(searchedPosition);
                        minPosition--;
                    }
                }
            }

            if (minPosition > 0 && minPosition <= getItemCount()) {
                itemTypes.remove(getItem(minPosition - 1));
            }
        } else {

            if (minPosition < getItemCount()) {
                itemTypes.remove(getItem(minPosition));
            }
        }
        int dummyMessageCount = getDummyMessageCount();
        if (minPosition > dummyMessageCount) {
            this.links.addAll(minPosition - dummyMessageCount, copyList);
        } else {
            this.links.addAll(minPosition, copyList);
        }
    }

    @Override
    public void remove(int position) {

        try {

            if (position > 0) {
                itemTypes.remove(links.get(position - 1));
            }
            if (position < getItemCount() - 1) {
                itemTypes.remove(links.get(position + 1));
            }
            ResMessages.Link removed = links.remove(position);
            itemTypes.remove(removed);

        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }

    }

    public void updateCachedType(int position) {
        itemTypes.remove(links.get(position));
    }

    @Override
    public ResMessages.Link getItem(int position) {
        if (position >= getItemCount()) {
            return null;
        }
        return links.get(position);
    }

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    @Override
    public int indexByMessageId(long messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    @Override
    public int indexOfDummyMessageId(long messageId) {
        int count = getItemCount();
        for (int idx = count; idx > 0; idx--) {
            ResMessages.Link item = getItem(idx);
            if (item instanceof DummyMessageLink
                    && item.messageId == messageId)
                return idx;
        }
        return -1;
    }

    @Override
    public int getLastIndexByMessageId(long messageId) {
        int lastIndex = -1;
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId) {
                lastIndex = idx;
            }
        }
        return lastIndex;
    }

    @Override
    public int indexOfLinkId(long linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public List<Integer> indexByFeedbackId(long messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_MESSAGE_COMMENT)
                    || TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_STICKER_COMMENT)) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }


        return indexList;
    }

    @Override
    public int getDummyMessagePositionByLocalId(long localId) {
        if (localId <= 0) {
            return -1;
        }

        int size = getItemCount();

        for (int idx = size - 1; idx >= 0; --idx) {
            ResMessages.Link link = getItem(idx);

            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
                if (dummyMessageLink.getLocalId() == localId) {
                    return idx;
                }
            } else {
                return -1;
            }
        }
        return -1;
    }

    public void setMarker(long lastMarker) {
        this.lastMarker = lastMarker;
    }

    public void setMoreFromNew(boolean moreFromNew) {
        this.moreFromNew = moreFromNew;
    }

    public void setNewLoadingComplete() {
        newMoreState = MoreState.Idle;
    }

    public void setNewNoMoreLoading() {
        newMoreState = MoreState.Nope;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return links.size();
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public int getDummyMessageCount() {

        int total = 0;
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            if (getItem(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    @Override
    public void removeAllDummy() {
        for (int idx = getItemCount(); idx > 0; idx--) {
            ResMessages.Link item = getItem(idx);
            if (item instanceof DummyMessageLink) {
                remove(idx);
            }
        }
    }

    @Override
    public void add(ResMessages.Link dummyMessage) {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            itemTypes.remove(links.get(itemCount - 1));
        }
        links.add(dummyMessage);
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        getItem(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }

    @Override
    public Date getItemDate(int position) {
        if (position >= getItemCount()) {
            return null;
        }

        ResMessages.Link item = getItem(position);
        return item != null ? item.time : null;
    }

    public void setMessagePointer(MessagePointer messagPointer) {
        this.messagePointer = messagPointer;
    }

    enum MoreState {
        Idle, Loading, Nope
    }

    enum AnimState {
        Idle, Loading, End
    }

    public interface NotifyDataSetChangedCallback {
        void callBack();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter adapter, int position);
    }

}
