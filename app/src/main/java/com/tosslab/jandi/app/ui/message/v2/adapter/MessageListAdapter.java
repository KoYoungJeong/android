package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
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
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.Divider;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder> {

    private Context context;

    private List<ResMessages.Link> messageList;

    private int lastMarker = -1;
    private AnimState markerAnimState = AnimState.Idle;
    private boolean moreFromNew;
    private MoreState oldMoreState;
    private MoreState newMoreState;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    private int teamId;
    private int roomId;
    private int entityId;
    private int lastReadLinkId = -1;

    public MessageListAdapter(Context context) {
        this.context = context;
        this.messageList = new ArrayList<>();
        oldMoreState = MoreState.Idle;

    }

    public int getLastReadLinkId() {
        return lastReadLinkId;
    }

    public void setLastReadLinkId(int lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
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

        if (bodyViewHolder instanceof Divider) {
            ((Divider) bodyViewHolder).setUpDividerVisible();
        }

        if (item.id == lastMarker) {
            if (markerAnimState == AnimState.Idle) {
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
                        markerAnimState = AnimState.End;
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

        if (position == 0 && oldMoreState == MoreState.Idle) {
            oldMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        } else if (moreFromNew && position == getItemCount() - 1 && newMoreState == MoreState.Idle) {
            newMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MessageListAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MessageListAdapter.this, position);
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
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addAll(int position, List<ResMessages.Link> messages) {
        // delete dummy message by same messageId
        for (int idx = messages.size() - 1; idx >= 0; --idx) {
            int dummyMessagePosition = getDummyMessagePositionByMessageId(messages.get(idx).messageId);
            if (dummyMessagePosition >= 0) {
                messageList.remove(dummyMessagePosition);
            } else {
                break;
            }
        }

        for (int idx = messageList.size() - 1; idx >= 0; idx--) {
            ResMessages.Link link = messageList.get(idx);
            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyLink = (DummyMessageLink) link;
                if (TextUtils.equals(dummyLink.getStatus(), SendMessage.Status.COMPLETE.name())) {
                    messageList.remove(idx);
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
                    messageList.set(searchedPosition, link);
                }
                messages.remove(link);
            } else if (TextUtils.equals(link.status, "archived")) {
                int searchedPosition = indexByMessageId(link.messageId);
                // if file type
                if (TextUtils.equals(link.message.contentType, "file")) {
                    if (searchedPosition >= 0) {
                        ResMessages.Link originLink = messageList.get(searchedPosition);
                        originLink.message = link.message;
                        originLink.status = "archived";
                        messages.remove(link);
                    }
                    // if cannot find same object, will be add to list.
                } else {
                    if (searchedPosition >= 0) {
                        messageList.remove(searchedPosition);
                    }
                    messages.remove(link);
                }
            } else {
                messages.remove(link);
            }
        }

        messageList.addAll(Math.min(position, messageList.size() - getDummyMessageCount()), messages);
    }

    public int getDummyMessageCount() {

        int total = 0;

        int count = getItemCount();

        for (int idx = count - 1; idx >= 0; --idx) {
            if (messageList.get(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    public int indexByMessageId(int messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public void clear() {
        messageList.clear();
    }

    public void addDummyMessage(DummyMessageLink dummyMessageLink) {
        messageList.add(dummyMessageLink);
    }

    public void updateMessageId(long localId, int id) {

        int dummeMessagePosition = getDummeMessagePositionByLocalId(localId);

        if (dummeMessagePosition >= 0) {
            ResMessages.Link item = getItem(dummeMessagePosition);
            item.messageId = id;
        }
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

    private int getDummyMessagePositionByMessageId(int messageId) {

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

    public void updateDummyMessageState(long localId, SendMessage.Status state) {
        int dummeMessagePositionByLocalId = getDummeMessagePositionByLocalId(localId);
        if (dummeMessagePositionByLocalId >= 0) {
            ((DummyMessageLink) getItem(dummeMessagePositionByLocalId)).setStatus(state.name());
        }
    }

    public void remove(int position) {
        messageList.remove(position);
    }

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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public MessageListAdapter setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
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

    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        messageList.get(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }

    private enum MoreState {
        Idle, Loading, Nope
    }

    private enum AnimState {
        Idle, Loading, End
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter adapter, int position);
    }

}
