package com.tosslab.jandi.app.ui.members.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {

    private Context context;

    private List<ChatChooseItem> chatChooseItems;

    public MembersAdapter(Context context) {
        this.context = context;
        chatChooseItems = new ArrayList<ChatChooseItem>();
    }

    public int getCount() {
        return chatChooseItems.size();
    }

    public ChatChooseItem getItem(int position) {
        return chatChooseItems.get(position);
    }

    @Override
    public MembersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MembersViewHolder membersViewHolder;
        View convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body, parent, false);
        membersViewHolder = new MembersViewHolder(convertView);
        membersViewHolder.textViewName = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
        membersViewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
        membersViewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
        membersViewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_user_count);
        membersViewHolder.disableLineThrouthView = convertView.findViewById(R.id.img_entity_listitem_line_through);
        membersViewHolder.disableCoverView = convertView.findViewById(R.id.view_entity_listitem_warning);


        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(MembersViewHolder membersViewHolder, int position) {

        ChatChooseItem item = getItem(position);

        membersViewHolder.textViewName.setText(item.getName());

        if (!TextUtils.isEmpty(item.getEmail())) {
            membersViewHolder.textViewAdditional.setVisibility(View.VISIBLE);
        } else {
            membersViewHolder.textViewAdditional.setVisibility(View.GONE);
        }
        membersViewHolder.textViewAdditional.setText(item.getEmail());


        if (item.isStarred()) {
            membersViewHolder.imageViewFavorite.setVisibility(View.VISIBLE);
        } else {
            membersViewHolder.imageViewFavorite.setVisibility(View.GONE);
        }

        Ion.with(membersViewHolder.imageViewIcon)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .load(item.getPhotoUrl());

        if (item.isEnabled()) {

            membersViewHolder.disableLineThrouthView.setVisibility(View.GONE);
            membersViewHolder.disableCoverView.setVisibility(View.GONE);

        } else {

            membersViewHolder.disableLineThrouthView.setVisibility(View.VISIBLE);
            membersViewHolder.disableCoverView.setVisibility(View.VISIBLE);
        }

        membersViewHolder.itemView.setOnClickListener(v -> EventBus.getDefault().post(new
                ProfileDetailEvent(item.getEntityId())));

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return chatChooseItems.size();
    }

    public void addAll(List<ChatChooseItem> chatListWithoutMe) {
        chatChooseItems.addAll(chatListWithoutMe);
    }

    public void clear() {
        chatChooseItems.clear();
    }

    static class MembersViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public View disableLineThrouthView;
        public View disableCoverView;

        public MembersViewHolder(View itemView) {
            super(itemView);
        }
    }

}
