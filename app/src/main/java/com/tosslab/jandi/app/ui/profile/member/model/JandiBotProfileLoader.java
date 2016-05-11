package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import uk.co.senab.photoview.PhotoView;

public class JandiBotProfileLoader implements ProfileLoader {
    private final Context context;

    public JandiBotProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, FormattedEntity member) {
        tvProfileName.setText(member.getName());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, FormattedEntity member) {
        tvProfileDescription.setText(R.string.jandi_bot_status);
    }

    @Override
    public void setProfileInfo(ViewGroup vgProfileTeamInfo, TextView tvProfileDivision, TextView tvProfilePosition, FormattedEntity member) {
        tvProfileDivision.setText(R.string.jandi_bot_role);
        tvProfileDivision.setMaxLines(3);

        tvProfilePosition.setVisibility(View.GONE);
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, FormattedEntity member) {
        RelativeLayout.LayoutParams layoutParams =
                ((RelativeLayout.LayoutParams) ivProfileImageSmall.getLayoutParams());
        layoutParams.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                100f,
                context.getResources().getDisplayMetrics());

        if (!isLandscape()) {
            int margin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    60f,
                    context.getResources().getDisplayMetrics());
            layoutParams.topMargin = -margin;
        }
        ivProfileImageSmall.setLayoutParams(layoutParams);

        ivProfileImageSmall.setImageResource(R.drawable.bot_80x100);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uri) {
        // do nothing.
    }

    @Override
    public void setStarButton(View btnProfileStar, FormattedEntity member) {
        btnProfileStar.setSelected(member.isStarred);
        btnProfileStar.setVisibility(View.VISIBLE);
        btnProfileStar.setEnabled(true);
    }

    @Override
    public boolean isEnabled(FormattedEntity member) {
        return member.isEnabled();
    }

    @Override
    public boolean hasChangedProfileImage(FormattedEntity member) {
        return false;
    }

    @Override
    public void setBlurBackgroundColor(View vProfileImageLargeOverlay) {
        int defaultColor = context.getResources().getColor(R.color.jandi_primary_color);
        vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
    }

    private boolean isLandscape() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}
