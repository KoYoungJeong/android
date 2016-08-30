package com.tosslab.jandi.app.ui.maintab.navigation.adapter;

import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.view.NavigationDataView;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.NavigationViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.ProfileViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamCreateViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamPendingActionViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamPendingViewHolder;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamRow;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.decoration.DividerViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class NavigationAdapter extends MultiItemRecyclerAdapter
        implements NavigationDataModel, NavigationDataView {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_TEAM_CREATE = 1;
    private static final int VIEW_TYPE_TEAM = 2;
    private static final int VIEW_TYPE_TEAM_PENDING = 3;
    private static final int VIEW_TYPE_TEAM_PENDING_ACTION = 4;
    private static final int VIEW_TYPE_NAVIGATION = 5;
    private static final int VIEW_TYPE_DIVIDER = 6;
    private static final int VIEW_TYPE_VERSION = 7;

    private OnNavigationItemClickListener onNavigationItemClickListener;
    private OnTeamClickListener onTeamClickListener;
    private TeamCreateViewHolder.OnRequestTeamCreateListener onRequestTeamCreateListener;
    private LongSparseArray<Boolean> pendingActionOpenedIds;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PROFILE:
                return ProfileViewHolder.newInstance(parent);
            case VIEW_TYPE_TEAM_CREATE:
                return TeamCreateViewHolder.newInstance(parent, onRequestTeamCreateListener);
            case VIEW_TYPE_TEAM:
                return TeamViewHolder.newInstance(parent);
            case VIEW_TYPE_TEAM_PENDING:
                return TeamPendingViewHolder.newInstance(parent);
            case VIEW_TYPE_TEAM_PENDING_ACTION:
                return TeamPendingActionViewHolder.newInstance(parent);
            case VIEW_TYPE_NAVIGATION:
                return NavigationViewHolder.newInstance(parent);
            case VIEW_TYPE_DIVIDER:
                return DividerViewHolder.newInstance(parent);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        View itemView = holder.itemView;
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case VIEW_TYPE_NAVIGATION:
                if (onNavigationItemClickListener != null) {
                    itemView.setOnClickListener(v ->
                            onNavigationItemClickListener.onNavigationItemClick(getItem(position)));
                }
                break;
            case VIEW_TYPE_TEAM:
                if (onTeamClickListener != null) {
                    itemView.setOnClickListener(v ->
                            onTeamClickListener.onTeamClick(getItem(position)));
                }
                break;

            case VIEW_TYPE_TEAM_PENDING:
                itemView.setOnClickListener(v -> openOrClosePendingActionView(getItem(position)));
                break;
        }
    }

    @Override
    public List<Row<?>> getNavigationRows(MenuBuilder menuBuilder) {
        List<Row<?>> rows = new ArrayList<>();
        ArrayList<MenuItemImpl> enableItems =
                menuBuilder == null ? new ArrayList<>() : menuBuilder.getVisibleItems();
        if (enableItems.size() <= 0) {
            return rows;
        }

        int groupId = -1;
        for (int i = 0; i < enableItems.size(); i++) {
            MenuItemImpl menuItem = enableItems.get(i);
            if (groupId == -1) {
                groupId = menuItem.getGroupId();
            } else if (menuItem.getGroupId() != groupId) {
                int dividerHeight = (int) UiUtils.getPixelFromDp(0.5f);
                int dividerColor = JandiApplication.getContext()
                        .getResources().getColor(R.color.rgb_d9d9d9);
                DividerViewHolder.Info dividerInfo =
                        DividerViewHolder.Info.create(dividerHeight, dividerColor);
                rows.add(Row.create(dividerInfo, VIEW_TYPE_DIVIDER));

                groupId = menuItem.getGroupId();
            }

            rows.add(Row.create(menuItem, VIEW_TYPE_NAVIGATION));
        }

        return rows;
    }

    @Override
    public Row<User> getProfileRow(User user) {
        return Row.create(user, VIEW_TYPE_PROFILE);
    }

    @Override
    public List<Row<?>> getTeamRows(List<Team> teams) {
        List<Row<?>> rows = new ArrayList<>();
        if (teams == null || teams.isEmpty()) {
            return rows;
        }

        rows.add(TeamRow.create(new Object(), VIEW_TYPE_TEAM_CREATE));
        Observable.from(teams)
                .map(team -> team.getStatus() == Team.Status.PENDING
                        ? TeamRow.create(team, VIEW_TYPE_TEAM_PENDING)
                        : TeamRow.create(team, VIEW_TYPE_TEAM))
                .collect(() -> rows, (list, teamRow) -> {
                    list.add(teamRow);
                    if (pendingActionOpenedIds == null) {
                        return;
                    }

                    if (teamRow.getItemViewType() == VIEW_TYPE_TEAM_PENDING) {
                        Team team = teamRow.getItem();
                        boolean hasOpened = pendingActionOpenedIds.get(team.getTeamId(), false);
                        if (hasOpened) {
                            list.add(TeamRow.create(team, VIEW_TYPE_TEAM_PENDING_ACTION));
                        }
                    }
                })
                .doOnCompleted(() -> {
                    int dividerHeight = (int) UiUtils.getPixelFromDp(0.5f);
                    int dividerColor = JandiApplication.getContext()
                            .getResources().getColor(R.color.rgb_d9d9d9);
                    DividerViewHolder.Info dividerInfo =
                            DividerViewHolder.Info.create(dividerHeight, dividerColor);
                    rows.add(TeamRow.create(dividerInfo, VIEW_TYPE_DIVIDER));
                })
                .subscribe();
        return rows;
    }

    @Override
    public void addTeamRows(List<Row<?>> teamRows) {
        addRows(0, teamRows);
    }

    public void openOrClosePendingActionView(Team team) {
        TeamRow<Team> row = TeamRow.create(team, VIEW_TYPE_TEAM_PENDING_ACTION);
        int targetPosition = -1;
        boolean open = false;
        for (int i = getRows().size() - 1; i >= 0; i--) {
            if (getItem(i) instanceof Team
                    && ((Team) getItem(i)).getTeamId() == team.getTeamId()) {
                if (getItemViewType(i) == VIEW_TYPE_TEAM_PENDING_ACTION) {
                    open = false;
                    targetPosition = i;
                } else {
                    open = true;
                    targetPosition = i + 1;
                }
                break;
            }
        }

        if (targetPosition >= 0) {
            if (pendingActionOpenedIds == null) {
                pendingActionOpenedIds = new LongSparseArray<>();
            }
            if (open) {
                pendingActionOpenedIds.put(team.getTeamId(), true);
                addRow(targetPosition, row);
                notifyItemInserted(targetPosition);
            } else {
                pendingActionOpenedIds.put(team.getTeamId(), false);
                remove(targetPosition);
                notifyItemRemoved(targetPosition);
            }
        }
    }

    @Override
    public void removePendingTeam(Team team) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (!(getItem(i) instanceof Team)) {
                continue;
            }

            Team target = getItem(i);
            if (target.getTeamId() == team.getTeamId()) {
                remove(i);
                break;
            }
        }
    }

    @Override
    public void removeAllTeamRows() {
        for (int i = getRows().size() - 1; i >= 0; i--) {
            Row row = getRow(i);
            if (row instanceof TeamRow) {
                remove(row);
            }
        }
    }

    @Override
    public Team getTeamById(long teamId) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (!(getItem(i) instanceof Team)) {
                continue;
            }

            int itemViewType = getItemViewType(i);
            if (itemViewType != VIEW_TYPE_TEAM) {
                continue;
            }

            Team target = getItem(i);
            if (target.getTeamId() == teamId) {
                return target;
            }
        }

        return Team.createEmptyTeam();
    }

    @Override
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();

        for (int i = getItemCount() - 1; i >= 0; i--) {
            int itemViewType = getItemViewType(i);
            if ((itemViewType == VIEW_TYPE_TEAM || itemViewType == VIEW_TYPE_TEAM_PENDING)
                    && getItem(i) instanceof Team) {
                teams.add(getItem(i));
            }
        }

        return teams;
    }

    @Override
    public void setOnNavigationItemClickListener(OnNavigationItemClickListener onNavigationItemClickListener) {
        this.onNavigationItemClickListener = onNavigationItemClickListener;
    }

    public void setOnRequestTeamCreateListener(
            TeamCreateViewHolder.OnRequestTeamCreateListener onRequestTeamCreateListener) {
        this.onRequestTeamCreateListener = onRequestTeamCreateListener;
    }

    @Override
    public void setOnTeamClickListener(OnTeamClickListener onTeamClickListener) {
        this.onTeamClickListener = onTeamClickListener;
    }


}
