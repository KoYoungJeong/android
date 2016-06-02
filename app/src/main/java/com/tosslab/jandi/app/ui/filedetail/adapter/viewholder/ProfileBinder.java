package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class ProfileBinder {
    private TextView tvUserName;
    private View vUserNameDisableIndicator;
    private ImageView ivUserProfile;
    private View vUserProfileDisableIndicator;

    private ProfileBinder(TextView tvUserName, View vUserNameDisableIndicator,
                          ImageView ivUserProfile, View vUserProfileDisableIndicator) {
        this.tvUserName = tvUserName;
        this.vUserNameDisableIndicator = vUserNameDisableIndicator;
        this.ivUserProfile = ivUserProfile;
        this.vUserProfileDisableIndicator = vUserProfileDisableIndicator;
    }

    public static ProfileBinder newInstance(
            TextView tvUserName, View vUserNameDisableIndicator,
            ImageView ivUserProfile, View vUserProfileDisableIndicator) {
        return new ProfileBinder(tvUserName,
                vUserNameDisableIndicator, ivUserProfile, vUserProfileDisableIndicator);
    }

    public void bind(User writer) {
        String profileUrl = writer.getPhotoUrl();
        ImageUtil.loadProfileImage(ivUserProfile, profileUrl, R.drawable.profile_img);

        tvUserName.setText(writer.getName());

        tvUserName.post(() -> {
            int gap = (int) UiUtils.getPixelFromDp(2);
            int width = (int) tvUserName.getLayout().getLineWidth(0) + gap;

            ViewGroup.LayoutParams params = vUserNameDisableIndicator.getLayoutParams();
            params.width = width;
            vUserNameDisableIndicator.setLayoutParams(params);
        });

        boolean isDisabledUser = !writer.isEnabled();
        vUserNameDisableIndicator.setVisibility(isDisabledUser ? View.VISIBLE : View.GONE);
        vUserProfileDisableIndicator.setVisibility(isDisabledUser ? View.VISIBLE : View.GONE);

        Resources resources = tvUserName.getResources();
        tvUserName.setTextColor(isDisabledUser
                ? resources.getColor(R.color.deactivate_text_color)
                : resources.getColor(R.color.black));

        ivUserProfile.setOnClickListener(
                v -> onProfileClick(writer.getId(), ShowProfileEvent.From.Image));
        tvUserName.setOnClickListener(
                v -> onProfileClick(writer.getId(), ShowProfileEvent.From.Name));
    }

    private void onProfileClick(final long writerId, ShowProfileEvent.From clickType) {
        EventBus.getDefault().post(new ShowProfileEvent(writerId, clickType));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewProfile);
    }

}
