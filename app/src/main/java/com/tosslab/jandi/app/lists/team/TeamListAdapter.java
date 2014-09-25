package com.tosslab.jandi.app.lists.team;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMyTeam;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
public class TeamListAdapter extends BaseAdapter {
    private List<ResMyTeam.Team> mMyTeams;
    private LayoutInflater layoutInflater;
    private Context context;

    public TeamListAdapter(Context context, List<ResMyTeam.Team> myTeams) {
        this.mMyTeams = myTeams;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mMyTeams.size();
    }

    @Override
    public ResMyTeam.Team getItem(int i) {
        return mMyTeams.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_team_list, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.txt_team_list_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ResMyTeam.Team myTeam = getItem(i);
        if (myTeam != null) {
            holder.textView.setText(myTeam.name);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
    }
}
