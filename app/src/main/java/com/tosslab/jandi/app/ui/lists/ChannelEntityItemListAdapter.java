package com.tosslab.jandi.app.ui.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.models.FormattedChannel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EBean
public class ChannelEntityItemListAdapter extends BaseAdapter {
    private List<FormattedChannel> mChannels;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mChannels = new ArrayList<FormattedChannel>();
    }

    public void retrieveList(List<FormattedChannel> formattedChannels) {
        mChannels.clear();
        mChannels.addAll(formattedChannels);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mChannels.size();
    }

    @Override
    public FormattedChannel getItem(int position) {
        return mChannels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChannelEntityItemView channelEntityItemView;
        if (convertView == null) {
            channelEntityItemView = ChannelEntityItemView_.build(mContext);
        } else {
            channelEntityItemView = (ChannelEntityItemView) convertView;
        }
        channelEntityItemView.bind(getItem(position));
        return channelEntityItemView;
    }
}
