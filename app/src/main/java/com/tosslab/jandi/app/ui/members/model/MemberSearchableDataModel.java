package com.tosslab.jandi.app.ui.members.model;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.List;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public interface MemberSearchableDataModel {

    List<FormattedEntity> getInitializedMembers();

    void setInitializedMembers(List<FormattedEntity> members);

    void addAll(List<FormattedEntity> members);

    void setEmptySearchedMember(String query);

    void clear();
}
