package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.Divider;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public abstract class MessageAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder> {

    Context context;

    int lastMarker = -1;
    AnimState markerAnimState = AnimState.Idle;
    boolean moreFromNew;
    MoreState oldMoreState;
    MoreState newMoreState;

    MessageListAdapter.OnItemClickListener onItemClickListener;
    MessageListAdapter.OnItemLongClickListener onItemLongClickListener;

    int teamId;
    int roomId = -1;
    int entityId;
    int lastReadLinkId = -1;

    List<ResMessages.Link> links;

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

        if (bodyViewHolder instanceof Divider) {
            ((Divider) bodyViewHolder).setUpDividerVisible();
        }

        if (item.id == lastMarker) {
            if (markerAnimState == MessageCursorListAdapter.AnimState.Idle) {
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
                        markerAnimState = MessageCursorListAdapter.AnimState.End;
                    }
                });
                colorAnimation.start();
                markerAnimState = MessageCursorListAdapter.AnimState.Loading;
            }
        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, lastReadLinkId);
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position <= getItemCount() / 10 && oldMoreState == MessageCursorListAdapter.MoreState.Idle) {
            oldMoreState = MessageCursorListAdapter.MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        } else if (moreFromNew && position == getItemCount() - 1 && newMoreState == MessageCursorListAdapter.MoreState.Idle) {
            newMoreState = MessageCursorListAdapter.MoreState.Loading;
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MessageAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MessageAdapter.this, position);
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

        return BodyViewFactory.getContentType(previousLink, currentLink, nextLink).ordinal();
    }

    public ResMessages.Link getItem(int position) {
        return links.get(position);
    }

    public abstract void addAll(int position, List<ResMessages.Link> messages);

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public int indexByMessageId(int messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    public int indexOfLinkId(int linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }

    public abstract void clear();

    public List<Integer> indexByFeedbackId(int messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (itemViewType == BodyViewHolder.Type.FileComment.ordinal() || itemViewType == BodyViewHolder.Type.PureComment.ordinal()) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }

        return indexList;
    }

    public abstract void updateMessageId(long localId, int messageId);

    public abstract void addDummyMessage(DummyMessageLink dummyMessageLink);

    public abstract void updateDummyMessageState(long localId, SendMessage.Status state);

    public int getDummeMessagePositionByLocalId(long localId) {
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

    public abstract void remove(int position);

    public void setMarker(int lastMarker) {
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

    public void setTeamId(int teamId) {
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

    public int getDummyMessageCount() {

        int total = 0;
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            if (links.get(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        links.get(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }

    public int getLastReadLinkId() {
        return lastReadLinkId;
    }

    public void setLastReadLinkId(int lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }

    enum MoreState {
        Idle, Loading, Nope
    }

    enum AnimState {
        Idle, Loading, End
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter adapter, int position);
    }
}
