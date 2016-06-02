package com.tosslab.jandi.app.ui.profile.member.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.team.member.Member;

import uk.co.senab.photoview.PhotoView;

public interface ProfileLoader {
    void setName(TextView tvProfileName, Member member);

    void setDescription(TextView tvProfileDescription, Member member);

    void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, Member member);

    void loadSmallThumb(ImageView ivProfileImageSmall, Member member);

    void loadFullThumb(PhotoView ivProfileImageFull, String uri);

    void setStarButton(View btnProfileStar, Member member);

    boolean isEnabled(Member member);

    boolean hasChangedProfileImage(Member member);

    void setBlurBackgroundColor(View vProfileImageLargeOverlay);
}
