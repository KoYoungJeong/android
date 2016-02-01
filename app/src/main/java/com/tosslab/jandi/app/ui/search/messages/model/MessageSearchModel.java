package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.strategy.TextStrategy;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchModel {

    @RootContext
    Context context;

    public ResMessageSearch requestSearchQuery(long teamId, String query, int page, int perPage, long entityId, long writerId) throws RetrofitError {
        ReqMessageSearchQeury reqMessageSearchQeury = new ReqMessageSearchQeury(teamId, query, page, perPage);
        reqMessageSearchQeury.entityId(entityId).writerId(writerId);
        return MessageSearchManager.newInstance(reqMessageSearchQeury).request();
    }

    public long getCurrentTeamId() {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

    public boolean hasEntity(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId) != EntityManager.UNKNOWN_USER_ENTITY;
    }

    public int getEntityType(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        if (entity.isPublicTopic()) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateGroup()) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    public boolean isStarredEntity(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isStarred;
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
        return EntityManager.getInstance().getEntityNameById(entityId);
    }

    public void trackMessageKeywordSearchSuccess(String keyword) {

        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessageKeywordSearch)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.SearchKeyword, keyword)
                        .build());

    }

    public void trackMessageKeywordSearchFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessageKeywordSearch)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

    }

    public long getRoomId(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        if (entity.isPublicTopic()
                || entity.isPrivateGroup()) {
            return entityId;
        } else {
            return -1;
        }

    }
}
