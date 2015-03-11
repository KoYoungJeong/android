package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class EntitySelectDialogAdatper extends BaseAdapter {

    private final Context context;
    private List<SimpleEntityInfo> entityInfoList;

    public EntitySelectDialogAdatper(Context context) {
        this.context = context;
        entityInfoList = new ArrayList<SimpleEntityInfo>();
    }

    @Override
    public int getCount() {
        return entityInfoList.size();
    }

    @Override
    public SimpleEntityInfo getItem(int position) {
        return entityInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void add(SimpleEntityInfo simpleEntityInfo) {
        entityInfoList.add(simpleEntityInfo);
    }

    public static class SimpleEntityInfo {
        private final int type;
        private final String name;
        private final int id;
        private final String photo;

        public SimpleEntityInfo(int type, String name, int id, String photo) {
            this.type = type;
            this.name = name;
            this.id = id;
            this.photo = photo;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getPhoto() {
            return photo;
        }
    }
}
