package com.tosslab.jandi.app.ui.maintab.team.presenter;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public interface TeamPresenter {

    void initSearchQueue();

    void stopSearchQueue();

    void onInitialize();

    void onSearchMember(String query);
}
