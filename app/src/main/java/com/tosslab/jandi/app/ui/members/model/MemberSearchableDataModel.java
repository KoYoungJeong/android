package com.tosslab.jandi.app.ui.members.model;

import com.tosslab.jandi.app.team.member.User;

import java.util.List;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public interface MemberSearchableDataModel {

    List<User> getInitializedMembers();

    void setInitializedMembers(List<User> members);

    void addAll(List<User> members);

    void setEmptySearchedMember(String query);

    void clear();
}
