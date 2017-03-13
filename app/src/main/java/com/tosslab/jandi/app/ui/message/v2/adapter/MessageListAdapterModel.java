package com.tosslab.jandi.app.ui.message.v2.adapter;

import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

public interface MessageListAdapterModel {
    void addAll(int position, List<ResMessages.Link> links);

    void remove(int position);

    ResMessages.Link getItem(int position);

    void setOldLoadingComplete();

    void setOldNoMoreLoading();

    int indexByMessageId(long messageId);

    int indexOfDummyLinkId(long linkId);

    int getLastIndexByMessageId(long messageId);

    int indexOfLinkId(long linkId);

    List<Integer> indexByFeedbackId(long messageId);

    int getDummyMessagePositionByLocalId(long localId);
    
    int getCount();

    int getDummyMessageCount();

    void removeAllDummy();

    void add(ResMessages.Link dummyMessage);

    void updateCachedType(int indexOfUnsharedFile);

    void add(int position, ResMessages.Link dummyMessage);

    void modifyStarredStateByPosition(int index, boolean starred);

    List<Integer> getIndexListByPollId(long pollId);

}
