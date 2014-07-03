package com.tosslab.toss.app;

import android.app.DialogFragment;
import android.graphics.Color;
import android.util.Log;
import android.widget.ListView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.toss.app.dialogs.EditTextDialogFragment;
import com.tosslab.toss.app.dialogs.ManipulateCdpDialogFragment;
import com.tosslab.toss.app.events.ConfirmCreateCdpEvent;
import com.tosslab.toss.app.events.ConfirmModifyCdpEvent;
import com.tosslab.toss.app.events.DeleteCdpEvent;
import com.tosslab.toss.app.events.ModifyCdpEvent;
import com.tosslab.toss.app.events.SelectCdpItemEvent;
import com.tosslab.toss.app.lists.CdpItem;
import com.tosslab.toss.app.lists.CdpItemListAdapter;
import com.tosslab.toss.app.lists.CdpItemManager;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ReqCreateCdp;
import com.tosslab.toss.app.network.models.ResLeftSideMenu;
import com.tosslab.toss.app.network.models.ResSendMessage;
import com.tosslab.toss.app.utils.ProgressWheel;

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

@EFragment(R.layout.fragment_navigation_drawer)
public class LeftMenuFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(LeftMenuFragment.class);
    private static final String TAG = "LeftMenuFragment";
    String mMyToken;

    @ViewById(R.id.list_cdps)
    ListView mListCdps;
    @Bean
    CdpItemListAdapter mCdpListAdapter;

    @RestService
    TossRestClient mTossRestClient;

    private ProgressWheel mProgressWheel;
    private CdpItemManager mCdpItemManager;

    @Override
    public String getTitleForThisFragment() {
        return getActivity().getString(R.string.app_name);
    }

    @AfterViews
    void bindAdapter() {
        // myToken 획득
        mMyToken = ((MainActivity)getActivity()).myToken;

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
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
            case TossConstants.TYPE_TITLE_JOINED_CHANNEL:
                // Joined channel 의 제목부를 터치했을 경우, 채널 생성 메뉴로...
                showDialogToCreate(TossConstants.TYPE_CHANNEL);
                break;
            case TossConstants.TYPE_TITLE_UNJOINED_CHANNEL:
                // 비등록 체널의 목록을 보여주고 join 할 수 있게 함.
                break;
            case TossConstants.TYPE_TITLE_DIRECT_MESSAGE:
                // DO NOTHING
                break;
            case TossConstants.TYPE_TITLE_PRIVATE_GROUP:
                // Private Group 생성 메뉴로...
                showDialogToCreate(TossConstants.TYPE_PRIVATE_GROUP);
                break;
            default:
                // 일반 CDP 를 터치했을 경우, 해당 CDP의 메시지 리스트를 획득할수 있게 이벤트 등록
                SelectCdpItemEvent event = new SelectCdpItemEvent(cdp.type, cdp.id);
                EventBus.getDefault().post(event);
                break;
        }

    }

    @ItemLongClick
    void list_cdpsItemLongClicked(CdpItem cdp) {
        switch (cdp.type) {
            case TossConstants.TYPE_TITLE_JOINED_CHANNEL:
            case TossConstants.TYPE_TITLE_UNJOINED_CHANNEL:
            case TossConstants.TYPE_TITLE_PRIVATE_GROUP:
            case TossConstants.TYPE_TITLE_DIRECT_MESSAGE:
                // DO NOTHING
                break;
            default:
                showDialogToManipulate(cdp);
                break;
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
            getCdpItemEnd(resLeftSideMenu);
        } catch (RestClientException e) {
            Log.e("HI", "Get Fail", e);
        } catch (HttpMessageNotReadableException e) {
            Log.e("HI", "Get Fail", e);
        } catch (Exception e) {
            Log.e("HI", "Get Fail", e);
        }
    }

    @UiThread
    public void getCdpItemEnd(ResLeftSideMenu resLeftSideMenu) {
        refreshCdpList(resLeftSideMenu);
        mProgressWheel.dismiss();
    }

    public void refreshCdpList(ResLeftSideMenu resLeftSideMenu) {
        mCdpItemManager = new CdpItemManager(resLeftSideMenu);
        ((MainActivity)getActivity()).cdpItemManager = mCdpItemManager;
        mCdpListAdapter.retrieveCdpItems(mCdpItemManager);
        mCdpListAdapter.notifyDataSetChanged();
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
            case TossConstants.TYPE_CHANNEL:
                requestCreateChannel(event.inputName);
                break;
            case TossConstants.TYPE_PRIVATE_GROUP:
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
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            restResId = mTossRestClient.createChannel(reqCreateCdp);
            getCdpItemFromServer();
            Log.e(TAG, "Create Success");
        } catch (RestClientException e) {
            Log.e(TAG, "Create Fail", e);
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
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            restResId = mTossRestClient.createPrivateGroup(reqCreateCdp);
            getCdpItemFromServer();
            Log.e(TAG, "Create Success");
        } catch (RestClientException e) {
            Log.e(TAG, "Create Fail", e);
        }
    }

    /************************************************************
     * Channel, PrivateGroup 수정 / 삭제
     ************************************************************/

    void showDialogToManipulate(CdpItem cdp) {
        log.debug("Try to manipulate cdp owned by user, " + cdp.ownerId);
        if (cdp.ownerId == mCdpItemManager.mMe.id) {
            DialogFragment newFragment = ManipulateCdpDialogFragment.newInstance(cdp);
            newFragment.show(getFragmentManager(), "dialog");
        } else {
            showWarningToast("권한이 없습니다.");
        }

    }

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
        if (event.cdpType == TossConstants.TYPE_CHANNEL) {
            modifyChannelInBackground(event.cdpId, event.inputName);
        } else if (event.cdpType == TossConstants.TYPE_PRIVATE_GROUP) {
            modifyGroupInBackground(event.cdpId, event.inputName);
        }
    }

    void modifyChannelInBackground(int cdpId, String nameToBeModified) {
        ResSendMessage resId = null;
        ReqCreateCdp channel = new ReqCreateCdp();
        channel.name = nameToBeModified;
        try {
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            resId = mTossRestClient.modifyChannel(channel, cdpId);
        } catch (RestClientException e) {
            Log.e(TAG, "delete Fail", e);
        }
        modifyCdpDone();
    }

    void modifyGroupInBackground(int cdpId, String nameToBeModified) {
        ResSendMessage resId = null;
        ReqCreateCdp privateGroup = new ReqCreateCdp();
        privateGroup.name = nameToBeModified;
        try {
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            resId = mTossRestClient.modifyGroup(privateGroup, cdpId);
        } catch (RestClientException e) {
            Log.e(TAG, "delete Fail", e);
        }
        modifyCdpDone();
    }

    @UiThread
    void modifyCdpDone() {
        mProgressWheel.dismiss();
        getCdpItemFromServer();
    }

    /**
     * 삭제 이벤트 획득 from DialogFragment
     * @param event
     */
    public void onEvent(DeleteCdpEvent event) {
        Log.e(TAG, "Delete Cdp :" + event.cdpId);
        deleteCdp(event);
    }

    @UiThread
    void deleteCdp(DeleteCdpEvent event) {
        mProgressWheel.show();
        deleteCdpInBackground(event);
    }

    @Background
    void deleteCdpInBackground(DeleteCdpEvent event) {
        if (event.cdpType == TossConstants.TYPE_CHANNEL) {
            deleteChannelInBackground(event.cdpId);
        } else if (event.cdpType == TossConstants.TYPE_PRIVATE_GROUP) {
            deleteGroupInBackground(event.cdpId);
        }
    }

    void deleteChannelInBackground(int cdpId) {
        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            restResId = mTossRestClient.deleteChannel(cdpId);
            Log.e(TAG, "delete Success");
        } catch (RestClientException e) {
            Log.e(TAG, "delete Fail", e);
        }
        deleteCdpDone();
    }

    void deleteGroupInBackground(int cdpId) {
        ResSendMessage restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            restResId = mTossRestClient.deleteGroup(cdpId);
            Log.e(TAG, "delete Success");
        } catch (RestClientException e) {
            Log.e(TAG, "delete Fail", e);
        }
        deleteCdpDone();
    }

    @UiThread
    void deleteCdpDone() {
        mProgressWheel.dismiss();
        getCdpItemFromServer();
    }

    @UiThread
    void showWarningToast(String message) {
        SuperToast superToast = new SuperToast(getActivity());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.ORANGE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }
}
