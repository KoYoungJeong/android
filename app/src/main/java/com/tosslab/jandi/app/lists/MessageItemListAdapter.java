package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.JandiConstants;
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
import java.util.Date;
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
//    private String mCurrentDay = "";

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
     * @param links
     * @param isDescendingOrder 내림 차순으로 정렬 여부
     * @return
     */
    private List<ResMessages.Link> sortByLinkId(List<ResMessages.Link> links, final boolean isDescendingOrder) {
        List<ResMessages.Link> ret = new ArrayList<ResMessages.Link>(links);

        Comparator<ResMessages.Link> sort = new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link link, ResMessages.Link link2) {
                if (link.id > link2.id)
                    return (isDescendingOrder)?-1:1;
                else if (link.id == link2.id)
                    return 0;
                else
                    return (isDescendingOrder)?1:-1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    public int getLastLinkId() {
        if (mMessages.size() > 0) {
            return mMessages.get(mMessages.size()-1).getLinkId();
        }
        return -1;
    }

    public void insertMessageItem(ResMessages messages) {
        patchMessageItem(messages, true);
    }

    public void updatedMessageItem(ResMessages messages) {
        patchMessageItem(messages, false);
    }

    /**
     * Message Item을 정렬하여 각 상태에 따라 삭제하고 추가하는 작업.
     * @param messages
     * @param isDescendingOrder
     */
    public void patchMessageItem(ResMessages messages, boolean isDescendingOrder) {
        if (mMessages == null || messages.messageCount <= 0) {
            return;
        }

        List<ResMessages.Link> sortedLinks = sortByLinkId(messages.messages, isDescendingOrder);

        // 업데이트 된 메시지들의 상태를 보고,
        // 새로 추가하던가, 기존 리스트 item 에 동일한 항목을 대체, 혹은 삭제한다.
        for (ResMessages.Link link : sortedLinks) {
            log.debug("patchMessageItem, LinkId:" + link.id + " / status:"+ link.status);
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
            } else if (link.status.equals("unshared")) {
                int position = searchIndexOfMessages(link.messageId);
                if (position >= 0) {
                    mMessages.set(position, new MessageItem(link));
                } else {
                    if (isDescendingOrder)
                        mMessages.add(0, new MessageItem(link));
                    else
                        mMessages.add(new MessageItem(link));
                }
            }
        }
        reformatMessages();
    }

    /**
     * 출력을 위한 그룹핑
     * 그룹핑의 조건은 날짜별, 코멘트의 대상이 같은 경우, 동일한 사용자의 경우 등이 있다.
     * 1. 날짜별로 경계선으로 구분한다.
     * 2. 현재 코멘트 타입인데 바로 상위 아이템이 feedback 아이템이면 그 아래에 종속된다.
     * 3. 현재 코멘트 타입인데 바로 상위 아이템 또한 동일 feedback의 코멘트이면 그 아래 종속된다.
     * -- 2, 3의 경우 상위 아이템과 내가 같은 작성자이면 이름을 표시하지 않는다.
     */
    private void reformatMessages() {
        mFormattedMessages.clear();
        String currentDay = "";
        MessageItem formerMessageItem = null;

        for (MessageItem item : mMessages) {
            String strDay = DATE_FORMATTER.format(item.getLinkTime());
            String strToday = DATE_FORMATTER.format(new Date());
            if (!currentDay.equals(strDay)) {
                // 바로 이전의 message 날짜와 같지 않으면 날짜 경계선을 먼저 추가한다.
                currentDay = strDay;
                try {
                    if (currentDay.equals(strToday))
                        mFormattedMessages.add(new MessageItem(DATE_FORMATTER.parse(strDay), true));
                    else
                        mFormattedMessages.add(new MessageItem(DATE_FORMATTER.parse(strDay), false));
                    formerMessageItem = null;   // 날짜 경계선이 바뀌면 다른 그룹핑은 초기화된다.
                } catch (ParseException e) {
                    log.error("Date Parse Error", e);
                }

            }

            if (formerMessageItem != null && formerMessageItem.isDateDivider == false) {
                if (item.getContentType() == MessageItem.TYPE_COMMENT) {
                    if (formerMessageItem.getMessageId() == item.getFeedbackId()) {
                        // 바로 상위 아이템이 feedback 아이템이면 그 아래에 종속된다.
                        item.isNested = true;
                    } else if (formerMessageItem.getFeedbackId() == item.getFeedbackId()) {
                        // 바로 상위 아이템 또한 동일 feedback의 코멘트이면 그 아래 종속된다.
                        item.isNested = true;
                    }

                    if (formerMessageItem.getUserId() == item.getUserId()) {
                        // 상위 아이템과 내가 같은 작성자이면 이름을 표시하지 않는다.
                        item.isNestedOfMine = true;
                    }
                }
            }
            mFormattedMessages.add(item);
            formerMessageItem = item;
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
