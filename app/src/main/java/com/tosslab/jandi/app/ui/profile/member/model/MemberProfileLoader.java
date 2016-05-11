package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import uk.co.senab.photoview.PhotoView;

public class MemberProfileLoader implements ProfileLoader {
    private final Context context;

    public MemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, FormattedEntity member) {
        tvProfileName.setText(member.getName());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, FormattedEntity member) {
        String description;
        if (isEnabled(member)) {
            description = member.getUserStatusMessage();
        } else {
            description = context.getString(R.string.jandi_disable_user_profile_explain);
        }

        tvProfileDescription.setText(description);
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, FormattedEntity member) {
        String userDivision = member.getUserDivision();
        String userPosition = member.getUserPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);
        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            vgProfileTeamInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, FormattedEntity member) {
        String profileImageUrlMedium = member.getUserMediumProfileUrl();
        ImageUtil.loadProfileImage(
                ivProfileImageSmall, profileImageUrlMedium, R.drawable.profile_img);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
        Uri uri = Uri.parse(uriString);

        ImageLoader.newInstance()
                .uri(uri)
                .into(ivProfileImageFull);

    }

    @Override
    public void setStarButton(View btnProfileStar, FormattedEntity member) {
        btnProfileStar.setSelected(member.isStarred);
        boolean isMe = isMe(member.getId());
        btnProfileStar.setVisibility(isMe ? View.INVISIBLE : View.VISIBLE);
        btnProfileStar.setEnabled(!isMe);
    }

    @Override
    public boolean isEnabled(FormattedEntity member) {
        return member.isEnabled();
    }

    @Override
    public boolean hasChangedProfileImage(FormattedEntity member) {
        String url = member.getUserLargeProfileUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isMe(long memberId) {
        return EntityManager.getInstance().isMe(memberId);
    }

}
