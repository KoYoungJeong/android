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

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmCreateEntityEvent;
import com.tosslab.jandi.app.events.RetrieveChattingListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityItemListAdapter;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.MixpanelAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
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
public class MainEntityListFragment extends Fragment {
    private final Logger log = Logger.getLogger(MainEntityListFragment.class);

    @FragmentArg
    int entityType;     // Channel 혹은 PrivateGroup
    @ViewById(R.id.main_list_entities)
    ListView mListViewEntities;
    @Bean
    EntityItemListAdapter mEntityListAdapter;
    @RestService
    JandiRestClient mJandiRestClient;
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
        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, mMyToken);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        mListViewEntities.setAdapter(mEntityListAdapter);

        retrieveEntityList();
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    /**
     * Event from MainTabActivity
     * @param event
     */
    public void onEvent(RetrieveChattingListEvent event) {
        retrieveEntityList();
    }

    private void retrieveEntityList() {
        EntityManager entityManager = ((JandiApplication)getActivity().getApplication()).getEntityManager();
        if (entityManager != null) {
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                mEntityListAdapter.retrieveList(entityManager.getFormattedChannels());
            } else {
                mEntityListAdapter.retrieveList(entityManager.getFormattedPrivateGroups());
            }
            mEntityManager = entityManager;
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
                moveToChannelMessageActivity(channel.id);
            } else {
                // 채널 가입 API 호출 (후 해당 채널로 이동)
                joinChannel(formattedEntity);
            }
        } else if (formattedEntity.isPrivateGroup()) {
            ResLeftSideMenu.PrivateGroup privateGroup = formattedEntity.getPrivateGroup();
            if (privateGroup == null) {
                return;     // ERROR
            }
            moveToPrivateGroupMessageActivity(privateGroup.id);
        } else {
            // DO NOTHING
        }
        return;
    }

    private void moveToChannelMessageActivity(int channelId) {
        moveToMessageActivity(channelId, JandiConstants.TYPE_CHANNEL);
    }
    private void moveToPrivateGroupMessageActivity(int privateGroupId) {
        moveToMessageActivity(privateGroupId, JandiConstants.TYPE_PRIVATE_GROUP);
    }

    private void moveToMessageActivity(final int entityId, final int entityType) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .entityType(entityType)
                        .entityId(entityId)
                        .start();
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
                = EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_CREATE_CHAT
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
            ResCommon restResId;
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                restResId = mJandiEntityClient.createChannel(entityName);
            } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
                restResId = mJandiEntityClient.createPrivateGroup(entityName);
            } else {
                return;
            }

            createEntitySucceed(restResId.id, entityName, entityType);
        } catch (JandiNetworkException e) {
            log.error("Create Fail", e);
            if (e.httpStatusCode == JandiNetworkException.BAD_REQUEST) {
                createEntityFailed(R.string.err_entity_duplicated_name);
            } else {
                createEntityFailed(R.string.err_entity_create);
            }
        }
    }

    @UiThread
    public void createEntitySucceed(int entityId, String entityName, int entityType) {
        try {
            MixpanelAnalyticsClient
                    .getInstance(mContext, mEntityManager.getDistictId())
                    .trackCreatingEntity((entityType == JandiConstants.TYPE_CHANNEL));
        } catch (JSONException e) {
            log.error("CAN NOT MEET", e);
        }
        moveToMessageActivity(entityId, entityType);
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
                getString(R.string.jandi_message_join_entity_pre)
                        + channel.getChannel().name
                        + getString(R.string.jandi_message_join_entity_post));
        joinChannelInBackground(channel);
    }

    @Background
    public void joinChannelInBackground(final FormattedEntity channel) {
        try {
            mJandiEntityClient.joinChannel(channel.getChannel());
            joinChannelSucceed(channel);
        } catch (JandiNetworkException e) {
            log.error("fail to join channel", e);
            joinChannelFailed();
        }
    }

    @UiThread
    public void joinChannelSucceed(final FormattedEntity channel) {
        MixpanelAnalyticsClient
                .getInstance(mContext, mEntityManager.getDistictId())
                .trackJoinChannel();
        moveToChannelMessageActivity(channel.getChannel().id);

    }

    @UiThread
    public void joinChannelFailed() {
        ColoredToast.showError(mContext, getString(R.string.err_entity_join));
    }
}
