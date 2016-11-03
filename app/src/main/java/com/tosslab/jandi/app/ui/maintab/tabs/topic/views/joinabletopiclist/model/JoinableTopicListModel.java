package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class JoinableTopicListModel {

    private final EntityClientManager entityClientManager;

    @Inject
    public JoinableTopicListModel(EntityClientManager entityClientManager) {
        this.entityClientManager = entityClientManager;
    }

    public void joinPublicTopic(long id) throws RetrofitException {
        entityClientManager.joinChannel(id);
    }

    public List<Topic> getSearchedTopics(final String query) {
        List<Topic> topics = new ArrayList<>();
        Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(room -> !room.isJoined())
                .map(topicRoom -> {
                    long creatorId = topicRoom.getCreatorId();
                    return new Topic.Builder()
                            .entityId(topicRoom.getId())
                            .description(topicRoom.getDescription())
                            .isJoined(false)
                            .creatorId(creatorId)
                            .isPublic(topicRoom.isPublicTopic())
                            .isStarred(topicRoom.isStarred())
                            .memberCount(topicRoom.getMemberCount())
                            .name(topicRoom.getName())
                            .markerLinkId(topicRoom.getReadLinkId())
                            .unreadCount(topicRoom.getUnreadCount())
                            .build();
                })
                .filter(topic -> TextUtils.isEmpty(query)
                        || topic.getName().toLowerCase().contains(query.toLowerCase()))
                .toSortedList((lhs, rhs) -> StringCompareUtil.compare(lhs.getName(), rhs.getName()))
                .collect(() -> topics, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);
        return topics;
    }

    public Observable<Topic> getJoinTopicObservable(final Topic topic) {
        return Observable.<Topic>create(subscriber -> {
            try {
                joinPublicTopic(topic.getEntityId());
                TopicRepository.getInstance().updateTopicJoin(topic.getEntityId(), true);
                TeamInfoLoader.getInstance().refresh();
                subscriber.onNext(topic);
            } catch (RetrofitException error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        });
    }
}
