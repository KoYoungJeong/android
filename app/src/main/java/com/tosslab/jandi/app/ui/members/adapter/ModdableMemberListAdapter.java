package com.tosslab.jandi.app.ui.members.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ModdableMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int OWNER_TYPE_TEAM = 0;
    public static final int OWNER_TYPE_TOPIC = 1;
    private Context context;
    private List<ChatChooseItem> memberChooseItems;
    private boolean isCheckMode = false;
    private boolean kickMode;
    private int ownerType = OWNER_TYPE_TEAM;
    private OnKickClickListener onKickClickListener;
    private OnMemberClickListener onMemberClickListener;

    public ModdableMemberListAdapter(Context context, int ownerType) {
        this.context = context;
        this.ownerType = ownerType;
        memberChooseItems = new ArrayList<>();
    }

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
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
        final ChatChooseItem item = getItem(position);
        if (isCheckMode) {
            ((MemberChoiceViewHolder) holder).bindView(item);
        } else {
            long myId = TeamInfoLoader.getInstance().getMyId();
            ((MemberViewHolder) holder).bindView(item, ownerType, kickMode, myId, v -> {
                if (onKickClickListener != null) {
                    onKickClickListener.onKickClick(ModdableMemberListAdapter.this, holder, position);
                }
            });
        }

        if (onMemberClickListener != null) {
            holder.itemView.setOnClickListener((v) -> onMemberClickListener.onMemberClick(item));
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

    public List<Long> getSelectedUserIds() {
        List<Long> selectedUserIds = new ArrayList<>();
        for (ChatChooseItem item : memberChooseItems) {
            if (item.isChooseItem()) {
                selectedUserIds.add(item.getEntityId());
            }
        }
        return selectedUserIds;
    }

    public void setKickMode(boolean kickMode) {
        this.kickMode = kickMode;
    }

    public void setOnKickClickListener(OnKickClickListener onKickClickListener) {
        this.onKickClickListener = onKickClickListener;
    }

    public void remove(int position) {
        memberChooseItems.remove(position);
    }

    public interface OnMemberClickListener {
        void onMemberClick(ChatChooseItem item);
    }

    public interface OnKickClickListener {
        void onKickClick(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, int position);
    }

    static class MemberChoiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private ImageView ivFavorite;
        private TextView tvName;
        private View vDisableLineThrough;
        private View vDisableCover;
        private CheckBox cbChoose;
        private TextView tvOwnerBadge;

        public MemberChoiceViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_fav);
            vDisableLineThrough = itemView.findViewById(R.id.iv_entity_listitem_line_through);
            vDisableCover = itemView.findViewById(R.id.v_entity_listitem_warning);
            cbChoose = (CheckBox) itemView.findViewById(R.id.cb_user);
            tvOwnerBadge = (TextView) itemView.findViewById(R.id.tv_owner_badge);
        }

        public void bindView(final ChatChooseItem item) {
            tvName.setText(item.getName());

            Resources resources = tvOwnerBadge.getResources();
            tvOwnerBadge.setText(resources.getString(R.string.jandi_team_owner));
            tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

            tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

//            ivFavorite.setVisibility(item.isStarred() ? View.VISIBLE : View.GONE);
            ivFavorite.setVisibility(View.GONE);

            vDisableLineThrough.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);
            vDisableCover.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);

            ImageUtil.loadProfileImage(ivIcon, item.getPhotoUrl(), R.drawable.profile_img);

            cbChoose.setChecked(item.isChooseItem());
            itemView.setOnClickListener(v -> {
                boolean isChecked = cbChoose.isChecked();
                cbChoose.setChecked(!isChecked);
                item.setIsChooseItem(!isChecked);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SelectMember);
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
        private View ivKick;
        private TextView tvOwnerBadge;

        public MemberViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
            tvAdditional = (TextView) itemView.findViewById(R.id.tv_entity_listitem_user_count);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_fav);
            vDisableLineThrough = itemView.findViewById(R.id.iv_entity_listitem_line_through);
            vDisableCover = itemView.findViewById(R.id.v_entity_listitem_warning);
            ivKick = itemView.findViewById(R.id.iv_entity_listitem_user_kick);
            tvOwnerBadge = (TextView) itemView.findViewById(R.id.tv_owner_badge);
        }

        public void bindView(ChatChooseItem item, int ownerType, boolean kickMode, long myId,
                             View.OnClickListener onKickClickListener) {
            if (!item.isInactive()) {
                tvName.setText(item.getName());
            } else {
                tvName.setText(item.getEmail());
            }

            Resources resources = tvOwnerBadge.getResources();
            tvOwnerBadge.setText(ownerType == ModdableMemberListAdapter.OWNER_TYPE_TEAM
                    ? resources.getString(R.string.jandi_team_owner)
                    : resources.getString(R.string.jandi_topic_owner));

            tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

            tvAdditional.setVisibility(!TextUtils.isEmpty(item.getStatusMessage()) ? View.VISIBLE : View.GONE);
            tvAdditional.setText(item.getStatusMessage());

            ivFavorite.setVisibility(View.GONE);

            vDisableLineThrough.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);
            vDisableCover.setVisibility(item.isEnabled() ? View.GONE : View.VISIBLE);

            if (kickMode && item.getEntityId() != myId && !item.isBot()) {
                ivKick.setVisibility(View.VISIBLE);
                ivKick.setOnClickListener(onKickClickListener);
            } else {
                ivKick.setVisibility(View.GONE);
                ivKick.setOnClickListener(null);
            }

            if (!item.isBot()) {
                ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
                layoutParams.height = ivIcon.getResources().getDimensionPixelSize(R.dimen.jandi_entity_item_icon);
                ivIcon.setLayoutParams(layoutParams);
                if (!item.isInactive()) {
                    ImageUtil.loadProfileImage(ivIcon, item.getPhotoUrl(), R.drawable.profile_img);
                } else {
                    ivIcon.setImageResource(R.drawable.profile_img_dummyaccount_43);
                }
            } else {
                ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
                DisplayMetrics displayMetrics = ivIcon.getResources().getDisplayMetrics();
                layoutParams.height = Math.round(
                        TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 54f, displayMetrics));
                ivIcon.setLayoutParams(layoutParams);
                ivIcon.setImageResource(R.drawable.bot_43x54);
            }

            itemView.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(item.getEntityId())));
        }
    }
}
