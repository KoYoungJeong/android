package com.tosslab.jandi.app.ui.search.filter.member.adapter.model;

import com.tosslab.jandi.app.team.member.User;

import java.util.List;

/**
 * Created by tonyjs on 2016. 7. 26..
 */
public interface MemberFilterableDataModel {
    List<User> getInitializedMembers();

    void setInitializedMembers(List<User> members);

    void addAll(List<User> members, long myId);

    void clear();

    void setSelectedMemberId(long selectedMemberId);
}
