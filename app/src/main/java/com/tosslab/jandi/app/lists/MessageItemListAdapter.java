package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EBean
public class MessageItemListAdapter extends BaseAdapter {
    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat ("yyyyMMdd");

    private final Logger log = Logger.getLogger(MessageItemListAdapter.class);
    private List<MessageItem> mFormattedMessages;
    private List<MessageItem> mMessages;
    private String mCurrentDay = "";

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mFormattedMessages = new ArrayList<MessageItem>();
        mMessages = new ArrayList<MessageItem>();
    }

    public void clearAdapter() {
        mFormattedMessages.clear();
        mMessages.clear();
        notifyDataSetChanged();
    }

    /**
     * Get Message 혹은, Update Message에서 들어오는 message를 link id로 정렬
     */
    private List<ResMessages.Link> descSortByLinkId(List<ResMessages.Link> links) {
        List<ResMessages.Link> ret = new ArrayList<ResMessages.Link>(links);

        Comparator<ResMessages.Link> sort = new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link link, ResMessages.Link link2) {
                if (link.id > link2.id)
                    return -1;
                else if (link.id == link2.id)
                    return 0;
                else
                    return 1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    private List<ResMessages.Link> asceSortByLinkId(List<ResMessages.Link> links) {
        List<ResMessages.Link> ret = new ArrayList<ResMessages.Link>(links);

        Comparator<ResMessages.Link> sort = new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link link, ResMessages.Link link2) {
                if (link.id > link2.id)
                    return 1;
                else if (link.id == link2.id)
                    return 0;
                else
                    return -1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    public int getLastLinkId() {
        return mMessages.get(mMessages.size()-1).getLinkId();
    }

    public void insertMessageItem(ResMessages messages) {
        patchMessageItem(messages, true);
    }

    public void updatedMessageItem(ResMessages messages) {
        patchMessageItem(messages, false);
    }

    public void patchMessageItem(ResMessages messages, boolean isDescendingOrder) {
        if (mMessages == null || messages.messageCount <= 0) {
            return;
        }

        List<ResMessages.Link> sortedLinks = (isDescendingOrder)
                ? descSortByLinkId(messages.messages)
                : asceSortByLinkId(messages.messages);

        // 업데이트 된 메시지들의 상태를 보고,
        // 새로 추가하던가, 기존 리스트 item 에 동일한 항목을 대체, 혹은 삭제한다.
        for (ResMessages.Link link : sortedLinks) {
            log.debug("updatedMessageItem : " + link.status);
            if (link.status.equals("created") || link.status.equals("shared")) {
                if (isDescendingOrder)
                    mMessages.add(0, new MessageItem(link));
                else
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
        // poor implement : 현재 isDescendingOrder면 신규 추가임.
        reformatMessages(isDescendingOrder);
    }

    private void reformatMessages(boolean isInsert) {
        mFormattedMessages.clear();
        for (MessageItem item : mMessages) {
            String strDay = DATE_FORMATTER.format(item.getLinkTime());
            if (!mCurrentDay.equals(strDay)) {
                // 바로 이전의 message 날짜와 같지 않으면 날짜 경계선을 먼저 추가한다.
                mCurrentDay = strDay;
                try {
                    mFormattedMessages.add(new MessageItem(DATE_FORMATTER.parse(strDay)));
                } catch (ParseException e) {
                    log.error("Date Parse Error", e);
                }

            }
            mFormattedMessages.add(item);
        }
    }

    // 현재 화면에 뿌려진 메시지들 중에 messageId와 동일한 놈의 index 반환
    private int searchIndexOfMessages(int messageId) {
        for (int i=0; i< mMessages.size(); i++) {
            if (mMessages.get(i).getMessageId() == messageId)
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
