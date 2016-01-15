package com.tosslab.jandi.app.ui.maintab.more.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.maintab.more.domain.VersionClickedInfo;
import com.tosslab.jandi.app.ui.maintab.more.presenter.MainMorePresenter;
import com.tosslab.jandi.app.ui.maintab.more.presenter.MainMorePresenterImpl;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.settings.main.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.views.IconWithTextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment implements MainMorePresenter.View {

    protected Context mContext;

    @ViewById(R.id.ly_more_profile)
    IconWithTextView profileIconView;

    @ViewById(R.id.ly_more_go_to_main)
    IconWithTextView vSwitchTeam;

    @ViewById(R.id.ly_more_invite)
    IconWithTextView vInvite;
    @ViewById(R.id.ly_more_team_member)
    IconWithTextView vTeamMember;

    @ViewById(R.id.vg_more_bottom_wrapper)
    FrameLayout vgMoreBottomWrapper;
    @ViewById(R.id.tv_more_additional_text)
    TextView tvMoreAdditionalText;
    @ViewById(R.id.iv_more_additional_image)
    ImageView ivMoreAdditionalImage;
    @ViewById(R.id.iv_more_additional_image_cover)
    ImageView ivMoreAdditionalImageCover;

    @ViewById(R.id.tv_version_title)
    TextView textViewJandiVersion;

    @ViewById(R.id.btn_update_version)
    View btnUpdateVersion;

    @Bean(MainMorePresenterImpl.class)
    MainMorePresenter mainMorePresenter;

    private VersionClickedInfo versionClickedInfo;

    @AfterInject
    void init() {
        mContext = getActivity();
        mainMorePresenter.setView(this);
        versionClickedInfo = new VersionClickedInfo();
    }

    @AfterViews
    void initView() {
        mainMorePresenter.onShowJandiVersion();
        mainMorePresenter.onShowOtherTeamMessageCount();
        mainMorePresenter.onShowTeamMember();
        Observable.just(1)
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> initTextLine(), Throwable::printStackTrace);

        setEasterEgg();
    }

    private void initTextLine() {

        int maxTextLine = Integer.MIN_VALUE;

        IconWithTextView[] views = {vTeamMember, vSwitchTeam, vInvite};
        int size = views.length;


        for (int idx = 0; idx < size; idx++) {
            maxTextLine = Math.max(views[idx].getTextLine(), maxTextLine);
        }

        for (int idx = 0; idx < size; idx++) {
            int textLine = views[idx].getTextLine();
            final int finalIdx = idx;
            Observable.range(0, maxTextLine - textLine)
                    .map(integer -> "\n")
                    .subscribe(s -> views[finalIdx].setIconText(views[finalIdx].getText() + s),
                            Throwable::printStackTrace);
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainMorePresenter.onShowUserProfile();
    }

    @Override
    public void showUserProfile(Uri uri) {
        SimpleDraweeView imageView = profileIconView.getImageView();
        ImageUtil.loadProfileImage(imageView, uri, R.drawable.profile_img);
    }

    private void setEasterEgg() {
        StringBuilder sb = new StringBuilder();

        String line1 = getTextWithSpace("MERRY");
        String line2 = getTextWithSpace("CHRISTMAS");

        if (shouldShowHappyNewYear()) {
            line1 = getTextWithSpace("HAPPY");
            line2 = getTextWithSpace("NEWYEAR");
        }

        sb.append(line1).append("\n");
        sb.append(line2);

        tvMoreAdditionalText.setText(sb.toString());
        ivMoreAdditionalImage.setImageResource(R.drawable.christmas_tree);
        ivMoreAdditionalImageCover.setImageResource(R.drawable.christmas_tree_longtap);
        ivMoreAdditionalImageCover.setAlpha(0.0f);

        vgMoreBottomWrapper.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    long downTime = event.getDownTime();
                    long eventTime = event.getEventTime();
                    long gap = eventTime - downTime;
                    if (gap > 30) {
                        ivMoreAdditionalImageCover.animate()
                                .alpha(1.0f)
                                .setDuration(1000);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ivMoreAdditionalImageCover.animate()
                            .alpha(0.0f)
                            .setDuration(1000);
                    break;
            }
            return true;
        });
    }

    private boolean shouldShowHappyNewYear() {
        long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.DECEMBER, 28);
        Date shouldShowHappyNewYearDate = calendar.getTime();

        long shouldShowHappyNewYearTime = shouldShowHappyNewYearDate.getTime();


        return currentTime >= shouldShowHappyNewYearTime;
    }

    private String getTextWithSpace(String text) {
        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
            if (i < chars.length - 1) {
                sb.append("  ");
            }
        }
        return sb.toString();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    public void onEventMainThread(MessageOfOtherTeamEvent event) {
        mainMorePresenter.onShowOtherTeamMessageCount();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setVersionButtonVisibility(int visibility) {
        btnUpdateVersion.setVisibility(visibility);
    }

    @Click(R.id.ly_more_profile)
    public void moveToProfileActivity() {
        ModifyProfileActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.EditProfile);
    }

    @Click(R.id.ly_more_team_member)
    public void moveToTeamMemberActivity() {
        MembersListActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .type(MembersListActivity.TYPE_MEMBERS_LIST_TEAM)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.TeamMembers);
    }

    @Click(R.id.ly_more_invite)
    public void onInvitationDisableCheck() {
        EventBus.getDefault().post(new InvitationDisableCheckEvent());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.InviteMember);
    }

    @Click(R.id.ly_more_go_to_main)
    public void moveToAccountActivity() {
        AccountHomeActivity_.intent(mContext)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.TeamSwitch);
    }

    @Click(R.id.rl_more_setting)
    public void moveToSettingActivity() {
        SettingsActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.Setting);
    }

    @Click(R.id.rl_more_help)
    public void launchHelpPageOnBrowser() {
        mainMorePresenter.onLaunchHelpPage();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.Help);
    }

    @Override
    public void launchHelpPageOnBrowser(String supportUrl) {
        InternalWebActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .url(supportUrl)
                .hideActionBar(true)
                .helpSite(true)
                .start();
    }

    @Override
    public void setLatestVersion(int latestVersionCode) {
        btnUpdateVersion.setTag(latestVersionCode);
    }

    @Override
    public void showBugReportDialog(SpannableStringBuilder userInfoSpans) {

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(userInfoSpans)
                .setTitle("Jandi Usage Information")
                .setNegativeButton(R.string.jandi_close, null)
                .setPositiveButton(R.string.jandi_send_to_email, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@tosslab.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Jandi Usage Information");
                    intent.putExtra(Intent.EXTRA_TEXT, userInfoSpans.toString());
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).create();
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.height = getResources().getDisplayMetrics().heightPixels * 2 / 3;
        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.show();

    }

    @Click(R.id.ly_more_mentioned)
    public void launchHelpPageOnMentioned() {
        StarMentionListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .extra("type", StarMentionListActivity.TYPE_MENTION_LIST)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.Mentions);
    }


    @Click(R.id.ly_more_starred)
    public void launchHelpPageOnStarred() {
        StarMentionListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .extra("type", StarMentionListActivity.TYPE_STAR_LIST)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.Stars);
    }

    @Click(R.id.tv_version_title)
    void onClickUserInfoReport() {
        mainMorePresenter.onReportUserInfo(versionClickedInfo);
    }

    @Click(R.id.btn_update_version)
    void onClickUpdateVersion() {
        final String appPackageName = JandiApplication.getContext().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            int latestVersion = btnUpdateVersion.getTag() != null ? (int) btnUpdateVersion.getTag() : -1;
            AlertUtil.showChooseUpdateWebsiteDialog(getActivity(), appPackageName, latestVersion);
        } finally {
            getActivity().finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setJandiVersion(String version) {
        textViewJandiVersion.setText(String.format("Version%s", version));
    }

    @Override
    public void setOtherTeamBadgeCount(int badgeCount) {
        vSwitchTeam.setBadgeCount(badgeCount);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setMemberTextWithCount(String fullTeamMemberText) {
        vTeamMember.setIconText(fullTeamMemberText);
    }

}