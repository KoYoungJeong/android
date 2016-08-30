package com.tosslab.jandi.app.ui.maintab;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.NavigationBadgeEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.poll.RefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.events.socket.EventUpdateFinish;
import com.tosslab.jandi.app.events.socket.EventUpdateInProgress;
import com.tosslab.jandi.app.events.socket.EventUpdateStart;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor_;
import com.tosslab.jandi.app.ui.maintab.component.DaggerMainTabComponent;
import com.tosslab.jandi.app.ui.maintab.module.MainTabModule;
import com.tosslab.jandi.app.ui.maintab.navigation.widget.BadgeOverFlowMenu;
import com.tosslab.jandi.app.ui.maintab.presenter.MainTabPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.TabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.ChatTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.MypageTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.TopicTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.util.BackPressConsumer;
import com.tosslab.jandi.app.ui.maintab.tabs.util.TabFactory;
import com.tosslab.jandi.app.ui.maintab.tabs.util.fab.FloatingActionButtonController;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.profile.insert.InsertProfileActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.TabView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabActivity extends BaseAppCompatActivity implements MainTabPresenter.View {

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

    @Inject
    MainTabPresenter mainTabPresenter;

    boolean fromPush = false;
    int tabIndex = 0;
    long selectedEntity = -1;

    private OfflineLayer offlineLayer;

    private TabView tabTopic;
    private TabView tabChat;
    private TabView tabMyPage;
    private MainTabPagerAdapter tabPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        if (AccountRepository.getRepository().getSelectedTeamInfo() == null) {
            finish();
            return;
        }

        injectComponent();

        ButterKnife.bind(this);

        initToolbars();

        initOffLineLayer();

        mainTabPresenter.onCheckIfNotLastestVersion(() -> {
            if (isFinishing()) {
                return;
            }

            startSocketService();

            initTabs();

//            initTabBadges();

            checkIfNotProfileSetUp();

            showInvitePopupIfNeed();

            EventBus.getDefault().register(this);
        });
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

            InvitationDialogExecutor invitationDialogExecutor =
                    InvitationDialogExecutor_.getInstance_(getBaseContext());
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

    private void initTabBadges() {
        mainTabPresenter.onInitTopicBadge();
        mainTabPresenter.onInitChatBadge();
        mainTabPresenter.onInitMyPageBadge();
    }

    private void removeAllSavedFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            Fragment fragment = fragmentManager.getFragments().get(i);
            ft.remove(fragment);
        }
        ft.commit();
        fragmentManager.executePendingTransactions();
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

    private void initTabs() {
        List<TabInfo> tabInfos = TabFactory.getTabs(selectedEntity);

        tabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabInfos);
        viewPager.setOffscreenPageLimit(tabInfos.size());
        viewPager.setAdapter(tabPagerAdapter);

        Observable.from(tabInfos)
                .subscribe(tabInfo -> {
                    TabView tabView = tabInfo.getTabView(getLayoutInflater(), tabLayout);
                    initTabView(tabInfo, tabView);

                    int index = tabInfo.getIndex();
                    boolean isFirstTab = tabIndex == tabInfo.getIndex();
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(tabInfo.getTitle())
                            .setCustomView(tabView), index, isFirstTab);
                });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                viewPager.setCurrentItem(position);

                Fragment fragment = tabPagerAdapter.getItem(position);
                if (fragment != null && fragment instanceof FloatingActionButtonController) {
                    ((FloatingActionButtonController) fragment).onFloatingActionButtonProvided(btnFab);
                } else {
                    btnFab.setOnClickListener(null);
                }

                if (fragment != null && fragment instanceof MainTabPagerAdapter.OnItemFocused) {
                    ((MainTabPagerAdapter.OnItemFocused) fragment).onItemFocused(true);
                }

                tvTitle.setText(tab.getText());
                vTopShadow.setVisibility(position == MypageTabInfo.INDEX ? View.GONE : View.VISIBLE);
                btnFab.setVisibility(position == TopicTabInfo.INDEX || position == ChatTabInfo.INDEX
                        ? View.VISIBLE : View.GONE);
                ColoredToast.show("Hello - " + getSupportFragmentManager().getFragments().size());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Fragment fragment = tabPagerAdapter.getItem(tab.getPosition());
                if (fragment != null && fragment instanceof MainTabPagerAdapter.OnItemFocused) {
                    ((MainTabPagerAdapter.OnItemFocused) fragment).onItemFocused(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


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

    @OnClick(R.id.btn_main_tab_menu)
    void openDrawer() {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    public void onEventMainThread(NavigationBadgeEvent event) {
        int badgeCount = event.getBadgeCount();

        if (badgeCount <= 0) {
            badgeOverFlowMenu.hideBadge();
            return;
        }

        badgeOverFlowMenu.showBadge();
        badgeOverFlowMenu.setBadgeText(Integer.toString(badgeCount));
    }

    public void onEventMainThread(TopicBadgeEvent event) {
        int count = event.getCount();
        setTopicBadge(count);
    }

    public void onEventMainThread(ChatBadgeEvent event) {
        int count = event.getCount();
        setChatBadge(count);
    }

    public void onEventMainThread(RefreshPollBadgeCountEvent event) {
        int count = event.getBadgeCount();
        setMypageBadge(count);
    }

    public void onEventMainThread(TeamInfoChangeEvent event) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo != null) {
            tvTitle.setText(selectedTeamInfo.getName());
        }
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

        fromPush = false;
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
        tabTopic.setBadgeText(Integer.toString(count));
    }

    @Override
    public void setChatBadge(int count) {
        if (count <= 0) {
            tabChat.hideBadge();
            return;
        }
        tabChat.showBadge();
        tabChat.setBadgeText(Integer.toString(count));
    }

    @Override
    public void setMypageBadge(int count) {
        if (count <= 0) {
            tabMyPage.hideBadge();
            return;
        }
        tabMyPage.showBadge();
        tabMyPage.setBadgeText(Integer.toString(count));
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
        if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }

        Fragment fragment = tabPagerAdapter.getItem(viewPager.getCurrentItem());
        if (fragment != null
                && fragment instanceof BackPressConsumer) {
            if (((BackPressConsumer) fragment).consumeBackPress()) {
                return;
            }
            super.onBackPressed();
        }

        super.onBackPressed();
    }

}
