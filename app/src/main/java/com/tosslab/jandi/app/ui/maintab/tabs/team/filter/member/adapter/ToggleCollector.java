package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter;


import java.util.List;

public interface ToggleCollector {
    boolean containsId(long id);
    void addId(long id);
    void addAllIds(List<Long> ids);
    void removeId(long id);
    void clearIds();
    int count();

    List<Long> getIds();
}
