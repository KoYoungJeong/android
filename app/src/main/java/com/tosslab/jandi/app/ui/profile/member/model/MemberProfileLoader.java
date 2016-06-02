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
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import uk.co.senab.photoview.PhotoView;

public class MemberProfileLoader implements ProfileLoader {
    private final Context context;

    public MemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, Member member) {
        tvProfileName.setText(member.getName());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, Member member) {
        String description;
        if (isEnabled(member)) {
            description = ((User) member).getStatusMessage();
        } else {
            description = context.getString(R.string.jandi_disable_user_profile_explain);
        }

        tvProfileDescription.setText(description);
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, Member member) {
        User user = (User) member;
        String userDivision = user.getDivision();
        String userPosition = user.getPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);
        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            vgProfileTeamInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, Member member) {
        String profileImageUrlMedium = member.getPhotoUrl();
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
    public void setStarButton(View btnProfileStar, Member member) {
        btnProfileStar.setSelected(TeamInfoLoader.getInstance().isChatStarred(member.getId()));
        boolean isMe = isMe(member.getId());
        btnProfileStar.setVisibility(isMe ? View.INVISIBLE : View.VISIBLE);
        btnProfileStar.setEnabled(!isMe);
    }

    @Override
    public boolean isEnabled(Member member) {
        return member.isEnabled();
    }

    @Override
    public boolean hasChangedProfileImage(Member member) {
        String url = member.getPhotoUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isMe(long memberId) {
        return TeamInfoLoader.getInstance().getMyId() == memberId;
    }

}
