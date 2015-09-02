package com.tosslab.jandi.app.ui.maintab.topics.views.joinabletopiclist.presentor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.maintab.topics.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topics.views.joinabletopiclist.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.views.joinabletopiclist.model.JoinableTopicModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;
import rx.Observable;

@EBean
public class MainTopicListPresenter {

    @Bean
    JoinableTopicModel mainTopicModel;

    private View view;

    public void setView(View view) {
        this.view = view;
    }

    public void onInitUnjoinedTopics() {
        EntityManager entityManager = EntityManager.getInstance();

        Observable<Topic> unjoinEntities =
                mainTopicModel.getUnjoinEntities(entityManager.getUnjoinedChannels());

        view.setEntities(unjoinEntities);
    }

    @Background
    public void onRefreshTopicList() {
        mainTopicModel.refreshEntity();
        onInitUnjoinedTopics();
    }

    @Background
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

    public void onItemClick(RecyclerView.Adapter adapter, int position) {
        Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);

        if (item == null) {
            return;
        }

        view.showUnjoinDialog(item);
        view.notifyDatasetChanged();
    }

    public void onItemLongClick(RecyclerView.Adapter adapter, int position) {

        Topic item = ((TopicRecyclerAdapter) adapter).getItem(position);

        if (item == null) {
            return;
        }

        view.showUnjoinDialog(item);
        view.notifyDatasetChanged();

    }

    public interface View {

        void setEntities(Observable<Topic> unjoinEntities);

        void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId);

        void showUnjoinDialog(Topic item);

        void notifyDatasetChanged();

        void showProgressWheel();

        void showToast(String message);

        void showErrorToast(String message);

        void dismissProgressWheel();
    }

}
