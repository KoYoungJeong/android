package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search;


public interface OnToggledUser {
    void onAddToggledUser(long[] users);

    void onAddAllUser();

    void onUnselectAll();

    void onInvite();
}
