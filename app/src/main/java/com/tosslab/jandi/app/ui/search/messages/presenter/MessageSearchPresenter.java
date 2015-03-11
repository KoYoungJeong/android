package com.tosslab.jandi.app.ui.search.messages.presenter;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public interface MessageSearchPresenter {

    public void setView(View view);

    void onSearchRequest(String query);

    public interface View {

    }
}
