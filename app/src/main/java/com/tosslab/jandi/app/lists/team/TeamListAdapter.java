package com.tosslab.jandi.app.lists.team;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
public class TeamListAdapter extends BaseAdapter {

    private final ArrayList<Team> teams;
    private Context context;

    public TeamListAdapter(Context context) {
        this.context = context;
        teams = new ArrayList<Team>();
    }

    @Override
    public int getCount() {
        return teams != null ? teams.size() : 0;
    }

    @Override
    public Team getItem(int position) {
        return teams.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Team team = getItem(position);
        getItemViewType(position);
        if (convertView == null) {
            switch (team.getStatus()) {
                case JOINED:
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_team_list, viewGroup, false);
                    break;
                case PENDING:
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_team_list_pending, viewGroup, false);
                    break;
                case CREATE:
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_team_list, viewGroup, false);
                    break;
            }
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.txt_team_list_name);
        ImageView iconView = (ImageView) convertView.findViewById(R.id.img_team_list_icon);

        Button ignoreBtn;
        Button acceptBtn;

        switch (team.getStatus()) {
            case JOINED:
                iconView.setImageResource(R.drawable.jandi_team_selector_icon);
                nameView.setText(team.getName());
                break;
            case PENDING:
                acceptBtn = (Button) convertView.findViewById(R.id.btn_team_list_accept);
                ignoreBtn = (Button) convertView.findViewById(R.id.btn_team_list_ignore);
                acceptBtn.setTag(team);
                ignoreBtn.setTag(team);
                iconView.setImageResource(R.drawable.jandi_team_selector_icon);
                nameView.setText(team.getName());
                ignoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Team clickedTeam = (Team) v.getTag();
                        Log.d("", clickedTeam.toString());
                        EventBus.getDefault().post(TeamInviteIgnoreEvent.create(clickedTeam));
                    }
                });

                acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Team clickedTeam = (Team) v.getTag();
                        Log.d("", clickedTeam.toString());
                        EventBus.getDefault().post(TeamInviteAcceptEvent.create(clickedTeam));

                    }
                });

                break;
            case CREATE:
                iconView.setImageResource(R.drawable.jandi_icon_teamlist_add);
                nameView.setText(R.string.jandi_team_select_create_a_team);
                break;
        }

        iconView.setSelected(team.isSelected());
        nameView.setSelected(team.isSelected());

        return convertView;
    }

    @Override
    public int getViewTypeCount() {

        return Team.Status.values().length;
    }

    @Override
    public int getItemViewType(int position) {

        return getItem(position).getStatus().ordinal();
    }

    public void add(Team userTeam) {
        teams.add(userTeam);
    }
}
