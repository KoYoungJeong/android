package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

public class ProfileUtil {
    public static void setProfile(long fromEntityId,
                                  SimpleDraweeView ivProfile,
                                  View vProfileCover,
                                  TextView tvName,
                                  View vDisableLineThrough) {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (entity.getUser() != null && entity.isEnabled()) {
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
