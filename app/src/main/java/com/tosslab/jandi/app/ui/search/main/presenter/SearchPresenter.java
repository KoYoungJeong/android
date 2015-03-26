package com.tosslab.jandi.app.ui.search.main.presenter;

import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface SearchPresenter {

    public void setView(View view);

    void onSearchTextChange(String s);

    void onSearchVoice();

    void onVoiceSearchResult(List<String> voiceSearchResults);

    void onSearchAction(String text);

    public interface View {

        void setOldQueries(List<SearchKeyword> searchKeywords);

        void startVoiceActivity();

        void setSearchText(String searchText);

        void showNoVoiceSearchItem();

        void sendNewQuery(String searchText);

        void setMicToClearImage();

        void setClearToMicImage();

        CharSequence getSearchText();

        void dismissDropDown();

        void hideSoftInput();

        void showSoftInput();
    }
}
