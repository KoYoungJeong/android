package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.model;

import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

import java.util.List;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
public interface StarredListDataModel {

    void clear();

    List<MultiItemRecyclerAdapter.Row<?>> getStarredListRows(List<StarredMessage> records);

    void addRows(List<MultiItemRecyclerAdapter.Row<?>> rows);

    void removeByMessageId(long messageId);

    StarredMessage findMessageById(long messageId);

    List<MultiItemRecyclerAdapter.Row<?>> getStarredMessageRow(StarredMessage message);

    void addRows(int position, List<MultiItemRecyclerAdapter.Row<?>> rows);

    boolean isEmpty();

}
