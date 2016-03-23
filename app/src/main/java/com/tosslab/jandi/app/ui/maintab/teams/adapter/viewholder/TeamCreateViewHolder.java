package com.tosslab.jandi.app.ui.maintab.teams.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 3. 22..
 */
public class TeamCreateViewHolder extends BaseViewHolder<Object> {

    private TeamCreateViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static TeamCreateViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_create, parent, false);
        return new TeamCreateViewHolder(itemView);
    }

    @Override
    public void onBindView(Object o) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
