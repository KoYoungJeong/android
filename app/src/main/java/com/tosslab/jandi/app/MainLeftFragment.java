package com.tosslab.jandi.app;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateCdpDialogFragment;
import com.tosslab.jandi.app.dialogs.SelectUnjoinedChannelFragment;
import com.tosslab.jandi.app.events.ConfirmCreateCdpEvent;
import com.tosslab.jandi.app.events.ConfirmJoinChannelEvent;
import com.tosslab.jandi.app.events.ConfirmModifyCdpEvent;
import com.tosslab.jandi.app.events.DeleteCdpEvent;
import com.tosslab.jandi.app.events.LeaveCdpEvent;
import com.tosslab.jandi.app.events.ModifyCdpEvent;
import com.tosslab.jandi.app.events.SelectCdpItemEvent;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemListAdapter;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResSendMessage;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_main_left)
public class MainLeftFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MainLeftFragment.class);

    private String mMyToken;

    @ViewById(R.id.list_cdps)
    ListView mListCdps;
    @Bean
    CdpItemListAdapter mCdpListAdapter;

    @RestService
    TossRestClient mTossRestClient;

    private ProgressWheel mProgressWheel;
    private CdpItemManager mCdpItemManager;
    private Context mContext;

    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        mListCdps.setAdapter(mCdpListAdapter);

        // C, D, P 리스트 획득
        getCdpItemFromServer();
    }

    /**
     * Event Listener로 등록
     */
    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /************************************************************
     * List 선택
     * 선택한 C, D, P 에 대한 메시지 리스트 획득 이벤트가
     * MessageListFragment로 전달됨.
     ************************************************************/
    @ItemClick
    void list_cdpsItemClicked(CdpItem cdp) {
        Log.e("HI", cdp.name + " Clicked. type is " + cdp.type);
        switch (cdp.type) {
            case JandiConstants.TYPE_TITLE_JOINED_CHANNEL:
                // Joined channel 의 제목부를 터치했을 경우, 채널 생성 메뉴로...
                showDialogToCreate(JandiConstants.TYPE_CHANNEL);
                break;
            case JandiConstants.TYPE_TITLE_UNJOINED_CHANNEL:
                // 비등록 체널의 목록을 보여주고 join 할 수 있게 함.
                showDialogToJoinChannel();
                break;
            case JandiConstants.TYPE_TITLE_DIRECT_MESSAGE:
                // DO NOTHING
                break;
            case JandiConstants.TYPE_TITLE_PRIVATE_GROUP:
                // Private Group 생성 메뉴로...
                showDialogToCreate(JandiConstants.TYPE_PRIVATE_GROUP);
                break;
            default:
                // 일반 CDP 를 터치했을 경우, 해당 CDP의 메시지 리스트를 획득할수 있게 이벤트 등록
                SelectCdpItemEvent event = new SelectCdpItemEvent(cdp.name, cdp.type, cdp.id);
                EventBus.getDefault().post(event);
                break;
        }

    }

    @ItemLongClick
    void list_cdpsItemLongClicked(CdpItem cdp) {
        switch (cdp.type) {
            case JandiConstants.TYPE_TITLE_JOINED_CHANNEL:
            case JandiConstants.TYPE_TITLE_UNJOINED_CHANNEL:
            case JandiConstants.TYPE_TITLE_PRIVATE_GROUP:
            case JandiConstants.TYPE_TITLE_DIRECT_MESSAGE:
                // DO NOTHING
                break;
            default:
                showDialogToManipulate(cdp);
                break;
        }
    }

    /************************************************************
     * Channel Join
     ************************************************************/
    private void showDialogToJoinChannel() {
        DialogFragment newFragment = new SelectUnjoinedChannelFragment();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(ConfirmJoinChannelEvent event) {
        joinChannelInBackground(event.channelId);
    }

    @Background
    public void joinChannelInBackground(int selectedChannelIdToBeJoined) {
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            ResSendMessage res = mTossRestClient.joinChannel(selectedChannelIdToBeJoined);
            joinChannelDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to join channel");
            joinChannelDone(false, "해당 채널 가입에 실패하였습니다.");
        } catch (Exception e) {
            log.error("fail to join channel");
            joinChannelDone(false, "해당 채널 가입에 실패하였습니다.");
        }

    }

    @UiThread
    public void joinChannelDone(boolean isOk, String message) {
        if (isOk) {
            getCdpItemFromServer();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * List Update / Refresh
     ************************************************************/
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
            refreshCdpList(resLeftSideMenu);
        } else {
            ColoredToast.showError(mContext, message);
            returnToLoginActivity();
        }
    }

    public void refreshCdpList(ResLeftSideMenu resLeftSideMenu) {
        mCdpItemManager = new CdpItemManager(resLeftSideMenu);
        ((MainActivity)getActivity()).cdpItemManager = mCdpItemManager;
        mCdpListAdapter.retrieveCdpItems(mCdpItemManager);
        mCdpListAdapter.notifyDataSetChanged();
    }

    public void returnToLoginActivity() {
        JandiPreference.clearMyToken(mContext);
        Intent intent = new Intent(mContext, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /************************************************************
     * Channel, PrivateGroup 생성
     ************************************************************/

    /**
     * Alert Dialog 관련
     */
    void showDialogToCreate(int cdpType) {
        DialogFragment newFragment
                = EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_CREATE_CDP
                , cdpType
                , 0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Channel, PrivateGroup 생성 이벤트 획득 from DialogFragment
     * @param event
     */
    public void onEvent(ConfirmCreateCdpEvent event) {
        switch (event.cdpType) {
            case JandiConstants.TYPE_CHANNEL:
                requestCreateChannel(event.inputName);
                break;
            case JandiConstants.TYPE_PRIVATE_GROUP:
                requestCreatePrivateGroup(event.inputName);
                break;
            default:
                break;
        }
    }

    /**
     * Channel 생성
     */
    @Background
    void requestCreateChannel(String channelName) {
        // TODO : Error 처리
        if (channelName.length() <= 0) {
            return;
        }
        ReqCreateCdp reqCreateCdp = new ReqCreateCdp();
        reqCreateCdp.name = channelName;

        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            restResId = mTossRestClient.createChannel(reqCreateCdp);
            getCdpItemFromServer();
            log.debug("Create Success");
        } catch (RestClientException e) {
            log.error("Create Fail", e);
        }
    }

    /**
     * Private Group 생성
     */
    @Background
    void requestCreatePrivateGroup(String pgName) {
        // TODO : Error 처리
        if (pgName.length() <= 0) {
            return;
        }
        ReqCreateCdp reqCreateCdp = new ReqCreateCdp();
        reqCreateCdp.name = pgName;

        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            restResId = mTossRestClient.createPrivateGroup(reqCreateCdp);
            getCdpItemFromServer();
            log.debug("Create Success");
        } catch (RestClientException e) {
            log.error("Create Fail", e);
        }
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
            joinChannelDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to leave cdp");
            joinChannelDone(false, "탈퇴에 실패했습니다.");
        } catch (Exception e) {
            log.error("fail to leave cdp");
            joinChannelDone(false, "탈퇴에 실패했습니다.");
        }
    }
}
