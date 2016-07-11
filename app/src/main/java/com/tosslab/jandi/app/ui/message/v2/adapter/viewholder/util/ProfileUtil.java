package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import de.greenrobot.event.EventBus;

public class ProfileUtil {
    public static void setProfile(long fromEntityId,
                                  ImageView ivProfile,
                                  View vProfileCover,
                                  TextView tvName,
                                  View vDisableLineThrough) {
        if (!TeamInfoLoader.getInstance().isUser(fromEntityId)) {
            return;
        }
        User entity = TeamInfoLoader.getInstance().getUser(fromEntityId);

        String profileUrl = entity.getPhotoUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (entity.isEnabled()) {
            tvName.setTextColor(tvName.getResources().getColor(R.color.jandi_messages_name));
            vProfileCover.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
            foreground.getPaint().setColor(0x66FFFFFF);
            vProfileCover.setBackgroundDrawable(foreground);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(entity.getName());
        if (TeamInfoLoader.getInstance().isUser(fromEntityId)) {
            ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Image)));
            tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Name)));
        } else {
            ivProfile.setOnClickListener(null);
            tvName.setOnClickListener(null);
        }
    }

    public static void setProfileForCommment(long fromEntityId,
                                             ImageView ivProfile,
                                             View vProfileCover,
                                             TextView tvName,
                                             View vDisableLineThrough) {
        if (!TeamInfoLoader.getInstance().isUser(fromEntityId)) {
            return;
        }

        User entity = TeamInfoLoader.getInstance().getUser(fromEntityId);

        ViewGroup.LayoutParams ivProfileLayoutParams = ivProfile.getLayoutParams();
        if (entity.isBot()) {
            ivProfileLayoutParams.height = (int) UiUtils.getPixelFromDp(32f);
            ivProfile.setLayoutParams(ivProfileLayoutParams);
            ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivProfile, R.drawable.bot_43x54);
        } else {
            ivProfileLayoutParams.height = (int) UiUtils.getPixelFromDp(25f);
            ivProfile.setLayoutParams(ivProfileLayoutParams);
            if (entity.isEnabled()) {
                ImageUtil.loadProfileImage(
                        ivProfile, entity.getPhotoUrl(), R.drawable.profile_img);
            } else {
                ivProfile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivProfile, R.drawable.profile_img_dummyaccount_43);
            }
        }

        if (entity.isEnabled()) {
            tvName.setTextColor(tvName.getResources().getColor(R.color.jandi_messages_name));
            vProfileCover.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
            foreground.getPaint().setColor(0x66FFFFFF);
            vProfileCover.setBackgroundDrawable(foreground);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(entity.getName());
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Name)));
    }
}
