package com.tosslab.toss.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.tosslab.toss.app.events.SelectCdpItemEvent;
import com.tosslab.toss.app.lists.CdpItem;
import com.tosslab.toss.app.lists.CdpItemManager;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Field;
import java.util.List;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
    @ViewById(R.id.fl_activity_main_container)
    FrameLayout flContainer;
    @ViewById(R.id.dl_activity_main_drawer)
    DrawerLayout mDrawer;

    @Extra
    public String myToken;

    @RestService
    TossRestClient tossRestClient;

    public CdpItemManager cdpItemManager;
    private ProgressWheel mProgressWheel;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentTitle = R.string.app_name;

    @AfterViews
    void initUi() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // ActionBar 아이콘으로 네비게이션 드로어 제어
        mDrawerToggle = new CustomActionBarDrawerToggle(this, mDrawer);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow_left, GravityCompat.START);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow_right, GravityCompat.END);
        mDrawer.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        // 네비게이션 드로어 터치 영역 확장
        expandRangeOfNavigationDrawerToggle();

        // Preference 추출
        SharedPreferences pref = getSharedPreferences("TossPref", 0);
        int cdpType = pref.getInt("cdpType", -1);
        int cdpId = pref.getInt("cdpId", -1);

        selectItem(0, cdpType, cdpId);  // 기본 프레그먼트 설정
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    public void onEvent(SelectCdpItemEvent event) {
        // 해당 이벤트는 LeftMenuFragment -> MessageListFragment 지만
        // 네비게이션 드로어를 닫아줘야 하기 때문에 후킹
        mDrawer.closeDrawers();
        SharedPreferences pref = getSharedPreferences("TossPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("cdpType", event.type);
        editor.putInt("cdpId", event.id);
        editor.commit();
    }

    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position, int cdpType, int cdpId) {
        BaseFragment baseFragment;
        switch (position) {
            case 0:
            default:
                baseFragment = MessageListFragment_
                        .builder()
                        .myToken(myToken)
                        .cdpId(cdpId)
                        .cdpType(cdpType)
                        .build();
                break;
        }
        openFragment(baseFragment);
        mDrawer.closeDrawers();
    }

    private void openFragment(BaseFragment baseFragment) {
        if (baseFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fl_activity_main_container, baseFragment)
                    .commit();
            if (baseFragment.getTitleResourceId() > 0) {
                mCurrentTitle = baseFragment.getTitleResourceId();
            }
        }
    }

    /**
     * Option Menu 버튼을 누를 경우 네비게이션 드로어 전개
     * @param keycode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_MENU) {
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawers();
            }
            else {
                mDrawer.openDrawer(GravityCompat.START);
            }
        }
        return super.onKeyDown(keycode,event);
    }

    /**
     * 화면 중앙에서도 화면을 드래그하면 네비게이션이 뜰 수 있게
     * 네비게이션 드로어의 스와이프 제스쳐 적용 범위를 기존보다 4배로 확장
     */
    void expandRangeOfNavigationDrawerToggle() {
        try {
            // 왼쪽 3배 확장
            Field dragger = mDrawer.getClass().getDeclaredField("mLeftDragger");
            dragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) dragger.get(mDrawer);

            Field edgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            edgeSize.setAccessible(true);
            int edge = edgeSize.getInt(draggerObj);
            edgeSize.setInt(draggerObj, edge * 3);

            // 오른쪽 3배 확장
            dragger = mDrawer.getClass().getDeclaredField("mRightDragger");
            dragger.setAccessible(true);
            draggerObj = (ViewDragHelper) dragger.get(mDrawer);

            edgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            edgeSize.setAccessible(true);
            edge = edgeSize.getInt(draggerObj);
            edgeSize.setInt(draggerObj, edge * 3);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 액션바 토글 재정의
     */
    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {
        private CustomActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout) {
            super(
                    activity,
                    drawerLayout,
                    R.drawable.ic_drawer,
                    R.string.navigation_drawer_open,
                    mCurrentTitle);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            ActionBar bar = getActionBar();
            bar.setTitle(getString(R.string.app_name));
//            super.onDrawerOpened(drawerView);
//
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            ActionBar bar = getActionBar();
            bar.setTitle(getString(mCurrentTitle));
//            super.onDrawerClosed(drawerView);

            invalidateOptionsMenu();
        }
    }
}
