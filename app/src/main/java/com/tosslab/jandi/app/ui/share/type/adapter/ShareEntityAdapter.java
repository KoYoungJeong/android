package com.tosslab.jandi.app.ui.share.type.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;

import java.util.ArrayList;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
public class ShareEntityAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<EntityInfo> entityInfos;

    public ShareEntityAdapter(Context context) {
        this.context = context;
        entityInfos = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return entityInfos.size();
    }

    @Override
    public EntityInfo getItem(int position) {
        return entityInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(EntityInfo entityInfo) {
        entityInfos.add(entityInfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_share_entity, parent, false);
            holder = new ViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.img_share_entity_icon);
            holder.textView = (TextView) convertView.findViewById(R.id.txt_share_entity_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EntityInfo item = getItem(position);

        holder.textView.setText(item.getName());

        if (item.isPublicTopic()) {
            holder.imageView.setImageResource(R.drawable.jandi_icon_topic);
        } else if (item.isPrivateTopic()) {
            holder.imageView.setImageResource(R.drawable.jandi_private_topic_icon);
        } else {

            holder.imageView.setImageResource(R.drawable.jandi_profile);

            Ion.with(holder.imageView)
                    .crossfade(true)
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .fitCenter()
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + item.getProfileImage());
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
