package com.tosslab.jandi.app.ui.members.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {

    private Context context;

    private List<ChatChooseItem> memberChooseItems;

    private boolean checkMode = false;

    public MembersAdapter(Context context) {
        this.context = context;
        memberChooseItems = new ArrayList<>();
    }

    public int getCount() {
        return memberChooseItems.size();
    }

    public ChatChooseItem getItem(int position) {
        return memberChooseItems.get(position);
    }

    @Override
    public MembersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MembersViewHolder membersViewHolder;
        View convertView;
        if (checkMode) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body_one_line, parent, false);
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body_two_line, parent, false);
        }

        membersViewHolder = new MembersViewHolder(convertView);
        membersViewHolder.textViewName = (TextView) convertView.findViewById(R.id.tv_entity_listitem_name);
        membersViewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.iv_entity_listitem_icon);
        membersViewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.iv_entity_listitem_fav);
        membersViewHolder.disableLineThrouthView = convertView.findViewById(R.id.iv_entity_listitem_line_through);
        membersViewHolder.disableCoverView = convertView.findViewById(R.id.v_entity_listitem_warning);

        if (!checkMode) {
            membersViewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.tv_entity_listitem_user_count);
        } else {
            membersViewHolder.chooseCheckBox = (CheckBox) convertView.findViewById(R.id.cb_user);
        }

        return membersViewHolder;
    }

    @Override
    public void onBindViewHolder(MembersViewHolder membersViewHolder, int position) {

        ChatChooseItem item = getItem(position);

        membersViewHolder.textViewName.setText(item.getName());


        if (!checkMode) {
            if (!TextUtils.isEmpty(item.getEmail())) {
                membersViewHolder.textViewAdditional.setVisibility(View.VISIBLE);
            } else {
                membersViewHolder.textViewAdditional.setVisibility(View.GONE);
            }
            membersViewHolder.textViewAdditional.setText(item.getEmail());
        }

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

        if (checkMode) {
            membersViewHolder.chooseCheckBox.setVisibility(View.VISIBLE);
            membersViewHolder.imageViewIcon.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(item.getEntityId(), ShowProfileEvent.From.Image)));
            membersViewHolder.chooseCheckBox.setChecked(item.isChooseItem());
            membersViewHolder.chooseCheckBox.setTag(item);

            membersViewHolder.chooseCheckBox.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                ChatChooseItem selectedItem = (ChatChooseItem) cb.getTag();
                selectedItem.setIsChooseItem(cb.isChecked());
                if (cb.isChecked()) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SelectMember);
                }
            });

        } else {
            membersViewHolder.itemView.setOnClickListener(v -> EventBus.getDefault().post(new
                    ShowProfileEvent(item.getEntityId(), ShowProfileEvent.From.Image)));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return memberChooseItems.size();
    }

    public void addAll(List<ChatChooseItem> memberList) {
        memberChooseItems.addAll(memberList);
    }

    public void clear() {
        memberChooseItems.clear();
    }

    public void setEnableCheckMode() {
        checkMode = true;
    }

    public List<Integer> getSelectedUserIds() {
        List<Integer> selectedUserIds = new ArrayList<Integer>();
        for (ChatChooseItem item : memberChooseItems) {
            if (item.isChooseItem()) {
                selectedUserIds.add(item.getEntityId());
            }
        }
        return selectedUserIds;
    }

    static class MembersViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public View disableLineThrouthView;
        public View disableCoverView;
        public CheckBox chooseCheckBox;

        public MembersViewHolder(View itemView) {
            super(itemView);
        }
    }

}
