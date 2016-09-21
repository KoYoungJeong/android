package com.tosslab.jandi.app.ui.search.messages.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchModel;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrMessageKeywordSearch;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

import java.util.List;


/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchPresenterImpl implements MessageSearchPresenter {

    private static final int ITEM_PER_PAGE = 20;
    private static final int START_PAGE = 1;

    private static final String SEARCH_TASK = "search_task";
    private static final String MORE_SEARCH_TASK = "more_search_task";
    @Bean
    MessageSearchModel messageSearchModel;

    private View view;

    private ReqMessageSearchQeury searchQeuryInfo;

    @AfterInject
    void initObject() {
        long currentTeamId = messageSearchModel.getCurrentTeamId();
        searchQeuryInfo = new ReqMessageSearchQeury(currentTeamId, "", 1, ITEM_PER_PAGE);
    }

    @Override
    public void onInitEntityId(long entityId) {
        String entityName = messageSearchModel.getEntityName(entityId);
        onSelectEntity(entityId, entityName, "");
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    @Background(id = SEARCH_TASK)
    public void onSearchRequest(String query) {

        BackgroundExecutor.cancelAll(MORE_SEARCH_TASK, true);

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        view.clearSearchResult();
        view.showLoading(query);

        ReqMessageSearchQeury tempSearchInfo = new ReqMessageSearchQeury(messageSearchModel.getCurrentTeamId(), query, START_PAGE, ITEM_PER_PAGE);
        tempSearchInfo.writerId(searchQeuryInfo.getWriterId()).entityId(searchQeuryInfo.getEntityId());

        searchQeuryInfo = tempSearchInfo;

        try {
            ResMessageSearch resMessageSearch = searchMessage(searchQeuryInfo);
            List<SearchResult> searchResults = messageSearchModel.convertSearchResult(resMessageSearch.getSearchRecords(), searchQeuryInfo.getQuery());
            view.setQueryResult(query, resMessageSearch.getQueryCursor().getTotalCount());
            view.addSearchResult(searchResults);

            SprinklrMessageKeywordSearch.sendLog(query);

            if (resMessageSearch.getQueryCursor().getRecordCount() >= ITEM_PER_PAGE) {
                view.setOnLoadingReady();
            } else {
                view.setOnLoadingEnd();
            }
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            SprinklrMessageKeywordSearch.trackFail(errorCode);
            e.printStackTrace();
        }

    }

    @Override
    @Background(id = MORE_SEARCH_TASK)
    public void onMoreSearchRequest() {

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        view.showMoreLoadingProgressBar();

        searchQeuryInfo.setPage(searchQeuryInfo.getPage() + 1);

        try {
            ResMessageSearch resMessageSearch = searchMessage(searchQeuryInfo);
            List<SearchResult> searchResults = messageSearchModel.convertSearchResult(resMessageSearch.getSearchRecords(), searchQeuryInfo.getQuery());
            view.addSearchResult(searchResults);
            if (resMessageSearch.getQueryCursor().getRecordCount() >= ITEM_PER_PAGE) {
                view.setOnLoadingReady();
            } else {
                view.setOnLoadingEnd();
            }
        } catch (RetrofitException e) {
            e.printStackTrace();
        } catch (Exception e) {

        } finally {
            view.dismissMoreLoadingProgressBar();
        }
    }

    @Override
    public void onEntityClick() {
        view.showEntityDialog();
    }

    @Override
    public void onMemberClick() {
        view.showMemberDialog();
    }

    @Override
    public void onSelectEntity(long entityId, String name, String searchText) {

        view.setEntityName(name);
        searchQeuryInfo.entityId(entityId);

        String targetText;
        if (searchText == null) {
            targetText = searchQeuryInfo.getQuery();
        } else {
            targetText = searchText;
        }

        if (TextUtils.isEmpty(targetText)) {
            return;
        }

        view.clearSearchResult();
        onSearchRequest(targetText);
    }

    @Override
    public void onSelectMember(long memberId, String name, String searchText) {
        view.setMemberName(name);
        searchQeuryInfo.writerId(memberId);

        String targetText;

        if (searchText == null) {
            targetText = searchQeuryInfo.getQuery();
        } else {
            targetText = searchText;
        }

        if (TextUtils.isEmpty(targetText)) {
            return;
        }

        view.clearSearchResult();
        onSearchRequest(targetText);
    }

    @Override
    public void onRecordClick(SearchResult searchRecord) {
        long entityId = searchRecord.getEntityId();
        if (!messageSearchModel.hasEntity(entityId)) {
            view.showInvalidateEntityToast();
            return;
        }

        long currentTeamId = messageSearchModel.getCurrentTeamId();
        int entityType = messageSearchModel.getEntityType(entityId);
        long roomId = messageSearchModel.getRoomId(entityId);
        boolean isStarred = messageSearchModel.isStarredEntity(entityId);
        long linkId = searchRecord.getLinkId();


        view.startMessageListActivity(currentTeamId, entityId, entityType, roomId, isStarred, linkId);
    }

    private ResMessageSearch searchMessage(ReqMessageSearchQeury searchQeuryInfo) throws RetrofitException {

        return messageSearchModel.requestSearchQuery(searchQeuryInfo.getTeamId(), searchQeuryInfo.getQuery(), searchQeuryInfo.getPage(), ITEM_PER_PAGE, searchQeuryInfo.getEntityId(), searchQeuryInfo.getWriterId());
    }

}
