package com.tosslab.jandi.app.ui.maintab.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            viewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            viewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
            viewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_additional);
            viewHolder.textViewBadgeCount = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatItem item = getItem(position);

        viewHolder.textViewName.setText(item.getName());
        if (item.isStarred()) {
            viewHolder.imageViewFavorite.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageViewFavorite.setVisibility(View.INVISIBLE);
        }

        viewHolder.textViewBadgeCount.setText(String.valueOf(item.getUnread()));

        if (item.getUnread() <= 0) {
            viewHolder.textViewBadgeCount.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.textViewBadgeCount.setVisibility(View.VISIBLE);
        }

        viewHolder.textViewAdditional.setText(item.getLastMessage());

        if (item.getStatus()) {

            viewHolder.imageViewIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));

            Ion.with(viewHolder.imageViewIcon)
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .load(item.getPhoto());
        } else {
            viewHolder.imageViewIcon.setImageResource(R.drawable.jandi_ic_launcher);
        }


        return convertView;
    }

    private View.OnClickListener getProfileClickListener(int entityId) {
        return v -> {

            EventBus.getDefault().post(new ProfileDetailEvent(entityId));
        };
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

    }
}