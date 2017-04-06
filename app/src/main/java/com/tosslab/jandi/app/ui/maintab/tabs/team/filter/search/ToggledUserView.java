package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search;

public interface ToggledUserView {
    void toggle(int count, int totalCnt);

    void addToggledUser(long[] users);
}
