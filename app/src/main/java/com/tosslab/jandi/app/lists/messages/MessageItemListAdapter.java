package com.tosslab.jandi.app.lists.messages;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.events.messages.ReqeustMoreMessageEvent;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EBean
public class MessageItemListAdapter extends BaseAdapter {
    private final Logger log = Logger.getLogger(MessageItemListAdapter.class);
    @RootContext
    Context mContext;
    private List<MessageItem> mFormattedMessages;

    private MoreState moreState;

    @AfterInject
    void initAdapter() {
        mFormattedMessages = new ArrayList<MessageItem>();
    }

    public void clearAdapter() {
        mFormattedMessages.clear();
        notifyDataSetChanged();
    }

    public void clearAdapterWithoutUpdate() {
        mFormattedMessages.clear();
    }

    public void replaceMessageItem(List<MessageItem> messageItems) {
        mFormattedMessages = messageItems;
        moreState = MoreState.IDLE;
    }

    public int getLastLinkId() {
        return (mFormattedMessages.size() <= 0)
                ? -1
                : mFormattedMessages.get(mFormattedMessages.size() - 1).getLinkId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MessageItemView messageItemView;
        if (convertView == null) {
            messageItemView = MessageItemView_.build(mContext);
        } else {
            messageItemView = (MessageItemView) convertView;
        }

        messageItemView.bind(getItem(position));

        if (position == 0 && moreState == MoreState.IDLE) {
            EventBus.getDefault().post(new ReqeustMoreMessageEvent());
            moreState = MoreState.LOADING;
        }

        return messageItemView;
    }

    @Override
    public int getCount() {
        return mFormattedMessages.size();
    }

    @Override
    public MessageItem getItem(int position) {
        return mFormattedMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private enum MoreState {
        IDLE, LOADING
    }
}
