package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

/**
 * Created by tonyjs on 2016. 8. 10..
 */

public class StarredMessageProfileBinder {

    private TextView tvUserName;
    private ImageView ivUserProfile;
    private android.view.View vProfileCover;

    public StarredMessageProfileBinder(TextView tvUserName, ImageView ivUserProfile, android.view.View vProfileCover) {
        this.tvUserName = tvUserName;
        this.ivUserProfile = ivUserProfile;
        this.vProfileCover = vProfileCover;
    }

    public static StarredMessageProfileBinder newInstance(TextView tvUserName, ImageView ivUserProfile, android.view.View vProfileCover) {
        return new StarredMessageProfileBinder(tvUserName, ivUserProfile, vProfileCover);
    }

    public void bind(Member writer) {
        if (writer == null) {
            return;
        }

        boolean isBot = TeamInfoLoader.getInstance().isBot(writer.getId());
        boolean isJandiBot = TeamInfoLoader.getInstance().isJandiBot(writer.getId());

        if (isJandiBot) {

            ivUserProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivUserProfile, R.drawable.logotype_80);

        } else {
            if (isBot) {
                Uri uri = Uri.parse(writer.getPhotoUrl());
                ImageLoader.newInstance()
                        .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                        .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        .transformation(new JandiProfileTransform(ivUserProfile.getContext(),
                                TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                                TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                                Color.TRANSPARENT))
                        .uri(uri)
                        .into(ivUserProfile);
            } else {
                ImageUtil.loadProfileImage(ivUserProfile, writer.getPhotoUrl(), R.drawable.profile_img);
            }
        }

        tvUserName.setText(writer.getName());

        if (writer.isEnabled()) {
            vProfileCover.setVisibility(View.GONE);
            if ((tvUserName.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                tvUserName.setPaintFlags(
                        tvUserName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } else {
            vProfileCover.setVisibility(View.VISIBLE);
            tvUserName.setPaintFlags(tvUserName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

    }
}
