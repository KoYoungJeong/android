package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
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
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.ResourceAccessException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseAnalyticsActivity {
    private final Logger log = Logger.getLogger(MainTabActivity.class);

    @RestService
    JandiRestClient mJandiRestClient;

    private String mMyToken;
    private ProgressWheel mProgressWheel;
    private Context mContext;

    private EntityManager mEntityManager;
    @Bean
    JandiEntityClient mJandiEntityClient;

    private MainTabPagerAdapter mMainTabPagerAdapter;
    private ViewPager mViewPager;

    private boolean isFirst = true;    // TODO poor implementation

    @AfterViews
    void initView() {
        mContext = getApplicationContext();
        mEntityManager = ((JandiApplication) getApplication()).getEntityManager();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        // Network Client 설정

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // ViewPager
        View[] tabViews = new View[4];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_more, null);
        mMainTabPagerAdapter = new MainTabPagerAdapter(getFragmentManager(), tabViews);
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
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

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        // Push가 MainTabActivity를 보고 있을 때
        // 발생한다면 알람 카운트 갱신을 위한 BR 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(JandiConstants.PUSH_REFRESH_ACTION);
        registerReceiver(mRefreshEntities, intentFilter);

        // Entity의 리스트를 획득하여 저장한다.
        getEntities();
    }

    @Override
    public void onPause() {
        unregisterReceiver(mRefreshEntities);
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /************************************************************
     * Entities List Update / Refresh
     ************************************************************/

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
            // TODO Temp TeamId
            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo(1);
            getEntitiesSucceed(resLeftSideMenu);
        } catch (JandiNetworkException e) {
            log.error(e.getErrorInfo() + "get entity failed", e);
            getEntitiesFailed(getString(R.string.err_expired_session));
        } catch (ResourceAccessException e) {
            log.error("connect failed", e);
            getEntitiesFailed(getString(R.string.err_service_connection));
        }
    }

    @UiThread
    public void getEntitiesSucceed(ResLeftSideMenu resLeftSideMenu) {
        mProgressWheel.dismiss();
        mEntityManager = new EntityManager(resLeftSideMenu);
        ((JandiApplication) getApplication()).setEntityManager(mEntityManager);
        mEntityManager.subscribeChannelForParse();
        trackSigningIn(mEntityManager);
        getActionBar().setTitle(mEntityManager.getTeamName());
        JandiPreference.setMyEntityId(this, mEntityManager.getMe().getId());
        checkNewTabBadges(mEntityManager);
        setBadgeCount(mEntityManager);
        postAllEvents();
    }

    @UiThread
    public void getEntitiesFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
        returnToIntroStartActivity();
    }

    private void checkNewTabBadges(EntityManager entityManager) {
        if (entityManager == null) {
            return;
        }

        if (entityManager.hasNewTopicMessage()) {
            mMainTabPagerAdapter.showNewTopicBadge();
        } else {
            mMainTabPagerAdapter.hideNewTopicBadge();
        }
        if (entityManager.hasNewChatMessage()) {
            mMainTabPagerAdapter.showNewChatBadge();
        } else {
            mMainTabPagerAdapter.hideNewChatBadge();
        }
    }

    @SupposeUiThread
    void setBadgeCount(EntityManager entityManager) {
        int badgeCount = entityManager.getTotalBadgeCount();
        if (entityManager != null) {
            log.debug("Reset badge count to " + badgeCount);
            JandiPreference.setBadgeCount(this, badgeCount);
            BadgeUtils.setBadge(this, badgeCount);
        }
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

    /**
     * JandiGCMBroadcastReceiver로부터 Push가 들어왔다는 event가 MainTabActivity를 보고 있을 때
     * 발생한다면 알람 카운트 갱신을 위해 다시 받아온다.
     */
    private BroadcastReceiver mRefreshEntities = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getEntities();
        }
    };

    /**
     * ************************
     * TODO Settings 에 있는 것과 동일. 뺄까 ??
     */

    public void onEvent(SignOutEvent event) {
        if (mEntityManager != null)
            trackSignOut(mEntityManager.getDistictId());
        returnToLoginActivity();
    }

    public void returnToLoginActivity() {
        // Access Token 삭제
        JandiPreference.clearMyToken(mContext);

        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.remove(JandiConstants.PARSE_CHANNELS);
        parseInstallation.saveInBackground();

        Intent intent = new Intent(mContext, IntroActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
