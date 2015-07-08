package com.tosslab.jandi.app.ui.members.adapter;

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
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class MembersAdapter extends BaseAdapter {

    private Context context;

    private List<ChatChooseItem> chatChooseItems;

    public MembersAdapter(Context context) {
        this.context = context;
        chatChooseItems = new ArrayList<ChatChooseItem>();
    }

    @Override
    public int getCount() {
        return chatChooseItems.size();
    }

    @Override
    public ChatChooseItem getItem(int position) {
        return chatChooseItems.get(position);
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
            viewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_user_count);
            viewHolder.textViewBadgeCount = (TextView) convertView.findViewById(R.id.txt_entity_listitem_badge);
            viewHolder.disableLineThrouthView = convertView.findViewById(R.id.img_entity_listitem_line_through);
            viewHolder.disableWarningView = convertView.findViewById(R.id.img_entity_listitem_warning);
            viewHolder.disableCoverView = convertView.findViewById(R.id.view_entity_listitem_warning);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textViewBadgeCount.setVisibility(View.GONE);

        ChatChooseItem item = getItem(position);

        viewHolder.textViewName.setText(item.getName());
        viewHolder.textViewAdditional.setText(item.getEmail());

        if (item.isStarred()) {
            viewHolder.imageViewFavorite.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageViewFavorite.setVisibility(View.GONE);
        }


        viewHolder.imageViewIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));
        Ion.with(viewHolder.imageViewIcon)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .load(item.getPhotoUrl());

        if (item.isEnabled()) {

            viewHolder.disableLineThrouthView.setVisibility(View.GONE);
            viewHolder.disableWarningView.setVisibility(View.GONE);
            viewHolder.disableCoverView.setVisibility(View.GONE);

        } else {

            viewHolder.disableLineThrouthView.setVisibility(View.VISIBLE);
            viewHolder.disableWarningView.setVisibility(View.VISIBLE);
            viewHolder.disableCoverView.setVisibility(View.VISIBLE);
        }


        return convertView;
    }

    private View.OnClickListener getProfileClickListener(int entityId) {
        return v -> {
            EventBus.getDefault().post(new ProfileDetailEvent(entityId));
        };
    }

    public void addAll(List<ChatChooseItem> chatListWithoutMe) {
        chatChooseItems.addAll(chatListWithoutMe);
    }

    public void clear() {
        chatChooseItems.clear();
    }

    static class ViewHolder {
        public Context context;
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public TextView textViewBadgeCount;
        public View disableLineThrouthView;
        public View disableWarningView;
        public View disableCoverView;
    }

}
