package com.tosslab.jandi.app.ui.maintab.topic.presenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.model.MainTopicModel;
import com.tosslab.jandi.app.ui.maintab.topics.domain.Topic;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

@EBean
public class MainTopicListPresenterImpl implements MainTopicListPresenter {

    @Bean
    MainTopicModel mainTopicModel;

    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInitTopics(Context context, int selectedEntity) {
        EntityManager entityManager = EntityManager.getInstance();

        Observable<Topic> joinEntities = mainTopicModel.getJoinEntities(
                entityManager.getJoinedChannels(), entityManager.getGroups());
        Observable<Topic> unjoinEntities =
                mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        view.setEntities(joinEntities, unjoinEntities);
        if (selectedEntity > 0) {
            view.setSelectedItem(selectedEntity);
            view.startAnimationSelectedItem();
        }


        boolean hasAlarmCount = mainTopicModel.hasAlarmCount(joinEntities);
        EventBus.getDefault().post(new TopicBadgeEvent(hasAlarmCount));
    }

    @Background
    @Override
    public void onRefreshTopicList() {
        mainTopicModel.refreshEntity();
        onInitTopics(JandiApplication.getContext(), -1);
    }

    @Override
    public void onItemClick(Context context, RecyclerView.Adapter adapter, int position) {
        Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);
        if (item == null) {
            return;
        }
        item.setUnreadCount(0);
        adapter.notifyDataSetChanged();

        mainTopicModel.resetBadge(context, item.getEntityId());
        int badgeCount = JandiPreference.getBadgeCount(context) - item.getUnreadCount();
        JandiPreference.setBadgeCount(context, badgeCount);
        BadgeUtils.setBadge(context, badgeCount);


        boolean isBadge = mainTopicModel.hasAlarmCount(
                Observable.from(view.getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(isBadge));

        if (item.isJoined() || !item.isPublic()) {
            int entityType = item.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            int teamId = AccountRepository.getRepository().getSelectedTeamInfo()
                    .getTeamId();
            view.moveToMessageActivity(item.getEntityId(), entityType, item.isStarred(), teamId,
                    item.getMarkerLinkId());
            int selectedEntity = item.getEntityId();
            view.setSelectedItem(selectedEntity);

            EventBus.getDefault().post(new MainSelectTopicEvent(selectedEntity));
        } else {

            view.showUnjoinDialog(item);

        }

        view.notifyDatasetChanged();
    }

    @Background
    @Override
    public void onJoinTopic(Context context, Topic topic) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showErrorToast(context.getString(R.string.err_entity_join));
            return;
        }

        view.showProgressWheel();

        String message = context.getString(R.string.jandi_message_join_entity, topic.getName());
        view.showToast(message);

        try {
            mainTopicModel.joinPublicTopic(topic.getEntityId());
            mainTopicModel.refreshEntity();
            EntityManager entityManager = EntityManager.getInstance();
            if (entityManager != null) {
                MixpanelMemberAnalyticsClient
                        .getInstance(context, entityManager.getDistictId())
                        .trackJoinChannel();
            }
            int entityType = topic.isPublic() ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            int teamId = AccountRepository.getRepository().getSelectedTeamInfo()
                    .getTeamId();
            view.moveToMessageActivity(topic.getEntityId(), entityType, topic.isStarred(),
                    teamId, topic.getMarkerLinkId());

        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("fail to join entity", e);
            view.showErrorToast(context.getString(R.string.err_entity_join));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("fail to join entity", e);
            view.showErrorToast(context.getString(R.string.err_entity_join));
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Override
    public void onNewMessage(SocketMessageEvent event) {

        List<Topic> joinedTopics = view.getJoinedTopics();

        if (mainTopicModel.isMe(event.getWriter())) {
            return;
        }

        if (mainTopicModel.updateBadge(event, joinedTopics)) {
            view.notifyDatasetChanged();
            EventBus.getDefault().post(new TopicBadgeEvent(true));
        }
    }

    @Override
    public void onItemLongClick(Context context, RecyclerView.Adapter adapter, int position) {

        Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);

        if (item == null) {
            return;
        }

        if (item.isJoined() || !item.isPublic()) {
            view.showEntityMenuDialog(item);
        } else {
            view.showUnjoinDialog(item);
            view.notifyDatasetChanged();
        }

    }

    @Override
    public void onFocusTopic(int selectedEntity) {
        List<Topic> joinedTopics = view.getJoinedTopics();

        int selectedEntityPosition = 0;
        int size = joinedTopics.size();
        for (int idx = 0; idx < size; idx++) {
            if (joinedTopics.get(idx).getEntityId() == selectedEntity) {
                selectedEntityPosition = idx;
                break;
            }
        }

        view.scrollToPosition(selectedEntityPosition);
    }

}
