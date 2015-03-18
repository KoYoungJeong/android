package com.tosslab.jandi.app.ui.search.messages.presenter;

import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.api.BackgroundExecutor;

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
    public void setView(View view) {
        this.view = view;
    }

    @Override
    @Background(id = SEARCH_TASK)
    public void onSearchRequest(String query) {

        BackgroundExecutor.cancelAll(MORE_SEARCH_TASK, true);

        view.showLoading(query);

        ReqMessageSearchQeury tempSearchInfo = new ReqMessageSearchQeury(messageSearchModel.getCurrentTeamId(), query, START_PAGE, ITEM_PER_PAGE);
        tempSearchInfo.writerId(searchQeuryInfo.getWriterId()).entityId(searchQeuryInfo.getEntityId());

        searchQeuryInfo = tempSearchInfo;

        try {
            ResMessageSearch resMessageSearch = searchMessage();
            view.clearSearchResult();
            view.setQueryResult(query, resMessageSearch.getQueryCursor().getTotalCount());
            view.addSearchResult(resMessageSearch.getSearchRecords());
            if (resMessageSearch.getQueryCursor().getRecordCount() >= ITEM_PER_PAGE) {
                view.setOnLoadingReady();
            } else {
                view.setOnLoadingEnd();
            }
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }

    }

    @Override
    @Background(id = MORE_SEARCH_TASK)
    public void onMoreSearchRequest() {

        searchQeuryInfo.setPage(searchQeuryInfo.getPage() + 1);

        try {
            ResMessageSearch resMessageSearch = searchMessage();
            view.addSearchResult(resMessageSearch.getSearchRecords());
            if (resMessageSearch.getQueryCursor().getRecordCount() >= ITEM_PER_PAGE) {
                view.setOnLoadingReady();
            } else {
                view.setOnLoadingEnd();
            }
        } catch (JandiNetworkException e) {
            e.printStackTrace();
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
    }

    @Override
    public void onSelectMember(int memberId, String name) {
        view.setMemberName(name);
    }

    @Override
    public void onRecordClick(ResMessageSearch.SearchRecord searchRecord) {
        int currentTeamId = messageSearchModel.getCurrentTeamId();
        int entityId = searchRecord.getSearchEntityInfo().getId();
        int entityType = messageSearchModel.getEntityType(entityId);
        boolean isStarred = messageSearchModel.isStarredEntity(entityId);
        int linkId = searchRecord.getCurrentRecord().getLinkId();

        view.startMessageListActivity(currentTeamId, entityId, entityType, isStarred, linkId);
    }

    private ResMessageSearch searchMessage() throws JandiNetworkException {

        return messageSearchModel.requestSearchQuery(searchQeuryInfo.getTeamId(), searchQeuryInfo.getQuery(), searchQeuryInfo.getPage(), ITEM_PER_PAGE, searchQeuryInfo.getEntityId(), searchQeuryInfo.getWriterId());
    }

}