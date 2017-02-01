package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import uk.co.senab.photoview.PhotoView;

public class InactivedMemberProfileLoader implements ProfileLoader {
    private final Context context;

    public InactivedMemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, Member member) {
        tvProfileName.setText(member.getName());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, Member member) {
        if (member instanceof User) {
            tvProfileDescription.setText(((User) member).getStatusMessage());
        }
    }

    @Override
    public void setProfileInfo(TextView tvProfileDivision, TextView tvProfilePosition, Member member) {
        User user = (User) member;
        String userDivision = user.getDivision();
        String userPosition = user.getPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);
        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            tvProfileDivision.setVisibility(View.GONE);
            tvProfilePosition.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, Member member) {
        String profileImageUrl = member.getPhotoUrl();
        if (ProfileUtil.isChangedPhoto(profileImageUrl)) {
            ImageUtil.loadProfileImage(ivProfileImageSmall, profileImageUrl, R.drawable.profile_img);
        } else {
            ivProfileImageSmall.setImageResource(R.drawable.profile_img_dummyaccount_80);
        }
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
        if (ProfileUtil.isChangedPhoto(uriString)) {

            Uri uri = Uri.parse(uriString);
            ImageLoader.newInstance()
                    .uri(uri)
                    .into(ivProfileImageFull);
        }
    }

    @Override
    public void setStarButton(View btnProfileStar, Member member, TextView tvTeamLevel, boolean isLandscape) {
        btnProfileStar.setSelected(TeamInfoLoader.getInstance().isStarredUser(member.getId()));
        boolean isMe = isMe(member.getId());
        btnProfileStar.setVisibility(isMe ? View.GONE : View.VISIBLE);
        btnProfileStar.setEnabled(!isMe);

        if (btnProfileStar.getVisibility() == View.GONE && !isLandscape) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) tvTeamLevel.getLayoutParams();
            DisplayMetrics displayMetrics = tvTeamLevel.getResources().getDisplayMetrics();
            lp.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics);
            tvTeamLevel.setLayoutParams(lp);
        }

    }

    @Override
    public boolean isEnabled(Member member) {
        return true;
    }

    @Override
    public boolean hasChangedProfileImage(Member member) {
        String url = member.getPhotoUrl();
        return ProfileUtil.isChangedPhoto(url);
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    @Override
    public void setLevel(Level level, TextView tvTeamLevel, boolean isLandscape) {
        if (isLandscape) {
            AccessLevelUtil.setTextOfLevelInNav(level, tvTeamLevel);
        } else {
            AccessLevelUtil.setTextOfLevel(level, tvTeamLevel);
        }
    }

    private boolean isMe(long memberId) {
        return TeamInfoLoader.getInstance().getMyId() == memberId;
    }

}
