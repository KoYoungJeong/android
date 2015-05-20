package com.tosslab.jandi.app.lists.messages;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by justinygchoi on 2014. 8. 8..
 */
@Deprecated
public class MessageItemConverter {

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    private CopyOnWriteArrayList<MessageItem> mOriginalMessageList;

    public MessageItemConverter() {
        mOriginalMessageList = new CopyOnWriteArrayList<MessageItem>();
    }

    public void clear() {
        mOriginalMessageList.clear();
    }

    /**
     * Get Message 혹은, Update Message에서 들어오는 message를 link id로 정렬
     *
     * @param links
     * @param isDescendingOrder 내림 차순으로 정렬 여부
     * @return
     */
    private List<ResMessages.Link> sortByLinkId(List<ResMessages.Link> links, final boolean isDescendingOrder) {
        List<ResMessages.Link> ret = new ArrayList<ResMessages.Link>(links);

        Comparator<ResMessages.Link> sort = new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link link, ResMessages.Link link2) {

                if (link == null) {
                    return (isDescendingOrder) ? -1 : 1;
                } else if (link2 == null) {
                    return (isDescendingOrder) ? 1 : -1;
                }

                if (link.id > link2.id)
                    return (isDescendingOrder) ? -1 : 1;
                else if (link.id == link2.id)
                    return 0;
                else
                    return (isDescendingOrder) ? 1 : -1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    public void insertMessageItem(ResMessages messages) {
        patchMessageItem(messages.records, true);
    }

    public void insertMessageItem(List<ResMessages.Link> messages) {
        patchMessageItem(messages, true);
    }

    public void updatedMessageItem(ResUpdateMessages messages) {
        patchMessageItem(messages.updateInfo.messages, false);
    }

    /**
     * Message Item을 정렬하여 각 상태에 따라 삭제하고 추가하는 작업.
     *
     * @param links
     * @param isDescendingOrder
     */
    synchronized public void patchMessageItem(List<ResMessages.Link> links, boolean isDescendingOrder) {
        if (links.size() <= 0) {
            return;
        }

        List<ResMessages.Link> sortedLinks = sortByLinkId(links, isDescendingOrder);

        // 업데이트 된 메시지들의 상태를 보고,
        // 새로 추가하던가, 기존 리스트 item 에 동일한 항목을 대체, 혹은 삭제한다.
        synchronized (mOriginalMessageList) {
            for (ResMessages.Link link : sortedLinks) {
                LogUtil.d("patchMessageItem, LinkId:" + link.id + " / status:" + link.status);
                if (link.status.equals("created") || link.status.equals("shared")) {
                    if (isDescendingOrder)
                        mOriginalMessageList.add(0, new MessageItem(link));
                    else
                        mOriginalMessageList.add(new MessageItem(link));
                } else if (link.status.equals("edited")) {
                    int position = searchIndexOfMessages(mOriginalMessageList, link.messageId);
                    if (position >= 0) {
                        mOriginalMessageList.set(position, new MessageItem(link));
                    }
                } else if (link.status.equals("archived")) {
                    int position = searchIndexOfMessages(mOriginalMessageList, link.messageId);
                    if (position >= 0) {
                        mOriginalMessageList.remove(position);
                    }
                } else if (link.status.equals("unshared")) {
                    int position = searchIndexOfMessages(mOriginalMessageList, link.messageId);
                    if (position >= 0) {
                        mOriginalMessageList.set(position, new MessageItem(link));
                    } else {
                        if (isDescendingOrder)
                            mOriginalMessageList.add(0, new MessageItem(link));
                        else
                            mOriginalMessageList.add(new MessageItem(link));
                    }
                }
            }
        }
    }

    /**
     * 출력을 위한 그룹핑
     * 그룹핑의 조건은 날짜별, 코멘트의 대상이 같은 경우, 동일한 사용자의 경우 등이 있다.
     * 1. 날짜별로 경계선으로 구분한다.
     * 2. 현재 코멘트 타입인데 바로 상위 아이템이 feedback 아이템이면 그 아래에 종속된다.
     * 3. 현재 코멘트 타입인데 바로 상위 아이템 또한 동일 feedback의 코멘트이면 그 아래 종속된다.
     * -- 2, 3의 경우 상위 아이템과 내가 같은 작성자이면 이름을 표시하지 않는다.
     */
    public List<MessageItem> reformatMessages() {
        List<MessageItem> formattedMessages = new ArrayList<MessageItem>();

        String currentDay = "";
        MessageItem formerMessageItem = null;

        // 동기화 유틸로 Fail-fast Iterator 멀티 쓰레드 무결성 해결
        synchronized (mOriginalMessageList) {
            for (MessageItem item : mOriginalMessageList) {
                String strDay = DATE_FORMATTER.format(item.getLinkTime());
                String strToday = DATE_FORMATTER.format(new Date());
                if (!currentDay.equals(strDay)) {
                    // 바로 이전의 message 날짜와 같지 않으면 날짜 경계선을 먼저 추가한다.
                    currentDay = strDay;
                    try {
                        if (currentDay.equals(strToday))
                            formattedMessages.add(new MessageItem(DATE_FORMATTER.parse(strDay), true));
                        else
                            formattedMessages.add(new MessageItem(DATE_FORMATTER.parse(strDay), false));
                        formerMessageItem = null;   // 날짜 경계선이 바뀌면 다른 그룹핑은 초기화된다.
                    } catch (ParseException e) {
                        LogUtil.e("Date Parse Error", e);
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
                formattedMessages.add(item);
                formerMessageItem = item;
            }
        }

        return formattedMessages;
    }

    // 현재 화면에 뿌려진 메시지들 중에 messageId와 동일한 놈의 index 반환
    private int searchIndexOfMessages(List<MessageItem> messageItems, int messageId) {
        for (int i = 0; i < messageItems.size(); i++) {
            if (messageItems.get(i).getMessageId() == messageId)
                return i;
        }
        return -1;
    }
}
