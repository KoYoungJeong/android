package com.tosslab.jandi.app;

import android.app.DialogFragment;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.SelectUnjoinedChannelFragment;
import com.tosslab.jandi.app.events.ChoicedCdpEvent;
import com.tosslab.jandi.app.events.ConfirmCreateCdpEvent;
import com.tosslab.jandi.app.events.ConfirmJoinChannelEvent;
import com.tosslab.jandi.app.events.RefreshCdpListEvent;
import com.tosslab.jandi.app.events.RequestCdpListEvent;
import com.tosslab.jandi.app.events.SelectCdpItemEvent;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.lists.CdpItemListAdapter;
import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ResCommon;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 11..
 */
@EFragment(R.layout.fragment_main_left)
public class MainLeftFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MainLeftFragment.class);

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
        mCdpListAdapter.retrieveCdpItems(event.cdpItemManager);
        mCdpListAdapter.notifyDataSetChanged();
    }

    /************************************************************
     * List 선택
     * 선택한 C, D, P 에 대한 메시지 리스트 획득 이벤트가
     * MessageListFragment로 전달됨.
     ************************************************************/

    /**
     * MainActivity에서 CDP 에 대한 MessageList 가 실행될 때 해당 CDP에 하일라이트를 해줌.
     * @param event
     */
    public void onEvent(ChoicedCdpEvent event) {
        mCdpListAdapter.selectCdpItemById(event.cdpId);
    }

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
                SelectCdpItemEvent event = new SelectCdpItemEvent(cdp.type, cdp.id);
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
        createCdpInBackground(event.cdpType, event.inputName);
    }

    /**
     * Channel, privateGroup 생성
     */
    @Background
    void createCdpInBackground(int cdpType, String cdpName) {
        // TODO : Error 처리
        if (cdpName.length() <= 0) {
            return;
        }
        ReqCreateCdp reqCreateCdp = new ReqCreateCdp();
        reqCreateCdp.name = cdpName;

        ResCommon restResId = null;
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            if (cdpType == JandiConstants.TYPE_CHANNEL) {
                restResId = mTossRestClient.createChannel(reqCreateCdp);    
            } else if (cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
                restResId = mTossRestClient.createPrivateGroup(reqCreateCdp);
            }
            
            createCdpDone(true, -1, new SelectCdpItemEvent(cdpType, restResId.id, false));
        } catch (HttpClientErrorException e) {
            log.error("Create Fail", e);
            if (e.getStatusCode().value() == JandiConstants.BAD_REQUEST) {
                createCdpDone(false, R.string.err_duplicated_name_of_cdp, null);
            } else {
                createCdpDone(false, R.string.err_common_create, null);
            }
        } catch (RestClientException e) {
            log.error("Create Fail", e);
            createCdpDone(false, R.string.err_common_create, null);
        } catch (Exception e) {
            log.error("Create Fail", e);
            createCdpDone(false, R.string.err_common_create, null);
        }
    }

    @UiThread
    void createCdpDone(boolean isOk, int resId, SelectCdpItemEvent event) {
        if (isOk) {
            EventBus.getDefault().post(event);
            EventBus.getDefault().post(new RequestCdpListEvent());
        } else {
            ColoredToast.showError(mContext, getString(resId));
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
            ResCommon res = mTossRestClient.joinChannel(selectedChannelIdToBeJoined);
            joinChannelDone(true, null, new SelectCdpItemEvent(JandiConstants.TYPE_CHANNEL, selectedChannelIdToBeJoined, false));
        } catch (RestClientException e) {
            log.error("fail to join channel");
            joinChannelDone(false, "해당 채널 가입에 실패하였습니다.", null);
        } catch (Exception e) {
            log.error("fail to join channel");
            joinChannelDone(false, "해당 채널 가입에 실패하였습니다.", null);
        }

    }

    @UiThread
    public void joinChannelDone(boolean isOk, String message, SelectCdpItemEvent event) {
        if (isOk) {
            EventBus.getDefault().post(event);
            EventBus.getDefault().post(new RequestCdpListEvent());
        } else {
            ColoredToast.showError(mContext, message);
        }
    }
}
