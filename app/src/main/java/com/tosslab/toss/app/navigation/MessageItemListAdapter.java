package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.toss.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EBean
public class MessageItemListAdapter extends BaseAdapter {
    List<MessageItem> mMessages;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mMessages = new ArrayList<MessageItem>();
    }

    public void clearAdapter() {
        mMessages.clear();
        notifyDataSetChanged();
    }

    public void retrieveMessageItem(ResMessages messages) {
        if (mMessages == null || messages.messageCount <= 0) {
            return;
        }

        for (ResMessages.Link link : messages.messages) {
            mMessages.add(0, new MessageItem(link));
        }
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
        return mMessages.size();
    }

    @Override
    public MessageItem getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
