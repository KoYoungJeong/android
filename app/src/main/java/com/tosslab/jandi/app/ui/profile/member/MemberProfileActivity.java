package com.tosslab.jandi.app.ui.profile.member;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.modify.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.utils.transform.ion.IonBlurTransform;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleStrokeTransform;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.views.SwipeExitLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.Map;

/**
 * Created by tonyjs on 15. 9. 8..
 */
@EActivity(R.layout.activity_member_profile)
public class MemberProfileActivity extends AppCompatActivity {

    @Extra
    int memberId;

    @Bean
    EntityClientManager entityClientManager;

    @ViewById(R.id.vg_swipe_exit_layout)
    SwipeExitLayout swipeExitLayout;
    @ViewById(R.id.v_background)
    View vBackground;

    @ViewById(R.id.tv_member_profile_description)
    TextView tvProfileDescription;
    @ViewById(R.id.tv_member_profile_name)
    TextView tvProfileName;
    @ViewById(R.id.tv_member_profile_division)
    TextView tvProfileDivision;
    @ViewById(R.id.tv_member_profile_position)
    TextView tvProfilePosition;
    @ViewById(R.id.tv_member_profile_phone)
    TextView tvProfilePhone;
    @ViewById(R.id.tv_member_profile_email)
    TextView tvProfileEmail;

    @ViewById(R.id.vg_member_profile_img_large_overlay)
    ViewGroup vgProfileImageOverlay;
    @ViewById(R.id.vg_member_profile_team_info)
    ViewGroup vgProfileTeamInfo;
    @ViewById(R.id.vg_member_profile_buttons)
    ViewGroup vgProfileTeamButtons;

    @ViewById(R.id.btn_member_profile_star)
    View btnProfileStar;

    @ViewById(R.id.iv_member_profile_img_full)
    ImageView ivProfileImageFull;
    @ViewById(R.id.iv_member_profile_img_large)
    ImageView ivProfileImageLarge;
    @ViewById(R.id.iv_member_profile_img_small)
    ImageView ivProfileImageSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_bottom_with_allpha, 0);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        swipeExitLayout.setOnExitListener(this::finish);

        swipeExitLayout.sevBackgroundDimView(vBackground);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FormattedEntity member = EntityManager.getInstance().getEntityById(memberId);

        tvProfileDescription.setText(member.getUserStatusMessage());
        tvProfileName.setText(member.getName());

        String userDivision = member.getUserDivision();
        String userPosition = member.getUserPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);

        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            vgProfileTeamInfo.setVisibility(View.GONE);
        }

        String userEmail = member.getUserEmail();
        tvProfileEmail.setText(userEmail);
        if (TextUtils.isEmpty(userEmail)) {
            tvProfileEmail.setVisibility(View.GONE);
        }

        String userPhoneNumber = member.getUserPhoneNumber();
        tvProfilePhone.setText(userPhoneNumber);
        if (TextUtils.isEmpty(userPhoneNumber)) {
            tvProfilePhone.setVisibility(View.GONE);
        }

        btnProfileStar.setSelected(member.isStarred);

        addButtons(member);

        String profileImageUrlLarge = member.getUserLargeProfileUrl();

        if (!hasChangedProfileUrl(profileImageUrlLarge)) {
            int color = getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
            vgProfileImageOverlay.setBackgroundColor(color);
        }
        Ion.with(ivProfileImageLarge)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .centerCrop()
                .transform(new IonBlurTransform())
                .load(profileImageUrlLarge);

        String profileImageUrlMedium = member.getUserMediumProfileUrl();
        IonCircleStrokeTransform transform = new IonCircleStrokeTransform(
                1, getResources().getColor(R.color.jandi_member_profile_img_circle_line_color));
        Ion.with(ivProfileImageSmall)
                .fitXY()
                .error(R.drawable.profile_img)
                .transform(transform)
                .load(profileImageUrlMedium);

        Ion.with(ivProfileImageFull)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .fitCenter()
                .load(profileImageUrlLarge);
    }

    @Click(R.id.iv_member_profile_img_small)
    void showFullImage(View v) {
        v.animate()
                .scaleX(3.0f)
                .scaleY(3.0f)
                .setDuration(300)
                .alpha(0.0f);

        ivProfileImageFull.setScaleX(1.0f);
        ivProfileImageFull.setScaleY(1.0f);
        ivProfileImageFull.setAlpha(0.0f);
        ivProfileImageFull.setVisibility(View.VISIBLE);
        ivProfileImageFull.animate()
                .setDuration(300)
                .alpha(1.0f)
                .setListener(null);
    }

    @Click(R.id.iv_member_profile_img_full)
    void hideFullImage(final View v) {
        v.animate()
                .setDuration(300)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                    }
                });

        ivProfileImageSmall.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .alpha(1.0f);
    }

    @Click(R.id.btn_member_profile_close)
    @Override
    public void onBackPressed() {
        if (ivProfileImageFull.isShown()) {
            hideFullImage(ivProfileImageFull);
            return;
        }

        if (swipeExitLayout != null) {
            swipeExitLayout.exit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Click(R.id.btn_member_profile_star)
    public void star(View v) {
        boolean futureSelected = !v.isSelected();
        v.setSelected(futureSelected);
        postStar(futureSelected);
    }

    @Background
    void postStar(boolean star) {
        if (star) {
            entityClientManager.enableFavorite(memberId);
        } else {
            entityClientManager.disableFavorite(memberId);
        }

        EntityManager.getInstance().getEntityById(memberId).isStarred = star;
    }

    private boolean hasChangedProfileUrl(String url) {
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    private void addButtons(final FormattedEntity member) {
        vgProfileTeamButtons.removeAllViews();

        final EntityManager entityManager = EntityManager.getInstance();

        boolean isMe = entityManager.isMe(member.getId());

        if (isMe) {
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_edit,
                            getString(R.string.jandi_member_profile_edit), (v) -> {
                                startModifyProfileActivity();
                            }));

            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_mention,
                            getString(R.string.jandi_member_profile_mention), (v) -> {
                                startStarMentionListActivity();
                            }));
        } else {
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_mobile,
                            getString(R.string.jandi_member_profile_phone), (v) -> {
                                call(member.getUserPhoneNumber());
                            }));
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_mail,
                            getString(R.string.jandi_member_profile_email), (v) -> {
                                sendEmail(member.getUserEmail());
                            }));
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_message,
                            getString(R.string.jandi_member_profile_dm), (v) -> {
                                int teamId = entityManager.getTeamId();
                                int entityId = member.getId();
                                boolean isStarred = member.isStarred;
                                startMessageListActivity(teamId, entityId, isStarred);
                            }));
        }
    }

    private View getButton(int iconResource, String title, View.OnClickListener onClickListener) {
        View buttonView = getLayoutInflater()
                .inflate(R.layout.item_member_profile_button, vgProfileTeamButtons, false);

        View vIcon = buttonView.findViewById(R.id.v_member_profile_button_icon);
        TextView tvTitle = (TextView) buttonView.findViewById(R.id.tv_member_profile_button_title);

        vIcon.setBackgroundResource(iconResource);
        tvTitle.setText(title);

        buttonView.setOnClickListener(onClickListener);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        buttonView.setLayoutParams(params);

        return buttonView;
    }

    private void startModifyProfileActivity() {
        ModifyProfileActivity_.intent(MemberProfileActivity.this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    private void startStarMentionListActivity() {
        StarMentionListActivity_.intent(MemberProfileActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .extra("type", StarMentionListActivity.TYPE_MENTION_LIST)
                .start();
    }

    private void sendEmail(String userEmail) {
        if (TextUtils.isEmpty(userEmail)) {
            return;
        }
        String uri = "mailto:" + userEmail;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        startActivity(intent);
    }

    private void call(String userPhoneNumber) {
        if (TextUtils.isEmpty(userPhoneNumber)) {
            return;
        }
        String uri = "tel:" + userPhoneNumber.replaceAll("[^0-9|\\+]", "");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startMessageListActivity(int teamId, int entityId, boolean isStarred) {
        MessageListV2Activity_.intent(MemberProfileActivity.this)
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(-1)
                .isFavorite(isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }
}
