package com.tosslab.jandi.app.ui.search.filter.member.presenter;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public interface BaseMemberSearchPresenter {

    void initMemberSearchQueue();

    void onInitializeWholeMembers();

    void stopMemberSearchQueue();

    void onSearchMember(String query);

}
