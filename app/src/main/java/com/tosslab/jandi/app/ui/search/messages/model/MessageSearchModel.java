package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.strategy.TextStrategy;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;
import com.tosslab.jandi.app.utils.JandiNetworkException;

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

    public ResMessageSearch requestSearchQuery(int teamId, String query, int page, int perPage, int entityId, int writerId) throws JandiNetworkException {
        ReqMessageSearchQeury reqMessageSearchQeury = new ReqMessageSearchQeury(teamId, query, page, perPage);
        reqMessageSearchQeury.entityId(entityId).writerId(writerId);
        return RequestManager.newInstance(context, MessageSearchRequest.newInstance(context, reqMessageSearchQeury)).request();
    }

    public int getCurrentTeamId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }

    public int getEntityType(int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        if (entity.isPublicTopic()) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateGroup()) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public boolean isStarredEntity(int entityId) {
        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }

    public List<SearchResult> convertSearchResult(List<ResMessageSearch.SearchRecord> searchRecordList, String query) {

        List<SearchResult> searchResults = new ArrayList<SearchResult>();

        if (searchRecordList == null || searchRecordList.isEmpty()) {
            return searchResults;
        }

        int size = searchRecordList.size();
        ResMessageSearch.SearchRecord searchRecord;
        SearchResult result;

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(15);
        for (int idx = 0; idx < size; ++idx) {
            result = new SearchResult();
            searchRecord = searchRecordList.get(idx);

            result.topicName(searchRecord.getSearchEntityInfo().getName());
            result.date(searchRecord.getCurrentRecord().getLastDate());

            if (searchRecord.getPrevRecord() != null) {
                result.previewText(TextStrategy.getSubSearchString(context, searchRecord.getPrevRecord(), textPaint));
            }

            if (searchRecord.getCurrentRecord() != null) {
                result.currentText(TextStrategy.getCurrentSearchString(context, searchRecord.getCurrentRecord(), query));
            }

            if (searchRecord.getNextRecord() != null) {
                result.nextText(TextStrategy.getSubSearchString(context, searchRecord.getNextRecord(), textPaint));
            }

            result.setLinkId(searchRecord.getCurrentRecord().getLinkId());
            result.setEntityId(searchRecord.getSearchEntityInfo().getId());

            searchResults.add(result);
        }

        return searchResults;
    }
}
