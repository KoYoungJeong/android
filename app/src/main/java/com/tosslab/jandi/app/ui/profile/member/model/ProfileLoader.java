package com.tosslab.jandi.app.ui.profile.member.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;

import uk.co.senab.photoview.PhotoView;

public interface ProfileLoader {
    void setName(TextView tvProfileName, Member member);

    void setDescription(TextView tvProfileDescription, Member member);

    void setProfileInfo(TextView tvProfileDivision, TextView tvProfilePosition, Member member);

    void loadSmallThumb(ImageView ivProfileImageSmall, Member member);

    void loadFullThumb(PhotoView ivProfileImageFull, String uri);

    void setStarButton(View btnProfileStar, Member member, TextView tvTeamLevel, boolean isLandscape);

    boolean isEnabled(Member member);

    boolean hasChangedProfileImage(Member member);

    void setBackgroundColor(View backgroundColor, View opacity, Level level, Member member);

    void setLevel(Level level, TextView tvTeamLevel, boolean isLandscape);
}
