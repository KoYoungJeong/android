package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.toss.app.network.entities.CdpMessages;
import com.tosslab.toss.app.network.entities.ResChannelMessages;
import com.tosslab.toss.app.network.entities.ResDirectMessages;
import com.tosslab.toss.app.network.entities.ResPrivateGroupMessage;

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

    public void retrieveChannelMessageItem(ResChannelMessages messages) {
        if (mMessages == null) {
            return;
        }
        for (ResChannelMessages.ChannelMessage message : messages.messages) {
            mMessages.add(0, new MessageItem(message.id, message.writer.nickname,
                    message.writer.photoUrl, message.createTime,
                    message.contentType, message.content));
        }
    }

    public void retrievePgMessageItem(ResPrivateGroupMessage messages) {
        if (mMessages == null) {
            return;
        }
        for (ResPrivateGroupMessage.PrivateGroupMessage message : messages.messages) {
            mMessages.add(0, new MessageItem(message.id, message.writer.nickname,
                    message.writer.photoUrl, message.createTime,
                    message.contentType, message.content));
        }
    }

    public void retrieveDirectMessageItem(ResDirectMessages messages) {
        if (mMessages == null) {
            return;
        }

        for (ResDirectMessages.DirectMessage message : messages.messages) {
            mMessages.add(0, new MessageItem(message.id, message.fromUser.nickname,
                    message.fromUser.photoUrl, message.createTime,
                    message.contentType, message.content));
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
