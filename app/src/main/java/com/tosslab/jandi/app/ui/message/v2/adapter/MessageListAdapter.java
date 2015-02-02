package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.HeaderViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
public class MessageListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;

    private List<ResMessages.Link> messageList;

    private MoreState moreState;

    public MessageListAdapter(Context context) {
        this.context = context;
        this.messageList = new CopyOnWriteArrayList<ResMessages.Link>();
        moreState = MoreState.Idle;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {

        HeaderViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message_header, parent, false);
            viewHolder = new HeaderViewHolder();
            viewHolder.dateTextView = (android.widget.TextView) convertView.findViewById(R.id.txt_message_date_devider);

            convertView.setTag(R.id.message_header, viewHolder);
        } else {
            viewHolder = (HeaderViewHolder) convertView.getTag(R.id.message_header);
        }

        long headerId = getHeaderId(position);

        if (DateUtils.isToday(headerId)) {
            viewHolder.dateTextView.setText(R.string.today);
        } else {
            viewHolder.dateTextView.setText(DateTransformator.getTimeStringForDivider(headerId));
        }

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int itemViewType = getItemViewType(position);

        BodyViewHolder viewHolder;

        int viewHolderId = BodyViewFactory.getViewHolderId(itemViewType);

        if (convertView == null) {

            viewHolder = BodyViewFactory.createViewHolder(itemViewType);
            convertView = LayoutInflater.from(context).inflate(viewHolder.getLayoutId(), parent, false);

            viewHolder.initView(convertView);

            convertView.setTag(viewHolderId, viewHolder);
        } else {
            viewHolder = (BodyViewHolder) convertView.getTag(viewHolderId);
        }

        ResMessages.Link item = getItem(position);
        viewHolder.bindData(item);

        if (position == 0 && moreState == MoreState.Idle) {
            EventBus.getDefault().post(new RefreshOldMessageEvent());
            moreState = MoreState.Loading;
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        long time = messageList.get(position).time.getTime();
        long extraTime = time % (1000 * 60 * 60 * 24);
        time -= extraTime;
        return time;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public int getViewTypeCount() {
        return BodyViewHolder.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0) {
            return getContentType(messageList.get(position), messageList.get(position - 1)).ordinal();
        } else {
            return getContentType(messageList.get(position), null).ordinal();
        }
    }

    @Override
    public ResMessages.Link getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addAll(int position, List<ResMessages.Link> messages) {

        synchronized (messageList) {

            int size = messages.size();
            ResMessages.Link link;
            for (int idx = size - 1; idx >= 0; --idx) {
                link = messages.get(idx);
                if (TextUtils.equals(link.status, "created") || TextUtils.equals(link.status, "shared")) {
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

            messageList.addAll(Math.min(position, getCount()), messages);
        }
    }

    private int searchIndexOfMessages(List<ResMessages.Link> messageItems, int messageId) {
        for (int i = 0; i < messageItems.size(); i++) {
            if (messageItems.get(i).messageId == messageId)
                return i;
        }
        return -1;
    }

    private BodyViewHolder.Type getContentType(ResMessages.Link message, ResMessages.Link beforeMessage) {

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

    public void setNoMoreLoading() {
        moreState = MoreState.Nope;
    }

    public void setLoadingComplete() {
        moreState = MoreState.Idle;
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

    private enum MoreState {
        Idle, Loading, Nope
    }
}
