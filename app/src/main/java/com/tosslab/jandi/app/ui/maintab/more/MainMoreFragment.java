package com.tosslab.jandi.app.ui.maintab.more;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
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
import com.tosslab.jandi.app.ui.profile.modify.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.views.IconWithTextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

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

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @ViewById(R.id.tv_more_jandi_version)
    TextView textViewJandiVersion;

    @ViewById(R.id.bt_update_version)
    Button btUpdateVersion;

    private EntityManager mEntityManager;

    @AfterInject
    void init() {
        LogUtil.d("MainMoreFragment");
        mContext = getActivity();
        mEntityManager = EntityManager.getInstance();
    }

    @AfterViews
    void initView() {
        LogUtil.d("initView MainMoreFragment");
        showJandiVersion();
        showOtherTeamMessageCount();
        showTeamMember();
        Observable.just(1)
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> initTextLine());
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
                    .subscribe(s -> {
                        views[finalIdx].setIconText(views[finalIdx].getText() + s);
                    });
        }


    }

    private void showTeamMember() {
        String teamMember = getString(R.string.jandi_team_member);
        int teamMemberCount = EntityManager.getInstance().getFormattedUsers().size();
        String fullTeamMemberText = String.format("%s\n(%d)", teamMember, teamMemberCount);
        vTeamMember.setIconText(fullTeamMemberText);
    }

    @Override
    public void onResume() {
        LogUtil.d("MainMoreFragment onResume");
        super.onResume();
        showUserProfile();
    }

    private void showUserProfile() {
        if (mEntityManager != null) {
            FormattedEntity me = mEntityManager.getMe();
            Ion.with(profileIconView.getImageView())
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .transform(new IonCircleTransform())
                    .load(me.getUserSmallProfileUrl());
        }
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
            textViewJandiVersion.setText("(v." + versionName + ")");
            configVersionButton();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Background
    void configVersionButton() {
        int currentVersion = getInstalledAppVersion();
        int latestVersion = getConfigInfo().latestVersions.android;
        if (currentVersion < latestVersion) {
            setVersionButtonVisibility(View.VISIBLE);
        } else {
            setVersionButtonVisibility(View.GONE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setVersionButtonVisibility(int visibility) {
        btUpdateVersion.setVisibility(visibility);
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

    @Click(R.id.bt_update_version)
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