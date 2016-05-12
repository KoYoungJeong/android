package com.tosslab.jandi.app.ui.message.v2.adapter;

import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

public interface MessageListAdapterModel {
    void addAll(int position, List<ResMessages.Link> links);

    void remove(int position);

    ResMessages.Link getItem(int position);

    int indexByMessageId(long messageId);

    int indexOfDummyMessageId(long messageId);

    int getLastIndexByMessageId(long messageId);

    int indexOfLinkId(long linkId);

    List<Integer> indexByFeedbackId(long messageId);

    int getDummyMessagePositionByLocalId(long localId);

    int getCount();

    int getDummyMessageCount();

    void removeAllDummy();

    void add(ResMessages.Link dummyMessage);
}
