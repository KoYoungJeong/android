package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.TossLogger;

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
    private static final String TAG = TossLogger.makeLogTag(MessageItemListAdapter.class);
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

    public void insertMessageItem(ResMessages messages) {
        if (mMessages == null || messages.messageCount <= 0) {
            return;
        }

        for (ResMessages.Link link : messages.messages) {
            mMessages.add(0, new MessageItem(link));
        }
    }

    public void updatedMessageItem(ResMessages messages) {
        if (mMessages == null || messages.messageCount <= 0) {
            return;
        }

        // 업데이트 된 메시지들의 상태를 보고,
        // 새로 추가하던가, 기존 리스트 item 에 동일한 항목을 대체, 혹은 삭제한다.
        for (ResMessages.Link link : messages.messages) {
//            TossLogger.LOGE(TAG, "update Item status : " + link.status);
            if (link.status.equals("created")) {
                mMessages.add(new MessageItem(link));
            } else if (link.status.equals("edited")) {

                int position = searchIndexOfMessages(link.messageId);
                if (position >= 0) {
                    mMessages.set(position, new MessageItem(link));
                }
            } else if (link.status.equals("archived")) {
                int position = searchIndexOfMessages(link.messageId);
                if (position >= 0) {
                    mMessages.remove(position);
                }
            }
        }

    }

    // 현재 화면에 뿌려진 메시지들 중에 messageId와 동일한 놈의 index 반환
    private int searchIndexOfMessages(int messageId) {
        for (int i=0; i<mMessages.size(); i++) {
            if (mMessages.get(i).getId() == messageId)
                return i;
        }
        return -1;
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
