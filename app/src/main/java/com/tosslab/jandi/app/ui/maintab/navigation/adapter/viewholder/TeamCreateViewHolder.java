package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

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

    private final OnRequestTeamCreateListener onRequestTeamCreateListener;

    private TeamCreateViewHolder(View itemView, OnRequestTeamCreateListener onRequestTeamCreateListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onRequestTeamCreateListener = onRequestTeamCreateListener;
    }

    public static TeamCreateViewHolder newInstance(ViewGroup parent,
                                                   OnRequestTeamCreateListener onRequestTeamCreateListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_team_create, parent, false);
        return new TeamCreateViewHolder(itemView, onRequestTeamCreateListener);
    }

    @Override
    public void onBindView(Object o) {
        if (onRequestTeamCreateListener != null) {
            itemView.setOnClickListener(v -> onRequestTeamCreateListener.onRequestTeamCreate());
        }
    }

    public interface OnRequestTeamCreateListener {
        void onRequestTeamCreate();
    }
}
