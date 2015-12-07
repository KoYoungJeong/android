package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

import uk.co.senab.photoview.PhotoView;

public class MemberProfileLoader implements ProfileLoader {
    private final Context context;

    public MemberProfileLoader() {
        context = JandiApplication.getContext();
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
        Ion.with(ivProfileImageSmall)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .fitCenter()
                .transform(new IonCircleTransform())
                .load(profileImageUrlMedium);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uri) {
        Ion.with(ivProfileImageFull)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .fitCenter()
                .load(uri);
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
        return TextUtils.equals(member.getUser().status, "enabled");
    }

    @Override
    public boolean hasChangedProfileImage(FormattedEntity member) {
        String url = member.getUserLargeProfileUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        Drawable defaultColor = context.getResources().getDrawable(R.color.jandi_member_profile_img_overlay_default);
        vProfileImageLargeOverlay.setBackground(defaultColor);
    }

    private boolean isMe(int memberId) {
        return EntityManager.getInstance().isMe(memberId);
    }

}
