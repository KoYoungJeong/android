package com.tosslab.jandi.app.ui.maintab.navigation.adapter.model;

import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuItem;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.List;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public interface NavigationDataModel {

    List<MultiItemRecyclerAdapter.Row<?>> getNavigationRows(MenuBuilder menuBuilder);

    List<MultiItemRecyclerAdapter.Row<?>> getTeamRows(List<Team> teams);

    void addTeamRows(List<MultiItemRecyclerAdapter.Row<?>> teamRows);

    void addRows(List<MultiItemRecyclerAdapter.Row<?>> rows);

    void removePendingTeam(Team team);

    void removeAllTeamRows();

    Team getTeamById(long teamId);

    List<Team> getTeams();

    MultiItemRecyclerAdapter.Row<String> getVersionRow(String versionName);

    MenuItem getNotificationItem();

}
