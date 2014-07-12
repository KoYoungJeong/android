package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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

    public void retrieveCdpItems(CdpItemManager cdpItemManager) {
        if (mCdpItems == null) {
            return;
        }
        mCdpItems = cdpItemManager.retrieve();
        notifyDataSetChanged();
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

    public List<CdpItem> getmCdpItems() {
        return mCdpItems;
    }
}
