package com.tosslab.jandi.app.ui.maintab.team.presenter;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public interface TeamPresenter {

    void initSearchQueue();

    void stopSearchQueue();

    void onInitializeTeam();

    void reInitializeTeam();

    void onSearchMember(String query);
}