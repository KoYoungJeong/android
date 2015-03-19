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
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHodler;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
public class MessageListAdapter extends RecyclerView.Adapter<RecyclerBodyViewHodler> {

    private Context context;

    private List<ResMessages.Link> messageList;

    private int lastMarker = -1;
    private AnimState markerAnimState = AnimState.Idle;
    private boolean moreFromNew;
    private MoreState oldMoreState;
    private MoreState newMoreState;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public MessageListAdapter(Context context) {
        this.context = context;
        this.messageList = new CopyOnWriteArrayList<ResMessages.Link>();
        oldMoreState = MoreState.Idle;
    }

    public int getCount() {
        return messageList.size();
    }

    public int getViewTypeCount() {
        return BodyViewHolder.Type.values().length;
    }


    @Override
    public RecyclerBodyViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {


        BodyViewHolder viewHolder = BodyViewFactory.createViewHolder(viewType);
        View convertView = LayoutInflater.from(context).inflate(viewHolder.getLayoutId(), parent, false);

        viewHolder.initView(convertView);


        RecyclerBodyViewHodler recyclerBodyViewHodler = new RecyclerBodyViewHodler(convertView, viewHolder);

        return recyclerBodyViewHodler;
    }

    @Override
    public void onBindViewHolder(RecyclerBodyViewHodler viewHolder, int position) {

        ResMessages.Link item = getItem(position);
        viewHolder.getViewHolder().bindData(item);

        if (item.id == lastMarker) {
            if (markerAnimState == AnimState.Idle) {
                final View view = viewHolder.itemView;
                Integer colorFrom = context.getResources().getColor(R.color.message_marker_highlight);
                Integer colorTo = context.getResources().getColor(R.color.jandi_message_search_item_highlight);
                final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(1);
                colorAnimation.addUpdateListener(animator -> view.setBackgroundColor((Integer) animator.getAnimatedValue()));

                colorAnimation.addListener(new SimpleEndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        markerAnimState = AnimState.End;
                        view.setBackgroundColor(context.getResources().getColor(R.color.message_marker_highlight));
                    }
                });
                colorAnimation.start();
                markerAnimState = AnimState.Loading;
            } else {
                viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.message_marker_highlight));
            }
        } else {
            viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        if (position == 0 && oldMoreState == MoreState.Idle) {
            oldMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        } else if (moreFromNew && position == getCount() - 1 && newMoreState == MoreState.Idle) {
            newMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(MessageListAdapter.this, position);
                }
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(MessageListAdapter.this, position);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0) {
            return getContentType(messageList.get(position), messageList.get(position - 1)).ordinal();
        } else {
            return getContentType(messageList.get(position), null).ordinal();
        }
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

        synchronized (messageList) {

            // delete dummy message by same messageId
            for (int idx = 0; idx < messages.size(); idx++) {
                int dummyMessagePosition = getDummyMessagePositionByMessageId(messages.get(idx).messageId);
                if (dummyMessagePosition >= 0) {
                    messageList.remove(dummyMessagePosition);
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
                    int searchedPosition = searchIndexOfMessages(messageList, link.messageId);
                    if (searchedPosition >= 0) {
                        messageList.set(searchedPosition, link);
                    }
                    messages.remove(link);
                } else if (TextUtils.equals(link.status, "archived")) {
                    int searchedPosition = searchIndexOfMessages(messageList, link.messageId);

                    // if file type
                    if (TextUtils.equals(link.message.contentType, "file")) {

                        if (searchedPosition >= 0) {
                            messageList.set(searchedPosition, link);
                            messages.remove(link);
                        }
                        // if cannot find same object, will be add to list.

                    } else {
                        if (searchedPosition >= 0) {
                            messageList.remove(searchedPosition);
                        }
                        messages.remove(link);
                    }
                } else if (TextUtils.equals(link.status, "unshared")) {
                    int searchedPosition = searchIndexOfMessages(messageList, link.messageId);
                    if (searchedPosition >= 0) {
                        messageList.set(searchedPosition, link);
                    }
                    messages.remove(link);
                } else {
                    messages.remove(link);
                }
            }

            messageList.addAll(Math.min(position, messageList.size() - getDummyMessageCount()), messages);
        }
    }

    private int getDummyMessageCount() {

        int total = 0;

        int count = getCount();

        for (int idx = count - 1; idx >= 0; --idx) {
            if (messageList.get(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    private int searchIndexOfMessages(List<ResMessages.Link> messageItems, int messageId) {
        int size = messageItems.size();
        for (int i = 0; i < size; i++) {
            if (messageItems.get(i).messageId == messageId)
                return i;
        }
        return -1;
    }

    public int indexByMessageId(int messageId) {
        int count = getCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    private BodyViewHolder.Type getContentType(ResMessages.Link message, ResMessages.Link beforeMessage) {

        if (message instanceof DummyMessageLink) {
            return BodyViewHolder.Type.Dummy;
        }

        if (TextUtils.equals(message.status, "event")) {
            return BodyViewHolder.Type.Event;
        }

        if (message.message instanceof ResMessages.TextMessage) {
            return BodyViewHolder.Type.Message;
        } else if (message.message instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) message.message).content.type;
            if (TextUtils.isEmpty(fileType) || fileType.equals("null")) {
                return BodyViewHolder.Type.File;
            }
            if (fileType.startsWith("image")) {
                return BodyViewHolder.Type.Image;
            } else {
                return BodyViewHolder.Type.File;
            }
        } else if (message.message instanceof ResMessages.CommentMessage) {

            if ((beforeMessage == null ||
                    message.feedbackId == beforeMessage.messageId ||
                    message.feedbackId == beforeMessage.feedbackId) && isSameDay(message, beforeMessage)) {
                return BodyViewHolder.Type.PureComment;
            } else {
                return BodyViewHolder.Type.FileComment;
            }

        }
        return BodyViewHolder.Type.Message;
    }

    private boolean isSameDay(ResMessages.Link message, ResMessages.Link beforeMessage) {

        if (message == null || beforeMessage == null) {
            return false;
        }

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(message.message.createTime);

        Calendar beforeCalendar = Calendar.getInstance();
        beforeCalendar.setTime(beforeMessage.message.createTime);

        int messageDay = messageCalendar.get(Calendar.DAY_OF_YEAR);
        int beforeMessageDay = beforeCalendar.get(Calendar.DAY_OF_YEAR);

        return (messageDay == beforeMessageDay);
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public ResMessages.Link getItemByLinkId(int linkId) {
        for (int idx = 0; idx < messageList.size(); idx++) {
            ResMessages.Link link = messageList.get(idx);
            if (link.messageId == linkId) {
                return link;
            }
        }
        return null;
    }

    public int getItemPositionByLinkId(int linkId) {
        for (int idx = 0; idx < messageList.size(); idx++) {
            ResMessages.Link link = messageList.get(idx);
            if (link.messageId == linkId) {
                return idx;
            }
        }

        return -1;
    }

    public void clear() {
        messageList.clear();
    }

    public void addDummyMessage(DummyMessageLink dummyMessageLink) {
        synchronized (messageList) {
            messageList.add(dummyMessageLink);
        }
    }

    public void updateMessageId(long localId, int id) {

        int dummeMessagePosition = getDummeMessagePositionByLocalId(localId);

        if (dummeMessagePosition >= 0) {
            ResMessages.Link item = getItem(dummeMessagePosition);
            item.messageId = id;
        }
    }

    public int getDummeMessagePositionByLocalId(long localId) {

        int size = getCount();

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

        int size = getCount();

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

    public void updateDummyMessageState(long localId, SendingState state) {
        int dummeMessagePositionByLocalId = getDummeMessagePositionByLocalId(localId);
        if (dummeMessagePositionByLocalId >= 0) {
            ((DummyMessageLink) getItem(dummeMessagePositionByLocalId)).setSendingState(state);
        }
    }

    public void remove(int position) {
        messageList.remove(position);
    }

    public List<Integer> indexByFeedbackId(int messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getCount();
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
