package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
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
    private SimpleDraweeView ivUserProfile;
    private View vUserProfileDisableIndicator;

    private ProfileBinder(TextView tvUserName, View vUserNameDisableIndicator,
                          SimpleDraweeView ivUserProfile, View vUserProfileDisableIndicator) {
        this.tvUserName = tvUserName;
        this.vUserNameDisableIndicator = vUserNameDisableIndicator;
        this.ivUserProfile = ivUserProfile;
        this.vUserProfileDisableIndicator = vUserProfileDisableIndicator;
    }

    public static ProfileBinder newInstance(
            TextView tvUserName, View vUserNameDisableIndicator,
            SimpleDraweeView ivUserProfile, View vUserProfileDisableIndicator) {
        return new ProfileBinder(tvUserName,
                vUserNameDisableIndicator, ivUserProfile, vUserProfileDisableIndicator);
    }

    public void bind(FormattedEntity writer) {
        String profileUrl = writer.getUserSmallProfileUrl();
        ImageUtil.loadProfileImage(ivUserProfile, profileUrl, R.drawable.profile_img);

        tvUserName.setText(writer.getName());

        tvUserName.post(() -> {
            int gap = (int) UiUtils.getPixelFromDp(2);
            int width = (int) tvUserName.getLayout().getLineWidth(0) + gap;

            ViewGroup.LayoutParams params = vUserNameDisableIndicator.getLayoutParams();
            params.width = width;
            vUserNameDisableIndicator.setLayoutParams(params);
        });

        boolean isDisabledUser = isDisabledUser(writer.getUser().status);
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

    private boolean isDisabledUser(String userStatus) {
        return !TextUtils.equals(userStatus, "enabled");
    }

    private void onProfileClick(final long writerId, ShowProfileEvent.From clickType) {
        EventBus.getDefault().post(new ShowProfileEvent(writerId, clickType));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewProfile);
    }

}
