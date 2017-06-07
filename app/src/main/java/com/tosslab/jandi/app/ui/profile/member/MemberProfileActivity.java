package com.tosslab.jandi.app.ui.profile.member;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.team.MemberOnlineStatusChangeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.member.MemberApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.member.MemberInfo;
import com.tosslab.jandi.app.network.models.start.Absence;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.MypageTabInfo;
import com.tosslab.jandi.app.ui.profile.member.dagger.DaggerMemberProfileComponent;
import com.tosslab.jandi.app.ui.profile.member.model.InactivedMemberProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.JandiBotProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.MemberProfileLoader;
import com.tosslab.jandi.app.ui.profile.member.model.ProfileLoader;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.SwipeExitLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoView;

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

    @InjectExtra
    long memberId;

    @Nullable
    @InjectExtra
    int from;

    @Bind(R.id.v_background_color)
    View vBackgroundColor;
    @Bind(R.id.v_opacity_color)
    View vOpacityColor;
    @Bind(R.id.vg_swipe_exit_layout)
    SwipeExitLayout swipeExitLayout;
    @Bind(R.id.tv_member_profile_description)
    TextView tvProfileDescription;
    @Bind(R.id.tv_member_profile_name)
    TextView tvProfileName;
    @Bind(R.id.tv_member_profile_division)
    TextView tvProfileDivision;
    @Bind(R.id.tv_member_profile_position)
    TextView tvProfilePosition;
    @Bind(R.id.tv_member_profile_phone)
    TextView tvProfilePhone;
    @Bind(R.id.tv_member_profile_email)
    TextView tvProfileEmail;
    @Bind(R.id.tv_member_profile_team_level)
    TextView tvTeamLevel;
    @Bind(R.id.iv_member_profile_star_btn)
    ImageView ivMemberProfileStarBtn;
    @Bind(R.id.iv_member_profile_edit_btn)
    ImageView ivMemberProfileEditBtn;
    @Bind(R.id.iv_member_profile_img_full)
    PhotoView ivProfileImageFull;
    @Bind(R.id.iv_member_profile_img_small)
    ImageView ivProfileImageSmall;
    @Bind(R.id.vg_profile_resend_email)
    ViewGroup vgProfileResendEmail;
    @Bind(R.id.vg_profile_mobile)
    ViewGroup vgProfileMobile;
    @Bind(R.id.vg_profile_cancel_invitation)
    ViewGroup vgProfileCancelInvitation;
    @Bind(R.id.vg_profile_email)
    ViewGroup vgProfileEmail;
    @Bind(R.id.vg_profile_1_1_message)
    ViewGroup vgProfile1to1Message;
    @Bind(R.id.vg_profile_mentions)
    ViewGroup vgProfileMentions;
    @Bind(R.id.v_button_divider)
    View vButtonDivider;
    @Bind(R.id.v_start_bottom_line)
    @Nullable
    View vStartBottomLine;
    @Bind(R.id.v_online)
    View vOnline;
    @Bind(R.id.iv_member_profile_absence_img)
    ImageView ivMemberProfileAbsenceImg;
    @Bind(R.id.v_background_profile_absence)
    View vBackgroundProfileAbsence;
    @Bind(R.id.vg_absence_message_wrapper)
    ViewGroup vgAbsenceMessageWrapper;
    @Bind(R.id.tv_absence_duration)
    TextView tvAbsenceDuration;

    ProfileLoader profileLoader;

    @Inject
    EntityClientManager entityClientManager;
    @Inject
    Lazy<TeamApi> teamApi;
    @Inject
    Lazy<InvitationApi> invitationApi;

    @Inject
    Lazy<MemberApi> memberApi;

    private boolean isFullSizeImageShowing = false;
    private boolean hasChangedProfileImage = true;
    private ProgressWheel progressWheel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_bottom_with_alpha, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        setShouldSetOrientation(!BuildConfig.DEBUG);

        Dart.inject(this);
        ButterKnife.bind(this);

        if (!TeamInfoLoader.getInstance().isUser(memberId)
                && !TeamInfoLoader.getInstance().isJandiBot(memberId)) {
            finish();
            return;
        }

        DaggerMemberProfileComponent.create().inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        if (savedInstanceState != null) {
            isFullSizeImageShowing = savedInstanceState.getBoolean(KEY_FULL_SIZE_IMAGE_SHOWING);
        }
        setNeedUnLockPassCode(false);

        initObject();
        initViews();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FULL_SIZE_IMAGE_SHOWING, ivProfileImageFull.isShown());
    }

    void initObject() {
        boolean isBot = TeamInfoLoader.getInstance().isJandiBot(memberId);
        if (!isBot) {
            if (!TeamInfoLoader.getInstance().getUser(memberId).isInactive()) {
                profileLoader = new MemberProfileLoader();
                if (vStartBottomLine != null)
                    vStartBottomLine.setVisibility(View.VISIBLE);
            } else {
                profileLoader = new InactivedMemberProfileLoader();
                if (vStartBottomLine != null)
                    vStartBottomLine.setVisibility(View.INVISIBLE);
            }
        } else {
            profileLoader = new JandiBotProfileLoader();
            if (vStartBottomLine != null)
                vStartBottomLine.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ModifyProfileActivity.REQUEST_CODE) {
            initViews();
        }
    }

    void initViews() {

        if (TeamInfoLoader.getInstance().isBot(memberId)) {
            // 잔디봇이 아닌 봇은 예외 처리
            finish();
            return;
        }

        Member member = TeamInfoLoader.getInstance().getMember(memberId);
        final String profileImageUrl = member.getPhotoUrl();

        hasChangedProfileImage = profileLoader.hasChangedProfileImage(member);

        initSwipeLayout();

        profileLoader.setLevel(member instanceof User ? ((User) member).getLevel() : null, tvTeamLevel);
        profileLoader.setName(tvProfileName, member);
        profileLoader.setDescription(tvProfileDescription, member);
        profileLoader.setProfileInfo(tvProfileDivision, tvProfilePosition, member);
        profileLoader.loadSmallThumb(ivProfileImageSmall, member);
        profileLoader.loadFullThumb(ivProfileImageFull, profileImageUrl);
        profileLoader.setBackgroundColor(vBackgroundColor, vOpacityColor,
                member instanceof User ? ((User) member).getLevel() : null, member);

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

        boolean isDisableUser = !profileLoader.isEnabled(member);

        if (isDisableUser) {
            ViewTreeObserver vto = tvProfileName.getViewTreeObserver();
            final boolean[] isLoadDisableUserName = new boolean[1];
            isLoadDisableUserName[0] = true;
            ViewTreeObserver.OnGlobalLayoutListener listener = () -> {
                if (isLoadDisableUserName[0]) {
                    String ellipsizedText = "";

                    if (tvProfileName.getLayout().getText().toString()
                            .equals(tvProfileName.getText().toString())) {
                        ellipsizedText = tvProfileName.getText().toString();
                    } else {
                        ellipsizedText = tvProfileName.getLayout().getText().toString();
                        ellipsizedText = ellipsizedText.substring(0, ellipsizedText.length() - 7) + "...";
                    }

                    String ellipsizeTextWithSpace = ellipsizedText + "  ";
                    SpannableString ss = new SpannableString(ellipsizeTextWithSpace);
                    Drawable drawable = getResources().getDrawable(R.drawable.icon_profile_warning);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    ss.setSpan(span, ellipsizedText.length() + 1,
                            ellipsizedText.length() + 2,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvProfileName.setText(ss);
                    isLoadDisableUserName[0] = false;
                }
            };

            vto.addOnGlobalLayoutListener(listener);

            vgProfileMobile.setVisibility(View.INVISIBLE);
            vgProfileEmail.setVisibility(View.INVISIBLE);
            vgProfile1to1Message.setVisibility(View.INVISIBLE);

            ivMemberProfileStarBtn.setVisibility(View.GONE);
            ivMemberProfileEditBtn.setVisibility(View.GONE);
            vButtonDivider.setVisibility(View.GONE);
            return;
        }

        String userEmail = member.getEmail();
        tvProfileEmail.setText(userEmail);
        if (TextUtils.isEmpty(userEmail)) {
            tvProfileEmail.setText("-");
        }

        if (!isJandiBot(member)) {
            String userPhoneNumber = ((User) member).getPhoneNumber();
            if (TextUtils.isEmpty(userPhoneNumber)) {
                tvProfilePhone.setText("-");
            } else {
                tvProfilePhone.setText(userPhoneNumber);
            }
        }

        profileLoader.setStarButton(ivMemberProfileStarBtn, member, tvTeamLevel);

        addButtons(member);

        if (TeamInfoLoader.getInstance().getOnlineStatus().isOnlineMember(memberId)) {
            vOnline.setVisibility(View.VISIBLE);
        }

        AnalyticsUtil.sendScreenName(getScreen());

        setAbsenceInfo();
    }

    private void setAbsenceInfo() {
        Absence absence = TeamInfoLoader.getInstance().getUser(memberId).getAbsence();
        if (!TeamInfoLoader.getInstance().getUser(memberId).isDisabled() &&
                (absence != null && absence.getStartAt() != null)) {
            vBackgroundProfileAbsence.setVisibility(View.VISIBLE);
            ivMemberProfileAbsenceImg.setVisibility(View.VISIBLE);
            vgAbsenceMessageWrapper.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            sb.append(new SimpleDateFormat("yyyy.MM.dd").format(absence.getStartAt()));
            sb.append(" - ");
            sb.append(new SimpleDateFormat("yyyy.MM.dd").format(absence.getEndAt()));
            tvAbsenceDuration.setText(sb);
            AnimationDrawable drawable =
                    (AnimationDrawable) ivMemberProfileAbsenceImg.getDrawable();
            drawable.start();
            if (!TextUtils.isEmpty(absence.getMessage())) {
                tvProfileDescription.setText(absence.getMessage());
            }
        } else {
            vBackgroundProfileAbsence.setVisibility(View.GONE);
            ivMemberProfileAbsenceImg.setVisibility(View.INVISIBLE);
            vgAbsenceMessageWrapper.setVisibility(View.GONE);
        }
    }

    private boolean isJandiBot(Member member) {
        return TeamInfoLoader.getInstance().isJandiBot(member.getId());
    }

    private void initSwipeLayout() {
        swipeExitLayout.setOnExitListener(this::finish);

        swipeExitLayout.setStatusListener(new SwipeExitLayout.StatusListener() {
            @Override
            public void onTranslateY(float translateY) {
            }

            @Override
            public void onCancel(float spareDistance, int cancelAnimDuration) {
            }

            @Override
            public void onExit(float spareDistance, int exitAnimDuration) {
                Completable.complete()
                        .delay(exitAnimDuration, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            finish();
                        });
            }
        });
    }

    @OnClick(R.id.iv_member_profile_img_small)
    void showFullImage(View v) {
        if (!hasChangedProfileImage || TeamInfoLoader.getInstance().isJandiBot(memberId)) {
            return;
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

    @OnClick(R.id.iv_member_profile_close_btn)
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

    @OnClick(R.id.iv_member_profile_star_btn)
    public void star(View v) {
        if (!v.isEnabled()) {
            return;
        }
        boolean futureSelected = !v.isSelected();
        v.setSelected(futureSelected);
        postStar(futureSelected);

        if (futureSelected) {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Star, AnalyticsValue.Label.On);
        } else {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Star, AnalyticsValue.Label.Off);
        }
    }

    void postStar(boolean star) {
        Completable.fromCallable(() -> {
            if (star) {
                entityClientManager.enableFavorite(memberId);
            } else {
                entityClientManager.disableFavorite(memberId);
            }
            HumanRepository.getInstance().updateStarred(memberId, star);
            return true;
        }).subscribeOn(Schedulers.io()).subscribe(() -> {
        }, Throwable::printStackTrace);
    }

    private void addButtons(Member member) {
        if (!isJandiBot(member)
                && (TeamInfoLoader.getInstance().getMyLevel() == Level.Admin
                || TeamInfoLoader.getInstance().getMyLevel() == Level.Owner
                || isMe())) {
            ivMemberProfileEditBtn.setVisibility(View.VISIBLE);
            onEditButtonClick();
        } else {
            ivMemberProfileEditBtn.setVisibility(View.GONE);
        }

        if (isMe()) {
            vgProfileMobile.setVisibility(View.VISIBLE);
            User user = (User) member;
            String phoneNumber = user.getPhoneNumber();
            if (!TextUtils.isEmpty(phoneNumber)) {
                vgProfileMobile.setOnClickListener(v -> {
                    onPhoneNumberClick();
                });
            } else {
                vgProfileMobile.setOnClickListener(null);
            }
            final String userEmail = user.getEmail();
            tvProfileEmail.setText(userEmail);
            vgProfileEmail.setVisibility(View.VISIBLE);
            vgProfileMentions.setVisibility(View.VISIBLE);
            vgProfileMentions.setOnClickListener(v -> {
                onMentionButtonClick();
            });
        } else if (member.isInactive()) {
            vgProfileResendEmail.setVisibility(View.VISIBLE);
            vgProfileResendEmail.setOnClickListener(v -> {
                onResendInvitationButtonClick();
            });

            vgProfileCancelInvitation.setVisibility(View.VISIBLE);
            vgProfileCancelInvitation.setOnClickListener(v -> {
                showRejectInvitationAlert();
            });

            vgProfile1to1Message.setVisibility(View.VISIBLE);
            vgProfile1to1Message.setOnClickListener(v -> {
                onOneToOneButtonClick(member);
            });
        } else {
            if (!isJandiBot(member)) {
                User user = (User) member;
                String phoneNumber = user.getPhoneNumber();
                vgProfileMobile.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    vgProfileMobile.setOnClickListener(v -> {
                        onPhoneNumberClick();
                    });
                } else {
                    vgProfileMobile.setOnClickListener(null);
                }

                final String userEmail = user.getEmail();
                tvProfileEmail.setText(userEmail);
                vgProfileEmail.setVisibility(View.VISIBLE);
                vgProfileEmail.setOnClickListener(v -> sendEmail());
            }

            vgProfile1to1Message.setVisibility(View.VISIBLE);
            vgProfile1to1Message.setOnClickListener(v -> {
                onOneToOneButtonClick(member);
            });
        }
    }

    private void onOneToOneButtonClick(Member member) {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long entityId = member.getId();
        startMessageListActivity(teamId, entityId);
        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.DirectMessage);
    }

    private void onResendInvitationButtonClick() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UserProfile, AnalyticsValue.Action.ResendInvitation);
        requestReInvite();
    }

    private void onMentionButtonClick() {
        startStarMentionListActivity();
        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Mentions);
    }

    private void onEditButtonClick() {
        ivMemberProfileEditBtn.setOnClickListener(v ->
                startModifyProfileActivityForAdmin(memberId));
    }

    private void showRejectInvitationAlert() {
        String invitationStatus = TeamInfoLoader.getInstance().getInvitationStatus();
        String invitationUrl = TeamInfoLoader.getInstance().getInvitationUrl();
        boolean teamOwner = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).isTeamOwner();
        if (InviteDialogExecutor.canBeInviation(invitationStatus, invitationUrl) || teamOwner) {
            new AlertDialog.Builder(MemberProfileActivity.this)
                    .setMessage(R.string.jandi_r_u_sure_cancel_invitation)
                    .setNegativeButton(R.string.jandi_cancel, null)
                    .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UserProfile, AnalyticsValue.Action.CancelInvitation);
                        requestRejectUser();
                    })
                    .create()
                    .show();
        } else if (!teamOwner) {
            ColoredToast.showError(R.string.jandi_reject_to_invitation_of_admin);
        }
    }

    void requestRejectUser() {
        if (!TeamInfoLoader.getInstance().isUser(memberId)) {
            return;
        }

        String userEmail = TeamInfoLoader.getInstance().getUser(memberId).getEmail();
        long teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
        Completable.fromCallable(() -> {
            teamApi.get().cancelInviteTeam(teamId, memberId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    showSuccessToRejectEmail(userEmail);
                    finishOnUiThread();
                }, t -> {
                    showNetworkErrorToast();
                });

    }

    void finishOnUiThread() {
        finish();
    }

    void showNetworkErrorToast() {
        ColoredToast.showError(R.string.err_network);
    }

    void showSuccessToRejectEmail(String userEmail) {
        ColoredToast.show(getString(R.string.jandi_success_to_cancel_invitation, userEmail));
    }

    void requestReInvite() {

        if (TeamInfoLoader.getInstance().isJandiBot(memberId)) {
            return;
        }

        showProgress();

        Completable.fromCallable(() -> {
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            User user = TeamInfoLoader.getInstance().getUser(memberId);
            List<String> invites = Arrays.asList(user.getEmail());
            int level = user.getLevel().getLevel();
            long topicId = -1;
            if (level == Level.Guest.getLevel()) {
                MemberInfo memberInfo = memberApi.get().getMemberInfo(teamId, memberId);
                topicId = memberInfo.getJoinTopics().get(0);
            }
            teamApi.get().inviteToTeam(teamId, new ReqInvitationMembers(teamId, invites, LanguageUtil.getLanguage(), level, topicId));
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    dismissProgress();
                    showSuccessReinvite();
                }, t -> {
                    dismissProgress();
                    showNetworkErrorToast();
                });


    }

    void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    void showProgress() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(MemberProfileActivity.this);
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    void showSuccessReinvite() {
        new AlertDialog.Builder(MemberProfileActivity.this)
                .setMessage(R.string.jandi_another_invitation_sent)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    void onPhoneNumberClick() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UserProfile, AnalyticsValue.Action.TapPhoneNumber);
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
                            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Call);
                            break;
                    }
                })
                .create()
                .show();
    }

    private void addToContacts() {
        try {
            if (!TeamInfoLoader.getInstance().isUser(memberId)) {
                return;
            }
            User entity = TeamInfoLoader.getInstance().getUser(memberId);
            String name = entity.getName();
            String phoneNumber = entity.getPhoneNumber();
            String userEmail = entity.getEmail();

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
            if (!TeamInfoLoader.getInstance().isUser(memberId)) {
                return;
            }
            String userPhoneNumber = TeamInfoLoader.getInstance()
                    .getUser(memberId).getPhoneNumber();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, userPhoneNumber));
            ColoredToast.show(R.string.jandi_copied_to_clipboard);
        } catch (Exception ignored) {
        }
    }

    void callIfHasPermission() {
        Permissions.getChecker()
                .activity(MemberProfileActivity.this)
                .permission(() -> Manifest.permission.CALL_PHONE)
                .hasPermission(() -> {
                    if (!TeamInfoLoader.getInstance().isUser(memberId)) {
                        return;
                    }
                    call(TeamInfoLoader.getInstance().getUser(memberId).getPhoneNumber());
                })
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.CALL_PHONE};
                    ActivityCompat.requestPermissions(MemberProfileActivity.this,
                            permissions,
                            REQ_CALL_PERMISSION);
                })
                .check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(MemberProfileActivity.this)
                .addRequestCode(REQ_CALL_PERMISSION)
                .addPermission(Manifest.permission.CALL_PHONE, () -> {
                    if (!TeamInfoLoader.getInstance().isUser(memberId)) {
                        return;
                    }
                    User member = TeamInfoLoader.getInstance().getUser(memberId);
                    call(member.getPhoneNumber());
                    AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.Profile_Cellphone);
                })
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showCallPermissionDialog(MemberProfileActivity.this);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode, permissions, grantResults));
    }

    private void startModifyProfileActivityForAdmin(long memberId) {
        startActivityForResult(Henson.with(this)
                .gotoModifyProfileActivity()
                .adminMode(true)
                .memberId(memberId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), ModifyProfileActivity.REQUEST_CODE);
    }

    private void startStarMentionListActivity() {
        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .tabIndex(MypageTabInfo.INDEX)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @OnClick(R.id.tv_member_profile_email)
    void onEmailClick() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.UserProfile, AnalyticsValue.Action.TapEmailAddress);
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
            if (!TeamInfoLoader.getInstance().isUser(memberId)) {
                return;
            }
            String userPhoneNumber = TeamInfoLoader.getInstance().getUser(memberId).getEmail();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, userPhoneNumber));
            ColoredToast.show(R.string.jandi_copied_to_clipboard);
        } catch (Exception ignored) {
        }
    }

    void sendEmail() {
        if (!TeamInfoLoader.getInstance().isUser(memberId)) {
            return;
        }
        String userEmail = TeamInfoLoader.getInstance().getUser(memberId).getEmail();
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

    private void startMessageListActivity(long teamId, long entityId) {
        startActivity(Henson.with(MemberProfileActivity.this)
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(-1)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private boolean isMe() {
        return TeamInfoLoader.getInstance().getMyId() == memberId;
    }

    private AnalyticsValue.Screen getScreen() {
        if (isMe()) {
            return AnalyticsValue.Screen.MyProfile;
        } else {
            return AnalyticsValue.Screen.UserProfile;
        }
    }

    public void onEventMainThread(MemberOnlineStatusChangeEvent event) {
        if (event.getMemberId() == memberId) {
            if (event.getPresence().equals("online")) {
                vOnline.setVisibility(View.VISIBLE);
            } else {
                vOnline.setVisibility(View.GONE);
            }
        }
    }

    public void onEventMainThread(ProfileChangeEvent event) {
        if (event.getMember().getId() == memberId) {
            setAbsenceInfo();
        }
    }
}
