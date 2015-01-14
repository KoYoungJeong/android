package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicListAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_topic_list)
@OptionsMenu(R.menu.add_entity_menu)
public class MainTopicListFragment extends Fragment {

    private static final Logger logger = Logger.getLogger(MainTopicListFragment.class);

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

    @OptionsItem(R.id.action_add_channel)
    void onAddTopicOptionSelect() {
        TopicCreateActivity_
                .intent(MainTopicListFragment.this)
                .start();
    }

    @AfterInject
    void initObject() {

        EntityManager entityManager = EntityManager.getInstance(getActivity());

        List<FormattedEntity> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        List<FormattedEntity> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);
    }

    @AfterViews
    void initView() {
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
                entity.alarmCount = 0;
                adapter.notifyDataSetChanged();

                if (entity.isJoined || entity.isPrivateGroup()) {
                    int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
                    mainTopicPresenter.moveToMessageActivity(entity.getId(), entityType, entity.isStarred);
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

                EntityMenuDialogFragment_.builder().entityId(child.getId()).build().show(getFragmentManager(), "dialog");

                return true;
            }
        });
    }


    @Background
    public void joinChannelInBackground(final FormattedEntity entity) {

        mainTopicPresenter.showProgressWheel();

        String message = getString(R.string.jandi_message_join_entity, entity.getChannel().name);
        mainTopicPresenter.showToast(message);

        try {
            mainTopicModel.joinPublicTopic(entity.getChannel());
            EntityManager entityManager = EntityManager.getInstance(getActivity());
            if (entityManager != null) {
                MixpanelMemberAnalyticsClient
                        .getInstance(getActivity(), entityManager.getDistictId())
                        .trackJoinChannel();
            }
            int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            mainTopicPresenter.moveToMessageActivity(entity.getId(), entityType, entity.isStarred);
        } catch (JandiNetworkException e) {
            logger.error("fail to join entity", e);
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
    }

}
