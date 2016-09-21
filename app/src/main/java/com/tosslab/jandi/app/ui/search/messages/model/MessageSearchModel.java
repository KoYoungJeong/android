package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.search.messages.adapter.strategy.TextStrategy;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchModel {

    @RootContext
    Context context;

    public ResMessageSearch requestSearchQuery(long teamId, String query, int page, int perPage, long entityId, long writerId) throws RetrofitException {
        ReqMessageSearchQeury reqMessageSearchQeury = new ReqMessageSearchQeury(teamId, query, page, perPage);
        reqMessageSearchQeury.entityId(entityId).writerId(writerId);
        return MessageSearchManager.newInstance(reqMessageSearchQeury).request();
    }

    public long getCurrentTeamId() {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

    public boolean hasEntity(long entityId) {
        return TeamInfoLoader.getInstance().isTopic(entityId)
                || TeamInfoLoader.getInstance().isUser(entityId);
    }

    public int getEntityType(long entityId) {
        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                return JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                return JandiConstants.TYPE_PRIVATE_TOPIC;
            }
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public boolean isStarredEntity(long entityId) {
        return TeamInfoLoader.getInstance().isStarred(entityId)
                || TeamInfoLoader.getInstance().isStarredUser(entityId);
    }

    public List<SearchResult> convertSearchResult(List<ResMessageSearch.SearchRecord> searchRecordList, String query) {

        List<SearchResult> searchResults = new ArrayList<SearchResult>();

        if (searchRecordList == null || searchRecordList.isEmpty()) {
            return searchResults;
        }

        int size = searchRecordList.size();
        ResMessageSearch.SearchRecord searchRecord;
        SearchResult result;

        for (int idx = 0; idx < size; ++idx) {
            result = new SearchResult();
            searchRecord = searchRecordList.get(idx);

            result.topicName(searchRecord.getSearchEntityInfo().getName());
            result.date(searchRecord.getCurrentRecord().getLastDate());

            if (searchRecord.getPrevRecord() != null) {
                result.previewText(TextStrategy.getSubSearchString(context, searchRecord.getPrevRecord()));
            }

            if (searchRecord.getCurrentRecord() != null) {
                result.currentText(TextStrategy.getCurrentSearchString(context, searchRecord.getCurrentRecord(), query));
            }

            if (searchRecord.getNextRecord() != null) {
                result.nextText(TextStrategy.getSubSearchString(context, searchRecord.getNextRecord()));
            }

            result.setLinkId(searchRecord.getCurrentRecord().getLinkId());
            result.setEntityId(searchRecord.getSearchEntityInfo().getId());

            searchResults.add(result);
        }

        return searchResults;
    }

    public String getEntityName(long entityId) {
        return TeamInfoLoader.getInstance().getName(entityId);
    }

    public long getRoomId(long entityId) {
        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            return entityId;
        } else {
            return -1;
        }

    }
}
