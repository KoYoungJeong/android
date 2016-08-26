package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 22..
 */
public class TeamViewHolder extends BaseViewHolder<Team> {

    @Bind(R.id.tv_team_name)
    TextView tvName;

    @Bind(R.id.tv_team_unread_badge)
    TextView tvBadge;

    private TeamViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static TeamViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(itemView);
    }

    @Override
    public void onBindView(Team team) {
        itemView.setSelected(team.isSelected());

        tvName.setTypeface(team.isSelected() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tvName.setText(team.getName());

        tvBadge.setVisibility(team.getUnread() > 0 ? View.VISIBLE : View.GONE);
        tvBadge.setText("" + team.getUnread());
    }
}
