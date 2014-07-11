package com.tosslab.jandi.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.tosslab.jandi.app.events.RefreshCdpListEvent;
import com.tosslab.jandi.app.events.RequestCdpListEvent;
import com.tosslab.jandi.app.events.SelectCdpItemEvent;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

@EActivity
public class MainActivity extends SlidingFragmentActivity {
    private final Logger log = Logger.getLogger(MainActivity.class);

    @RestService
    TossRestClient mTossRestClient;

    private String mMyToken;
    public CdpItemManager mCdpItemManager;
    private ProgressWheel mProgressWheel;
    private String mCurrentTitle;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.frame_main);
        drawFragments();

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // Preference 추출
        SharedPreferences pref = getSharedPreferences(JandiConstants.PREF_NAME, 0);
        String cdpName = pref.getString("cdpName", "");
        int cdpType = pref.getInt("cdpType", -1);
        int cdpId = pref.getInt("cdpId", -1);

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        getCdpItemFromServer();
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

    void drawFragments() {
        BaseFragment baseFragment = MainCenterFragment_.builder().build();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, baseFragment)
                .commit();

        // 왼쪽 Fragment - CDP List
        setBehindContentView(R.layout.frame_menu);
        BaseFragment menuLeftFragment = MainCdpFragment_.builder().build();
        getFragmentManager().beginTransaction().replace(R.id.menu_frame, menuLeftFragment).commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.drawer_shadow_left);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        // 오른쪽 Fragment - File Search
        BaseFragment menuRightFragment = MainCdpFragment_.builder().build();
        sm.setSecondaryMenu(R.layout.frame_menu);
        sm.setSecondaryShadowDrawable(R.drawable.drawer_shadow_right);
        getFragmentManager().beginTransaction().replace(R.id.menu_frame, menuRightFragment).commit();
    }


    /**
     * 해당 이벤트는 MainLeftFragment -> MainMessageListFragment 지만
     * 네비게이션 드로어를 닫아줘야 하기 때문에 후킹
     * @param event
     */
    public void onEvent(SelectCdpItemEvent event) {
        log.debug("EVENT : from MainLeftFragment : SelectCdpItemEvent");

        mCurrentTitle = FormatConverter.cdpName(event.name, event.type);

        // Preference 저장
        SharedPreferences pref = getSharedPreferences(JandiConstants.PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("cdpName", event.name);
        editor.putInt("cdpType", event.type);
        editor.putInt("cdpId", event.id);
        editor.commit();

        getSlidingMenu().showContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_main_right_drawer) {
            getSlidingMenu().showSecondaryMenu();
            return true;
        } else if (item.getItemId() == R.id.action_main_manipulate_cdp) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /************************************************************
     * List Update / Refresh
     ************************************************************/
    public void onEvent(RequestCdpListEvent event) {
        getCdpItemFromServer();
    }

    /**
     * 해당 사용자의 채널, DM, PG 리스트를 획득 (with 통신)
     */
    @UiThread
    public void getCdpItemFromServer() {
        mProgressWheel.show();
        getCdpItemInBackground();
    }

    @Background
    public void getCdpItemInBackground() {
        ResLeftSideMenu resLeftSideMenu = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            resLeftSideMenu = mTossRestClient.getInfosForSideMenu();
            getCdpItemDone(true, resLeftSideMenu, null);
        } catch (RestClientException e) {
            Log.e("HI", "Get Fail", e);
            getCdpItemDone(false, null, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        } catch (HttpMessageNotReadableException e) {
            Log.e("HI", "Get Fail", e);
            getCdpItemDone(false, null, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        } catch (Exception e) {
            Log.e("HI", "Get Fail", e);
            getCdpItemDone(false, null, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        }
    }

    @UiThread
    public void getCdpItemDone(boolean isOk, ResLeftSideMenu resLeftSideMenu, String message) {
        mProgressWheel.dismiss();
        if (isOk) {
            mCdpItemManager = new CdpItemManager(resLeftSideMenu);
            EventBus.getDefault().post(new RefreshCdpListEvent(mCdpItemManager));
        } else {
            ColoredToast.showError(mContext, message);
            returnToLoginActivity();
        }
    }

    public void returnToLoginActivity() {
        JandiPreference.clearMyToken(mContext);
        Intent intent = new Intent(mContext, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
