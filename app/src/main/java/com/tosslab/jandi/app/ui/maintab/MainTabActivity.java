package com.tosslab.jandi.app.ui.maintab;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.events.ServiceMaintenanceEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.intro.viewmodel.IntroActivityViewModel;
import com.tosslab.jandi.app.ui.intro.viewmodel.IntroActivityViewModel_;
import com.tosslab.jandi.app.ui.invites.InviteActivity_;
import com.tosslab.jandi.app.ui.invites.InviteUtils;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.PagerSlidingTabStrip;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseAnalyticsActivity {
    private final Logger log = Logger.getLogger(MainTabActivity.class);

    @Bean
    JandiEntityClient mJandiEntityClient;

    private ProgressWheel mProgressWheel;
    private Context mContext;
    private EntityManager mEntityManager;
    private MainTabPagerAdapter mMainTabPagerAdapter;
    private ViewPager mViewPager;

    private boolean isFirst = true;    // poor implementation

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    private String invitationUrl;
    private String teamName;

    @AfterViews
    void initView() {
        mContext = getApplicationContext();
        mEntityManager = EntityManager.getInstance(MainTabActivity.this);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(MainTabActivity.this).getSelectedTeamInfo();

        setupActionBar(selectedTeamInfo.getName());

        // ViewPager
        View[] tabViews = new View[4];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_more, null);
        mMainTabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabViews);
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        mViewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mMainTabPagerAdapter);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(mViewPager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                log.debug("onPageSelected at " + position);
                trackGaTab(mEntityManager, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        if (needInvitePopup()) {
            JandiPreference.setInvitePopup(MainTabActivity.this);
            showInvitePopup();
        }

        JandiSocketService.startSocketServiceIfStop(MainTabActivity.this);
    }

    private void showInvitePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabActivity.this);
        View view = LayoutInflater.from(MainTabActivity.this).inflate(R.layout.dialog_invite_popup, null);

        final AlertDialog materialDialog = builder.setView(view)
                .show();

        view.findViewById(R.id.btn_invitation_popup_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                onInvitationDisableCheck();
//                InviteActivity_.intent(MainTabActivity.this).start();
            }
        });

        view.findViewById(R.id.btn_invitation_popup_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
            }
        });

    }

    private boolean needInvitePopup() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance(MainTabActivity.this).getFormattedUsersWithoutMe();
        return JandiPreference.isInvitePopup(MainTabActivity.this) && (formattedUsersWithoutMe == null || formattedUsersWithoutMe.isEmpty());
    }

    private void setupActionBar(String teamName) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(teamName);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Entity의 리스트를 획득하여 저장한다.
        EventBus.getDefault().register(this);
        getEntities();

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(MainTabActivity.this).getSelectedTeamInfo();
        setupActionBar(selectedTeamInfo.getName());

    }

    /**
     * *********************************************************
     * Entities List Update / Refresh
     * **********************************************************
     */

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @UiThread
    public void getEntities() {
        getEntitiesInBackground();
    }

    @Background
    public void getEntitiesInBackground() {
        try {
            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(MainTabActivity.this).upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            BadgeUtils.setBadge(MainTabActivity.this, totalUnreadCount);
            JandiPreference.setBadgeCount(MainTabActivity.this, totalUnreadCount);
            EntityManager.getInstance(MainTabActivity.this).refreshEntity(resLeftSideMenu);
            getEntitiesSucceed(resLeftSideMenu);
        } catch (JandiNetworkException e) {
            log.error(e.getErrorInfo() + "get entity failed", e);
            if (e.httpStatusCode == HttpStatus.UNAUTHORIZED.value()) {
                getEntitiesFailed(getString(R.string.err_expired_session));
                stopJandiServiceInMainThread();
            } else if (e.httpStatusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                EventBus.getDefault().post(new ServiceMaintenanceEvent());
            } else {
                getEntitiesFailed(getString(R.string.err_service_connection));
            }
        } catch (ResourceAccessException e) {
            log.error("connect failed", e);
            getEntitiesFailed(getString(R.string.err_service_connection));
        } catch (Exception e) {
            getEntitiesFailed(getString(R.string.err_service_connection));
        }
    }

    @UiThread
    void stopJandiServiceInMainThread() {
        JandiSocketService.stopSocketServiceIfRunning(MainTabActivity.this);

    }

    @UiThread
    public void getEntitiesSucceed(ResLeftSideMenu resLeftSideMenu) {
        mProgressWheel.dismiss();
        mEntityManager = EntityManager.getInstance(MainTabActivity.this);
        mEntityManager.subscribeChannelForParse();
        trackSigningIn(mEntityManager);
        getSupportActionBar().setTitle(mEntityManager.getTeamName());
        JandiPreference.setMyEntityId(this, mEntityManager.getMe().getId());
        postAllEvents();
    }

    @UiThread
    public void getEntitiesFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
        returnToIntroStartActivity();
    }

    private void postAllEvents() {
        if (isFirst) {
            // 처음 TabActivity를 시도하면 0번째 탭이 자동 선택됨으로 이를 tracking
            trackGaTab(mEntityManager, 0);
            isFirst = false;
        }

        postShowChattingListEvent();
    }

    private void postShowChattingListEvent() {
        EventBus.getDefault().post(new RetrieveTopicListEvent());
    }

    public void onEvent(InvitationDisableCheckEvent event) {
        onInvitationDisableCheck();
    }

    @Background
    public void onInvitationDisableCheck() {
        showProgressWheel();

        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, mEntityManager.getTeamId());

        dismissProgressWheel();

        switch (result.first) {
            case NETWORK_ERROR:
                showErrorToast(getResources().getString(R.string.err_network));
                break;
            case ERROR:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case INVITATION_DISABLED:
                showTextDialog(
                        getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                break;
            case UNDEFINED_URL:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case SUCCESS:
                moveToInvitationActivity(result.second);
                break;
            default:
                break;
        }
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(mContext).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    @UiThread
    public void moveToInvitationActivity(ResTeamDetailInfo.InviteTeam inviteTeam) {
        invitationUrl = inviteTeam.getInvitationUrl();
        teamName = inviteTeam.getName();
        DialogFragment invitationDialog = new InvitationDialogFragment();
        invitationDialog.show(getSupportFragmentManager(), "invitationsDialog");
    }

    public void onEvent(TeamInvitationsEvent event) {
        String invitationContents =
                teamName + getResources().getString(R.string.jandi_invite_contents);
        int eventType = event.type;
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink(invitationUrl, invitationContents);
            showTextDialog(getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = InviteUtils.getInviteIntent(
                    mContext, event, invitationUrl, invitationContents);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink(invitationUrl, invitationContents);
                showTextDialog(getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    public void copyLink(String publicLink, String contents) {
        ClipData clipData = ClipData.newPlainText("", contents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(mContext, message);
    }

    @UiThread
    void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (mProgressWheel == null) {
            mProgressWheel = new ProgressWheel(this);
            mProgressWheel.init();
        }

        if (mProgressWheel != null && !mProgressWheel.isShowing()) {
            mProgressWheel.show();
        }
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(this)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    public void onEvent(MessagePushEvent event) {
        if (!TextUtils.equals(event.getEntityType(), "user")) {
            getEntities();
        }
    }

    public void onEventMainThread(ChatBadgeEvent event) {
        if (event.isBadge()) {
            mMainTabPagerAdapter.showNewChatBadge();
        } else {
            mMainTabPagerAdapter.hideNewChatBadge();

        }
    }

    public void onEventMainThread(TopicBadgeEvent event) {
        if (event.isBadge()) {
            mMainTabPagerAdapter.showNewTopicBadge();
        } else {
            mMainTabPagerAdapter.hideNewTopicBadge();
        }
    }

    public void onEvent(TeamInfoChangeEvent event) {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(MainTabActivity.this).getSelectedTeamInfo();
        setupActionBar(selectedTeamInfo.getName());

    }

    public void onEventMainThread(ServiceMaintenanceEvent event) {
        IntroActivityViewModel introViewModel = IntroActivityViewModel_.getInstance_(MainTabActivity.this);
        introViewModel.showMaintenanceDialog();
    }
}
