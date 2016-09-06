package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.graphics.Color;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.utils.UiUtils;
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

    public StarredMessageProfileBinder(TextView tvUserName, ImageView ivUserProfile) {
        this.tvUserName = tvUserName;
        this.ivUserProfile = ivUserProfile;
    }

    public static StarredMessageProfileBinder newInstance(TextView tvUserName, ImageView ivUserProfile) {
        return new StarredMessageProfileBinder(tvUserName, ivUserProfile);
    }

    public void bind(Member writer) {
        boolean isBot = TeamInfoLoader.getInstance().isBot(writer.getId());
        boolean isJandiBot = TeamInfoLoader.getInstance().isJandiBot(writer.getId());

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivUserProfile.getLayoutParams();
        if (!isJandiBot) {
            layoutParams.topMargin = (int) UiUtils.getPixelFromDp(16f);
            layoutParams.height = (int) UiUtils.getPixelFromDp(44f);
        } else {
            layoutParams.topMargin = (int) UiUtils.getPixelFromDp(16f) - layoutParams.width / 4;
            layoutParams.height = layoutParams.width * 5 / 4;
        }

        ivUserProfile.setLayoutParams(layoutParams);

        if (isJandiBot) {

            ivUserProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivUserProfile, R.drawable.bot_80x100);

        } else {
            if (isBot) {
                Uri uri = Uri.parse(ImageUtil.getLargeProfileUrl(writer.getPhotoUrl()));
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
    }

}
