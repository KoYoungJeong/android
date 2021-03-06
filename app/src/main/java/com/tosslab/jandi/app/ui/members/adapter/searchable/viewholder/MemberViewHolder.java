package com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder;

import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 7. 12..
 */
public abstract class MemberViewHolder<T> extends BaseViewHolder<T> {

    private final int teamOwnerPaddingTop;
    @Bind(R.id.iv_profile)
    ImageView ivProfile;
    @Bind(R.id.vg_profile_absence)
    ViewGroup vgProfileAbsence;
    @Bind(R.id.vg_content)
    ViewGroup vgContent;
    @Bind(R.id.vg_user_name)
    ViewGroup vgUserName;
    @Bind(R.id.tv_user_name)
    TextView tvUserName;
    @Bind(R.id.iv_name_line_through)
    View vDisableNameLineThrough;
    @Bind(R.id.v_name_warning)
    View vDisableNameWarning;
    @Bind(R.id.tv_user_department)
    TextView tvUserDepartment;
    @Bind(R.id.tv_job_title)
    TextView tvUserJobTitle;
    @Bind(R.id.tv_authority_badge)
    TextView tvAuthorityBadge;
    @Bind(R.id.vg_authority_badge)
    LinearLayout vgAuthorityBadge;
    @Bind(R.id.vg_user_kick)
    ViewGroup vgUserKick;
    @Bind(R.id.iv_user_kick)
    View ivUserKick;
    @Bind(R.id.vg_user_selected)
    ViewGroup vgUserSelected;
    @Bind(R.id.cb_user_selected)
    AppCompatCheckBox cbUserSelected;
    @Bind(R.id.v_half_divider)
    View vHalfDivider;
    @Bind(R.id.v_full_divider)
    View vFullDivider;
    @Bind(R.id.v_online)
    View vOnline;

    private boolean isTeamMemberList = false;
    private boolean isKickMode = false;
    private boolean isSelectMode = false;
    private boolean isProfileImageClickable = false;

    protected MemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        teamOwnerPaddingTop = vgAuthorityBadge.getContext().getResources().getDimensionPixelSize(R.dimen.jandi_member_list_owner_badge_padding);
    }

    public static MemberViewHolder createForChatChooseItem(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_entity_body_two_line, parent, false);
        return new ChatChooseItemViewHolder(itemView);
    }

    public void showHalfDivider() {
        vHalfDivider.setVisibility(View.VISIBLE);
        vFullDivider.setVisibility(View.GONE);
    }

    public void showFullDivider() {
        vHalfDivider.setVisibility(View.GONE);
        vFullDivider.setVisibility(View.VISIBLE);
    }

    public void dismissDividers() {
        vHalfDivider.setVisibility(View.GONE);
        vFullDivider.setVisibility(View.GONE);
    }

    public void setKickMode(boolean kickMode) {
        isKickMode = kickMode;
    }

    public void setSelectMode(boolean selectMode) {
        isSelectMode = selectMode;
    }

    public void setProfileImageClickable(boolean profileImageClickable) {
        isProfileImageClickable = profileImageClickable;
    }

    public void setIsTeamMemberList(boolean isTeamMemberList) {
        this.isTeamMemberList = isTeamMemberList;
    }

    public void setKickClickListener(View.OnClickListener onKickClickListener) {
        ivUserKick.setOnClickListener(onKickClickListener);
    }

    protected void bindView(ChatChooseItem item) {
        setProfileImage(item);

        measureContentWidth(item);

        setUserInfo(item);

        if (isKickMode && !item.isMyId() && !item.isBot()) {
            vgUserKick.setVisibility(View.VISIBLE);
        } else {
            vgUserKick.setVisibility(View.GONE);
            setKickClickListener(null);
        }

        setCheckBoxIfSelectMode(item);

        setItemViewClickListener(item.getEntityId());

        if (TeamInfoLoader.getInstance().getOnlineStatus().isOnlineMember(item.getEntityId())) {
            vOnline.setVisibility(View.VISIBLE);
        } else {
            vOnline.setVisibility(View.GONE);
        }
    }

    private void setCheckBoxIfSelectMode(ChatChooseItem item) {
        if (isSelectMode) {
            cbUserSelected.setChecked(item.isChooseItem());
            itemView.setOnClickListener(v -> {
                boolean isChecked = cbUserSelected.isChecked();
                cbUserSelected.setChecked(!isChecked);
                item.setIsChooseItem(!isChecked);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SelectMember);
            });
        }
    }

    private void setUserInfo(ChatChooseItem item) {
        String name = item.getName();
        setName(name, item.isEnabled());

        String department = item.getDepartment();
        if (TextUtils.isEmpty(department)) {
            tvUserDepartment.setVisibility(View.GONE);
        } else {
            tvUserDepartment.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams departmentLP = (LinearLayout.LayoutParams) tvUserDepartment.getLayoutParams();
            departmentLP.width = vgContent.getLayoutParams().width;
            tvUserDepartment.setLayoutParams(departmentLP);
        }
        tvUserDepartment.setText(department);

        String jobTitle = item.getJobTitle();
        if (TextUtils.isEmpty(jobTitle)) {
            tvUserJobTitle.setVisibility(View.GONE);
        } else {
            tvUserJobTitle.setVisibility(View.VISIBLE);
        }
        tvUserJobTitle.setText(jobTitle);

    }

    private void measureContentWidth(ChatChooseItem item) {

        if (isKickMode) {
            vgUserKick.setVisibility(View.VISIBLE);
            vgUserSelected.setVisibility(View.GONE);
        } else if (isSelectMode) {
            vgUserKick.setVisibility(View.GONE);
            vgUserSelected.setVisibility(View.VISIBLE);
        } else {
            vgUserKick.setVisibility(View.GONE);
            vgUserSelected.setVisibility(View.GONE);
        }

        if (!isTeamMemberList && item.isOwner()) {
            AccessLevelUtil.setTextOfLevel(Level.Admin, tvAuthorityBadge);
            tvAuthorityBadge.setText(JandiApplication.getContext()
                    .getString(R.string.jandi_topic_owner));
        } else {
            Level level = item.getLevel();
            AccessLevelUtil.setTextOfLevel(level, tvAuthorityBadge);
        }

    }

    private void setProfileImage(ChatChooseItem item) {
        if (!item.isBot()) {
            if (!item.isInactive()) {
                if (TeamInfoLoader.getInstance().getUser(item.getEntityId()).isDisabled() ||
                        (item.getAbsence() == null || item.getAbsence().getStartAt() == null)) {
                    vgProfileAbsence.setVisibility(View.GONE);
                } else {
                    vgProfileAbsence.setVisibility(View.VISIBLE);
                }
                ImageUtil.loadProfileImage(ivProfile, item.getPhotoUrl(), R.drawable.profile_img);
            } else {
                vgProfileAbsence.setVisibility(View.GONE);
                String url = item.getPhotoUrl();
                if (ProfileUtil.isChangedPhoto(url)) {
                    ImageUtil.loadProfileImage(ivProfile, url, R.drawable.profile_img_dummyaccount_40);
                } else {
                    ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ImageLoader.loadFromResources(ivProfile, R.drawable.profile_img_dummyaccount_40);
                }
            }
        } else {
            vgProfileAbsence.setVisibility(View.GONE);
            ivProfile.setImageResource(R.drawable.logotype_80);
        }
    }

    private void setName(String name, boolean enabled) {
        tvUserName.setText(name);
        vDisableNameLineThrough.setVisibility(enabled ? View.GONE : View.VISIBLE);
        vDisableNameWarning.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private void setItemViewClickListener(long itemId) {
        if (isProfileImageClickable) {
            ivProfile.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(itemId)));
        } else {
            itemView.setOnClickListener(v ->
                    EventBus.getDefault().post(new ShowProfileEvent(itemId)));
        }
    }

    private static class ChatChooseItemViewHolder extends MemberViewHolder<ChatChooseItem> {

        ChatChooseItemViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onBindView(ChatChooseItem item) {
            bindView(item);
        }
    }

}
