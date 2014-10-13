package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.CategorizingAsEntity;
import com.tosslab.jandi.app.events.CategorizingAsOwner;
import com.tosslab.jandi.app.events.RetrieveChattingListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.UserEntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.files.FileTypeSimpleListAdapter;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.PagerSlidingTabStrip;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

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
    private JandiEntityClient mJandiEntityClient;
    private MainTabPagerAdapter mMainTabPagerAdapter;
    private ViewPager mViewPager;

    private boolean isFirst = true;    // TODO poor implementation

    @AfterViews
    void initView() {
        mContext = getApplicationContext();
        mEntityManager = ((JandiApplication)getApplication()).getEntityManager();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        // Network Client 설정
        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, mMyToken);

        final ActionBar actionBar = getActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // ViewPager
        mMainTabPagerAdapter = new MainTabPagerAdapter(mContext, getFragmentManager());
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
            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo();
            getEntitiesDone(true, resLeftSideMenu, null);
        } catch (JandiNetworkException e) {
            log.error("get entity failed", e);
            getEntitiesDone(false, null, getString(R.string.err_expired_session));
        } catch (ResourceAccessException e) {
            log.error("connect failed", e);
            getEntitiesDone(false, null, getString(R.string.err_service_connection));
        }
    }

    @UiThread
    public void getEntitiesDone(boolean isOk, ResLeftSideMenu resLeftSideMenu, String errMessage) {
        log.debug("getEntitiesDone");
        if (isOk) {
            mEntityManager = new EntityManager(resLeftSideMenu);
            ((JandiApplication)getApplication()).setEntityManager(mEntityManager);
            trackSigningIn(mEntityManager);
            getActionBar().setTitle(mEntityManager.getTeamName());
            postAllEvents();
        } else {
            ColoredToast.showError(mContext, errMessage);
            returnToIntroStartActivity();
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
        EventBus.getDefault().post(new RetrieveChattingListEvent());
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
}
