package com.tosslab.jandi.app.ui;

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
import com.tosslab.jandi.app.network.AnalyticsClient;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.TossRestClient;
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
import com.tosslab.jandi.app.utils.JandiException;
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
import org.json.JSONException;

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
    private JandiEntityClient mJandiEntityClient;

    private ProgressWheel mProgressWheel;
    private String mMyToken;
    private Context mContext;
    private EntityManager mEntityManager;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);
        mContext = getActivity();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        mJandiEntityClient = new JandiEntityClient(mTossRestClient, mMyToken);

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
        if (entityType == JandiConstants.TYPE_CHANNEL) {
            mEntityManager = event.entityManager;
            mEntityListAdapter.retrieveList(event.entityManager.getFormattedChannels());
        }
    }

    public void onEvent(RetrievePrivateGroupList event) {
        if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
            mEntityManager = event.entityManager;
            mEntityListAdapter.retrieveList(event.entityManager.getFormattedPrivateGroups());
        }
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
        // 알람 카운트가 있던 아이템이면 이를 0으로 바꾼다.
        formattedEntity.alarmCount = 0;
        mEntityListAdapter.notifyDataSetChanged();

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
        moveToMessageActivity(channelId, channelName, JandiConstants.TYPE_CHANNEL,
                mEntityManager.isMyEntity(channelId));
    }
    private void moveToPrivateGroupMessageActivity(int privateGroupId, String privateGroupName) {
        moveToMessageActivity(privateGroupId, privateGroupName, JandiConstants.TYPE_PRIVATE_GROUP,
                mEntityManager.isMyEntity(privateGroupId));
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
                MessageListActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .entityType(entityType)
                        .entityId(entityId)
                        .entityName(entityName)
                        .isMyEntity(isMyEntity)
                        .start();
                EventBus.getDefault().postSticky(new StickyEntityManager(mEntityManager));
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
            ColoredToast.show(mContext,
                    event.inputName + getString(R.string.jandi_message_create_entity));
            createEntityInBackground(event.cdpType, event.inputName);
        }
    }

    /**
     * Channel, privateGroup 생성
     */
    @Background
    void createEntityInBackground(int entityType, String entityName) {
        // TODO : Error 처리
        if (entityName.length() <= 0) {
            return;
        }

        try {
            ResCommon restResId = null;
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                restResId = mJandiEntityClient.createChannel(entityName);
            } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
                restResId = mJandiEntityClient.createPrivateGroup(entityName);
            } else {
                return;
            }

            createEntitySucceed(restResId.id, entityName, entityType);
        } catch (JandiException e) {
            log.error("Create Fail", e);
            if (e.httpStatusCode == JandiException.BAD_REQUEST) {
                createEntityFailed(R.string.err_entity_duplicated_name);
            } else {
                createEntityFailed(R.string.err_entity_create);
            }
        }
    }

    @UiThread
    public void createEntitySucceed(int entityId, String entityName, int entityType) {
        try {
            AnalyticsClient
                    .getInstance(mContext, mEntityManager.getDistictId())
                    .trackCreatingEntity((entityType == JandiConstants.TYPE_CHANNEL));
        } catch (JSONException e) {
            log.error("CAN NOT MEET", e);
        }
        moveToMessageActivityAfterCreation(entityId, entityName, entityType);
    }

    @UiThread
    public void createEntityFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }


    /************************************************************
     * Join Channel
     ************************************************************/
    @UiThread
    public void joinChannel(final FormattedEntity channel) {
        ColoredToast.show(mContext,
                channel.getChannel().name + getString(R.string.jandi_message_join_entity));
        joinChannelInBackground(channel);
    }

    @Background
    public void joinChannelInBackground(final FormattedEntity channel) {
        try {
            mJandiEntityClient.joinChannel(channel.getChannel());
            joinChannelSucceed(channel);
        } catch (JandiException e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        }
    }

    private void joinChannelSucceed(final FormattedEntity channel) {
        AnalyticsClient
                .getInstance(mContext, mEntityManager.getDistictId())
                .trackJoinChannel();
        joinChannelDone(channel, null);
    }

    private void joinChannelFailed() {
        joinChannelDone(null, getString(R.string.err_entity_join));
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
