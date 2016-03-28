package com.tosslab.jandi.app.ui.profile.member.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.lists.FormattedEntity;

import uk.co.senab.photoview.PhotoView;

public interface ProfileLoader {
    void setName(TextView tvProfileName, FormattedEntity member);

    void setDescription(TextView tvProfileDescription, FormattedEntity member);

    void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, FormattedEntity member);

    void loadSmallThumb(SimpleDraweeView ivProfileImageSmall, FormattedEntity member);

    void loadFullThumb(PhotoView ivProfileImageFull, String uri);

    void setStarButton(View btnProfileStar, FormattedEntity member);

    boolean isEnabled(FormattedEntity member);

    boolean hasChangedProfileImage(FormattedEntity member);

    void setBlurBackgroundColor(View vProfileImageLargeOverlay);
}
