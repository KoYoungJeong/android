package com.tosslab.jandi.app.ui.profile.member;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.modify.ModifyProfileActivity;
import com.tosslab.jandi.app.ui.profile.modify.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.transform.ion.IonBlurTransform;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.views.SwipeExitLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by tonyjs on 15. 9. 8..
 */
@EActivity(R.layout.activity_member_profile)
public class MemberProfileActivity extends BaseAppCompatActivity {
    public static final String TAG = MemberProfileActivity.class.getSimpleName();

    public static final int EXTRA_FROM_FILE_DETAIL = 2;
    public static final int EXTRA_FROM_MAIN_CHAT = 3;
    public static final int EXTRA_FROM_TEAM_MEMBER = 4;
    public static final int EXTRA_FROM_TOPIC_CHAT = 5;
    public static final int EXTRA_FROM_MESSAGE = 6;
    public static final int EXTRA_FROM_PARTICIPANT = 7;
    private static final String KEY_FULL_SIZE_IMAGE_SHOWING = "full_size_image_showing";
    @Extra
    int memberId;

    @Extra
    int from;

    @Bean
    EntityClientManager entityClientManager;

    @ViewById(R.id.vg_swipe_exit_layout)
    SwipeExitLayout swipeExitLayout;
    @ViewById(R.id.v_member_profile_img_large_overlay)
    View vProfileImageLargeOverlay;

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

    @ViewById(R.id.vg_member_profile_img_large)
    ViewGroup vgProfileImageLarge;
    @ViewById(R.id.vg_member_profile_detail)
    ViewGroup vgProfileTeamDetail;
    @ViewById(R.id.vg_member_profile_team_info)
    ViewGroup vgProfileTeamInfo;
    @ViewById(R.id.vg_member_profile_bottoms)
    ViewGroup vgProfileTeamBottoms;
    @ViewById(R.id.vg_member_profile_buttons)
    ViewGroup vgProfileTeamButtons;

    @ViewById(R.id.v_member_profile_disable)
    View vDisableIcon;
    @ViewById(R.id.btn_member_profile_star)
    View btnProfileStar;

    @ViewById(R.id.iv_member_profile_img_full)
    PhotoView ivProfileImageFull;
    @ViewById(R.id.iv_member_profile_img_large)
    ImageView ivProfileImageLarge;
    @ViewById(R.id.iv_member_profile_img_small)
    ImageView ivProfileImageSmall;

    private boolean isFullSizeImageShowing = false;
    private boolean hasChangedProfileImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_bottom_with_alpha, 0);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        if (savedInstanceState != null) {
            isFullSizeImageShowing = savedInstanceState.getBoolean(KEY_FULL_SIZE_IMAGE_SHOWING);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FULL_SIZE_IMAGE_SHOWING, ivProfileImageFull.isShown());
    }

    @OnActivityResult(ModifyProfileActivity.REQUEST_CODE)
    @AfterViews
    void initViews() {
        FormattedEntity member = EntityManager.getInstance().getEntityById(memberId);

        final String profileImageUrlLarge = member.getUserLargeProfileUrl();

        hasChangedProfileImage = hasChangedProfileImage(profileImageUrlLarge);

        initSwipeLayout(hasChangedProfileImage);

        initLargeImageSize(profileImageUrlLarge);

        boolean isDisableUser = !isEnableUser(member.getUser().status);
        vDisableIcon.setVisibility(isDisableUser ? View.VISIBLE : View.GONE);

        String description = isDisableUser
                ? getString(R.string.jandi_disable_user_profile_explain)
                : member.getUserStatusMessage();

        tvProfileDescription.setText(description);

        tvProfileName.setText(member.getName());

        String userDivision = member.getUserDivision();
        String userPosition = member.getUserPosition();
        tvProfileDivision.setText(userDivision);
        tvProfilePosition.setText(userPosition);

        if (TextUtils.isEmpty(userDivision) && TextUtils.isEmpty(userPosition)) {
            vgProfileTeamInfo.setVisibility(View.GONE);
        }

        String profileImageUrlMedium = member.getUserMediumProfileUrl();
        Ion.with(ivProfileImageSmall)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .fitXY()
                .transform(new IonCircleTransform())
                .load(profileImageUrlMedium);

        ivProfileImageFull.setOnViewTapListener((view, x, y) -> {
            ivProfileImageFull.setScale(1.0f, true);
            hideFullImage(view);
        });

        Ion.with(ivProfileImageFull)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .fitCenter()
                .load(profileImageUrlLarge);

        if (isFullSizeImageShowing) {
            ivProfileImageFull.setAlpha(1.0f);
            ivProfileImageFull.setVisibility(View.VISIBLE);

            ivProfileImageSmall.setScaleX(3.0f);
            ivProfileImageSmall.setScaleY(3.0f);
            ivProfileImageSmall.setAlpha(0.0f);
        }

        if (isDisableUser) {
            tvProfileEmail.setVisibility(View.GONE);
            tvProfilePhone.setVisibility(View.GONE);
            btnProfileStar.setVisibility(View.INVISIBLE);
            btnProfileStar.setEnabled(false);
            return;
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
        btnProfileStar.setVisibility(isMe() ? View.INVISIBLE : View.VISIBLE);
        btnProfileStar.setEnabled(!isMe());

        addButtons(member);
    }

    private void initSwipeLayout(boolean setViewToAlpha) {
        swipeExitLayout.setOnExitListener(this::finish);
        if (setViewToAlpha) {
            swipeExitLayout.setViewToAlpha(vProfileImageLargeOverlay);
        }
        swipeExitLayout.setStatusListener(new SwipeExitLayout.StatusListener() {
            private float lastDistance;


            @Override
            public void onScroll(float distance) {
                lastDistance += distance;

                int measuredWidth = vgProfileImageLarge.getMeasuredWidth();
                int measuredHeight = vgProfileImageLarge.getMeasuredHeight();

                float scaleX = (measuredWidth - (lastDistance * 2)) / measuredWidth;
                LogUtil.e("jsp", "scaleX = " + scaleX);
                scaleX = Math.max(1, scaleX);

                float scaleY = (measuredHeight - (lastDistance * 2)) / measuredHeight;
                LogUtil.e(TAG, "scaleY = " + scaleY);
                scaleY = Math.max(1, scaleY);

                vgProfileImageLarge.setScaleX(scaleX);
                vgProfileImageLarge.setScaleY(scaleY);
            }

            @Override
            public void onIgnore(float spareDistance) {
                lastDistance = 0;

                int measuredHeight = vgProfileImageLarge.getMeasuredHeight();
                int scaledHeight = (int) (measuredHeight * vgProfileImageLarge.getScaleY());

                int duration = Math.min(
                        SwipeExitLayout.MIN_IGNORE_ANIM_DURATION, scaledHeight - measuredHeight);
                vgProfileImageLarge.animate()
                        .setDuration(duration)
                        .scaleX(1)
                        .scaleY(1);
            }

            @Override
            public void onExit(float spareDistance) {
                lastDistance = 0;

                int measuredHeight = vgProfileImageLarge.getMeasuredHeight();
                int scaledHeight = (int) (measuredHeight * vgProfileImageLarge.getScaleY());

                int duration = Math.min(
                        SwipeExitLayout.MIN_IGNORE_ANIM_DURATION,
                        Math.abs(getResources().getDisplayMetrics().heightPixels - scaledHeight));

                float scaleY =
                        getResources().getDisplayMetrics().heightPixels / (float) measuredHeight;

                vgProfileImageLarge.animate()
                        .setDuration(duration)
                        .scaleX(scaleY * 2)
                        .scaleY(scaleY * 2);
            }
        });
    }

    private void initLargeImageSize(final String profileImageUrlLarge) {
        vgProfileTeamDetail.post(() -> {
            int screenHeight = findViewById(android.R.id.content).getMeasuredHeight();
            int vgProfileTeamDetailHeight = vgProfileTeamDetail.getMeasuredHeight();
            if (isLandscape()) {
                vgProfileTeamDetailHeight = vgProfileTeamBottoms.getMeasuredHeight();
            }
            int ivProfileImageLargeHeight = screenHeight - vgProfileTeamDetailHeight;

            ViewGroup.LayoutParams layoutParams = vgProfileImageLarge.getLayoutParams();
            layoutParams.height = ivProfileImageLargeHeight;
            vgProfileImageLarge.setLayoutParams(layoutParams);

            loadLargeImage(profileImageUrlLarge);
        });
    }

    private void loadLargeImage(String profileImageUrlLarge) {
        int defaultColor = getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
        if (!hasChangedProfileImage) {
            vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
            return;
        }
        Drawable placeHolder = new ColorDrawable(defaultColor);
        Ion.with(ivProfileImageLarge)
                .placeholder(placeHolder)
                .error(placeHolder)
                .centerCrop()
                .transform(new IonBlurTransform())
                .load(profileImageUrlLarge);
    }

    @Click(R.id.iv_member_profile_img_small)
    void showFullImage(View v) {
        if (!hasChangedProfileImage) {
            return;
        }

        if (isLandscape()) {
            v.setPivotX(0);
        }
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

        AnalyticsUtil.sendEvent(getScreen(from), AnalyticsValue.Action.Profile_Photo);
    }

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
        overridePendingTransition(0, R.anim.alpha_on_exit);
    }

    @Click(R.id.btn_member_profile_star)
    public void star(View v) {
        if (!v.isEnabled()) {
            return;
        }
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

    private boolean hasChangedProfileImage(String url) {
        return !TextUtils.isEmpty(url) && url.contains("files-profile");
    }

    private void addButtons(final FormattedEntity member) {
        vgProfileTeamButtons.removeAllViews();

        final EntityManager entityManager = EntityManager.getInstance();

        if (isMe()) {
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
            final String userPhoneNumber = member.getUserPhoneNumber();
            if (!TextUtils.isEmpty(userPhoneNumber)) {
                vgProfileTeamButtons.addView(
                        getButton(R.drawable.icon_profile_mobile,
                                getString(R.string.jandi_member_profile_call), (v) -> {
                                    call(userPhoneNumber);
                                    AnalyticsUtil.sendEvent(getScreen(from), AnalyticsValue.Action.Profile_Cellphone);
                                }));
            }

            final String userEmail = member.getUserEmail();
            if (!TextUtils.isEmpty(userEmail)) {
                vgProfileTeamButtons.addView(
                        getButton(R.drawable.icon_profile_mail,
                                getString(R.string.jandi_member_profile_email), (v) -> {
                                    sendEmail(userEmail);
                                    AnalyticsUtil.sendEvent(getScreen(from), AnalyticsValue.Action.Profile_Email);
                                }));
            }

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

        boolean landscape = isLandscape();
        int width = landscape
                ? getResources().getDimensionPixelSize(R.dimen.jandi_member_profile_buttons_width)
                : 0;

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        if (!landscape) {
            params.weight = 1;
        }
        buttonView.setLayoutParams(params);

        return buttonView;
    }

    private void startModifyProfileActivity() {
        ModifyProfileActivity_.intent(MemberProfileActivity.this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .startForResult(ModifyProfileActivity.REQUEST_CODE);
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

    private boolean isMe() {
        return EntityManager.getInstance().isMe(memberId);
    }

    private boolean isEnableUser(String status) {
        return "enabled".equals(status);
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private AnalyticsValue.Screen getScreen(int from) {
        switch (from) {
            default:
            case EXTRA_FROM_TOPIC_CHAT:
                return AnalyticsValue.Screen.TopicChat;
            case EXTRA_FROM_MESSAGE:
                return AnalyticsValue.Screen.Message;
            case EXTRA_FROM_PARTICIPANT:
                return AnalyticsValue.Screen.Participants;
            case EXTRA_FROM_TEAM_MEMBER:
                return AnalyticsValue.Screen.TeamMembers;
            case EXTRA_FROM_FILE_DETAIL:
                return AnalyticsValue.Screen.FileDetail;
            case EXTRA_FROM_MAIN_CHAT:
                return AnalyticsValue.Screen.MessageTab;
        }
    }
}
