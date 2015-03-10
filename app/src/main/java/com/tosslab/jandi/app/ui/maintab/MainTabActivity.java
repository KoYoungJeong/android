package com.tosslab.jandi.app.ui.maintab;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
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
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

import de.greenrobot.event.EventBus;

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

    @AfterViews
    void initView() {
        mContext = getApplicationContext();
        mEntityManager = EntityManager.getInstance(MainTabActivity.this);


        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(MainTabActivity.this).getSelectedTeamInfo();

        setupActionBar(selectedTeamInfo);

        // ViewPager
        View[] tabViews = new View[4];
        tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
        tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
        tabViews[2] = getLayoutInflater().inflate(R.layout.tab_file, null);
        tabViews[3] = getLayoutInflater().inflate(R.layout.tab_more, null);
        mMainTabPagerAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), tabViews);
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
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

    }

    private void setupActionBar(ResAccountInfo.UserTeam selectedTeamInfo) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(selectedTeamInfo.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Entity의 리스트를 획득하여 저장한다.
        EventBus.getDefault().register(this);
        getEntities();
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
            } else {
                getEntitiesFailed(getString(R.string.err_service_connection));
            }
        } catch (ResourceAccessException e) {
            log.error("connect failed", e);
            getEntitiesFailed(getString(R.string.err_service_connection));
        }
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
}
