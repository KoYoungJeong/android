package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.model;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

import java.util.List;

/**
 * Created by tonyjs on 2016. 9. 21..
 */
public interface MentionListDataModel {

    void addAll(List<MentionMessage> mentionMessageList);

    @Nullable
    MentionMessage getItem(int position);

    void setLastReadMessageId(long messageId);

    void clear();

    void remove(int index);

    int indexOfLink(long linkId);

    void addAll(int position, List<MentionMessage> mentionMessages);

    void add(int position, MentionMessage mentionMessage);

    int getItemCount();
}
