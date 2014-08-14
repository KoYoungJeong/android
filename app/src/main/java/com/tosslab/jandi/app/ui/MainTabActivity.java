package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.ui.events.ReadyToRetrievePrivateGroupList;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveUserList;
import com.tosslab.jandi.app.ui.events.RetrieveChannelList;
import com.tosslab.jandi.app.ui.events.RetrievePrivateGroupList;
import com.tosslab.jandi.app.ui.events.RetrieveUserList;
import com.tosslab.jandi.app.ui.lists.EntityManager;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EActivity(R.layout.activity_main_tab)
public class MainTabActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(MainTabActivity.class);

    @RestService
    TossRestClient mTossRestClient;

    private String mMyToken;
    private ProgressWheel mProgressWheel;
    private Context mContext;

    private EntityManager mEntityManager;

    private MainTabPagerAdapter mMainTabPagerAdapter;
    private ViewPager mViewPager;

    private boolean isReadyToRetrieveEntityList = false;

    @AfterViews
    void initView() {
        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        // Create the dapter and ViewPager
        mMainTabPagerAdapter = new MainTabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        mViewPager.setAdapter(mMainTabPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // add a tab to the action bar
        addTabToActionBar(actionBar, tabListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        // Entity의 리스트를 획득하여 저장한다.
        getEntities();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private void addTabToActionBar(ActionBar actionBar, ActionBar.TabListener tabListener) {
        for (int i = 0; i < mMainTabPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                        .setText(mMainTabPagerAdapter.getPageTitle(i))
                        .setTabListener(tabListener)
            );
        }
    }

    public void onEvent(ReadyToRetrieveChannelList event) {
        log.debug("onEvent : ReadyToRetrieveChannelList");
        if (isReadyToRetrieveEntityList) {
            postShowChannelListEvent();
        }
    }

    public void onEvent(ReadyToRetrieveUserList event) {
        log.debug("onEvent : ReadyToRetrieveUserList");
        if (isReadyToRetrieveEntityList) {
            postShowUserListEvent();
        }
    }

    public void onEvent(ReadyToRetrievePrivateGroupList event) {
        log.debug("onEvent : ReadyToRetrievePrivateGroupList");
        if (isReadyToRetrieveEntityList) {
            postShowPrivateGroupListEvent();
        }
    }

    /************************************************************
     * Entities List Update / Refresh
     ************************************************************/

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @UiThread
    public void getEntities() {
        mProgressWheel.show();
        getEntitiesInBackground();
    }

    @Background
    public void getEntitiesInBackground() {
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            ResLeftSideMenu resLeftSideMenu = mTossRestClient.getInfosForSideMenu();
            getEntitiesDone(true, resLeftSideMenu, null);
        } catch (Exception e) {
            Log.e("HI", "Get Fail", e);
            getEntitiesDone(false, null, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        }
    }

    @UiThread
    public void getEntitiesDone(boolean isOk, ResLeftSideMenu resLeftSideMenu, String errMessage) {
        log.debug("getEntitiesDone");
        mProgressWheel.dismiss();
        if (isOk) {
            mEntityManager = new EntityManager(resLeftSideMenu);
            isReadyToRetrieveEntityList = true;
            postAllEvents();
        } else {
            ColoredToast.showError(mContext, errMessage);
            returnToLoginActivity();
        }
    }

    private void postAllEvents() {
        postShowChannelListEvent();
        postShowUserListEvent();
        postShowPrivateGroupListEvent();
    }

    private void postShowChannelListEvent() {
        EventBus.getDefault().post(
                new RetrieveChannelList(mEntityManager.getFormattedChannels())
        );
    }

    private void postShowUserListEvent() {
        EventBus.getDefault().post(new RetrieveUserList(mEntityManager.getUsersWithoutMe()));
    }

    private void postShowPrivateGroupListEvent() {
        EventBus.getDefault().post(
                new RetrievePrivateGroupList(mEntityManager.getFormattedPrivateGroups())
        );
    }

    public EntityManager getEntityManager() {
        return mEntityManager;
    }
}
