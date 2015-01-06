package com.tosslab.jandi.app.ui.maintab.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class MainChatListAdapter extends BaseAdapter {

    private Context context;

    private List<FormattedEntity> entities;

    public MainChatListAdapter(Context context) {
        this.context = context;
        entities = new ArrayList<FormattedEntity>();
    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public FormattedEntity getItem(int position) {
        return entities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
