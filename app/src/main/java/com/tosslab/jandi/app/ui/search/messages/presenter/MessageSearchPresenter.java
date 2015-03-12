package com.tosslab.jandi.app.ui.search.messages.presenter;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface MessageSearchPresenter {

    public void setView(View view);

    void onSearchRequest(String query);

    void onMoreSearchRequest();

    void onEntityClick();

    void onMemberClick();

    void onSelectEntity(int entityId, String name);

    void onSelectMember(int memberId, String name);

    public interface View {

        void clearSearchResult();

        void addSearchResult(List<ResMessageSearch.SearchRecord> searchRecords);

        void showEntityDialog();

        void showMemberDialog();

        void setEntityName(String name);

        void setMemberName(String name);

        void setQueryWord(String query);
    }
}
