package com.tosslab.jandi.app.ui.maintab;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.events.ServiceMaintenanceEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.services.socket.to.MessageOfOtherTeamEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.teams.adapter.TeamsAdapter;
import com.tosslab.jandi.app.ui.maintab.teams.component.DaggerTeamsComponent;
import com.tosslab.jandi.app.ui.maintab.teams.module.TeamsModule;
import com.tosslab.jandi.app.ui.maintab.teams.presenter.TeamsPresenter;
import com.tosslab.jandi.app.ui.maintab.teams.view.TeamsView;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.profile.insert.InsertProfileActivity;
import com.tosslab.jandi.app.ui.team.create.CreateTeamActivity;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.FloatingActionMenu;
import com.tosslab.jandi.app.views.MaxHeightRecyclerView;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;
import com.tosslab.jandi.app.views.listeners.ListScroller;
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
    @Extra
    int tabIndex = -1;

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

    @ViewById(R.id.btn_main_tab_show_another_team)
    View btnShowAnotherTeam;
    @ViewById(R.id.tv_main_tab_title)
    TextView tvTitle;

    @ViewById(R.id.v_main_tab_metaphor_another_team)
    View vMetaphorAnotherTeam;
    @ViewById(R.id.v_main_tab_metaphor_another_team_has_message)
    View vMetaphorAnotherTeamHasMessage;

    @ViewById(R.id.pager_main_tab)
    ViewPager vpMainTab;

    @ViewById(R.id.sliding_tabs)
    PagerSlidingTabStrip mainTapStrip;

    long selectedEntity = -1;
    @Inject
    TeamsPresenter teamsPresenter;

    private OfflineLayer offlineLayer;
    private ProgressWheel progressWheel;
    private MainTabPagerAdapter mainTabPagerAdapter;
    private boolean isFirst = true;    // poor implementation
    private PopupWindow teamsPopupWindow;
    private TeamsAdapter teamsAdapter;
    private ListScrollHandler listScrollHandler;

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
        PushInterfaceActivity.selectedEntityId = -1;

        // ViewPager
        initMainTabViewPager();

        // Bind the tabs to the ViewPager
        initMainTabStrip();

        showCoachMarkIfNeed();

        offlineLayer = new OfflineLayer(vgOffline);

        JandiPreference.setSocketReconnectDelay(0L);
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        initializeTeamsView();
    }

    private void initMainTabViewPager() {
        View[] tabViews = new View[5];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_team, null);
        tabViews[4] = getLayoutInflater().inflate(R.layout.tab_mypage, null);
        mainTabPagerAdapter =
                new MainTabPagerAdapter(getSupportFragmentManager(), tabViews, selectedEntity);
        vpMainTab.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        vpMainTab.setOffscreenPageLimit(4);
        vpMainTab.setAdapter(mainTabPagerAdapter);
    }

    private void initMainTabStrip() {
        mainTapStrip.setViewPager(vpMainTab);

        if (tabIndex > -1) {
            vpMainTab.setCurrentItem(tabIndex);
        } else if (selectedEntity > 0) {
            boolean human = HumanRepository.getInstance().isHuman(selectedEntity);
            if (human) {
                vpMainTab.setCurrentItem(CHAT_INDEX);
            }
        } else {
            vpMainTab.setCurrentItem(JandiPreference.getLastSelectedTab());
        }

        int currentItem = vpMainTab.getCurrentItem();
        if (currentItem != 0) {
            setFABMenuVisibility(false);
        }
        trackScreenView(currentItem);

        mainTapStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                LogUtil.i("MainTabActivity", "onPageSelected at " + position);
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

                listScrollHandler.setCurrentIndex(position);
                JandiPreference.setLastSelectedTab(position);
            }
        });

        listScrollHandler = new ListScrollHandler();

        mainTapStrip.setOnTabClickListener(index -> {
            listScrollHandler.onTabClick(index);
        });
    }

    private void updateChatBadge() {

        final int[] total = {0};
        Observable.from(TeamInfoLoader.getInstance().getDirectMessageRooms())
                .subscribe(formattedEntity -> {
                    total[0] += formattedEntity.getUnreadCount();
                });
        mainTabPagerAdapter.updateChatBadge(total[0]);

    }

    private void updateTopicBadge() {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        List<Topic> topics = TopicRepository.getInstance().getTopics(teamId);

        int count = Observable.from(topics)
                .map(Topic::getUnreadCount)
                .scan((count1, count2) -> count1 + count2)
                .toBlocking()
                .firstOrDefault(0);

        mainTabPagerAdapter.updateTopicBadge(count);

    }

    private void showCoachMarkIfNeed() {
        if (needInvitePopup()) {
            JandiPreference.setInvitePopup(MainTabActivity.this);
            showInvitePopup(dialog -> TutorialCoachMarkUtil.showCoachMarkTopicListIfNotShown(this));
        } else {
            TutorialCoachMarkUtil.showCoachMarkTopicListIfNotShown(this);
        }
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
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        int memberCount = HumanRepository.getInstance().getMemberCount(teamId);
        return JandiPreference.isInvitePopup(MainTabActivity.this) && memberCount > 0;
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
        int displayHeight = ApplicationUtil.getDisplaySize(true);
        int maxHeight = displayHeight / 2;
        MaxHeightRecyclerView recyclerView =
                (MaxHeightRecyclerView) teamView.findViewById(R.id.lv_team);
        recyclerView.setMaxHeight(maxHeight);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        teamsAdapter = new TeamsAdapter();
        teamsAdapter.setOnRequestTeamCreateListener(() -> {
            Intent intent = new Intent(this, CreateTeamActivity.class);
            startActivityForResult(intent, REQUEST_TEAM_CREATE);
            teamsPopupWindow.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.CreateNewTeam);
        });
        teamsAdapter.setOnTeamClickListener(team -> {
            teamsPresenter.onTeamJoinAction(team.getTeamId());

            teamsPopupWindow.dismiss();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.ChooseTeam);
        });
        recyclerView.setAdapter(teamsAdapter);

        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        teamsPopupWindow = new PopupWindow(teamView, matchParent, wrapContent);
        teamsPopupWindow.setTouchable(true);
        teamsPopupWindow.setFocusable(true);
        teamsPopupWindow.setOutsideTouchable(true);
        teamsPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        btnShowAnotherTeam.setOnClickListener(v -> {
            int yoff = -tvTitle.getMeasuredHeight() - (int) UiUtils.getPixelFromDp(8) /* 조금 더 올리려고 */;
            teamsPopupWindow.showAsDropDown(tvTitle, 0, yoff);

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.OpenTeamList);
        });
    }

    @Override
    public void showAnotherTeamHasMessageMetaphor() {
        ValueAnimator whiteToRedAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
        whiteToRedAnim.setDuration(1000);
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
        btnShowAnotherTeam.setOnClickListener(null);
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

        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        finish();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void moveSetProfileActivity() {
        Intent intent = new Intent(this, InsertProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onEvent(TeamInviteIgnoreEvent event) {
        teamsPopupWindow.dismiss();
        teamsPresenter.onTeamInviteIgnoreAction(event.getTeam());

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.AcceptTeamInvitation);
    }

    public void onEvent(TeamInviteAcceptEvent event) {
        teamsPopupWindow.dismiss();
        teamsPresenter.onTeamInviteAcceptAction(event.getTeam());

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.IgnoreTeamInvitation);
    }

    public void onEvent(TeamDeletedEvent event) {
        teamsPresenter.reInitializeTeams();
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
        if (event.isConnected()) {
            offlineLayer.dismissOfflineView();

            teamsPresenter.onInitializeTeams();
        } else {
            offlineLayer.showOfflineView();
            ColoredToast.showGray(JandiApplication.getContext().getString(R
                    .string.jandi_msg_network_offline_warn));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Entity의 리스트를 획득하여 저장한다.
        EventBus.getDefault().register(this);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo != null) {
            setupActionBar(selectedTeamInfo.getName());
        } else {
            finish();
            return;
        }

        if (NetworkCheckUtil.isConnected()) {
            offlineLayer.dismissOfflineView();
        } else {
            offlineLayer.showOfflineView();
        }

        fromPush = false;

        updateMoreBadge();
        updateTopicBadge();
        updateChatBadge();

        teamsPresenter.onInitializeTeams();

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
        super.onDestroy();
    }

    @UiThread
    void stopJandiServiceInMainThread() {
        JandiSocketService.stopService(MainTabActivity.this);
    }


    public void onEvent(MainSelectTopicEvent event) {
        selectedEntity = event.getSelectedEntity();
    }


    public void onEvent(RequestInviteMemberEvent event) {
        int from = event.getFrom() > 0 ? event.getFrom() : InvitationDialogExecutor.FROM_MAIN_INVITE;
        invitationDialogExecutor.setFrom(from);
        invitationDialogExecutor.execute();
    }

    public void onEventMainThread(ChatBadgeEvent event) {
        mainTabPagerAdapter.updateChatBadge(event.getCount());
    }

    public void onEventMainThread(TopicBadgeEvent event) {
        mainTabPagerAdapter.updateTopicBadge(event.getCount());
    }

    public void onEventMainThread(MessageOfOtherTeamEvent event) {
        teamsPresenter.reInitializeTeams();
    }

    public void updateMoreBadge() {
        int messageCount = getOtherTeamMessageCount();
        if (messageCount > 0) {
            mainTabPagerAdapter.showMoreNewBadge();
        } else {
            mainTabPagerAdapter.hideMoreNewBadge();
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
        }
        teamsPresenter.reInitializeTeams();

    }

    public void onEventMainThread(ServiceMaintenanceEvent event) {
        AlertUtil.showConfirmDialog(MainTabActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    private void trackScreenView(int position) {
        LogUtil.d("MainTabActivity", "trackScreenView at " + position);
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
                screen = AnalyticsValue.Screen.TeamTab;
                break;
            case 4:
                screen = AnalyticsValue.Screen.MypageTab;
                break;
        }

        AnalyticsUtil.sendScreenName(screen);

        if (position < 3) {
            AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                    .event(Event.ScreenView)
                    .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                    .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                    .property(PropertyKey.ScreenView, screenView)
                    .build());
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @UiThread
    public void showUpdateVersionDialog(ResConfig configInfo) {
        if (isFinishing()) {
            return;
        }
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


    private ResConfig getConfigInfo() {
        try {
            return new ConfigApi(RetrofitBuilder.getInstance()).getConfig();
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCurrentAppVersionCode() {
        return ApplicationUtil.getAppVersionCode();
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

    private class ListScrollHandler implements PagerSlidingTabStrip.OnTabClickListener {
        private int currentIndex = 0;

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public void onTabClick(int index) {
            if (currentIndex == index) {
                Fragment fragment = (Fragment) mainTabPagerAdapter.instantiateItem(vpMainTab, index);
                if (fragment instanceof ListScroller) {
                    ((ListScroller) fragment).scrollToTop();
                }
            }
            currentIndex = index;
        }
    }

}
