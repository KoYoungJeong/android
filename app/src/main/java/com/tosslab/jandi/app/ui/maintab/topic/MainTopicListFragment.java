package com.tosslab.jandi.app.ui.maintab.topic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicListAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.FAButtonUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_topic_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainTopicListFragment extends Fragment {

    @Bean
    MainTopicModel mainTopicModel;
    @Bean
    MainTopicPresenter mainTopicPresenter;

    @ViewById(R.id.list_main_topic)
    ExpandableListView topicListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Click(R.id.btn_main_topic_fab)
    void onAddTopicClick() {
        TopicCreateActivity_
                .intent(MainTopicListFragment.this)
                .start();

        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
    }

    @AfterInject
    void initObject() {

        LogUtil.d("MainTopicListFragment");

        EntityManager entityManager = EntityManager.getInstance(getActivity());

        List<FormattedEntity> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        List<FormattedEntity> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);
    }

    @AfterViews
    void initView() {

        LogUtil.d("MainTopicListFragment initView");
        topicListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        topicListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                TopicListAdapter adapter = (TopicListAdapter) parent.getExpandableListAdapter();
                FormattedEntity entity = adapter.getChild(groupPosition, childPosition);
                int badgeCount = JandiPreference.getBadgeCount(getActivity()) - entity.alarmCount;
                JandiPreference.setBadgeCount(getActivity(), badgeCount);
                BadgeUtils.setBadge(getActivity(), badgeCount);
                entity.alarmCount = 0;
                adapter.notifyDataSetChanged();

                EventBus.getDefault().post(new TopicBadgeEvent(mainTopicModel.hasAlarmCount(mainTopicPresenter.getJoinedTopics())));

                if (entity.isJoined || entity.isPrivateGroup()) {
                    int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
                    int teamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();
                    mainTopicPresenter.moveToMessageActivity(entity.getId(), entityType, entity.isStarred, teamId);
                } else {
                    joinChannelInBackground(entity);
                }

                return false;
            }
        });

        topicListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ExpandableListView.getPackedPositionType(id) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    return false;
                }

                ExpandableListView expandableListView = (ExpandableListView) parent;
                long expandableListPosition = expandableListView.getExpandableListPosition(position);

                int groupPosition = ExpandableListView.getPackedPositionGroup(expandableListPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(expandableListPosition);

                TopicListAdapter expandableListAdapter = (TopicListAdapter) expandableListView.getExpandableListAdapter();
                FormattedEntity child = expandableListAdapter.getChild(groupPosition, childPosition);

                if (child.isPublicTopic() && !child.isJoined) {
                    return false;
                }

                EntityMenuDialogFragment_.builder().entityId(child.getId()).build().show(getFragmentManager(), "dialog");

                return true;
            }
        });

        FAButtonUtil.setFAButtonController(topicListView, getView().findViewById(R.id.btn_main_topic_fab));
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();
    }

    @Background
    public void joinChannelInBackground(final FormattedEntity entity) {

        mainTopicPresenter.showProgressWheel();

        String message = getString(R.string.jandi_message_join_entity, entity.getChannel().name);
        mainTopicPresenter.showToast(message);

        try {
            mainTopicModel.joinPublicTopic(entity.getChannel());
            mainTopicModel.refreshEntity();
            EntityManager entityManager = EntityManager.getInstance(getActivity());
            if (entityManager != null) {
                MixpanelMemberAnalyticsClient
                        .getInstance(getActivity(), entityManager.getDistictId())
                        .trackJoinChannel();
            }
            int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            int teamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();
            mainTopicPresenter.moveToMessageActivity(entity.getId(), entityType, entity.isStarred, teamId);
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("fail to join entity", e);
            mainTopicPresenter.showErrorToast(getString(R.string.err_entity_join));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("fail to join entity", e);
            mainTopicPresenter.showErrorToast(getString(R.string.err_entity_join));
        } finally {
            mainTopicPresenter.dismissProgressWheel();
        }
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {
        EntityManager entityManager = EntityManager.getInstance(getActivity());

        List<FormattedEntity> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        List<FormattedEntity> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);

        boolean hasAlarmCount = mainTopicModel.hasAlarmCount(joinEntities);
        EventBus.getDefault().post(new TopicBadgeEvent(hasAlarmCount));
    }

    public void onEvent(MessagePushEvent event) {
        if (!TextUtils.equals(event.getEntityType(), "user")) {

        }
    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "chat")) {
            return;
        }

        List<FormattedEntity> joinedTopics = mainTopicPresenter.getJoinedTopics();
        if (mainTopicModel.updateBadge(event, joinedTopics)) {
            mainTopicPresenter.refreshList();
            EventBus.getDefault().post(new TopicBadgeEvent(true));
        }
    }

}
