package com.tosslab.jandi.app.lists.messages;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EBean
public class MessageItemListAdapter extends BaseAdapter {
    private final Logger log = Logger.getLogger(MessageItemListAdapter.class);
    private List<MessageItem> mFormattedMessages;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mFormattedMessages = new ArrayList<MessageItem>();
    }

    public void clearAdapter() {
        mFormattedMessages.clear();
        notifyDataSetChanged();
    }

    public void replaceMessageItem(List<MessageItem> messageItems) {
        mFormattedMessages = messageItems;
    }

    public int getLastLinkId() {
        return (mFormattedMessages.size() <= 0)
                ? -1
                : mFormattedMessages.get(mFormattedMessages.size()-1).getLinkId();
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
}
