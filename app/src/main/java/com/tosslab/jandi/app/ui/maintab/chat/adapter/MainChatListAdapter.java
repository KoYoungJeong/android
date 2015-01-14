package com.tosslab.jandi.app.ui.maintab.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class MainChatListAdapter extends BaseAdapter {

    private Context context;

    private List<ChatItem> entities;

    public MainChatListAdapter(Context context) {
        this.context = context;
        entities = new ArrayList<ChatItem>();
    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public ChatItem getItem(int position) {
        return entities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
            viewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);
            viewHolder.viewMaskUnjoined = convertView.findViewById(R.id.view_entity_unjoined);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatItem item = getItem(position);

        viewHolder.textViewName.setText(item.getName());
        if (item.isStarred()) {
            viewHolder.imageViewFavorite.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageViewFavorite.setVisibility(View.GONE);
        }

        viewHolder.textViewBadgeCount.setText(String.valueOf(item.getUnread()));

        if (item.getUnread() <= 0) {
            viewHolder.textViewBadgeCount.setVisibility(View.GONE);
        } else {
            viewHolder.textViewBadgeCount.setVisibility(View.VISIBLE);
        }

        viewHolder.textViewAdditional.setText(item.getLastMessage());

        viewHolder.viewMaskUnjoined.setVisibility(View.GONE);

        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(context))
                .into(viewHolder.imageViewIcon);


        return convertView;
    }

    public void setChatItem(List<ChatItem> chatItems) {
        entities.clear();
        entities.addAll(chatItems);
    }

    static class ViewHolder {
        public Context context;
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
        public View viewMaskUnjoined;

    }
}