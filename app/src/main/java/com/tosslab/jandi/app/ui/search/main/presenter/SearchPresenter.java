package com.tosslab.jandi.app.ui.search.main.presenter;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface SearchPresenter {

    void setView(View view);

    void onSearchTextChange(String s);

    void onSearchVoice();

    void onVoiceSearchResult(List<String> voiceSearchResults);

    void onSearchAction(String text);

    interface View {

        void setOldQueries(List<String> searchKeywords);

        void startVoiceActivity();

        void showNoVoiceSearchItem();

        void sendNewQuery(String searchText);

        void setMicToClearImage();

        void setClearToMicImage();

        CharSequence getSearchText();

        void setSearchText(String searchText);

        void dismissDropDown();

        void hideSoftInput();

        void showSoftInput();
    }
}
