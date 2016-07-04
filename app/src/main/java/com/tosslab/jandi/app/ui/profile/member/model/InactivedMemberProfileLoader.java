package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
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

import uk.co.senab.photoview.PhotoView;

public class InactivedMemberProfileLoader implements ProfileLoader {
    private final Context context;

    public InactivedMemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, Member member) {
        tvProfileName.setText(member.getEmail());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, Member member) {
        if (member instanceof User) {
            tvProfileDescription.setText(((User) member).getStatusMessage());
        }
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, Member member) {
        vgProfileTeamInfo.setVisibility(View.GONE);
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, Member member) {
        ivProfileImageSmall.setImageResource(R.drawable.profile_img_dummyaccount_80);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
    }

    @Override
    public void setStarButton(View btnProfileStar, Member member) {
        btnProfileStar.setSelected(TeamInfoLoader.getInstance().isStarredUser(member.getId()));
        boolean isMe = isMe(member.getId());
        btnProfileStar.setVisibility(isMe ? View.INVISIBLE : View.VISIBLE);
        btnProfileStar.setEnabled(!isMe);
    }

    @Override
    public boolean isEnabled(Member member) {
        return true;
    }

    @Override
    public boolean hasChangedProfileImage(Member member) {
        String url = member.getPhotoUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isMe(long memberId) {
        return TeamInfoLoader.getInstance().getMyId() == memberId;
    }

}
