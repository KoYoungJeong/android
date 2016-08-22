package com.tosslab.jandi.app.ui.maintab.team.filter.search;


public interface OnToggledUser {
    void onAddToggledUser(long[] users);

    void onAddAllUser();

    void onUnselectAll();

    void onInvite();
}
