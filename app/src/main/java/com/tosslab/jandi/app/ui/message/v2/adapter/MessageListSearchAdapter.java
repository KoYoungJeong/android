package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.queue.LimitMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.HighlightView;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.TypeUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MessageListSearchAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder> implements MessageListHeaderAdapter.MessageItemDate {

    Context context;
    long lastMarker = -1;
    AnimState markerAnimState = AnimState.Idle;
    boolean moreFromNew;
    MoreState oldMoreState;
    MoreState newMoreState;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;
    long teamId;
    long roomId = -1;
    long entityId;
    long lastReadLinkId = -1;
    List<ResMessages.Link> links;
    private boolean isLimited = false;

    public MessageListSearchAdapter(Context context) {
        this.context = context;
        this.links = new ArrayList<>();
        oldMoreState = MoreState.Idle;
        setHasStableIds(true);
    }

    public void addAll(int position, List<ResMessages.Link> messages) {

        long targetLinkId = 89869288;

        for (int i = messages.size() - 1; i >= 0; i--) {
            if (targetLinkId >= links.get(i).id) {
                if (i != messages.size() - 1) {
                    messages = messages.subList(i + 1, links.size());
                } else {
                    messages.clear();
                    messages.add(0, new LimitMessageLink());
                }
                if (messages.size() > 0 &&
                        !(messages.get(0) instanceof LimitMessageLink)) {
                    messages.add(0, new LimitMessageLink());
                }
                isLimited = true;
                break;
            }
        }

        // delete dummy message by same messageId
        for (int idx = messages.size() - 1; idx >= 0; --idx) {
            int dummyMessagePosition = getDummyMessagePositionByMessageId(messages.get(idx).messageId);
            if (dummyMessagePosition >= 0) {
                links.remove(dummyMessagePosition);
            } else {
                break;
            }
        }

        for (int idx = links.size() - 1; idx >= 0; idx--) {
            ResMessages.Link link = links.get(idx);
            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyLink = (DummyMessageLink) link;
                if (TextUtils.equals(dummyLink.getStatus(), SendMessage.Status.COMPLETE.name())) {
                    links.remove(idx);
                }
            } else {
                break;
            }
        }

        int size = messages.size();
        ResMessages.Link link;

        for (int idx = size - 1; idx >= 0; --idx) {
            link = messages.get(idx);

            if (TextUtils.equals(link.status, "created") || TextUtils.equals(link.status, "shared") || TextUtils.equals(link.status, "event")) {
            } else if (TextUtils.equals(link.status, "edited")) {
                int searchedPosition = indexByMessageId(link.messageId);
                if (searchedPosition >= 0) {
                    links.set(searchedPosition, link);
                }
                messages.remove(link);
            } else if (TextUtils.equals(link.status, "archived")) {
                int searchedPosition = indexByMessageId(link.messageId);
                // if file type
                if (TextUtils.equals(link.message.contentType, "file")) {
                    if (searchedPosition >= 0) {
                        ResMessages.Link originLink = links.get(searchedPosition);
                        originLink.message = link.message;
                        originLink.status = "archived";
                        messages.remove(link);
                    }
                    // if cannot find same object, will be addToggledUser to list.
                } else {
                    if (searchedPosition >= 0) {
                        links.remove(searchedPosition);
                    }
                    messages.remove(link);
                }
            } else {
                messages.remove(link);
            }
        }

        links.addAll(Math.min(position, links.size() - getDummyMessageCount()), messages);
    }

    public void clear() {
        links.clear();
    }

    private int getDummyMessagePositionByMessageId(long messageId) {

        int size = getItemCount();

        for (int idx = size - 1; idx >= 0; --idx) {
            ResMessages.Link link = getItem(idx);

            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
                if (dummyMessageLink.getMessageId() == messageId) {
                    return idx;
                }
            } else {
                return -1;
            }
        }
        return -1;

    }

    public void remove(int position) {
        links.remove(position);
    }

    public boolean isEndOfLoad() {
        return newMoreState == MoreState.Nope;
    }

    @Override
    public Date getItemDate(int position) {
        return getItem(position).time;
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

        if (item.id == lastMarker &&
                bodyViewHolder instanceof HighlightView) {

            View view = ((HighlightView) bodyViewHolder).getHighlightView();

            if (view != null && markerAnimState == AnimState.Idle) {
                final View contentView = view;
                Drawable originBackground = view.getBackground();
                Integer startBackgroundColor = 0;

                if (TeamInfoLoader.getInstance().getMyId() == item.fromEntity) {
                    startBackgroundColor = context.getResources().getColor(
                            R.color.jandi_messages_blue_background);
                } else {
                    startBackgroundColor = context.getResources().getColor(
                            R.color.white);
                }

                Integer colorFrom = startBackgroundColor;
                Integer colorTo = context.getResources().getColor(
                        R.color.rgb_fffad1);

                final ValueAnimator colorAnimation = ValueAnimator.ofObject(
                        new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(1);
                colorAnimation.addUpdateListener(animator ->
                        contentView.setBackgroundColor((Integer) animator.getAnimatedValue()));

                colorAnimation.addListener(new SimpleEndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        markerAnimState = AnimState.End;
                        contentView.setBackground(originBackground);
                    }
                });

                colorAnimation.start();
                markerAnimState = AnimState.Loading;
            }

        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, lastReadLinkId);
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position <= getItemCount() / 10 && oldMoreState == MoreState.Idle && !isLimited) {
            oldMoreState = MoreState.Loading;
            synchronized (this) {
                if (oldMoreState != MoreState.Idle) {
                    EventBus.getDefault().post(new RefreshOldMessageEvent());
                }
            }
        } else if (moreFromNew && position == getItemCount() - 1 && newMoreState == MoreState.Idle) {
            newMoreState = MoreState.Loading;
            synchronized (this) {
                if (newMoreState != MoreState.Idle) {
                    EventBus.getDefault().post(new RefreshNewMessageEvent());
                }
            }
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MessageListSearchAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MessageListSearchAdapter.this, position);
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

        return BodyViewFactory.getContentType(previousLink, currentLink, nextLink, roomId);
    }

    public ResMessages.Link getItem(int position) {
        return links.get(position);
    }


    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public int indexByMessageId(long messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    public int indexOfLinkId(long linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }


    public List<Integer> indexByFeedbackId(long messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_MESSAGE_COMMENT_FOR_FILE)
                    || TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_STICKER_COMMENT_FOR_FILE)
                    || TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_MESSAGE_COMMENT_FOR_POLL)
                    || TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_STICKER_COMMENT_FOR_POLL)) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }

        return indexList;
    }

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
        links.get(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }

    public long getLastReadLinkId() {
        return lastReadLinkId;
    }

    public void setLastReadLinkId(long lastReadLinkId) {
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
