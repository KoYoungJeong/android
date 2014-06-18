package com.tosslab.toss.app;

import android.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.events.ConfirmCreateCdpEvent;
import com.tosslab.toss.app.events.ConfirmModifyCdpEvent;
import com.tosslab.toss.app.events.DeleteCdpEvent;
import com.tosslab.toss.app.events.ModifyCdpEvent;
import com.tosslab.toss.app.navigation.CdpItem;
import com.tosslab.toss.app.navigation.CdpItemListAdapter;
import com.tosslab.toss.app.navigation.CdpItemManager;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.ReqCreateCdp;
import com.tosslab.toss.app.network.entities.ResLeftSideMenu;
import com.tosslab.toss.app.network.entities.ResSendCdpMessage;
import com.tosslab.toss.app.utils.EditTextAlertDialogFragment;
import com.tosslab.toss.app.utils.ManipulateCdpAlertDialog;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_navigation_drawer)
public class LeftMenuFragment extends BaseFragment {
    private static final String TAG = "LeftMenuFragment";
    String mMyToken;

    @ViewById(R.id.list_cdps)
    ListView mListCdps;
    @Bean
    CdpItemListAdapter mCdpListAdapter;

    @RestService
    TossRestClient mTossRestClient;

    private ProgressWheel mProgressWheel;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
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
        ChooseNaviActionEvent event = new ChooseNaviActionEvent(TossConstants.TYPE_CHANNEL, cdp.id);
        EventBus.getDefault().post(event);
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
        CdpItemManager cdpItemManager = new CdpItemManager(resLeftSideMenu);
        mCdpListAdapter.retrieveCdpItems(cdpItemManager);
        mCdpListAdapter.notifyDataSetChanged();
    }


    /************************************************************
     * Channel, PrivateGroup 생성
     ************************************************************/

    @Click(R.id.btn_action_add_channel)
    void createChannel() {
        Log.e(TAG, "Create Channel");
        showDialogToCreate(TossConstants.TYPE_CHANNEL);
    }

//    @Click(R.id.btn_add_private_group)
//    void createPrivateGroup() {
//        Log.e(TAG, "Create Private Group");
//        showDialogToCreate(TossConstants.TYPE_PRIVATE_GROUP);
//    }

    /**
     * Alert Dialog 관련
     */
    void showDialogToCreate(int cdpType) {
        DialogFragment newFragment
                = EditTextAlertDialogFragment.newInstance(EditTextAlertDialogFragment.ACTION_CREATE_CDP
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

        ResSendCdpMessage restResId = null;
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

        ResSendCdpMessage restResId = null;
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
    @ItemLongClick
    void list_cdpsItemLongClicked(CdpItem cdp) {
        showDialogToManipulate(cdp);
    }

    void showDialogToManipulate(CdpItem cdp) {
        DialogFragment newFragment = ManipulateCdpAlertDialog.newInstance(cdp);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(ModifyCdpEvent event) {
        DialogFragment newFragment = EditTextAlertDialogFragment.newInstance(
                EditTextAlertDialogFragment.ACTION_MODIFY_CDP
                , event.cdpType
                , event.cdpId
                , event.currentName);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * 수정 이벤트 획득 from EditTextAlertDialogFragment
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
        ResSendCdpMessage resId = null;
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
        ResSendCdpMessage resId = null;
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
        ResSendCdpMessage restResId = null;
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
        ResSendCdpMessage restResId = null;
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
}
