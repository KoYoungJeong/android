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
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private List<ChatChooseItem> memberChooseItems;

    private boolean isCheckMode = false;

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (isCheckMode) {
            View itemView = inflater.inflate(R.layout.item_entity_body_one_line, parent, false);
            return new MemberChoiceViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.item_entity_body_two_line, parent, false);
            return new MemberViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatChooseItem item = getItem(position);
        if (isCheckMode) {
            ((MemberChoiceViewHolder) holder).bindView(item);
        } else {
            ((MemberViewHolder) holder).bindView(item);
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

    public void setCheckMode() {
        isCheckMode = true;
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

    static class MemberChoiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private ImageView ivFavorite;
        private TextView tvName;
        private View vDisableLineThrough;
        private View vDisableCover;
        private CheckBox cbChoose;

        public MemberChoiceViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_fav);
            vDisableLineThrough = itemView.findViewById(R.id.iv_entity_listitem_line_through);
            vDisableCover = itemView.findViewById(R.id.v_entity_listitem_warning);
            cbChoose = (CheckBox) itemView.findViewById(R.id.cb_user);
        }

        public void bindView(final ChatChooseItem item) {
            tvName.setText(item.getName());

            ivFavorite.setVisibility(item.isStarred() ? View.VISIBLE : View.GONE);

            vDisableLineThrough.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);
            vDisableCover.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);

            Ion.with(ivIcon)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .transform(new IonCircleTransform())
                    .load(item.getPhotoUrl());

            cbChoose.setChecked(item.isChooseItem());
            itemView.setOnClickListener(v -> {
                boolean isChecked = cbChoose.isChecked();
                cbChoose.setChecked(!isChecked);
                item.setIsChooseItem(!isChecked);
            });

            ivIcon.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(item.getEntityId())));

        }
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private ImageView ivFavorite;
        private TextView tvName;
        private TextView tvAdditional;
        private View vDisableLineThrough;
        private View vDisableCover;

        public MemberViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
            tvAdditional = (TextView) itemView.findViewById(R.id.tv_entity_listitem_user_count);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_fav);
            vDisableLineThrough = itemView.findViewById(R.id.iv_entity_listitem_line_through);
            vDisableCover = itemView.findViewById(R.id.v_entity_listitem_warning);
        }

        public void bindView(ChatChooseItem item) {
            tvName.setText(item.getName());

            tvAdditional.setVisibility(!TextUtils.isEmpty(item.getEmail()) ? View.VISIBLE : View.GONE);
            tvAdditional.setText(item.getEmail());

            ivFavorite.setVisibility(item.isStarred() ? View.VISIBLE : View.GONE);

            vDisableLineThrough.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);
            vDisableCover.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);

            Ion.with(ivIcon)
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .transform(new IonCircleTransform())
                    .load(item.getPhotoUrl());

            itemView.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(item.getEntityId())));
        }
    }
}
