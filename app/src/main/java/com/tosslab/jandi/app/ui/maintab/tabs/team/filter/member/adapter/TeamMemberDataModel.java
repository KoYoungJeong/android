package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import java.util.List;

public interface TeamMemberDataModel {
    void add(TeamMemberItem user);

    void addAll(List<TeamMemberItem> users);

    TeamMemberItem getItem(int position);

    int getSize();

    void clear();

    int findItemOfEntityId(long userId);
}
