package com.tosslab.jandi.app.ui.search.messages.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchModel;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

import java.util.List;

import retrofit.RetrofitError;

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
        int currentTeamId = messageSearchModel.getCurrentTeamId();
        searchQeuryInfo = new ReqMessageSearchQeury(currentTeamId, "", 1, ITEM_PER_PAGE);
    }

    @Override
    public void onInitEntityId(int entityId) {
        String entityName = messageSearchModel.getEntityName(entityId);
        onSelectEntity(entityId, entityName);
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    @Background(id = SEARCH_TASK)
    public void onSearchRequest(String query) {

        BackgroundExecutor.cancelAll(MORE_SEARCH_TASK, true);
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
            if (resMessageSearch.getQueryCursor().getRecordCount() >= ITEM_PER_PAGE) {
                view.setOnLoadingReady();
            } else {
                view.setOnLoadingEnd();
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    @Override
    @Background(id = MORE_SEARCH_TASK)
    public void onMoreSearchRequest() {

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
        } catch (RetrofitError e) {
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
    public void onSelectEntity(int entityId, String name) {

        view.setEntityName(name);
        searchQeuryInfo.entityId(entityId);

        if (TextUtils.isEmpty(searchQeuryInfo.getQuery())) {
            return;
        }

        view.clearSearchResult();
        onSearchRequest(searchQeuryInfo.getQuery());
    }

    @Override
    public void onSelectMember(int memberId, String name) {
        view.setMemberName(name);
        searchQeuryInfo.writerId(memberId);

        if (TextUtils.isEmpty(searchQeuryInfo.getQuery())) {
            return;
        }

        view.clearSearchResult();
        onSearchRequest(searchQeuryInfo.getQuery());
    }

    @Override
    public void onRecordClick(SearchResult searchRecord) {
        int currentTeamId = messageSearchModel.getCurrentTeamId();
        int entityId = searchRecord.getEntityId();
        int entityType = messageSearchModel.getEntityType(entityId);
        boolean isStarred = messageSearchModel.isStarredEntity(entityId);
        int linkId = searchRecord.getLinkId();

        view.startMessageListActivity(currentTeamId, entityId, entityType, isStarred, linkId);
    }

    private ResMessageSearch searchMessage(ReqMessageSearchQeury searchQeuryInfo) throws RetrofitError {

        return messageSearchModel.requestSearchQuery(searchQeuryInfo.getTeamId(), searchQeuryInfo.getQuery(), searchQeuryInfo.getPage(), ITEM_PER_PAGE, searchQeuryInfo.getEntityId(), searchQeuryInfo.getWriterId());
    }

}
