package com.tosslab.jandi.app.ui.profile.member;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.model.InactivedMemberProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.JandiBotProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.ProfileLoader;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.transform.fresco.BlurPostprocessor;
import com.tosslab.jandi.app.views.SwipeExitLayout;

import org.androidannotations.annotations.AfterInject;
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
    private static final int REQ_CALL_PERMISSION = 102;
    @Extra
    long memberId;

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
    SimpleDraweeView ivProfileImageLarge;
    @ViewById(R.id.iv_member_profile_img_small)
    SimpleDraweeView ivProfileImageSmall;

    ProfileLoader profileLoader;

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
        setNeedUnLockPassCode(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FULL_SIZE_IMAGE_SHOWING, ivProfileImageFull.isShown());
    }

    @AfterInject
    void initObject() {
        boolean isBot = EntityManager.getInstance().isBot(memberId);
        if (!isBot) {
            if (!EntityManager.getInstance().getEntityById(memberId).isInavtived()) {
                profileLoader = new MemberProfileLoader();
            } else {
                profileLoader = new InactivedMemberProfileLoader();
            }
        } else {
            profileLoader = new JandiBotProfileLoader();
        }
    }

    @OnActivityResult(ModifyProfileActivity.REQUEST_CODE)
    @AfterViews
    void initViews() {

        if (EntityManager.getInstance().isBot(memberId)
                && !EntityManager.getInstance().isJandiBot(memberId)) {
            // 잔디봇이 아닌 봇은 예외 처리
            finish();
            return;
        }

        FormattedEntity member = EntityManager.getInstance().getEntityById(memberId);
        final String profileImageUrlLarge = member.getUserLargeProfileUrl();

        hasChangedProfileImage = profileLoader.hasChangedProfileImage(member);

        initSwipeLayout(hasChangedProfileImage);

        if (!hasChangedProfileImage) {
            profileLoader.setBlurBackgroundColor(vProfileImageLargeOverlay);
        }

        initLargeImageSize(profileImageUrlLarge);

        boolean isDisableUser = !profileLoader.isEnabled(member);
        vDisableIcon.setVisibility(isDisableUser ? View.VISIBLE : View.GONE);

        profileLoader.setName(tvProfileName, member);
        profileLoader.setDescription(tvProfileDescription, member);
        profileLoader.setProfileInfo(vgProfileTeamInfo, tvProfileDivision, tvProfilePosition, member);
        profileLoader.loadSmallThumb(ivProfileImageSmall, member);
        profileLoader.loadFullThumb(ivProfileImageFull, profileImageUrlLarge);

        ivProfileImageFull.setOnViewTapListener((view, x, y) -> {
            ivProfileImageFull.setScale(1.0f, true);
            hideFullImage(view);
        });

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

        profileLoader.setStarButton(btnProfileStar, member);

        addButtons(member);

        AnalyticsUtil.sendScreenName(getScreen());
    }

    private boolean isBot(FormattedEntity member) {
        return member instanceof BotEntity;
    }

    private void initSwipeLayout(boolean setViewToAlpha) {
        swipeExitLayout.setOnExitListener(this::finish);
        if (setViewToAlpha) {
            swipeExitLayout.setViewToAlpha(vProfileImageLargeOverlay);
        }

        swipeExitLayout.setStatusListener(new SwipeExitLayout.StatusListener() {
            @Override
            public void onTranslateY(float translateY) {
                int measuredWidth = vgProfileImageLarge.getMeasuredWidth();
                int measuredHeight = vgProfileImageLarge.getMeasuredHeight();

                float scaleX = (measuredWidth + (translateY * 2)) / measuredWidth;
                scaleX = Math.max(1, scaleX);

                float scaleY = (measuredHeight + (translateY * 2)) / measuredHeight;
                scaleY = Math.max(1, scaleY);

                vgProfileImageLarge.setScaleX(scaleX);
                vgProfileImageLarge.setScaleY(scaleY);
            }

            @Override
            public void onCancel(float spareDistance, int cancelAnimDuration) {
                vgProfileImageLarge.animate()
                        .setDuration(cancelAnimDuration)
                        .scaleX(1)
                        .scaleY(1);
            }

            @Override
            public void onExit(float spareDistance, int exitAnimDuration) {
                int measuredWidth = vgProfileImageLarge.getMeasuredWidth();
                int measuredHeight = vgProfileImageLarge.getMeasuredHeight();

                float ratio = (measuredWidth / (float) measuredHeight);

                int spareBottom = swipeExitLayout.getMeasuredHeight() - measuredHeight;

                float scaleY = (measuredHeight + spareBottom) / (float) measuredHeight;

                vgProfileImageLarge.animate()
                        .scaleX(scaleY * ratio)
                        .scaleY(scaleY)
                        .setDuration(exitAnimDuration);
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
        if (!hasChangedProfileImage) {
            return;
        }

        int defaultColor = getResources().getColor(R.color.jandi_member_profile_img_overlay_default);
        Drawable placeHolder = new ColorDrawable(defaultColor);

        GenericDraweeHierarchy hierarchy = ivProfileImageLarge.getHierarchy();
        hierarchy.setPlaceholderImage(placeHolder);
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);

        ImageRequest imageRequest =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(profileImageUrlLarge))
                        .setPostprocessor(new BlurPostprocessor())
                        .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(ivProfileImageLarge.getController())
                .build();

        ivProfileImageLarge.setController(controller);
    }

    @Click(R.id.iv_member_profile_img_small)
    void showFullImage(View v) {
        if (!hasChangedProfileImage || isBot(EntityManager.getInstance().getEntityById(memberId))) {
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

        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.ShowProfilePicture);
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

        super.onBackPressed();
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

        if (futureSelected) {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.TurnOnStar);
        } else {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.TurnOffStar);
        }
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

    private void addButtons(final FormattedEntity member) {
        vgProfileTeamButtons.removeAllViews();

        final EntityManager entityManager = EntityManager.getInstance();

        if (isMe()) {
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_edit,
                            getString(R.string.jandi_member_profile_edit), (v) -> {
                                startModifyProfileActivity();
                                AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.EditProfile);
                            }));

            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_mention,
                            getString(R.string.jandi_mention_mentions), (v) -> {
                                startStarMentionListActivity();
                                AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Mentions);
                            }));
        } else if (member.isInavtived()) {
            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_mail,
                            getString(R.string.jandi_resend_invitation),
                            v -> {

                            }));

            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_cancel,
                            getString(R.string.jandi_cancel_invitation),
                            v -> {

                            }));
        } else {
            final String userPhoneNumber = member.getUserPhoneNumber();
            if (!TextUtils.isEmpty(userPhoneNumber)) {
                vgProfileTeamButtons.addView(
                        getButton(R.drawable.icon_profile_mobile,
                                getString(R.string.jandi_member_profile_call), v -> callIfHasPermission()));
            }

            final String userEmail = member.getUserEmail();
            if (!TextUtils.isEmpty(userEmail)) {
                vgProfileTeamButtons.addView(
                        getButton(R.drawable.icon_profile_mail,
                                getString(R.string.jandi_member_profile_email), (v) -> sendEmail()));
            }

            vgProfileTeamButtons.addView(
                    getButton(R.drawable.icon_profile_message,
                            getString(R.string.jandi_member_profile_dm), (v) -> {
                                long teamId = entityManager.getTeamId();
                                long entityId = member.getId();
                                boolean isStarred = member.isStarred;
                                startMessageListActivity(teamId, entityId, isStarred);
                                AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.DirectMessage);
                            }));
        }
    }

    @Click(R.id.tv_member_profile_phone)
    void onPhoneNumberClick() {
        new AlertDialog.Builder(MemberProfileActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setItems(R.array.jandi_profile_tel_actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            copyPhoneToClipboard();
                            break;
                        case 1:
                            addToContacts();
                            break;
                        default:
                        case 2:
                            callIfHasPermission();
                            break;
                    }
                })
                .create()
                .show();
    }

    private void addToContacts() {
        try {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(memberId);
            String name = entity.getName();
            String phoneNumber = entity.getUserPhoneNumber();
            String userEmail = entity.getUserEmail();

            Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
            insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, name)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
                    .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .putExtra(ContactsContract.Intents.Insert.EMAIL, userEmail)
                    .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
            startActivity(insertIntent);
        } catch (Exception ignored) {
        }
    }

    private void copyPhoneToClipboard() {
        try {
            String userPhoneNumber = EntityManager.getInstance()
                    .getEntityById(memberId)
                    .getUserPhoneNumber();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, userPhoneNumber));
            ColoredToast.show(R.string.jandi_copied_to_clipboard);
        } catch (Exception ignored) {
        }
    }

    void callIfHasPermission() {
        Permissions.getChecker()
                .permission(() -> Manifest.permission.CALL_PHONE)
                .hasPermission(() -> {
                    call(EntityManager.getInstance().getEntityById(memberId).getUserPhoneNumber());
                    AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Profile_Cellphone);
                })
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.CALL_PHONE};
                    requestPermissions(permissions, REQ_CALL_PERMISSION);
                })
                .check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQ_CALL_PERMISSION)
                .addPermission(Manifest.permission.CALL_PHONE, () -> {
                    FormattedEntity member = EntityManager.getInstance().getEntityById(memberId);
                    call(member.getUserPhoneNumber());
                    AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Profile_Cellphone);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode, permissions, grantResults));
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

    @Click(R.id.tv_member_profile_email)
    void onEmailClick() {
        new AlertDialog.Builder(MemberProfileActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setItems(R.array.jandi_profile_email_actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            copyEmailToClipboard();
                            break;
                        default:
                        case 1:
                            sendEmail();
                            break;
                    }
                })
                .create()
                .show();
    }

    private void copyEmailToClipboard() {
        try {
            String userPhoneNumber = EntityManager.getInstance()
                    .getEntityById(memberId)
                    .getUserEmail();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, userPhoneNumber));
            ColoredToast.show(R.string.jandi_copied_to_clipboard);
        } catch (Exception ignored) {
        }
    }

    void sendEmail() {
        String userEmail = EntityManager.getInstance().getEntityById(memberId).getUserEmail();
        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Email);
        if (TextUtils.isEmpty(userEmail)) {
            return;
        }
        String uri = "mailto:" + userEmail;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
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

    private void startMessageListActivity(long teamId, long entityId, boolean isStarred) {
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

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private AnalyticsValue.Screen getScreen() {
        if (isMe()) {
            return AnalyticsValue.Screen.MyProfile;
        } else {
            return AnalyticsValue.Screen.UserProfile;
        }
    }
}
