package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.presenter;

import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.model.JoinableTopicDataModel;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.model.JoinableTopicListModel;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.view.JoinableTopicListView;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class JoinableTopicListPresenterImpl implements JoinableTopicListPresenter {

    @Inject
    JoinableTopicListModel joinableTopicListModel;

    private final JoinableTopicDataModel joinableTopicDataModel;
    private final JoinableTopicListView view;

    private PublishSubject<String> searchQueryQueue;
    private Subscription searchQueryQueueSubscription;

    @Inject
    public JoinableTopicListPresenterImpl(JoinableTopicDataModel joinableTopicDataModel,
                                          JoinableTopicListView view) {
        this.joinableTopicDataModel = joinableTopicDataModel;
        this.view = view;

        initSearchTopicQueue();
    }

    @Override
    public void initSearchTopicQueue() {
        searchQueryQueue = PublishSubject.create();
        searchQueryQueueSubscription =
                searchQueryQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .map(query -> {
                            List<Topic> joinableTopics =
                                    joinableTopicListModel.getJoinableTopicsForSearch();
                            return joinableTopicListModel.getSearchedTopics(query, joinableTopics);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(topics -> {
                            joinableTopicDataModel.clear();
                            if (topics == null || topics.isEmpty()) {
                                view.notifyDataSetChanged();
                                return;
                            }
                            joinableTopicDataModel.setJoinableTopics(topics);
                            view.notifyDataSetChanged();
                        });
    }

    @Override
    public void stopSearchTopicQueue() {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onInitJoinableTopics() {
        view.showProgressWheel();

        joinableTopicDataModel.clear();
        joinableTopicListModel.setJoinableTopicsForSearch(null);

        view.notifyDataSetChanged();

        EntityManager entityManager = EntityManager.getInstance();
        joinableTopicListModel.getJoinableTopics(entityManager.getUnjoinedChannels())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topics -> {
                    view.dismissProgressWheel();

                    if (topics == null || topics.isEmpty()) {
                        view.showHasNoTopicToJoinErrorToast();
                        view.finish();
                    } else {
                        joinableTopicDataModel.setJoinableTopics(topics);
                        joinableTopicListModel.setJoinableTopicsForSearch(topics);

                        view.notifyDataSetChanged();
                    }
                }, e -> {
                    view.dismissProgressWheel();
                    view.showHasNoTopicToJoinErrorToast();
                    view.finish();
                });
    }

    @Override
    public void onJoinTopic(Topic topic) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showJoinToTopicErrorToast();
            return;
        }

        view.showJoinToTopicToast(topic.getName());

        view.showProgressWheel();

        try {
            joinableTopicListModel.joinPublicTopic(topic.getEntityId());
            joinableTopicListModel.refreshEntity();

            EntityManager entityManager = EntityManager.getInstance();
            MixpanelMemberAnalyticsClient
                    .getInstance(JandiApplication.getContext(), entityManager.getDistictId())
                    .trackJoinChannel();

            int entityType = topic.isPublic()
                    ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            long teamId = AccountRepository.getRepository().getSelectedTeamInfo()
                    .getTeamId();

            view.dismissProgressWheel();

            view.moveToMessageActivity(topic.getEntityId(), entityType, topic.isStarred(),
                    teamId, topic.getMarkerLinkId());
        } catch (RetrofitError e) {
            LogUtil.e(Log.getStackTraceString(e));
            view.dismissProgressWheel();
            view.showJoinToTopicErrorToast();
        }
    }

    @Override
    public void onTopicClick(int position) {
        Topic item = joinableTopicDataModel.getItem(position);

        if (item == null) {
            return;
        }

        view.showUnjoinDialog(item);
    }

    @Override
    public void onTopicLongClick(int position) {
        Topic item = joinableTopicDataModel.getItem(position);

        if (item == null) {
            return;
        }

        view.showUnjoinDialog(item);
    }

    @Override
    public void onSearchTopic(CharSequence query) {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueue.onNext(query.toString());
        }
    }

}
