package com.tosslab.jandi.app.lists.team;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
public class TeamListAdapter extends ArrayAdapter<Team> {

    public TeamListAdapter(Context context) {
        super(context, R.layout.item_team_list, new ArrayList<Team>());
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Team team = getItem(position);
        Context context = getContext();
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

                iconView.setImageResource(R.drawable.jandi_team_selector_icon);
                nameView.setText(team.getName());
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

        int realCount = getCount();

        boolean hasJoinTeam = false;
        boolean hasPendingTeam = false;
        // Create Team Type
        int viewTypeCount = 1;

        for (int idx = 0; idx < realCount; ++idx) {
            Team.Status status = getItem(idx).getStatus();

            switch (status) {

                case JOINED:
                    if (!hasJoinTeam) {
                        hasJoinTeam = true;
                        ++viewTypeCount;
                    }
                    break;
                case PENDING:
                    if (!hasPendingTeam) {
                        hasPendingTeam = true;
                        ++viewTypeCount;
                    }
                    break;
                case CREATE:
                    break;
            }

            if (hasJoinTeam && hasPendingTeam) {
                break;
            }
        }

        return viewTypeCount;
    }

    @Override
    public int getItemViewType(int position) {

        return getItem(position).getStatus().ordinal();
    }
}
