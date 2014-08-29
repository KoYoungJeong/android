package com.tosslab.jandi.app.lists.entities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.lists.FormattedEntity;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EBean
public class EntityItemListAdapter extends BaseAdapter {
    private List<FormattedEntity> mEntities;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mEntities = new ArrayList<FormattedEntity>();
    }

    public void retrieveList(List<FormattedEntity> formattedEntities) {
        mEntities.clear();
        mEntities.addAll(formattedEntities);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEntities.size();
    }

    @Override
    public FormattedEntity getItem(int position) {
        return mEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EntityItemView entityItemView;
        if (convertView == null) {
            entityItemView = EntityItemView_.build(mContext);
        } else {
            entityItemView = (EntityItemView) convertView;
        }
        entityItemView.bind(getItem(position));
        return entityItemView;
    }
}
