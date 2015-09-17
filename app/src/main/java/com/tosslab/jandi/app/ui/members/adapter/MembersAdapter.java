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
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

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
        membersViewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_entity_listitem_name);
        membersViewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_entity_listitem_icon);
        membersViewHolder.ivFavorite = (ImageView) convertView.findViewById(R.id.iv_entity_listitem_fav);
        membersViewHolder.tvAdditional = (TextView) convertView.findViewById(R.id.tv_entity_listitem_user_count);
        membersViewHolder.vDisableLineThrough = convertView.findViewById(R.id.iv_entity_listitem_line_through);
        membersViewHolder.vDisableCover = convertView.findViewById(R.id.v_entity_listitem_warning);

        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(MembersViewHolder membersViewHolder, int position) {

        ChatChooseItem item = getItem(position);

        membersViewHolder.tvName.setText(item.getName());

        if (!TextUtils.isEmpty(item.getEmail())) {
            membersViewHolder.tvAdditional.setVisibility(View.VISIBLE);
        } else {
            membersViewHolder.tvAdditional.setVisibility(View.GONE);
        }
        membersViewHolder.tvAdditional.setText(item.getEmail());


        if (item.isStarred()) {
            membersViewHolder.ivFavorite.setVisibility(View.VISIBLE);
        } else {
            membersViewHolder.ivFavorite.setVisibility(View.GONE);
        }

        Ion.with(membersViewHolder.ivIcon)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .load(item.getPhotoUrl());

        if (item.isEnabled()) {

            membersViewHolder.vDisableLineThrough.setVisibility(View.GONE);
            membersViewHolder.vDisableCover.setVisibility(View.GONE);

        } else {

            membersViewHolder.vDisableLineThrough.setVisibility(View.VISIBLE);
            membersViewHolder.vDisableCover.setVisibility(View.VISIBLE);
        }

        membersViewHolder.itemView.setOnClickListener(v ->
                EventBus.getDefault().post(
                        new ShowProfileEvent(item.getEntityId())));
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
        public ImageView ivIcon;
        public ImageView ivFavorite;
        public TextView tvName;
        public TextView tvAdditional;
        public View vDisableLineThrough;
        public View vDisableCover;

        public MembersViewHolder(View itemView) {
            super(itemView);
        }
    }

}
