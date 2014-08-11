package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EBean
public class PrivateGroupEntityItemListAdapter extends BaseAdapter {
    private List<ResLeftSideMenu.PrivateGroup> mPrivateGroups;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mPrivateGroups = new ArrayList<ResLeftSideMenu.PrivateGroup>();
    }

    public void retrieveList(List<ResLeftSideMenu.PrivateGroup> joinedChannel) {
        mPrivateGroups.clear();
        mPrivateGroups.addAll(joinedChannel);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPrivateGroups.size();
    }

    @Override
    public ResLeftSideMenu.PrivateGroup getItem(int position) {
        return mPrivateGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PrivateGroupEntityItemView privateGroupEntityItemView;
        if (convertView == null) {
            privateGroupEntityItemView = PrivateGroupEntityItemView_.build(mContext);
        } else {
            privateGroupEntityItemView = (PrivateGroupEntityItemView) convertView;
        }
        privateGroupEntityItemView.bind(getItem(position));
        return privateGroupEntityItemView;
    }

}
