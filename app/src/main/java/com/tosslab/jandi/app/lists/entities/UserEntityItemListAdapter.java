package com.tosslab.jandi.app.lists.entities;

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
public class UserEntityItemListAdapter extends BaseAdapter {
    private List<ResLeftSideMenu.User> mUsers;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mUsers = new ArrayList<ResLeftSideMenu.User>();
    }

    public void retrieveList(List<ResLeftSideMenu.User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public ResLeftSideMenu.User getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserEntityItemView userEntityItemView;
        if (convertView == null) {
            userEntityItemView = UserEntityItemView_.build(mContext);
        } else {
            userEntityItemView = (UserEntityItemView) convertView;
        }
        userEntityItemView.bind(getItem(position));
        return userEntityItemView;
    }
}
