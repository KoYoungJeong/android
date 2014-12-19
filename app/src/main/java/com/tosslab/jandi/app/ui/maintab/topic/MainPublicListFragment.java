package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePublicTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityExpandableListAdapter;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.BaseChatListFragment;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
@EFragment(R.layout.fragment_main_list)
public class MainPublicListFragment extends BaseChatListFragment {
    private final Logger log = Logger.getLogger(MainPublicListFragment.class);

    @ViewById(R.id.main_exlist_entities)
    ExpandableListView mListViewEntities;

    @RestService
    JandiRestClient mJandiRestClient;
    @Bean
    JandiEntityClient mJandiEntityClient;
    private EntityExpandableListAdapter mEntityListAdapter;

    private EntityManager mEntityManager;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        mEntityListAdapter = new EntityExpandableListAdapter(mContext,
                EntityExpandableListAdapter.TYPE_PUBLIC_ENTITY_LIST);
        mListViewEntities.setAdapter(mEntityListAdapter);
        setExpandableListViewAction(mListViewEntities);

        retrieveEntityList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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


    /**
     * *********************************************************
     * List item 처리
     * **********************************************************
     */
    private void setExpandableListViewAction(ExpandableListView expandableListView) {
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                // 그룹 클릭 이벤트를 받지 않는다.
                return true;
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                FormattedEntity clickedEntity
                        = mEntityListAdapter.getChild(groupPosition, childPosition);
                main_list_entitiesItemClicked(clickedEntity);
                return false;
            }
        });
    }
    /************************************************************
     * Events & Actions
     ************************************************************/

    /**
     * Event from MainTabActivity
     *
     * @param event
     */
    public void onEvent(RetrieveTopicListEvent event) {
        retrieveEntityList();
    }

    private void retrieveEntityList() {
        EntityManager entityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();
        if (entityManager != null) {
            mEntityManager = entityManager;
            mEntityListAdapter.retrieveChildList(
                    entityManager.getJoinedChannels(),
                    entityManager.getUnjoinedChannels());
        }
    }
    /************************************************************
     *
     ************************************************************/

    /**
     * 채널에 대한 리스트를 눌렀을 때...
     *
     * @param formattedEntity
     */
    void main_list_entitiesItemClicked(final FormattedEntity formattedEntity) {
        // 알람 카운트가 있던 아이템이면 이를 0으로 바꾼다.
        formattedEntity.alarmCount = 0;
        mEntityListAdapter.notifyDataSetChanged();

        if (formattedEntity.isJoined) {
            moveToMessageActivity(formattedEntity);
            return;
        }

        // 채널 가입 API 호출 (후 해당 채널로 이동)
        joinPublicTopic(formattedEntity);

        return;
    }

    /************************************************************
     * Channel 생성
     ************************************************************/

    /**
     * Alert Dialog 관련
     */
    void showDialogToCreateChannel() {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_CREATE_TOPIC,
                JandiConstants.TYPE_PUBLIC_TOPIC,
                0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Channel, PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
     *
     * @param event
     */
    public void onEvent(ConfirmCreatePublicTopicEvent event) {
        createTopicInBackground(event.topicName);
    }

    public void onEvent(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(mContext, getString(event.errorMessageResId));
    }

    /**
     * topic 생성
     */
    @Background
    void createTopicInBackground(String entityName) {
        try {
            ResCommon restResId = mJandiEntityClient.createPublicTopic(entityName);
            createTopicSucceed(restResId.id, entityName);
        } catch (JandiNetworkException e) {
            log.error(e.getErrorInfo(), e);
            if (e.errCode == JandiNetworkException.DUPLICATED_NAME) {
                createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                createTopicFailed(R.string.err_entity_create);
            }
        }
    }

    @UiThread
    public void createTopicSucceed(int entityId, String entityName) {
        String rawString = getString(R.string.jandi_message_create_entity);
        String formatString = String.format(rawString, entityName);
        ColoredToast.show(mContext, formatString);
        try {
            if (mEntityManager != null) {
                MixpanelAnalyticsClient
                        .getInstance(mContext, mEntityManager.getDistictId())
                        .trackCreatingEntity(true);
            }
        } catch (JSONException e) {
            log.error("CAN NOT MEET", e);
        }
        moveToPublicTopicMessageActivity(entityId);
    }

    @UiThread
    public void createTopicFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }


    /**
     * *********************************************************
     * Join Public Topic
     * **********************************************************
     */
    @UiThread
    public void joinPublicTopic(final FormattedEntity topic) {
        String rawString = getString(R.string.jandi_message_join_entity);
        String formattedString = String.format(rawString, topic.getChannel().name);
        ColoredToast.show(mContext, formattedString);
        joinChannelInBackground(topic);
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
        if (mEntityManager != null) {
            MixpanelAnalyticsClient
                    .getInstance(mContext, mEntityManager.getDistictId())
                    .trackJoinChannel();
        }
        moveToMessageActivity(channel);
    }

    @UiThread
    public void joinChannelFailed() {
        ColoredToast.showError(mContext, getString(R.string.err_entity_join));
    }
}
