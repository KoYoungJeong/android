package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.DialogFragment;
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
import com.tosslab.jandi.app.events.ConfirmCreateEntityEvent;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.events.ReadyToRetrievePrivateGroupList;
import com.tosslab.jandi.app.events.RetrieveChannelList;
import com.tosslab.jandi.app.events.RetrievePrivateGroupList;
import com.tosslab.jandi.app.events.StickyEntityManager;
import com.tosslab.jandi.app.lists.entities.EntityItemListAdapter;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
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
@EFragment(R.layout.fragment_main_entity_list)
public class MainEntityListFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MainEntityListFragment.class);

    @FragmentArg
    int entityType;     // Channel 혹은 PrivateGroup
    @ViewById(R.id.main_list_entities)
    ListView mListViewEntities;
    @Bean
    EntityItemListAdapter mEntityListAdapter;

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

        mListViewEntities.setAdapter(mEntityListAdapter);

        sendReadyEventToMainTabActivity();
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
                // 채널, private group 생성
                showDialogToCreateChannel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /************************************************************
     * Events & Actions
     ************************************************************/
    private void sendReadyEventToMainTabActivity() {
        // 이 프레그먼트들의 main 에 리스트 받을 준비가 되었다고 알림
        if (entityType == JandiConstants.TYPE_CHANNEL) {
            EventBus.getDefault().post(new ReadyToRetrieveChannelList());
        } else {
            EventBus.getDefault().post(new ReadyToRetrievePrivateGroupList());
        }
    }

    /**
     * Event from MainTabActivity
     * @param event
     */
    public void onEvent(RetrieveChannelList event) {
        if (entityType == JandiConstants.TYPE_CHANNEL)
            mEntityListAdapter.retrieveList(event.channels);
    }

    public void onEvent(RetrievePrivateGroupList event) {
        if (entityType == JandiConstants.TYPE_PRIVATE_GROUP)
            mEntityListAdapter.retrieveList(event.privateGroups);
    }

    /************************************************************
     *
     ************************************************************/

    /**
     * 채널에 대한 리스트를 눌렀을 때...
     * @param formattedEntity
     */
    @ItemClick
    void main_list_entitiesItemClicked(final FormattedEntity formattedEntity) {
        if (formattedEntity.isChannel()) {
            if (formattedEntity.isJoined) {
                ResLeftSideMenu.Channel channel = formattedEntity.getChannel();
                if (channel == null) {
                    return;     // ERROR
                }
                moveToChannelMessageActivity(channel.id, channel.name);
            } else {
                // 채널 가입 API 호출 (후 해당 채널로 이동)
                joinChannel(formattedEntity);
            }
        } else if (formattedEntity.isPrivateGroup()) {
            ResLeftSideMenu.PrivateGroup privateGroup = formattedEntity.getPrivateGroup();
            if (privateGroup == null) {
                return;     // ERROR
            }
            moveToPrivateGroupMessageActivity(privateGroup.id, privateGroup.name);
        } else {
            // DO NOTHING
        }
        return;
    }

    private void moveToChannelMessageActivity(int channelId, String channelName) {
        Activity activity = getActivity();
        if (activity instanceof MainTabActivity_) {
            EntityManager entityManager = ((MainTabActivity_)activity).getEntityManager();
            moveToMessageActivity(
                    channelId,
                    channelName,
                    JandiConstants.TYPE_CHANNEL,
                    entityManager.isMyEntity(channelId));
        }
    }
    private void moveToPrivateGroupMessageActivity(int privateGroupId, String privateGroupName) {
        Activity activity = getActivity();
        if (activity instanceof MainTabActivity_) {
            EntityManager entityManager = ((MainTabActivity_)activity).getEntityManager();
            moveToMessageActivity(
                    privateGroupId,
                    privateGroupName,
                    JandiConstants.TYPE_PRIVATE_GROUP,
                    entityManager.isMyEntity(privateGroupId));
        }
    }

    private void moveToMessageActivityAfterCreation(final int channelId,
                                                    final String channelName,
                                                    final int entityType) {
        moveToMessageActivity(channelId, channelName, entityType, true);
    }

    private void moveToMessageActivity(final int entityId, final String entityName,
                                       final int entityType, final boolean isMyEntity) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity instanceof MainTabActivity_) {
                    MessageListActivity_.intent(mContext)
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .entityType(entityType)
                            .entityId(entityId)
                            .entityName(entityName)
                            .isMyEntity(isMyEntity)
                            .start();

                    EntityManager entityManager = ((MainTabActivity_)activity).getEntityManager();
                    EventBus.getDefault().postSticky(new StickyEntityManager(entityManager));
                }
            }
        }, 250);
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
                , entityType
                , 0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Channel, PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
     * @param event
     */
    public void onEvent(ConfirmCreateEntityEvent event) {
        if (event.cdpType == entityType) {
            ColoredToast.show(mContext, event.inputName + " 을 생성합니다");
            createCdpInBackground(event.cdpType, event.inputName);
        }
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

            createChannelSucceed(restResId.id, cdpName, cdpType);
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
    public void createChannelSucceed(int channelId, String channelName, int cdpType) {
        moveToMessageActivityAfterCreation(channelId, channelName, cdpType);
    }

    @UiThread
    public void createChannelFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }


    /************************************************************
     * Join Channel
     ************************************************************/
    @UiThread
    public void joinChannel(final FormattedEntity channel) {
        ColoredToast.show(mContext, channel.getChannel().name + "에 가입합니다");
        joinChannelInBackground(channel);
    }

    @Background
    public void joinChannelInBackground(final FormattedEntity channel) {
        try {
            mTossRestClient.setHeader("Authorization", mMyToken);
            ResCommon res = mTossRestClient.joinChannel(channel.getChannel().id);
            joinChannelSucceed(channel);
        } catch (RestClientException e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        } catch (Exception e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        }
    }

    private void joinChannelSucceed(final FormattedEntity channel) {
        joinChannelDone(channel, null);
    }

    private void joinChannelFailed() {
        joinChannelDone(null, "해당 채널 가입에 실패하였습니다.");
    }

    @UiThread
    public void joinChannelDone(final FormattedEntity channel, String message) {
        if (channel == null) {
            ColoredToast.showError(mContext, message);
        } else {
            moveToChannelMessageActivity(channel.getChannel().id, channel.getChannel().name);
        }
    }
}
