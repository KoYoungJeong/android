package com.tosslab.jandi.app.ui.maintab.more;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.IconWithTextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    protected static final String SUPPORT_URL_KO = "https://jandi.zendesk.com/hc/ko";
    protected static final String SUPPORT_URL_JA = "https://jandi.zendesk.com/hc/ja";
    protected static final String SUPPORT_URL_ZH_CH = "https://jandi.zendesk.com/hc/zh-cn";
    protected static final String SUPPORT_URL_ZH_TW = "https://jandi.zendesk.com/hc/zh-tw";
    protected static final String SUPPORT_URL_EN = "https://jandi.zendesk.com/hc/en-us";

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

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @ViewById(R.id.tv_more_jandi_version)
    TextView textViewJandiVersion;

    @ViewById(R.id.btn_update_version)
    View btnUpdateVersion;

    private EntityManager mEntityManager;

    @AfterInject
    void init() {
        mContext = getActivity();
        mEntityManager = EntityManager.getInstance();
    }

    @AfterViews
    void initView() {
        showJandiVersion();
        showOtherTeamMessageCount();
        showTeamMember();
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

    private void showTeamMember() {
        String teamMember = getString(R.string.jandi_team_member);
        int teamMemberCount = getEnabledUserCount();
        String fullTeamMemberText = String.format("%s\n(%d)", teamMember, teamMemberCount);
        vTeamMember.setIconText(fullTeamMemberText);
    }

    private int getEnabledUserCount() {
        List<FormattedEntity> formattedUsers = EntityManager.getInstance().getFormattedUsers();
        int enabledUserCount = Observable.from(formattedUsers)
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .count()
                .toBlocking()
                .firstOrDefault(0);
        return enabledUserCount;
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
        showUserProfile();
    }

    private void showUserProfile() {
        if (mEntityManager != null) {
            FormattedEntity me = mEntityManager.getMe();

            SimpleDraweeView imageView = profileIconView.getImageView();
            Uri uri = Uri.parse(me.getUserSmallProfileUrl());

            ImageUtil.loadCircleImageByFresco(imageView, uri, R.drawable.profile_img);
        }
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
        showOtherTeamMessageCount();
    }

    public void showOtherTeamMessageCount() {
        AccountRepository accountRepository = AccountRepository.getRepository();
        int selectedTeamId = accountRepository.getSelectedTeamId();
        final int badgeCount[] = {0};
        Observable.from(accountRepository.getAccountTeams())
                .filter(userTeam -> userTeam.getTeamId() != selectedTeamId)
                .subscribe(userTeam -> {
                    badgeCount[0] += userTeam.getUnread();
                    BadgeCountRepository.getRepository()
                            .upsertBadgeCount(userTeam.getTeamId(), userTeam.getUnread());
                });

        BadgeUtils.setBadge(getActivity(), BadgeCountRepository.getRepository().getTotalBadgeCount());
        vSwitchTeam.setBadgeCount(badgeCount[0]);
    }

    private void showJandiVersion() {
        try {
            String packageName = getActivity().getPackageName();
            String versionName = getActivity().getPackageManager().getPackageInfo(packageName, 0).versionName;
            textViewJandiVersion.setText(String.format("(v%s)", versionName));
            configVersionButton();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Background
    void configVersionButton() {

        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        int currentVersion = getInstalledAppVersion();
        try {
            int latestVersion = getConfigInfo().latestVersions.android;
            if (currentVersion < latestVersion) {
                setVersionButtonVisibility(View.VISIBLE);
            } else {
                setVersionButtonVisibility(View.GONE);
            }
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setVersionButtonVisibility(int visibility) {
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
        InternalWebActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .url(getSupportUrlEachLanguage())
                .hideActionBar(true)
                .helpSite(true)
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoreTab, AnalyticsValue.Action.Help);
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

    String getSupportUrlEachLanguage() {
        String language = LanguageUtil.getLanguage(mContext);
        String supportUrl;
        if (TextUtils.equals(language, LanguageUtil.LANG_KO)) {
            supportUrl = SUPPORT_URL_KO;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_JA)) {
            supportUrl = SUPPORT_URL_EN; //일본어 컨텐츠가 없어서 영어버전 사용
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_CN)) {
            supportUrl = SUPPORT_URL_ZH_CH;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_TW)) {
            supportUrl = SUPPORT_URL_ZH_TW;
        } else {
            supportUrl = SUPPORT_URL_EN;
        }
        return supportUrl;
    }

    public ResConfig getConfigInfo() throws RetrofitError {
        return RequestApiManager.getInstance().getConfigByMainRest();
    }

    public int getInstalledAppVersion() {
        try {
            Context context = JandiApplication.getContext();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    @Click(R.id.btn_update_version)
    void onClickUpdateVersion() {
        final String appPackageName = JandiApplication.getContext().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        } finally {
            getActivity().finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
        }
    }
}