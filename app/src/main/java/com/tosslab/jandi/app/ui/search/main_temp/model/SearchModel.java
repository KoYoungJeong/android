package com.tosslab.jandi.app.ui.search.main_temp.model;

import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
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

    public ResSearch search(long teamId, ReqSearch reqSearch) throws RetrofitException {
        return searchApi.get().getSearch(teamId, reqSearch);
    }

    public List<com.tosslab.jandi.app.ui.maintab.topic.domain.Topic> getSearchedTopics(String keyword, boolean isPaticipate) {

        long teamId = TeamInfoLoader.getInstance().getTeamId();

        List<com.tosslab.jandi.app.ui.maintab.topic.domain.Topic> topics = new ArrayList<>();

        InitialInfo initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);
        List<Topic> initialInfoTopics = (ArrayList) initialInfo.getTopics();
        Observable.from(initialInfoTopics)
                .filter(topic -> {
                    if (isPaticipate) {
                        return topic.isJoined() == true;
                    } else {
                        return true;
                    }
                })
                .filter(topic -> topic.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toSortedList((lhs, rhs) -> StringCompareUtil.compare(lhs.getName(), rhs.getName()))
                .collect(() -> topics, List::addAll)
                .subscribe();

        return topics;
    }

}