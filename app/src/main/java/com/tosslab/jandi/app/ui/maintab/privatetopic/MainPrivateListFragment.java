package com.tosslab.jandi.app.ui.maintab.privatetopic;

import android.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePrivateTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityExpandableListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.BaseChatListFragment;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 10. 2..
 */
@Deprecated
@EFragment(R.layout.fragment_main_list)
public class MainPrivateListFragment extends BaseChatListFragment {

    @ViewById(R.id.main_exlist_entities)
    ExpandableListView mListViewEntities;

    @Bean
    EntityClientManager mEntityClientManager;

    private EntityExpandableListAdapter mEntityListAdapter;
    private EntityManager mEntityManager;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

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
                showDialogToCreatePrivateGroup();
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

    /**
     * Event from MainTabActivity
     *
     * @param event
     */
    public void onEventMainThread(RetrieveTopicListEvent event) {
        retrieveEntityList();
    }

    private void retrieveEntityList() {
        EntityManager entityManager = EntityManager.getInstance(getActivity());
        if (entityManager != null) {
            mEntityManager = entityManager;
            mEntityListAdapter.retrieveChildList(
                    entityManager.getGroups(),
                    entityManager.getFormattedUsersWithoutMe());
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
        moveToMessageActivity(formattedEntity);
        return;
    }

    /************************************************************
     * PrivateGroup 생성
     ************************************************************/

    /**
     * Alert Dialog 관련
     */
    void showDialogToCreatePrivateGroup() {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_CREATE_TOPIC,
                JandiConstants.TYPE_PRIVATE_TOPIC,
                0);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * PrivateGroup 생성 이벤트 획득 from EditTextDialogFragment
     *
     * @param event
     */
    public void onEventMainThread(ConfirmCreatePrivateTopicEvent event) {
        String rawString = getString(R.string.jandi_message_create_entity);
        String formatString = String.format(rawString, event.groupName);
        ColoredToast.show(mContext, formatString);
        createGroupInBackground(event.groupName);
    }

    public void onEventMainThread(ErrorDialogFragmentEvent event) {
        ColoredToast.showError(mContext, getString(event.errorMessageResId));
    }

    /**
     * privateGroup 생성
     */
    @Background
    void createGroupInBackground(String entityName) {
        try {
            ResCommon restResId = mEntityClientManager.createPrivateGroup(entityName, "");
            createGroupSucceed(restResId.id, entityName);
        } catch (RetrofitError e) {
            e.printStackTrace();
            if (e.getResponse() != null) {
                if (e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                    createGroupFailed(R.string.err_entity_duplicated_name);
                } else {
                    createGroupFailed(R.string.err_entity_create);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            createGroupFailed(R.string.err_entity_create);
        }
    }

    @UiThread
    public void createGroupSucceed(int entityId, String entityName) {
        try {
            if (mEntityManager != null) {
                MixpanelMemberAnalyticsClient
                        .getInstance(mContext, mEntityManager.getDistictId())
                        .trackCreatingEntity(false);
            }
        } catch (JSONException e) {
            LogUtil.e("CAN NOT MEET", e);
        }
        moveToPrivateTopicMessageActivity(entityId);
    }

    @UiThread
    public void createGroupFailed(int errStringResId) {
        ColoredToast.showError(mContext, getString(errStringResId));
    }
}
