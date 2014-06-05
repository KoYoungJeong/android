package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.toss.app.network.entities.TossRestInfosForSideMenu;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EBean
public class CdpItemListAdapter extends BaseAdapter {
    List<CdpItem> mCdpItems;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mCdpItems = new ArrayList<CdpItem>();
    }

    public void clearAdapter() {
        mCdpItems.clear();
        notifyDataSetChanged();
    }

    public void retrieveCdpItemsFromChannels(List<TossRestInfosForSideMenu.Channel> channels) {
        if (mCdpItems == null) {
            return;
        }
        for (TossRestInfosForSideMenu.Channel channel : channels) {
            mCdpItems.add(new CdpItem("# " + channel.name, CdpItem.TYPE_CHANNEL, channel.id));
        }
    }

    public void retrieveCdpItemsFromMembers(List<TossRestInfosForSideMenu.Member> members) {
        if (mCdpItems == null) {
            return;
        }

        for (TossRestInfosForSideMenu.Member member : members) {
            mCdpItems.add(new CdpItem("@" + member.nickname, member.id));
        }
    }

    public void retrieveCdpItemsFromPravateGroups(List<TossRestInfosForSideMenu.PrivateGroup> privateGroups) {
        if (mCdpItems == null) {
            return;
        }

        for (TossRestInfosForSideMenu.PrivateGroup group : privateGroups) {
            mCdpItems.add(new CdpItem(group.name, CdpItem.TYPE_PRIVATE_GROUP, group.id));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CdpItemView cdpItemView;
        if (convertView == null) {
            cdpItemView = CdpItemView_.build(mContext);
        } else {
            cdpItemView = (CdpItemView) convertView;
        }

        cdpItemView.bind(getItem(position));

        return cdpItemView;
    }

    @Override
    public int getCount() {
        return mCdpItems.size();
    }

    @Override
    public CdpItem getItem(int position) {
        return mCdpItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
