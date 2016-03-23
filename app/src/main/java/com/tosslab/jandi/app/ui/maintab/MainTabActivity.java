package com.tosslab.jandi.app.ui.maintab;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.events.ServiceMaintenanceEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.ui.MixpanelAnalytics;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.ui.maintab.teams.adapter.TeamsAdapter;
import com.tosslab.jandi.app.ui.maintab.teams.component.DaggerTeamsComponent;
import com.tosslab.jandi.app.ui.maintab.teams.module.TeamsModule;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenter;
import com.tosslab.jandi.app.ui.maintab.teams.view.TeamsView;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.app.views.FloatingActionMenu;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;
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
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseAppCompatActivity implements TeamsView {

    public static final int CHAT_INDEX = 1;
    public static final int REQUEST_TEAM_CREATE = 1603;
    @Extra
    boolean fromPush = false;
    @ViewById(R.id.vg_fab_menu)
    FloatingActionMenu floatingActionMenu;
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

    @ViewById(R.id.tv_main_tab_title)
    TextView tvTitle;

    @ViewById(R.id.v_main_tab_metaphor_another_team)
    View vMetaphorAnotherTeam;
    @ViewById(R.id.v_main_tab_metaphor_another_team_has_message)
    View vMetaphorAnotherTeamHasMessage;

    long selectedEntity = -1;
    @Inject
    TeamsPresenter teamsPresenter;
    private UiUtils.KeyboardHandler keyboardHandler;
    private OfflineLayer offlineLayer;
    private ProgressWheel progressWheel;
    private MainTabPagerAdapter mMainTabPagerAdapter;
    private EntityManager mEntityManager;
    private boolean isFirst = true;    // poor implementation
    private PopupWindow teamsPopupWindow;
    private TeamsAdapter teamsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerTeamsComponent.builder()
                .teamsModule(new TeamsModule(this))
                .build()
                .inject(this);

    }

    @AfterViews
    void initView() {
        showDialogIfNotLastestVersion();
        ParseUpdateUtil.addChannelOnServer();

        mEntityManager = EntityManager.getInstance();
        new MixpanelAnalytics().trackSigningIn(mEntityManager);

        // Progress Wheel 설정
        progressWheel = new ProgressWheel(this);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo != null) {
            setupActionBar(selectedTeamInfo.getName());
        } else {
            finish();
            return;
        }

        selectedEntity = PushInterfaceActivity.selectedEntityId;

        // ViewPager
        View[] tabViews = new View[5];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_team, null);
        tabViews[4] = getLayoutInflater().inflate(R.layout.tab_mypage, null);
        mMainTabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabViews, selectedEntity);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        viewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(mMainTabPagerAdapter);

        PushInterfaceActivity.selectedEntityId = -1;

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(viewPager);

        if (selectedEntity > 0) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(selectedEntity);
            if (entity == EntityManager.UNKNOWN_USER_ENTITY || entity.isUser()) {
                viewPager.setCurrentItem(CHAT_INDEX);
            }
        }

        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                LogUtil.d("onPageSelected at " + position);
                trackScreenView(position);
                switch (position) {
                    case 0:
                        setFABMenuVisibility(true);
                        break;
                    case 1:
                        setFABMenuVisibility(false);
                        TutorialCoachMarkUtil.showCoachMarkDirectMessageListIfNotShown(MainTabActivity.this);
                        break;
                    case 2:
                        setFABMenuVisibility(false);
                        TutorialCoachMarkUtil.showCoachMarkFileListIfNotShown(MainTabActivity.this);
                        break;
                    case 3:
                        setFABMenuVisibility(false);
                        break;
                    case 4:
                        setFABMenuVisibility(false);
                        break;
                }

                hideKeyboardIfNeed(position);
            }

            void hideKeyboardIfNeed(int position) {
                if (keyboardHandler == null) {
                    return;
                }

                if (position != MainTabPagerAdapter.TAB_TEAM) {
                    keyboardHandler.hideKeyboard();
                }
            }
        });

        // Track for first load(MainTopicListFragment).
        trackScreenView(0);

        if (needInvitePopup()) {
            JandiPreference.setInvitePopup(MainTabActivity.this);
            showInvitePopup(dialog -> TutorialCoachMarkUtil.showCoachMarkTopicListIfNotShown(this));
        } else {
            TutorialCoachMarkUtil.showCoachMarkTopicListIfNotShown(this);
        }

        offlineLayer = new OfflineLayer(vgOffline);

        JandiPreference.setSocketReconnectDelay(0l);
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
        // onResume -> AfterViews 로 이동
        // (소켓에서 필요한 갱신을 다 처리한다고 간주)
        if (NetworkCheckUtil.isConnected()) {
            getEntities();
        }

        initializeTeamsView();
    }

    private void updateChatBadge() {

        final int[] total = {0};
        Observable.from(ChatRepository.getRepository().getChats())
                .subscribe(formattedEntity -> {
                    total[0] += formattedEntity.getUnread();
                });
        mMainTabPagerAdapter.updateChatBadge(total[0]);

    }

    private void updateTopicBadge() {
        EntityManager entityManager = EntityManager.getInstance();
        final int[] total = {0};
        Observable.merge(Observable.from(entityManager.getJoinedChannels()), Observable.from(entityManager.getGroups()))
                .subscribe(formattedEntity -> {
                    total[0] += formattedEntity.alarmCount;
                });
        mMainTabPagerAdapter.updateTopicBadge(total[0]);

    }

    private void showInvitePopup(DialogInterface.OnDismissListener onDismissListener) {
        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.InviteTeamMember);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        View view = LayoutInflater.from(MainTabActivity.this).inflate(R.layout.dialog_invite_popup, null);

        builder.setOnDismissListener(dialog -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.CloseModal);
            if (onDismissListener != null) {
                onDismissListener.onDismiss(dialog);
            }
        });

        final AlertDialog dialog = builder.setView(view)
                .show();

        view.findViewById(R.id.btn_invitation_popup_invite).setOnClickListener(v -> {
            dialog.dismiss();
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_MAIN_POPUP);
            invitationDialogExecutor.execute();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SendInvitations);
        });

        view.findViewById(R.id.btn_invitation_popup_later).setOnClickListener(v -> {
            dialog.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.Later);
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
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        setActionBarTitle(teamName);
    }

    private void setActionBarTitle(String title) {
        tvTitle.setText(title);
    }

    @Override
    public void initializeTeamsView() {
        View teamView = getLayoutInflater().inflate(R.layout.layout_teams, null);

        RecyclerView recyclerView = (RecyclerView) teamView.findViewById(R.id.lv_team);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        teamsAdapter = new TeamsAdapter();
        teamsAdapter.setOnRequestTeamCreateListener(() -> {
            TeamDomainInfoActivity_.intent(MainTabActivity.this)
                    .startForResult(REQUEST_TEAM_CREATE);

            teamsPopupWindow.dismiss();
        });
        teamsAdapter.setOnTeamClickListener(team -> {
            teamsPresenter.onTeamJoinAction(team.getTeamId());
            teamsPopupWindow.dismiss();
        });
        recyclerView.setAdapter(teamsAdapter);

        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        teamsPopupWindow = new PopupWindow(teamView, matchParent, wrapContent);
        teamsPopupWindow.setOutsideTouchable(true);
    }

    @Override
    public void setTeams(List<Team> teams) {
        teamsAdapter.clear();

        vMetaphorAnotherTeam.setVisibility(View.VISIBLE);

        Observable.from(teams)
                .subscribe(team -> {
                    int viewType = team.getStatus() == Team.Status.PENDING
                            ? TeamsAdapter.VIEW_TYPE_TEAM_PENDING : TeamsAdapter.VIEW_TYPE_TEAM;

                    teamsAdapter.addRow(new MultiItemRecyclerAdapter.Row<>(team, viewType));
                });

        teamsAdapter.addRow(
                new MultiItemRecyclerAdapter.Row<>(null, TeamsAdapter.VIEW_TYPE_TEAM_CREATE));

        teamsAdapter.notifyDataSetChanged();

        tvTitle.setOnClickListener(v -> {
            int yoff = -tvTitle.getMeasuredHeight() - (int) UiUtils.getPixelFromDp(8) /* 조금 더 올리려고 */;
            teamsPopupWindow.showAsDropDown(tvTitle, 0, yoff);
        });
    }

    @Override
    public void showAnotherTeamHasMessageMetaphor() {
        ValueAnimator whiteToRedAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        whiteToRedAnim.setDuration(2000);
        whiteToRedAnim.setRepeatMode(ValueAnimator.REVERSE);
        whiteToRedAnim.setRepeatCount(ValueAnimator.INFINITE);
        whiteToRedAnim.addUpdateListener(animation -> {
            Float alpha = (Float) animation.getAnimatedValue();
            vMetaphorAnotherTeamHasMessage.setAlpha(alpha);
        });
        vMetaphorAnotherTeamHasMessage.setTag(whiteToRedAnim);
        whiteToRedAnim.start();
    }

    @Override
    public void hideAnotherTeamHasMessageMetaphor() {
        Object whiteToRedAnim = vMetaphorAnotherTeamHasMessage.getTag();
        if (whiteToRedAnim != null && whiteToRedAnim instanceof ValueAnimator) {
            ((ValueAnimator) whiteToRedAnim).cancel();
        }
        vMetaphorAnotherTeamHasMessage.setAlpha(0.0f);
    }

    @Override
    public void clearTeams() {
        hideAnotherTeamHasMessageMetaphor();

        vMetaphorAnotherTeam.setVisibility(View.GONE);
        tvTitle.setOnClickListener(null);
        teamsAdapter.clear();
        teamsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgressWheel() {
        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void moveToSelectTeam() {
        JandiSocketService.stopService(this);
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        ParseUpdateUtil.addChannelOnServer();

        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        finish();
    }

    public void onEvent(TeamInviteIgnoreEvent event) {
        teamsPopupWindow.dismiss();
        teamsPresenter.onTeamInviteIgnoreAction(event.getTeam());
    }

    public void onEvent(TeamInviteAcceptEvent event) {
        teamsPopupWindow.dismiss();
        teamsPresenter.onTeamInviteAcceptAction(event.getTeam());
    }

    @Override
    public void removePendingTeam(Team team) {
        for (int i = teamsAdapter.getItemCount() - 1; i >= 0; i--) {
            if (teamsAdapter.getItem(i) instanceof Team) {
                Team targetTeam = teamsAdapter.getItem(i);
                if (targetTeam.getTeamId() == team.getTeamId()) {
                    teamsAdapter.remove(i);
                    teamsAdapter.notifyItemRemoved(i);
                }
            }
        }
    }

    @Override
    public void showTeamInviteIgnoreFailToast(String errorMessage) {
        ColoredToast.showError(errorMessage);
    }

    @Override
    public void showTeamInviteAcceptFailDialog(String errorMessage, final Team team) {
        AlertUtil.showConfirmDialog(this, errorMessage, (dialog, which) -> {
            teamsPresenter.onTeamInviteIgnoreAction(team);
        }, false);
    }

    @Click(R.id.vg_main_offline)
    void onOfflineClick() {
        offlineLayer.dismissOfflineView();
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        // TODO show toast

        if (event.isConnected()) {
            offlineLayer.dismissOfflineView();

            teamsPresenter.initializeTeams();
        } else {
            offlineLayer.showOfflineView();
            ColoredToast.showGray(JandiApplication.getContext().getString(R
                    .string.jandi_msg_network_offline_warn));
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof UiUtils.KeyboardHandler) {
            keyboardHandler = (UiUtils.KeyboardHandler) fragment;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
        // Entity의 리스트를 획득하여 저장한다.
        EventBus.getDefault().register(this);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo != null) {
            setupActionBar(selectedTeamInfo.getName());
        } else {
            finish();
            return;
        }

        refreshEntityIfNeed();

        if (NetworkCheckUtil.isConnected()) {
            offlineLayer.dismissOfflineView();
        } else {
            offlineLayer.showOfflineView();
        }

        fromPush = false;

        updateMoreBadge();
        updateTopicBadge();
        updateChatBadge();

        teamsPresenter.initializeTeams();

    }

    private void refreshEntityIfNeed() {
        long diffTime = System.currentTimeMillis() - JandiPreference.getSocketConnectedLastTime();
        if (diffTime > 1000 * 60 * 5) {
            LogUtil.d("refreshEntityIfNeed");
            getEntities();
        }

    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @OnActivityResult(REQUEST_TEAM_CREATE)
    void onTeamCreateResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            teamsPresenter.onTeamCreated();
        }
    }

    @Override
    protected void onDestroy() {
        teamsPresenter.clearTeamInitializeQueue();
        JandiSocketService.stopService(this);
        super.onDestroy();
    }

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @Background(serial = "getEntities")
    public void getEntities() {
        try {
            ResLeftSideMenu resLeftSideMenu = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.upsertBadgeCount(resLeftSideMenu.team.id, totalUnreadCount);
            BadgeUtils.setBadge(getApplicationContext(), badgeCountRepository.getTotalBadgeCount());
            mEntityManager.refreshEntity();
            getEntitiesSucceed(resLeftSideMenu);
        } catch (RetrofitError e) {
            e.printStackTrace();
            if (e.getResponse() != null) {
                if (e.getResponse().getStatus() == JandiConstants.NetworkError.UNAUTHORIZED) {

                    SignOutUtil.removeSignData();

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
        progressWheel.dismiss();
        setActionBarTitle(mEntityManager.getTeamName());
        JandiPreference.setMyEntityId(this, mEntityManager.getMe().getId());
        postAllEvents();
    }

    @UiThread
    public void getEntitiesFailed(String errMessage) {
        ColoredToast.showError(errMessage);
        if (isFinishing()) {
            return;
        }
        IntroMainActivity_.intent(MainTabActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
        finish();
    }

    private void postAllEvents() {
        if (isFirst) {
            // 처음 TabActivity를 시도하면 0번째 탭이 자동 선택됨으로 이를 tracking
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


    public void onEvent(RequestInviteMemberEvent event) {
        invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_MAIN_INVITE);
        invitationDialogExecutor.execute();
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
        int messageCount = getOtherTeamMessageCount();
        if (messageCount > 0) {
            mMainTabPagerAdapter.showMoreNewBadge();
            teamsPresenter.reInitializeTeams();
        } else {
            mMainTabPagerAdapter.hideMoreNewBadge();
        }
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
        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        Observable.from(AccountRepository.getRepository().getAccountTeams())
                .filter(userTeam -> userTeam.getTeamId() != selectedTeamId)
                .map(ResAccountInfo.UserTeam::getUnread)
                .subscribe(integer -> messageCount[0] += integer);
        return messageCount[0];
    }

    public void onEventMainThread(TeamInfoChangeEvent event) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo != null) {
            setupActionBar(selectedTeamInfo.getName());
        } else {
            return;
        }

    }

    public void onEventMainThread(ServiceMaintenanceEvent event) {
        AlertUtil.showConfirmDialog(MainTabActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    private void trackScreenView(int position) {
        int screenView = ScreenViewProperty.TOPIC_PANEL;
        AnalyticsValue.Screen screen = AnalyticsValue.Screen.TopicsTab;
        switch (position) {
            case 0:
                screenView = ScreenViewProperty.TOPIC_PANEL;
                screen = AnalyticsValue.Screen.TopicsTab;
                break;
            case 1:
                screenView = ScreenViewProperty.MESSAGE_PANEL;
                screen = AnalyticsValue.Screen.TopicChat;
                break;
            case 2:
                screenView = ScreenViewProperty.FILE_PANEL;
                screen = AnalyticsValue.Screen.FilesTab;
                break;
            case 3:
                screenView = ScreenViewProperty.SETTING_PANEL;
                screen = AnalyticsValue.Screen.MoreTab;
                break;
        }

        AnalyticsUtil.sendScreenName(screen);

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

    @Background
    public void showDialogIfNotLastestVersion() {
        if (!NetworkCheckUtil.isConnected())
            return;

        ResConfig configInfo = getConfigInfo();
        if (configInfo != null && configInfo.latestVersions != null &&
                (getCurrentAppVersionCode() < configInfo.latestVersions.android)) {
            final long oneDayMillis = 1000 * 60 * 60 * 24;
            long timeFromLastPopup = System.currentTimeMillis() - JandiPreference.getVersionPopupLastTime();
            if (timeFromLastPopup > oneDayMillis) {
                showUpdateVersionDialog(configInfo);
            }
        }
    }

    @UiThread
    public void showUpdateVersionDialog(ResConfig configInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(getString(R.string.jandi_update_title))
                .setMessage(getString(R.string.jandi_update_message))
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    String appPackageName = JandiApplication.getContext().getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + appPackageName)));
                        finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                    } catch (ActivityNotFoundException anfe) {
                        AlertUtil.showChooseUpdateWebsiteDialog(MainTabActivity.this, appPackageName, configInfo.latestVersions.android);
                    }
                })
                .setNegativeButton(getString(R.string.jandi_cancel)
                        , (dialog, which) -> {
                    JandiPreference.setVersionPopupLastTimeToCurrentTime(System.currentTimeMillis());
                })
                .setCancelable(true);
        builder.create().show();
    }

    public ResConfig getConfigInfo() {
        ResConfig resConfig = null;
        try {
            resConfig = RequestApiManager.getInstance().getConfigByMainRest();
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return resConfig;
    }

    public int getCurrentAppVersionCode() {
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

    public void setFABMenuVisibility(boolean visibility) {
        if (floatingActionMenu == null) {
            return;
        }
        if (visibility) {
            floatingActionMenu.setVisibility(View.VISIBLE);
        } else {
            floatingActionMenu.setVisibility(View.INVISIBLE);
            if (floatingActionMenu.isOpened()) {
                floatingActionMenu.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (teamsPopupWindow != null && teamsPopupWindow.isShowing()) {
            teamsPopupWindow.dismiss();
            return;
        }

        if (floatingActionMenu != null && floatingActionMenu.isOpened()) {
            floatingActionMenu.close();
        } else {
            super.onBackPressed();
        }
    }

    public FloatingActionMenu getFloatingActionMenu() {
        return floatingActionMenu;
    }

}
