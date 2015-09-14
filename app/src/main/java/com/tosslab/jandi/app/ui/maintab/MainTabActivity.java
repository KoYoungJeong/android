package com.tosslab.jandi.app.ui.maintab;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.events.ServiceMaintenanceEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseAnalyticsActivity {

    public static final int CHAT_INDEX = 1;
    public static final int REQ_START_MESSAGE = 1211;
    @Bean
    EntityClientManager entityClientManager;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @SystemService
    ClipboardManager clipboardManager;
    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    @ViewById(R.id.vg_main_offline)
    View vgOffline;
    int selectedEntity = -1;
    private OfflineLayer offlineLayer;
    private ProgressWheel mProgressWheel;
    private Context mContext;
    private EntityManager mEntityManager;
    private MainTabPagerAdapter mMainTabPagerAdapter;
    private boolean isFirst = true;    // poor implementation

    @AfterViews
    void initView() {
        LogUtil.d("시작은 여기");

        ParseUpdateUtil.addChannelOnServer();

        mContext = getApplicationContext();
        mEntityManager = EntityManager.getInstance();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        setupActionBar(selectedTeamInfo.getName());

        selectedEntity = PushInterfaceActivity.selectedEntityId;

        // ViewPager
        View[] tabViews = new View[4];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_more, null);
        mMainTabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabViews, selectedEntity);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        mViewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mMainTabPagerAdapter);

        PushInterfaceActivity.selectedEntityId = -1;

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(mViewPager);

        if (selectedEntity > 0) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(selectedEntity);
            if (entity == EntityManager.UNKNOWN_USER_ENTITY || entity.isUser()) {
                mViewPager.setCurrentItem(CHAT_INDEX);
            }
        }

        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                LogUtil.d("onPageSelected at " + position);
                trackGaTab(mEntityManager, position);
                trackScreenView(position);
                switch (position) {
                    case 1:
                        TutorialCoachMarkUtil.showCoachMarkDirectMessageListIfNotShown(MainTabActivity.this);
                        break;
                    case 2:
                        TutorialCoachMarkUtil.showCoachMarkFileListIfNotShown(MainTabActivity.this);
                        break;
                    case 3:
                        TutorialCoachMarkUtil.showCoachMarkMoreIfNotShown(MainTabActivity.this);
                        break;
                }
            }
        });

        // Track for first load(MainTopicListFragment).
        trackScreenView(0);

        if (needInvitePopup()) {
            JandiPreference.setInvitePopup(MainTabActivity.this);
            showInvitePopup();
        }

        offlineLayer = new OfflineLayer(vgOffline);

        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
        // onResume -> AfterViews 로 이동
        // (소켓에서 필요한 갱신을 다 처리한다고 간주)
        if (NetworkCheckUtil.isConnected()) {
            getEntities();
        }

        updateMoreBadge();
    }

    private void showInvitePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabActivity.this);
        View view = LayoutInflater.from(MainTabActivity.this).inflate(R.layout.dialog_invite_popup, null);

        builder.setOnDismissListener(dialog ->
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.CloseModal));

        final AlertDialog materialDialog = builder.setView(view)
                .show();

        view.findViewById(R.id.btn_invitation_popup_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                invitationDialogExecutor.execute();

                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SendInvitations);
            }
        });

        view.findViewById(R.id.btn_invitation_popup_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.Later);
            }
        });


    }

    private boolean needInvitePopup() {
        List<FormattedEntity> formattedUsersWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe();
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

    @Click(R.id.vg_main_offline)
    void onOfflineClick() {
        offlineLayer.dismissOfflineView();
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        // TODO show toast

        if (event.isConnected()) {
            offlineLayer.dismissOfflineView();
        } else {
            offlineLayer.showOfflineView();
            ColoredToast.showGray(MainTabActivity.this, JandiApplication.getContext().getString(R
                    .string.jandi_msg_network_offline_warn));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d("MainTabAcitivity.onResume");

        // Entity의 리스트를 획득하여 저장한다.
        EventBus.getDefault().register(this);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        setupActionBar(selectedTeamInfo.getName());

        TutorialCoachMarkUtil.showCoachMarkTopicListIfNotShown(this);

        if (NetworkCheckUtil.isConnected()) {
            offlineLayer.dismissOfflineView();
        } else {
            offlineLayer.showOfflineView();
        }
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

    @Override
    protected void onDestroy() {
        JandiSocketService.stopService(this);
        super.onDestroy();
    }

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @Background
    public void getEntities() {
        try {
            ResLeftSideMenu resLeftSideMenu = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            BadgeUtils.setBadge(MainTabActivity.this, totalUnreadCount);
            JandiPreference.setBadgeCount(MainTabActivity.this, totalUnreadCount);
            mEntityManager.refreshEntity();
            getEntitiesSucceed(resLeftSideMenu);
        } catch (RetrofitError e) {
            e.printStackTrace();
            if (e.getResponse() != null) {
                if (e.getResponse().getStatus() == JandiConstants.NetworkError.UNAUTHORIZED) {

                    JandiPreference.signOut(JandiApplication.getContext());

                    ParseUpdateUtil.deleteChannelOnServer();

                    OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                            .clearAllData();

                    getEntitiesFailed(getString(R.string.err_expired_session));
                    stopJandiServiceInMainThread();
                } else if (e.getResponse().getStatus() == JandiConstants.NetworkError.SERVICE_UNAVAILABLE) {
                    EventBus.getDefault().post(new ServiceMaintenanceEvent());
                } else {
                    getEntitiesFailed(getString(R.string.err_service_connection));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            getEntitiesFailed(getString(R.string.err_service_connection));
        }
    }

    @UiThread
    void stopJandiServiceInMainThread() {
        JandiSocketService.stopService(MainTabActivity.this);
    }

    @UiThread
    public void getEntitiesSucceed(ResLeftSideMenu resLeftSideMenu) {
        mProgressWheel.dismiss();
        mEntityManager = EntityManager.getInstance();
        trackSigningIn(mEntityManager);
        getSupportActionBar().setTitle(mEntityManager.getTeamName());
        JandiPreference.setMyEntityId(this, mEntityManager.getMe().getId());
        postAllEvents();
    }

    @UiThread
    public void getEntitiesFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
        if (isFinishing()) {
            return;
        }
        IntroMainActivity_.intent(MainTabActivity.this).start();
        finish();
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

    public void onEvent(MainSelectTopicEvent event) {
        selectedEntity = event.getSelectedEntity();
    }


    public void onEvent(InvitationDisableCheckEvent event) {
        invitationDialogExecutor.execute();
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
        LogUtil.d("MainTabAcitivity.MessagePushEventCall");
        if (!TextUtils.equals(event.getEntityType(), PushTO.RoomType.CHAT.getName())) {
            getEntities();
        }
    }

    public void onEventMainThread(ChatBadgeEvent event) {
        mMainTabPagerAdapter.updateChatBadge(event.getCount());
    }

    public void onEventMainThread(TopicBadgeEvent event) {
        mMainTabPagerAdapter.updateTopicBadge(event.getCount());
    }

    public void onEventMainThread(MessageOfOtherTeamEvent event) {
        updateMoreBadge();
    }

    public void updateMoreBadge() {
        int messageCount = getOtherTeamMessageCount();
        if (messageCount > 0) {
            mMainTabPagerAdapter.showMoreNewBadge();
        } else {
            mMainTabPagerAdapter.hideMoreNewBadge();
        }
    }

    private int getOtherTeamMessageCount() {
        final int[] messageCount = {0};
        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        Observable.from(AccountRepository.getRepository().getAccountTeams())
                .filter(userTeam -> userTeam.getTeamId() != selectedTeamId)
                .map(ResAccountInfo.UserTeam::getUnread)
                .subscribe(integer -> messageCount[0] += integer);
        return messageCount[0];
    }

    public void onEvent(TeamInfoChangeEvent event) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        setupActionBar(selectedTeamInfo.getName());

    }

    public void onEventMainThread(ServiceMaintenanceEvent event) {
        AlertUtil.showConfirmDialog(MainTabActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    private void trackScreenView(int position) {
        int screenView = ScreenViewProperty.TOPIC_PANEL;
        String screenViewForGA = "TOPIC_PANEL";
        switch (position) {
            case 0:
                screenView = ScreenViewProperty.TOPIC_PANEL;
                screenViewForGA = "TOPIC_PANEL";
                break;
            case 1:
                screenView = ScreenViewProperty.MESSAGE_PANEL;
                screenViewForGA = "MESSAGE_PANEL";
                break;
            case 2:
                screenView = ScreenViewProperty.FILE_PANEL;
                screenViewForGA = "FILE_PANEL";
                break;
            case 3:
                screenView = ScreenViewProperty.SETTING_PANEL;
                screenViewForGA = "SETTING_PANEL";
                break;
        }

        GoogleAnalyticsUtil.sendScreenName(screenViewForGA);

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, screenView)
                        .build());
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
