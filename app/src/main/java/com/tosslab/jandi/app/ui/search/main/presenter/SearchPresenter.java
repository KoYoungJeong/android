package com.tosslab.jandi.app.ui.search.main.presenter;

import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface SearchPresenter {

    public void setView(View view);

    void onSearchTextChange(String s);

    void onSearchAction(CharSequence text);

    public interface View {

        void setOldQueries(List<SearchKeyword> searchKeywords);
    }
}
