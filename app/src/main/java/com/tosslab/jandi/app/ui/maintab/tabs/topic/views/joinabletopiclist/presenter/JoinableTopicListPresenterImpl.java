package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.presenter;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model.JoinableTopicDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model.JoinableTopicListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicListView;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class JoinableTopicListPresenterImpl implements JoinableTopicListPresenter {

    private final JoinableTopicDataModel joinableTopicDataModel;
    private final JoinableTopicListView view;
    @Inject
    JoinableTopicListModel joinableTopicListModel;
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
                        .map(query -> Pair.create(query,
                                joinableTopicListModel.getSearchedTopics(query)))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            view.dismissProgressWheel();

                            String query = pair.first;
                            List<Topic> topics = pair.second;

                            joinableTopicDataModel.clear();

                            if (topics == null || topics.isEmpty()) {
                                view.notifyDataSetChanged();
                                if (TextUtils.isEmpty(query)) {
                                    view.showHasNoTopicToJoinErrorToast();
                                    view.finish();
                                } else {
                                    view.showEmptyQueryMessage(query);
                                }
                                return;
                            }

                            view.hideEmptyQueryMessage();
                            joinableTopicDataModel.setJoinableTopics(topics);
                            view.notifyDataSetChanged();
                        }, e -> {
                            LogUtil.e(Log.getStackTraceString(e));
                            view.dismissProgressWheel();
                            view.showHasNoTopicToJoinErrorToast();
                            view.finish();
                        });
    }

    @Override
    public void stopSearchTopicQueue() {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onJoinTopic(final long topicEntityId) {
        Topic topic = joinableTopicDataModel.getItemByEntityId(topicEntityId);

        if (!NetworkCheckUtil.isConnected()
                || topic == null
                || topic.getEntityId() <= 0) {
            view.showJoinToTopicErrorToast();
            return;
        }

        view.showJoinToTopicToast(topic.getName());
        view.showProgressWheel();

        joinableTopicListModel.getJoinTopicObservable(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topic1 -> {
                    int entityType = topic.isPublic()
                            ? JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
                    long teamId = AccountRepository.getRepository().getSelectedTeamInfo()
                            .getTeamId();
                    view.dismissProgressWheel();

                    view.moveToMessageActivity(topic.getEntityId(), entityType, topic.isStarred(),
                            teamId, topic.getMarkerLinkId());
                }, e -> {

                    LogUtil.e(Log.getStackTraceString(e));
                    view.dismissProgressWheel();
                    view.showJoinToTopicErrorToast();
                });
    }

    @Override
    public void onTopicClick(int position) {
        Topic item = joinableTopicDataModel.getItem(position);

        if (item == null) {
            return;
        }

        TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(item.getEntityId());

        view.showTopicInfoDialog(topicRoom);
    }

    @Override
    public void onSearchTopic(boolean withProgress, CharSequence query) {
        if (withProgress) {
            view.showProgressWheel();
        }
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            if (query == null) {
                query = "";
            }
            searchQueryQueue.onNext(query.toString());
        }
    }

    @Override
    public void onShouldShowSelectedTopic(long topicEntityId) {
        int position = joinableTopicDataModel.getPositionByTopicEntityId(topicEntityId);
        if (position < 0) {
            return;
        }

        view.showSelectedTopic(position);

    }

}
