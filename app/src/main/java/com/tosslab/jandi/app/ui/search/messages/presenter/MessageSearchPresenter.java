package com.tosslab.jandi.app.ui.search.messages.presenter;

import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface MessageSearchPresenter {

    void setView(View view);

    void onSearchRequest(String query);

    void onMoreSearchRequest();

    void onEntityClick();

    void onMemberClick();

    void onSelectEntity(int entityId, String name, String searchText);

    void onSelectMember(int memberId, String name, String searchText);

    void onRecordClick(SearchResult searchRecord);

    void onInitEntityId(int entityId);

    interface View {

        void clearSearchResult();

        void addSearchResult(List<SearchResult> searchRecords);

        void showEntityDialog();

        void showMemberDialog();

        void setEntityName(String name);

        void setMemberName(String name);

        void setQueryResult(String query, int totalCount);

        void showLoading(String query);

        void startMessageListActivity(int currentTeamId, int entityId, int entityType, boolean isStarred, int linkId);

        void setOnLoadingReady();

        void setOnLoadingEnd();

        void showMoreLoadingProgressBar();

        void dismissMoreLoadingProgressBar();

        void showInvalidNetworkDialog();
    }
}
