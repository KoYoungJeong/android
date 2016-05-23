package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import uk.co.senab.photoview.PhotoView;

public class InactivedMemberProfileLoader implements ProfileLoader {
    private final Context context;

    public InactivedMemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, FormattedEntity member) {
        tvProfileName.setText(member.getUserEmail());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, FormattedEntity member) {
        tvProfileDescription.setText(member.getUserStatusMessage());
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, FormattedEntity member) {
        vgProfileTeamInfo.setVisibility(View.GONE);
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, FormattedEntity member) {
        ImageUtil.loadProfileImage(
                ivProfileImageSmall, UriUtil.getResourceUri(R.drawable.profile_img_dummyaccount_80), R.drawable.profile_img);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
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
        return true;
    }

    @Override
    public boolean hasChangedProfileImage(FormattedEntity member) {
        String url = member.getUserLargeProfileUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_message_search_item_topic_txt_color);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isMe(long memberId) {
        return EntityManager.getInstance().isMe(memberId);
    }

}
