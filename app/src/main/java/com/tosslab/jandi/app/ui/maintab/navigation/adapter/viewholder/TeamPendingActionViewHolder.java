package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 3. 22..
 */
public class TeamPendingActionViewHolder extends BaseViewHolder<Team> {

    @Bind(R.id.btn_team_pending_invite_ignore)
    View btnTeamInviteIgnore;

    @Bind(R.id.btn_team_pending_invite_accept)
    View btnTeamInviteAccept;

    private TeamPendingActionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static TeamPendingActionViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_pending_action, parent, false);
        return new TeamPendingActionViewHolder(itemView);
    }

    @Override
    public void onBindView(final Team team) {
        btnTeamInviteIgnore.setOnClickListener(v ->
                EventBus.getDefault().post(TeamInviteIgnoreEvent.create(team)));

        btnTeamInviteAccept.setOnClickListener(v ->
                EventBus.getDefault().post(TeamInviteAcceptEvent.create(team)));
    }


}
