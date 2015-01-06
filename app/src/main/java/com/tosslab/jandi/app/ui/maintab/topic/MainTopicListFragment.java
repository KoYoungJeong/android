package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.Fragment;
import android.os.Bundle;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
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
    void initViews() {

        EntityManager entityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();

        List<FormattedEntity> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        List<FormattedEntity> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {
        EntityManager entityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();

        List<FormattedEntity> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        List<FormattedEntity> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);
    }

    @Background
    void createTopic(String topicName) {
        try {
            ResCommon topicInBackground = mainTopicModel.createTopicInBackground(topicName);
        } catch (JandiNetworkException e) {
            logger.error(e.getErrorInfo(), e);
            if (e.errCode == JandiNetworkException.DUPLICATED_NAME) {
                mainTopicPresenter.createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                mainTopicPresenter.createTopicFailed(R.string.err_entity_create);
            }

        }
    }

}
