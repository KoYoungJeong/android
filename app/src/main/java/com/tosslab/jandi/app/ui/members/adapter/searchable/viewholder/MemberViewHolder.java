package com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder;

import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.UiUtils;
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
public class MemberViewHolder<T> extends BaseViewHolder<T> {

    @Bind(R.id.iv_profile)
    ImageView ivProfile;
    @Bind(R.id.iv_favorite)
    ImageView ivFavorite;

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
    @Bind(R.id.tv_owner_badge)
    TextView tvOwnerBadge;
    @Bind(R.id.vg_user_kick)
    ViewGroup vgUserKick;
    @Bind(R.id.iv_user_kick)
    View ivUserKick;
    @Bind(R.id.vg_user_selected)
    ViewGroup vgUserSelected;
    @Bind(R.id.cb_user_selected)
    AppCompatCheckBox cbUserSelected;

    private boolean isTeamMemberList = false;
    private boolean isKickMode = false;
    private boolean isSelectMode = false;
    private boolean isProfileImageClickable = false;

    public MemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MemberViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_entity_body_two_line, parent, false);
        return new MemberViewHolder(itemView);
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

    @Override
    public void onBindView(T t) {
        if (t instanceof ChatChooseItem) {
            bindView((ChatChooseItem) t);
        } else if (t instanceof User) {
            User userItem = (User) t;
            ChatChooseItem item = new ChatChooseItem();
            item.name(userItem.getName());
            item.email(userItem.getEmail());
            item.inactive(userItem.isInactive());
            item.isBot(userItem.isBot());
            item.photoUrl(userItem.getPhotoUrl());
            item.jobTitle(userItem.getPosition());
            item.department(userItem.getDivision());
            item.enabled(userItem.isEnabled());
            item.owner(userItem.isTeamOwner());
            bindView(item);
        }
    }

    private void bindView(ChatChooseItem item) {
        setProfileImage(item);

        measureContentWidth(item);

        insertContents(item);

        long myId = TeamInfoLoader.getInstance().getMyId();

        if (isKickMode && item.getEntityId() != myId && !item.isBot()) {
            ivUserKick.setVisibility(View.VISIBLE);
        } else {
            ivUserKick.setVisibility(View.GONE);
            setKickClickListener(null);
        }

        setCheckBoxIfSelectMode(item);

        setItemViewClickListener(item.getEntityId());

        ivFavorite.setVisibility(View.GONE);
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

    private void insertContents(ChatChooseItem item) {
        if (!item.isInactive()) {
            setName(item.getName(), item.isEnabled());
        } else {
            setName(item.getEmail(), item.isEnabled());
        }

        String department = item.getDepartment();
        if (TextUtils.isEmpty(department)) {
            tvUserDepartment.setVisibility(View.GONE);
        } else {
            tvUserDepartment.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams departmentLP = (LinearLayout.LayoutParams) tvUserDepartment.getLayoutParams();
            departmentLP.width = vgContent.getLayoutParams().width;
            tvUserDepartment.setLayoutParams(departmentLP);
            tvUserDepartment.setText(department);
        }

        String jobTitle = item.getJobTitle();
        if (TextUtils.isEmpty(jobTitle)) {
            tvUserJobTitle.setVisibility(View.GONE);
            LinearLayout.LayoutParams userNameLP = (LinearLayout.LayoutParams) vgUserName.getLayoutParams();
            userNameLP.width = vgContent.getLayoutParams().width;
            vgUserName.setLayoutParams(userNameLP);
        } else {
            tvUserJobTitle.setVisibility(View.VISIBLE);
            int maxUserNameWidth = (int) (vgContent.getLayoutParams().width * 0.7);
            Paint userNamePaint = tvUserName.getPaint();
            int nameWidth = (int) userNamePaint.measureText(tvUserName.getText().toString());
            if (nameWidth > maxUserNameWidth) {
                LinearLayout.LayoutParams userNameLP =
                        (LinearLayout.LayoutParams) vgUserName.getLayoutParams();
                userNameLP.width = (int) (vgContent.getLayoutParams().width * 0.7);
                vgUserName.setLayoutParams(userNameLP);
                int maxUserJobTitleWidth = (int) (vgContent.getLayoutParams().width * 0.3);
                LinearLayout.LayoutParams userJobTitleLP =
                        (LinearLayout.LayoutParams) tvUserJobTitle.getLayoutParams();
                userJobTitleLP.width = maxUserJobTitleWidth;
                tvUserJobTitle.setLayoutParams(userJobTitleLP);
            } else {
                LinearLayout.LayoutParams userNameLP =
                        (LinearLayout.LayoutParams) vgUserName.getLayoutParams();
                userNameLP.width = nameWidth;
                vgUserName.setLayoutParams(userNameLP);

                int leftWidth = vgContent.getLayoutParams().width - nameWidth;
                LinearLayout.LayoutParams userJobTitleLP =
                        (LinearLayout.LayoutParams) tvUserJobTitle.getLayoutParams();
                userJobTitleLP.width = leftWidth;
                tvUserJobTitle.setLayoutParams(userJobTitleLP);
            }
            tvUserJobTitle.setText(jobTitle);
        }
    }

    private void measureContentWidth(ChatChooseItem item) {
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        float displayWidth = displayMetrics.widthPixels;

        LinearLayout.LayoutParams contentLP = (LinearLayout.LayoutParams) vgContent.getLayoutParams();

        contentLP.width = (int) (displayWidth - UiUtils.getPixelFromDp(75));
        contentLP.rightMargin = (int) UiUtils.getPixelFromDp(16);

        int boxWidth = (int) UiUtils.getPixelFromDp(50);

        contentLP.width = contentLP.width - contentLP.rightMargin;

        if (isKickMode) {
            vgUserKick.setVisibility(View.VISIBLE);
            vgUserSelected.setVisibility(View.GONE);
            if (!item.isOwner()) {
                contentLP.width = contentLP.width - boxWidth;
            } else {
                contentLP.width = contentLP.width - contentLP.rightMargin;
            }
        } else if (isSelectMode) {
            vgUserKick.setVisibility(View.GONE);
            vgUserSelected.setVisibility(View.VISIBLE);
            contentLP.width = contentLP.width - boxWidth;
        } else {
            ivUserKick.setVisibility(View.GONE);
            vgUserSelected.setVisibility(View.GONE);
        }

        Resources resources = tvOwnerBadge.getResources();

        tvOwnerBadge.setText(isTeamMemberList
                ? resources.getString(R.string.jandi_team_owner)
                : resources.getString(R.string.jandi_topic_owner));

        tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

        if (item.isOwner()) {
            tvOwnerBadge.setVisibility(View.VISIBLE);
            Paint ownerBadgePaint = tvOwnerBadge.getPaint();
            int ownerPadding = (int) UiUtils.getPixelFromDp(14);
            int ownerMargin = (int) UiUtils.getPixelFromDp(16);
            int ownerBadgeWidth = (int) ownerBadgePaint.measureText(tvOwnerBadge.getText().toString()) + ownerPadding;

            contentLP.width = contentLP.width - ownerBadgeWidth;

            if (!(isKickMode || isSelectMode)) {
                contentLP.width = contentLP.width - ownerMargin;
            }
        }

        vgContent.setLayoutParams(contentLP);
    }

    private void setProfileImage(ChatChooseItem item) {
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        if (!item.isBot()) {
            ViewGroup.LayoutParams layoutParams = ivProfile.getLayoutParams();
            layoutParams.height = ivProfile.getResources().getDimensionPixelSize(R.dimen.jandi_entity_item_icon);
            ivProfile.setLayoutParams(layoutParams);
            if (!item.isInactive()) {
                ImageUtil.loadProfileImage(ivProfile, item.getPhotoUrl(), R.drawable.profile_img);
            } else {
                ImageLoader.loadFromResources(ivProfile, R.drawable.profile_img_dummyaccount_43);
            }
        } else {
            ViewGroup.LayoutParams layoutParams = ivProfile.getLayoutParams();
            layoutParams.height = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 54f, displayMetrics));
            ivProfile.setLayoutParams(layoutParams);
            ImageLoader.loadFromResources(ivProfile, R.drawable.bot_43x54);
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

}
