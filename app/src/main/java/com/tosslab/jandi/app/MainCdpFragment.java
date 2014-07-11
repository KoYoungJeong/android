package com.tosslab.jandi.app;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.SelectUnjoinedChannelFragment;
import com.tosslab.jandi.app.events.ConfirmCreateCdpEvent;
import com.tosslab.jandi.app.events.ConfirmJoinChannelEvent;
import com.tosslab.jandi.app.events.RefreshCdpListEvent;
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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 11..
 */
@EFragment(R.layout.fragment_main_left)
public class MainCdpFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MainCdpFragment.class);

    @ViewById(R.id.list_cdps)
    ListView mListCdps;
    @Bean
    CdpItemListAdapter mCdpListAdapter;
    @RestService
    TossRestClient mTossRestClient;

    private ProgressWheel mProgressWheel;
    private CdpItemManager mCdpItemManager;
    private Context mContext;
    private String mMyToken;

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

    /**
     * event from MainActivity
     * @param event
     */
    public void onEvent(RefreshCdpListEvent event) {
        getCdpItemFromServer();
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
        ((MainActivity)getActivity()).mCdpItemManager = mCdpItemManager;
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
     * Channel, PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
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
}
