package com.tosslab.jandi.app.ui.main;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmCreateEntityEvent;
import com.tosslab.jandi.app.events.RetrieveChattingListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityExpandableListAdapter;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.MixpanelAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.MessageListActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.json.JSONException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
@EFragment(R.layout.fragment_main_list)
public class MainPrivateListFragment extends BaseChatListFragment {
    private final Logger log = Logger.getLogger(MainPrivateListFragment.class);

    @ViewById(R.id.main_exlist_entities)
    ExpandableListView mListViewEntities;
    @RestService
    JandiRestClient mJandiRestClient;

    private EntityExpandableListAdapter mEntityListAdapter;
    private JandiEntityClient mJandiEntityClient;
    private String mMyToken;
    private EntityManager mEntityManager;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);
        mJandiEntityClient = new JandiEntityClient(mJandiRestClient, mMyToken);

        mEntityListAdapter = new EntityExpandableListAdapter(mContext,
                EntityExpandableListAdapter.TYPE_PRIVATE_ENTITY_LIST);
        mListViewEntities.setAdapter(mEntityListAdapter);
        setExpandableListViewAction(mListViewEntities);

        retrieveEntityList();
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
     * @param event
     */
    public void onEvent(RetrieveChattingListEvent event) {
        retrieveEntityList();
    }

    private void retrieveEntityList() {
        EntityManager entityManager = ((JandiApplication)getActivity().getApplication()).getEntityManager();
        if (entityManager != null) {
            mEntityManager = entityManager;
            mEntityListAdapter.retrievePublicList(
                    entityManager.getFormattedPrivateGroups(),
                    entityManager.getFormattedUsersWithoutMe());
        }
    }
    /************************************************************
     *
     ************************************************************/

    /**
     * 채널에 대한 리스트를 눌렀을 때...
     * @param formattedEntity
     */
    void main_list_entitiesItemClicked(final FormattedEntity formattedEntity) {
        // 알람 카운트가 있던 아이템이면 이를 0으로 바꾼다.
        formattedEntity.alarmCount = 0;
        mEntityListAdapter.notifyDataSetChanged();

        ResLeftSideMenu.PrivateGroup privateGroup = formattedEntity.getPrivateGroup();
        if (privateGroup == null) {
            return;     // ERROR
        }
        moveToPrivateGroupMessageActivity(privateGroup.id);

        return;
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
                , JandiConstants.TYPE_PRIVATE_GROUP
                , 0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Channel, PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
     * @param event
     */
    public void onEvent(ConfirmCreateEntityEvent event) {
        ColoredToast.show(mContext,
                event.inputName + getString(R.string.jandi_message_create_entity));
        createEntityInBackground(event.cdpType, event.inputName);
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
            ResCommon restResId = mJandiEntityClient.createPrivateGroup(entityName);
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
        moveToPrivateGroupMessageActivity(entityId);
    }

    @UiThread
    public void createEntityFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }
}
