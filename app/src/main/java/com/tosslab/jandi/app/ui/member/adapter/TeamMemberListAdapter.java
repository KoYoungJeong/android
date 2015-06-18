package com.tosslab.jandi.app.ui.member.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.team.TeamMemberView;
import com.tosslab.jandi.app.lists.team.TeamMemberView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 22..
 */
@Deprecated
@EBean
public class TeamMemberListAdapter extends BaseAdapter {
    private List<FormattedEntity> mMembers;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mMembers = new ArrayList<FormattedEntity>();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void retrieveList(List<FormattedEntity> formattedEntities) {
        mMembers.clear();
        mMembers.addAll(formattedEntities);
        notifyDataSetChanged();
    }

    public void addMember(FormattedEntity entity) {
        mMembers.add(entity);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMembers.size();
    }

    @Override
    public FormattedEntity getItem(int position) {
        return mMembers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TeamMemberView teamMemberView;
        if (convertView == null) {
            teamMemberView = TeamMemberView_.build(mContext);
        } else {
            teamMemberView = (TeamMemberView) convertView;
        }
        teamMemberView.bind(getItem(position));
        return teamMemberView;
    }
}
