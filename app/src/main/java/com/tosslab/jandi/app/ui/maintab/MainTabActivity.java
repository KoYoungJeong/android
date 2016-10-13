package com.tosslab.jandi.app.ui.maintab;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.NavigationBadgeEvent;
import com.tosslab.jandi.app.events.RefreshMypageBadgeCountEvent;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.poll.RefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.events.socket.EventUpdateFinish;
import com.tosslab.jandi.app.events.socket.EventUpdateInProgress;
import com.tosslab.jandi.app.events.socket.EventUpdateStart;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.services.keep.KeepAliveService;
import com.tosslab.jandi.app.services.keep.KeepExecutedService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor_;
import com.tosslab.jandi.app.ui.maintab.component.DaggerMainTabComponent;
import com.tosslab.jandi.app.ui.maintab.module.MainTabModule;
import com.tosslab.jandi.app.ui.maintab.navigation.NavigationFragment;
import com.tosslab.jandi.app.ui.maintab.navigation.widget.BadgeOverFlowMenu;
import com.tosslab.jandi.app.ui.maintab.presenter.MainTabPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.TabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.ChatTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.MypageTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.team.TeamTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.TopicTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.util.BackPressConsumer;
import com.tosslab.jandi.app.ui.maintab.tabs.util.FloatingActionBarDetector;
import com.tosslab.jandi.app.ui.maintab.tabs.util.TabFactory;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.profile.insert.InsertProfileActivity;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.LongPressListener;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.TabView;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabActivity extends BaseAppCompatActivity implements MainTabPresenter.View,
        NavigationFragment.NavigationOwner {

    @Bind(R.id.toolbar_main_tab)
    Toolbar toolbar;

    @Bind(R.id.btn_main_tab_menu)
    BadgeOverFlowMenu badgeOverFlowMenu;

    @Bind(R.id.tv_main_tab_title)
    TextView tvTitle;

    @Bind(R.id.drawer_main_tab)
    DrawerLayout drawerLayout;

    @Bind(R.id.vg_main_tab_tabs_container)
    TabLayout tabLayout;

    @Bind(R.id.v_main_tab_top_shadow)
    View vTopShadow;

    @Bind(R.id.vg_main_offline)
    ViewGroup vgOffline;

    @Bind(R.id.vg_main_synchronize)
    View vgSynchronize;
    @Bind(R.id.tv_synchronize)
    TextView tvSynchronize;

    @Bind(R.id.btn_main_tab_fab)
    View btnFab;

    @Bind(R.id.page_main_tab)
    ViewPager viewPager;

    @Bind(R.id.vg_main_tab_navigation_wrapper)
    View vgNavigationWrapper;

    @Inject
    MainTabPresenter mainTabPresenter;

    @Nullable
    @InjectExtra
    int tabIndex = -1;

    @Nullable
    @InjectExtra
    boolean isLoadInitialInfo = false;

    private long selectedEntity = -1;

    private OfflineLayer offlineLayer;

    private TabView tabTopic;
    private TabView tabChat;
    private TabView tabMyPage;
    private MainTabPagerAdapter tabPagerAdapter;
    private int navigationDirection;
    private InvitationDialogExecutor invitationDialogExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_tab);

        if (AccountRepository.getRepository().getSelectedTeamInfo() == null) {
            finish();
            return;
        }

        injectComponent();

        Dart.inject(this);

        ButterKnife.bind(this);

        initSelectedEntity();

        // Easter Egg
        initNavigationEasterEgg();

        initToolbars();

        initOffLineLayer();

        mainTabPresenter.onCheckIfNotLatestVersion(() -> {
            if (isFinishing()) {
                return;
            }

            startSocketService();

            initTabs();

            initTabBadges();

            checkIfNotProfileSetUp();

            showInvitePopupIfNeed();

            EventBus.getDefault().register(this);
        });

        KeepExecutedService.start(this);
        KeepAliveService.start(this);
        initFirebaseUserProperties();
    }

    private void initFirebaseUserProperties() {
        FirebaseAnalytics.getInstance(this)
                .setUserProperty("accountId", AccountUtil.getAccountId(this));
        FirebaseAnalytics.getInstance(this)
                .setUserProperty("memberId", String.valueOf(AccountUtil.getMemberId(this)));

        Completable.defer(() -> {

            if ((System.currentTimeMillis() - JandiPreference.getLatestFcmTokenUpdate()) > 1000 * 60 * 60 * 6) {
                return Completable.complete();
            } else {
                return Completable.never();
            }

        }).subscribeOn(Schedulers.newThread())
                .subscribe(() -> {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        PushTokenRepository.getInstance().deleteGcmToken();
                        FirebaseInstanceId.getInstance().getToken();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
    }

    private void initSelectedEntity() {
        selectedEntity = PushInterfaceActivity.selectedEntityId;
        PushInterfaceActivity.selectedEntityId = -1;
    }

    private void showInvitePopupIfNeed() {
        mainTabPresenter.onCheckIfNOtShowInvitePopup();
    }

    @Override
    public void showInvitePopup() {
        if (isFinishing()) {
            return;
        }

        JandiPreference.setInvitePopup(MainTabActivity.this);
        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.InviteTeamMember);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        View view = getLayoutInflater().inflate(R.layout.dialog_invite_popup, null);

        builder.setOnDismissListener(dialog -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.CloseModal);
        });

        final AlertDialog dialog = builder.setView(view)
                .show();

        view.findViewById(R.id.btn_invitation_popup_invite).setOnClickListener(v -> {
            dialog.dismiss();

            if (invitationDialogExecutor == null) {
                invitationDialogExecutor = InvitationDialogExecutor_.getInstance_(this);
            }
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_MAIN_POPUP);
            invitationDialogExecutor.execute();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.SendInvitations);
        });

        view.findViewById(R.id.btn_invitation_popup_later).setOnClickListener(v -> {
            dialog.dismiss();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMember, AnalyticsValue.Action.Later);
        });
    }

    private void checkIfNotProfileSetUp() {
        mainTabPresenter.onCheckIfNotProfileSetUp();
    }

    private void initOffLineLayer() {
        offlineLayer = new OfflineLayer(vgOffline);
    }

    private void startSocketService() {
        JandiPreference.setSocketReconnectDelay(0L);
        Observable.just(new Object())
                .observeOn(Schedulers.computation())
                .subscribe(it -> {
                    sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
                });
    }

    private void injectComponent() {
        DaggerMainTabComponent.builder()
                .mainTabModule(new MainTabModule(this))
                .build()
                .inject(this);
    }

    private void initToolbars() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initTabBadges() {
        mainTabPresenter.onInitTopicBadge();
        mainTabPresenter.onInitChatBadge();
        mainTabPresenter.onInitMyPageBadge(true);
    }

    private void initTabs() {
        List<TabInfo> tabInfos = TabFactory.getTabs(selectedEntity);

        tabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabInfos);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setOffscreenPageLimit(Math.max(tabInfos.size() -1 , 1));
        setPosition();

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);

                int position = tab.getPosition();

                Fragment item = tabPagerAdapter.getItem(viewPager.getCurrentItem());
                if (item instanceof FloatingActionBarDetector) {
                    ((FloatingActionBarDetector) item).onDetectFloatAction(btnFab);
                }

                tvTitle.setText(tab.getText());
                boolean withoutShadow = position == MypageTabInfo.INDEX || position == TeamTabInfo.INDEX;
                vTopShadow.setVisibility(withoutShadow ? View.GONE : View.VISIBLE);

                boolean isFABController = position == TopicTabInfo.INDEX || position == ChatTabInfo.INDEX;
                btnFab.setVisibility(isFABController ? View.VISIBLE : View.GONE);

                JandiPreference.setLastSelectedTab(position);

                if (position == MypageTabInfo.INDEX) {
                    mainTabPresenter.onInitMyPageBadge(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);

                Fragment fragment = getFragment(tab.getPosition());
                if (fragment != null && fragment instanceof ListScroller) {
                    ((ListScroller) fragment).scrollToTop();
                }
            }

        });

        Observable.from(tabInfos)
                .subscribe(tabInfo -> {
                    TabView tabView = tabInfo.getTabView(getLayoutInflater(), tabLayout);
                    initTabView(tabInfo, tabView);

                    int index = tabInfo.getIndex();
                    boolean isFirstTab = viewPager.getCurrentItem() == tabInfo.getIndex();
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(tabInfo.getTitle())
                            .setCustomView(tabView), index, isFirstTab);
                });
    }

    private void setPosition() {
        if (tabIndex > -1) {
            viewPager.setCurrentItem(tabIndex);
        } else if (selectedEntity > 0) {
            boolean human = HumanRepository.getInstance().isHuman(selectedEntity);
            if (human) {
                viewPager.setCurrentItem(ChatTabInfo.INDEX);
            }
        } else {
            int lastSelectedTab = JandiPreference.getLastSelectedTab();
            viewPager.setCurrentItem(lastSelectedTab);
        }
    }

    private void initTabView(TabInfo tabInfo, TabView tabView) {
        if (tabInfo instanceof TopicTabInfo) {
            tabTopic = tabView;
        } else if (tabInfo instanceof ChatTabInfo) {
            tabChat = tabView;
        } else if (tabInfo instanceof MypageTabInfo) {
            tabMyPage = tabView;
        }
    }

    private void initNavigationEasterEgg() {
        initNavigationPosition();

        badgeOverFlowMenu.setOnTouchListener(new LongPressListener() {
            @Override
            public void onLongPressed() {
                navigationEasterEggOpen();
            }
        });
    }

    @OnClick(R.id.btn_main_tab_menu)
    @Override
    public void openNavigation() {
        drawerLayout.openDrawer(navigationDirection);
    }

    @Override
    public void closeNavigation() {
        drawerLayout.closeDrawer(navigationDirection);
    }

    public void onEventMainThread(NavigationBadgeEvent event) {
        int badgeCount = event.getBadgeCount();

        if (badgeCount <= 0) {
            badgeOverFlowMenu.hideBadge();
            return;
        }

        badgeOverFlowMenu.showBadge();
        badgeOverFlowMenu.setBadgeText(String.valueOf(Math.min(badgeCount, 999)));
    }

    public void onEventMainThread(TopicBadgeEvent event) {
        int count = event.getCount();
        if (count > 999) {
            count = 999;
        }
        setTopicBadge(count);
    }

    public void onEventMainThread(ChatBadgeEvent event) {
        int count = event.getCount();
        if (count > 999) {
            count = 999;
        }
        setChatBadge(count);
    }

    public void onEventMainThread(RefreshPollBadgeCountEvent event) {
        int count = event.getBadgeCount();
        if (count > 999) {
            count = 999;
        }
        setMypageBadge(count);
    }

    public void onEventMainThread(RefreshMypageBadgeCountEvent event) {
        mainTabPresenter.onInitMyPageBadge(viewPager.getCurrentItem() != MypageTabInfo.INDEX);
    }

    public void onEventMainThread(TeamInfoChangeEvent event) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo != null) {
            tvTitle.setText(selectedTeamInfo.getName());
        }
    }

    public void onEventMainThread(RequestInviteMemberEvent event) {
        int from = event.getFrom() > 0 ? event.getFrom() : InvitationDialogExecutor.FROM_MAIN_INVITE;
        if (invitationDialogExecutor == null) {
            invitationDialogExecutor = InvitationDialogExecutor_.getInstance_(this);
        }
        invitationDialogExecutor.setFrom(from);
        invitationDialogExecutor.execute();
    }

    @OnClick(R.id.vg_main_offline)
    void onOfflineClick() {
        offlineLayer.dismissOfflineView();
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (event.isConnected()) {
            offlineLayer.dismissOfflineView();
            // 네트워크가 재연결되면 소켓 서버에 접속하도록 함
            JandiPreference.setSocketReconnectDelay(0);
            sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        } else {
            offlineLayer.showOfflineView();
            ColoredToast.showGray(JandiApplication.getContext().getString(R
                    .string.jandi_msg_network_offline_warn));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NetworkCheckUtil.isConnected()) {
            offlineLayer.dismissOfflineView();
        } else {
            offlineLayer.showOfflineView();
        }

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void setTopicBadge(int count) {
        if (count <= 0) {
            tabTopic.hideBadge();
            return;
        }
        tabTopic.showBadge();
        tabTopic.setBadgeText(String.valueOf(Math.min(count, 999)));
    }

    @Override
    public void setChatBadge(int count) {
        if (count <= 0) {
            tabChat.hideBadge();
            return;
        }
        tabChat.showBadge();
        tabChat.setBadgeText(String.valueOf(Math.min(count, 999)));
    }

    @Override
    public void setMypageBadge(int count) {
        if (count <= 0) {
            tabMyPage.hideBadge();
            return;
        }
        tabMyPage.showBadge();
        tabMyPage.setBadgeText(String.valueOf(Math.min(count, 999)));
    }

    @Override
    public void showUpdateVersionDialog(ResConfig configInfo) {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainTabActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(getString(R.string.jandi_update_title))
                .setMessage(getString(R.string.jandi_update_message))
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    String appPackageName = JandiApplication.getContext().getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + appPackageName)));
                        finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                    } catch (ActivityNotFoundException anfe) {
                        AlertUtil.showChooseUpdateWebsiteDialog(MainTabActivity.this,
                                appPackageName, configInfo.latestVersions.android);
                    }
                })
                .setNegativeButton(getString(R.string.jandi_cancel)
                        , (dialog, which) -> {
                            long time = System.currentTimeMillis();
                            JandiPreference.setVersionPopupLastTimeToCurrentTime(time);
                        })
                .setCancelable(true);
        builder.create().show();
    }

    @Override
    public void moveSetProfileActivity() {
        Intent intent = new Intent(this, InsertProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onEventMainThread(EventUpdateStart event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            vgSynchronize.setVisibility(View.VISIBLE);
            tvSynchronize.setText(R.string.jandi_syncing_message);
        }
    }

    public void onEventMainThread(EventUpdateInProgress event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            if (vgSynchronize.getVisibility() != View.VISIBLE) {
                vgSynchronize.setVisibility(View.VISIBLE);
            }
            int percent = (event.getProgress() * 100) / event.getMax();
            String syncMsg = JandiApplication.getContext().getString(R.string.jandi_syncing_message);
            tvSynchronize.setText(String.format(syncMsg + "...(%d%%)", percent));
        }
    }

    public void onEventMainThread(EventUpdateFinish event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            if (vgSynchronize.getVisibility() != View.GONE) {
                vgSynchronize.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(navigationDirection)) {
            drawerLayout.closeDrawer(navigationDirection);
            return;
        }

        Fragment fragment = getFragment(viewPager.getCurrentItem());
        if (fragment != null
                && fragment instanceof BackPressConsumer) {
            if (((BackPressConsumer) fragment).consumeBackPress()) {
                return;
            }
        }

        super.onBackPressed();
    }

    @Nullable
    private Fragment getFragment(int position) {
        try {
            Object item = tabPagerAdapter.instantiateItem(viewPager, position);
            if (item != null && item instanceof Fragment) {
                return (Fragment) item;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Easter Egg
    private void navigationEasterEggOpen() {
        int navigationPosition = JandiPreference.getNavigationPosition();
        if (navigationPosition == -1) {
            navigationPosition = Gravity.LEFT;
        }

        final int[] check = new int[]{navigationPosition};

        View root = LayoutInflater.from(this).inflate(R.layout.dialog_setup_navigation, null);
        View radioLeft = root.findViewById(R.id.radio_setup_navigation_left);
        View radioRight = root.findViewById(R.id.radio_setup_navigation_right);
        radioLeft.setSelected(navigationPosition == Gravity.LEFT);
        radioRight.setSelected(navigationPosition == Gravity.RIGHT);

        root.findViewById(R.id.btn_setup_navigation_left).setOnClickListener(v -> {
            radioLeft.setSelected(true);
            radioRight.setSelected(false);
            check[0] = Gravity.LEFT;
        });
        root.findViewById(R.id.btn_setup_navigation_right).setOnClickListener(v -> {
            radioRight.setSelected(true);
            radioLeft.setSelected(false);
            check[0] = Gravity.RIGHT;
        });

        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setView(root)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    JandiPreference.setNavigationPosition(check[0]);
                    initNavigationPosition();
                })
                .create().show();
    }

    // Easter Egg
    private void initNavigationPosition() {
        int gravity = JandiPreference.getNavigationPosition();
        if (gravity == -1) {
            gravity = Gravity.LEFT;
            JandiPreference.setNavigationPosition(gravity);
        }

        navigationDirection = gravity;

        DrawerLayout.LayoutParams params =
                ((DrawerLayout.LayoutParams) vgNavigationWrapper.getLayoutParams());
        if (params.gravity != navigationDirection) {
            params.gravity = navigationDirection;
            vgNavigationWrapper.setLayoutParams(params);
        }

        ViewGroup parent = (ViewGroup) badgeOverFlowMenu.getParent();
        if (navigationDirection == Gravity.LEFT) {
            parent.removeView(badgeOverFlowMenu);

            parent.addView(badgeOverFlowMenu, 0);

            ViewGroup.MarginLayoutParams toolbarLp = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            toolbarLp.leftMargin = 0;

            toolbar.setLayoutParams(toolbarLp);
        } else {
            parent.removeView(badgeOverFlowMenu);

            parent.addView(badgeOverFlowMenu);

            ViewGroup.MarginLayoutParams toolbarLp = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            toolbarLp.leftMargin = (int) UiUtils.getPixelFromDp(16f);

            toolbar.setLayoutParams(toolbarLp);
        }
    }
}
