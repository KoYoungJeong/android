package com.tosslab.jandi.app.ui.members.search.presenter;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public interface MemberSearchPresenter {

    void initMemberSearchQueue();

    void onInitializeWholeMembers();

    void stopMemberSearchQueue();

    void onSearchMember(String query);

}
