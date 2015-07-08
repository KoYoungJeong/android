package com.tosslab.jandi.app.ui.maintab.topic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
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

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_topic_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainTopicListFragment extends Fragment {

    @Bean
    MainTopicModel mainTopicModel;
    @Bean
    MainTopicView mainTopicPresenter;

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

        Observable<Topic> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels
                (), entityManager.getGroups());
        Observable<Topic> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager
                .getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);
    }

    @AfterViews
    void initView() {

        LogUtil.d("MainTopicListFragment initView");

        mainTopicPresenter.setOnItemClickListener((view, adapter, position) -> {

            Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);
            item.setUnreadCount(0);
            adapter.notifyItemChanged(position);

            mainTopicModel.resetBadge(getActivity().getApplicationContext(), item.getEntityId());
            int badgeCount = JandiPreference.getBadgeCount(getActivity()) - item.getUnreadCount();
            JandiPreference.setBadgeCount(getActivity(), badgeCount);
            BadgeUtils.setBadge(getActivity(), badgeCount);


            boolean isBadge = mainTopicModel.hasAlarmCount(Observable.from(mainTopicPresenter.getJoinedTopics()));
            EventBus.getDefault().post(new TopicBadgeEvent(isBadge));

            if (item.isJoined() || !item.isPublic()) {
                int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
                int teamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();
                mainTopicPresenter.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId);
            } else {
                // TODO Show Description
            }


        });

        mainTopicPresenter.setOnItemLongClickListener((view, adapter, position) -> {

            Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);
            EntityMenuDialogFragment_.builder().entityId(item.getEntityId())
                    .build()
                    .show(getFragmentManager(), "dialog");

            return true;

        });

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

        Observable<Topic> joinEntities = mainTopicModel.getJoinEntities(entityManager.getJoinedChannels(), entityManager.getGroups());
        Observable<Topic> unjoinEntities = mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        mainTopicPresenter.setEntities(joinEntities, unjoinEntities);

        boolean hasAlarmCount = mainTopicModel.hasAlarmCount(joinEntities);
        EventBus.getDefault().post(new TopicBadgeEvent(hasAlarmCount));
    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "chat")) {
            return;
        }

        List<Topic> joinedTopics = mainTopicPresenter.getJoinedTopics();
        if (mainTopicModel.updateBadge(event, joinedTopics)) {
            mainTopicPresenter.refreshList();
            EventBus.getDefault().post(new TopicBadgeEvent(true));
        }
    }

}
