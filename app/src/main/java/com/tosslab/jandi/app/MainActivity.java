package com.tosslab.jandi.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateCdpDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyCdpEvent;
import com.tosslab.jandi.app.events.DeleteCdpEvent;
import com.tosslab.jandi.app.events.InviteCdpEvent;
import com.tosslab.jandi.app.events.LeaveCdpEvent;
import com.tosslab.jandi.app.events.ModifyCdpEvent;
import com.tosslab.jandi.app.events.RefreshCdpListEvent;
import com.tosslab.jandi.app.events.RequestCdpListEvent;
import com.tosslab.jandi.app.events.RequestMessageListEvent;
import com.tosslab.jandi.app.events.SelectCdpItemEvent;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.lists.CdpSelectListAdapter;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResSendMessage;
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

import java.util.List;

import de.greenrobot.event.EventBus;

@EActivity
public class MainActivity extends SlidingFragmentActivity {
    private final Logger log = Logger.getLogger(MainActivity.class);

    @RestService
    TossRestClient mTossRestClient;

    private String mMyToken;
    public CdpItemManager mCdpItemManager;
    private ProgressWheel mProgressWheel;
    private Context mContext;
    private CdpItem mCurrentSelectedCdpItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.frame_main);

        drawFragments();

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

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

    /**
     * Activity에 붙일 Fragments 를 설정한다.
     * 왼쪽 - CDP List
     * 중앙 - Message List
     * 오른쪽 - File Search & ETC
     */
    void drawFragments() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        // 중앙 Fragment - Message List
        BaseFragment baseFragment = MainCenterFragment_.builder().build();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, baseFragment)
                .commit();

        // 왼쪽 Fragment - CDP List
        setBehindContentView(R.layout.frame_cdp);
        BaseFragment menuLeftFragment = MainCdpFragment_.builder().build();
        getFragmentManager().beginTransaction().replace(R.id.cdp_frame, menuLeftFragment).commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.drawer_shadow_left);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        // 오른쪽 Fragment - File Search
        BaseFragment menuRightFragment = MainRightFragment_.builder().build();
        sm.setSecondaryMenu(R.layout.frame_menu);
        sm.setSecondaryShadowDrawable(R.drawable.drawer_shadow_right);
        getFragmentManager().beginTransaction().replace(R.id.menu_frame, menuRightFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSlidingMenu().showMenu();
                return true;
            case R.id.action_main_right_drawer:
                getSlidingMenu().showSecondaryMenu();
                return true;
            case R.id.action_main_manipulate_cdp:
                showDialogToManipulate(mCurrentSelectedCdpItem);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Event from MainCdpFragment
     * 선택한 CDP 정보를 저장하고 MainCenterFragment에 전달한다.
     * 슬라이딩 메뉴를 닫고, 선택한 CDP 이름을 타이틀바에 셋팅
     * @param event
     */
    public void onEvent(SelectCdpItemEvent event) {
        log.debug("EVENT : from MainLeftFragment : SelectCdpItemEvent");

        // Preference 저장
        SharedPreferences pref = getSharedPreferences(JandiConstants.PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("cdpName", event.cdpItem.name);
        editor.putInt("cdpType", event.cdpItem.type);
        editor.putInt("cdpId", event.cdpItem.id);
        editor.commit();

        getSlidingMenu().showContent();
        getMessageListOfSelectedCdp();
    }

    /************************************************************
     * CDP List Update / Refresh
     ************************************************************/

    /**
     * event from MainCdpFragment
     * CDP List에서 생성, 삭제 등의 통신이 일어난 이후에 List를 refresh 하기 위한 이벤트
     * @param event
     */
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
            getMessageListOfSelectedCdp();
        } else {
            ColoredToast.showError(mContext, message);
            returnToLoginActivity();
        }
    }

    /**
     * MainCenterFragment에 Message List 출력을 위한 작업 수행
     * 만약 이전에 저장한 CDP 가 있으면 해당 CDP의 Message List를 출력한다.
     * 그게 없으면 Join Channel의 첫번째...
     */
    public void getMessageListOfSelectedCdp() {
        // Preference 추출
        SharedPreferences pref = getSharedPreferences(JandiConstants.PREF_NAME, 0);
        String cdpName = pref.getString("cdpName", "");
        int cdpType = pref.getInt("cdpType", -1);
        int cdpId = pref.getInt("cdpId", -1);

        if (cdpId > 0) {
            mCurrentSelectedCdpItem = mCdpItemManager.getCdpItemById(cdpId);
            if (mCurrentSelectedCdpItem != null) {
                getActionBar().setTitle(FormatConverter.cdpName(cdpName, cdpType));
                EventBus.getDefault().post(new RequestMessageListEvent(cdpType, cdpId));
                return;
            }
        }

        mCurrentSelectedCdpItem = mCdpItemManager.getDefaultChannel();
        getActionBar().setTitle(FormatConverter.cdpName(mCurrentSelectedCdpItem.name
                , mCurrentSelectedCdpItem.type));
        EventBus.getDefault().post(new RequestMessageListEvent(mCurrentSelectedCdpItem.type
                , mCurrentSelectedCdpItem.id));
    }

    public void returnToLoginActivity() {
        JandiPreference.clearMyToken(mContext);
        Intent intent = new Intent(mContext, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /************************************************************
     * Channel, PrivateGroup 수정 / 삭제 / Leave
     ************************************************************/

    void showDialogToManipulate(CdpItem cdp) {
        // 아래와 같은 조건에서...
        // Channel : owner 가 아니면 leave 만 나타남
        // DM : 아무것도...
        // PG : owner 가 아니면 leave 만 나타남
        if (cdp.type == JandiConstants.TYPE_DIRECT_MESSAGE) {
            showWarning("수정, 삭제가 불가능합니다.");
            return;
        }
        log.debug("Try to manipulate cdp owned by user, " + cdp.ownerId);
        boolean isMyCdp = false;
        if (cdp.ownerId == mCdpItemManager.mMe.id) {
            isMyCdp = true;
        }
        DialogFragment newFragment = ManipulateCdpDialogFragment.newInstance(cdp, isMyCdp);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @UiThread
    void showWarning(String message) {
        ColoredToast.showWarning(mContext, message);
    }

    /************************************************************
     * Channel, PrivateGroup 수정
     ************************************************************/
    public void onEvent(ModifyCdpEvent event) {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_CDP
                , event.cdpType
                , event.cdpId
                , event.currentName);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * 수정 이벤트 획득 from EditTextDialogFragment
     */
    public void onEvent(ConfirmModifyCdpEvent event) {
        modifyCdp(event);
    }

    @UiThread
    void modifyCdp(ConfirmModifyCdpEvent event) {
        modifyCdpInBackground(event);
    }

    @Background
    void modifyCdpInBackground(ConfirmModifyCdpEvent event) {
        if (event.cdpType == JandiConstants.TYPE_CHANNEL) {
            modifyChannelInBackground(event.cdpId, event.inputName);
        } else if (event.cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
            modifyGroupInBackground(event.cdpId, event.inputName);
        }
    }

    void modifyChannelInBackground(int cdpId, String nameToBeModified) {
        ResSendMessage resId = null;
        ReqCreateCdp channel = new ReqCreateCdp();
        channel.name = nameToBeModified;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            resId = mTossRestClient.modifyChannel(channel, cdpId);
        } catch (RestClientException e) {
            log.error("modify failed", e);
        }
        modifyCdpDone();
    }

    void modifyGroupInBackground(int cdpId, String nameToBeModified) {
        ResSendMessage resId = null;
        ReqCreateCdp privateGroup = new ReqCreateCdp();
        privateGroup.name = nameToBeModified;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            resId = mTossRestClient.modifyGroup(privateGroup, cdpId);
        } catch (RestClientException e) {
            log.error("modify failed", e);
        }
        modifyCdpDone();
    }

    @UiThread
    void modifyCdpDone() {
        mProgressWheel.dismiss();
        getCdpItemFromServer();
    }

    /************************************************************
     * Channel, PrivateGroup 삭제
     ************************************************************/
    /**
     * 삭제 이벤트 획득 from DialogFragment
     * @param event
     */
    public void onEvent(DeleteCdpEvent event) {
        log.debug("Delete Cdp :" + event.cdpId);
        deleteCdp(event);
    }

    @UiThread
    void deleteCdp(DeleteCdpEvent event) {
        mProgressWheel.show();
        deleteCdpInBackground(event);
    }

    @Background
    void deleteCdpInBackground(DeleteCdpEvent event) {
        if (event.cdpType == JandiConstants.TYPE_CHANNEL) {
            deleteChannelInBackground(event.cdpId);
        } else if (event.cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
            deleteGroupInBackground(event.cdpId);
        }
    }

    void deleteChannelInBackground(int cdpId) {
        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            restResId = mTossRestClient.deleteChannel(cdpId);
            log.debug("delete Success");
        } catch (RestClientException e) {
            log.error("delete Fail", e);
        }
        deleteCdpDone();
    }

    void deleteGroupInBackground(int cdpId) {
        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            restResId = mTossRestClient.deleteGroup(cdpId);
            log.debug("delete Success");
        } catch (RestClientException e) {
            log.error("delete Fail", e);
        }
        deleteCdpDone();
    }

    @UiThread
    void deleteCdpDone() {
        mProgressWheel.dismiss();
        getCdpItemFromServer();
    }

    /************************************************************
     * Channel, PrivateGroup Invite
     ************************************************************/
    public void onEvent(InviteCdpEvent event) {
        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<CdpItem> unjoinedMembers = mCdpItemManager.getUnjoinedMembersByChoosenCdp(mCurrentSelectedCdpItem);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(mContext, "이미 모든 사용자가 가입되어 있습니다.");
            return;
        }

        final CdpSelectListAdapter adapter = new CdpSelectListAdapter(this, unjoinedMembers);
        lv.setAdapter(adapter);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setIcon(android.R.drawable.ic_menu_agenda);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_cdp_invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Integer> selectedCdp = adapter.getSelectedCdpIds();
                for (int item : selectedCdp) {
                    log.debug("CDP ID, " + item + " is Selected");
                }
                inviteCdpInBackground(mCurrentSelectedCdpItem.type, mCurrentSelectedCdpItem.id, selectedCdp);
            }
        });
        dialog.show();

    }

    @Background
    public void inviteCdpInBackground(int cdpType, int cdpId, List<Integer> invitedUsers) {
        ResSendMessage res = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
            if (cdpType == JandiConstants.TYPE_CHANNEL) {
                res = mTossRestClient.inviteChannel(cdpId, reqInviteUsers);
            } else if (cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
                res = mTossRestClient.inviteGroup(cdpId, reqInviteUsers);
            }
            inviteCdpDone(true, invitedUsers.size() + "명의 사용자를 초대했습니다.");
        } catch (RestClientException e) {
            log.error("fail to invite cdp");
            inviteCdpDone(false, "초대에 실패했습니다.");
        } catch (Exception e) {
            log.error("fail to invite cdp");
            inviteCdpDone(false, "초대에 실패했습니다.");
        }
    }

    @UiThread
    public void inviteCdpDone(boolean isOk, String message) {
        if (isOk) {
            ColoredToast.show(mContext, message);
            getCdpItemFromServer();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * Channel, PrivateGroup Leave
     ************************************************************/
    // receieve event from ManipulateCdpDialogFragment
    public void onEvent(LeaveCdpEvent event) {
        leaveCdpInBackground(event.cdpType, event.cdpId);
    }

    @Background
    public void leaveCdpInBackground(int cdpType, int cdpId) {
        ResSendMessage res = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            if (cdpType == JandiConstants.TYPE_CHANNEL) {
                res = mTossRestClient.leaveChannel(cdpId);
            } else if (cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
                res = mTossRestClient.leaveGroup(cdpId);
            }
            leaveCdpDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to leave cdp");
            leaveCdpDone(false, "탈퇴에 실패했습니다.");
        } catch (Exception e) {
            log.error("fail to leave cdp");
            leaveCdpDone(false, "탈퇴에 실패했습니다.");
        }
    }

    @UiThread
    public void leaveCdpDone(boolean isOk, String message) {
        if (isOk) {
            getCdpItemFromServer();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }
}
