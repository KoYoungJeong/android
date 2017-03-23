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
        ImageUtil.loadProfileImage(
                ivProfileImageSmall, profileImageUrl, R.drawable.profile_img);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
        Uri uri = Uri.parse(uriString);

        ImageLoader.newInstance()
                .uri(uri)
                .into(ivProfileImageFull);

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
        return member.isEnabled();
    }

    @Override
    public boolean hasChangedProfileImage(Member member) {
        String url = member.getPhotoUrl();
        return ProfileUtil.isChangedPhoto(url);
    }

    @Override
    public void setBackgroundColor(View backgroundColor, View opacity, Level level, Member member) {
        if (!isEnabled(member)) {
            backgroundColor.setBackgroundColor(0xff666666);
            opacity.setBackgroundColor(0x40000000);
            return;
        }

        if (level == Level.Owner) {
            backgroundColor.setBackgroundColor(0xff3a97d2);
            opacity.setBackgroundColor(0x40000000);
        } else if (level == Level.Admin) {
            backgroundColor.setBackgroundColor(0xff7b6ff1);
            opacity.setBackgroundColor(0x40000000);
        } else if (level == Level.Member) {
            backgroundColor.setBackgroundColor(0xfff79521);
            opacity.setBackgroundColor(0x40000000);
        } else if (level == Level.Guest) {
            backgroundColor.setBackgroundColor(0xff88c10e);
            opacity.setBackgroundColor(0x40000000);
        }
    }

    @Override
    public void setLevel(Level level, TextView tvTeamLevel, boolean isLandscape) {
        AccessLevelUtil.setTextOfLevelInProfile(level, tvTeamLevel);
    }

    private boolean isMe(long memberId) {
        return TeamInfoLoader.getInstance().getMyId() == memberId;
    }

}
