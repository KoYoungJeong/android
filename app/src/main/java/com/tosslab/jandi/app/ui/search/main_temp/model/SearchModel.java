package com.tosslab.jandi.app.ui.search.main_temp.model;

import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 16. 7. 20..
 */
public class SearchModel {

    @Inject
    Lazy<SearchApi> searchApi;

    @Inject
    public SearchModel() {
    }

    public ResSearch searchMessages(long teamId, long writerId, long roomId, String keyword) throws RetrofitException {
        ReqSearch.Builder reqSearchBuilder = new ReqSearch.Builder();

        reqSearchBuilder.setType("message");

        if (writerId != -1) {
            reqSearchBuilder.setWriterId(writerId);
        }

        if (roomId != -1) {
            reqSearchBuilder.setRoomId(roomId);
        }

        reqSearchBuilder.setKeyword(keyword);

        return searchApi.get().getSearch(teamId, reqSearchBuilder.build());
    }

    public List<SearchTopicRoomData> getSearchedTopics(String keyword, boolean isShowUnjoinedTopic) {
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        List<SearchTopicRoomData> topics = new ArrayList<>();

        InitialInfo initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);
        Collection<Topic> initialInfoTopics = initialInfo.getTopics();
        Observable.from(initialInfoTopics)
                .map(topicRoom -> new SearchTopicRoomData.Builder()
                        .setTopicId(topicRoom.getId())
                        .setTitle(topicRoom.getName())
                        .setMemberCnt(topicRoom.getMembers().size())
                        .setIsPublic(topicRoom.getType().equals("channel"))
                        .setIsJoined(topicRoom.isJoined())
                        .setIsStarred(topicRoom.isStarred())
                        .setDescription(topicRoom.getDescription())
                        .build())
                .filter(topic -> {
                    if (!isShowUnjoinedTopic) {
                        return topic.isJoined() == true;
                    } else {
                        return true;
                    }
                })
                .filter(topic -> topic.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .toSortedList((lhs, rhs) -> {
                    if (lhs.isStarred() && rhs.isStarred()) {
                        return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                    }
                })
                .collect(() -> topics, List::addAll)
                .subscribe();

        return topics;
    }

}