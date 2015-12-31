package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.image.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.ClosableAttachStateChangeListener;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.senab.photoview.PhotoView;

public class MemberProfileLoader implements ProfileLoader {
    private final Context context;

    public MemberProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setDescription(TextView tvProfileDescription, FormattedEntity member) {
        String description;
        if (isEnabled(member)) {
            description = member.getUserStatusMessage();
        } else {
            description = context.getString(R.string.jandi_disable_user_profile_explain);
        }

        tvProfileDescription.setText(description);
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, FormattedEntity member) {
        String userDivision = member.getUserDivision();
        String userPosition = member.getUserPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);
        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            vgProfileTeamInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadSmallThumb(SimpleDraweeView ivProfileImageSmall, FormattedEntity member) {
        String profileImageUrlMedium = member.getUserMediumProfileUrl();
        ImageUtil.loadCircleImageByFresco(
                ivProfileImageSmall, profileImageUrlMedium, R.drawable.profile_img);

    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uriString) {
        Uri uri = Uri.parse(uriString);

        ImageUtil.loadDrawable(uri, new BaseOnResourceReadyCallback() {
            @Override
            public void onReady(Drawable drawable, CloseableReference reference) {
                Observable.empty()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            setImageResource(ivProfileImageFull, drawable, reference);
                        });
            }

            @Override
            public void onFail(Throwable cause) {
                LogUtil.e(Log.getStackTraceString(cause));
            }
        });
    }

    void setImageResource(PhotoView ivProfileImageFull, Drawable drawable, CloseableReference reference) {
        ivProfileImageFull.setImageDrawable(drawable);
        ivProfileImageFull.addOnAttachStateChangeListener(
                new ClosableAttachStateChangeListener(reference));
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
        return TextUtils.equals(member.getUser().status, "enabled");
    }

    @Override
    public boolean hasChangedProfileImage(FormattedEntity member) {
        String url = member.getUserLargeProfileUrl();
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isMe(int memberId) {
        return EntityManager.getInstance().isMe(memberId);
    }

}
