package com.tosslab.jandi.app.ui;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmCreateCdpEvent;
import com.tosslab.jandi.app.events.RequestCdpListEvent;
import com.tosslab.jandi.app.events.SelectCdpItemEvent;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.ui.events.RetrieveChannelList;
import com.tosslab.jandi.app.ui.lists.ChannelEntityItemListAdapter;
import com.tosslab.jandi.app.ui.models.FormattedChannel;
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
 * Created by justinygchoi on 2014. 8. 11..
 */
@EFragment(R.layout.fragment_main_channel_list)
public class MainChannelListFragment extends Fragment {
    private final Logger log = Logger.getLogger(MainChannelListFragment.class);

    @ViewById(R.id.main_list_channels)
    ListView mListViewChannels;
    @Bean
    ChannelEntityItemListAdapter mChannelListAdapter;

    @RestService
    TossRestClient mTossRestClient;

    private ProgressWheel mProgressWheel;
    private String mMyToken;
    private Context mContext;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        mContext = getActivity();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        mListViewChannels.setAdapter(mChannelListAdapter);
        EventBus.getDefault().post(new ReadyToRetrieveChannelList());
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_entity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_channel:
                // 채널 생성
                showDialogToCreateChannel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Event from MainTabActivity
     * @param event
     */
    public void onEvent(RetrieveChannelList event) {
        mChannelListAdapter.retrieveList(event.channels);
    }

    /************************************************************
     *
     ************************************************************/

    /**
     * 채널에 대한 리스트를 눌렀을 때...
     * @param channel
     */
    @ItemClick
    void main_list_channelsItemClicked(final FormattedChannel channel) {
        if (channel.type != FormattedChannel.TYPE_REAL_CHANNEL) {
            return;
        }
        if (channel.isJoined) {
            moveToChannelMessageActivity(channel.original.id, channel.original.name);
        } else {
            // 채널 가입 API 호출 후 해당 채널로 이동
            joinChannel(channel);
        }

    }

    void moveToChannelMessageActivity(final int channelId, final String channelName) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .entityType(JandiConstants.TYPE_CHANNEL)
                        .entityId(channelId)
                        .entityName(channelName)
                        .start();
            }
        }, 250);
    }

    /************************************************************
     * Join Channel
     ************************************************************/
    @UiThread
    public void joinChannel(final FormattedChannel channel) {
        ColoredToast.show(mContext, channel.original.name + "에 가입합니다");
        joinChannelInBackground(channel);
    }

    @Background
    public void joinChannelInBackground(final FormattedChannel channel) {
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            ResCommon res = mTossRestClient.joinChannel(channel.original.id);
            joinChannelSucceed(channel);
        } catch (RestClientException e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        } catch (Exception e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        }
    }

    private void joinChannelSucceed(final FormattedChannel channel) {
        joinChannelDone(channel, null);
    }

    private void joinChannelFailed() {
        joinChannelDone(null, "해당 채널 가입에 실패하였습니다.");
    }

    @UiThread
    public void joinChannelDone(final FormattedChannel channel, String message) {
        if (channel == null) {
            ColoredToast.showError(mContext, message);
        } else {
            moveToChannelMessageActivity(channel.original.id, channel.original.name);
        }
    }

    /************************************************************
     * Channel, PrivateGroup 생성
     ************************************************************/

    /**
     * Alert Dialog 관련
     */
    void showDialogToCreateChannel() {
        DialogFragment newFragment
                = EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_CREATE_CDP
                , JandiConstants.TYPE_CHANNEL
                , 0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Channel, PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
     * @param event
     */
    public void onEvent(ConfirmCreateCdpEvent event) {
        ColoredToast.show(mContext, event.inputName + " 채널을 생성합니다");
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

            createChannelSucceed(restResId.id, cdpName);
        } catch (HttpClientErrorException e) {
            // TODO RestClient 에서 JandiException으로 유도하는 랩퍼 클래스 만들기
            log.error("Create Fail", e);
            if (e.getStatusCode().value() == JandiConstants.BAD_REQUEST) {
                createChannelFailed(R.string.err_duplicated_name_of_cdp);
            } else {
                createChannelFailed(R.string.err_common_create);
            }
        } catch (RestClientException e) {
            log.error("Create Fail", e);
            createChannelFailed(R.string.err_common_create);
        } catch (Exception e) {
            log.error("Create Fail", e);
            createChannelFailed(R.string.err_common_create);
        }
    }

    @UiThread
    public void createChannelSucceed(int channelId, String channelName) {
        moveToChannelMessageActivity(channelId, channelName);
    }

    @UiThread
    public void createChannelFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }
}
