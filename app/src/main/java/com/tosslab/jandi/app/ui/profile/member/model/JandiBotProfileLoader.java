package com.tosslab.jandi.app.ui.profile.member.model;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;

import uk.co.senab.photoview.PhotoView;

public class JandiBotProfileLoader implements ProfileLoader {
    private final Context context;

    public JandiBotProfileLoader() {
        context = JandiApplication.getContext();
    }

    @Override
    public void setName(TextView tvProfileName, Member member) {
        tvProfileName.setText(member.getName());
    }

    @Override
    public void setDescription(TextView tvProfileDescription, Member member) {
        String helloJandi = context.getString(R.string.common_profilemodal_jandi_botstatus);
        String jandibotRole = context.getString(R.string.jandi_bot_role);
        tvProfileDescription.setText(helloJandi + "\n" + jandibotRole);

    }

    @Override
    public void setProfileInfo(TextView tvProfileDivision, TextView tvProfilePosition, Member member) {
        tvProfileDivision.setVisibility(View.GONE);
        tvProfilePosition.setVisibility(View.GONE);
    }

    @Override
    public void loadSmallThumb(ImageView ivProfileImageSmall, Member member) {
        ivProfileImageSmall.setImageResource(R.drawable.logotype_80);
    }

    @Override
    public void loadFullThumb(PhotoView ivProfileImageFull, String uri) {
        // do nothing.
    }

    @Override
    public void setStarButton(View btnProfileStar, Member member, TextView tvTeamLevel) {
        btnProfileStar.setSelected(TeamInfoLoader.getInstance().isStarredUser(member.getId()));
        btnProfileStar.setVisibility(View.VISIBLE);
        btnProfileStar.setEnabled(true);
    }

    @Override
    public boolean isEnabled(Member member) {
        return member.isEnabled();
    }

    @Override
    public boolean hasChangedProfileImage(Member member) {
        return false;
    }

    @Override
    public void setBackgroundColor(View backgroundColor, View opacity, Level level, Member member) {
        backgroundColor.setBackgroundColor(0xff11456b);
        opacity.setBackgroundColor(0x40000000);
    }

    @Override
    public void setLevel(Level level, TextView tvTeamLevel) {
        tvTeamLevel.setVisibility(View.INVISIBLE);
    }

    private boolean isLandscape() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}