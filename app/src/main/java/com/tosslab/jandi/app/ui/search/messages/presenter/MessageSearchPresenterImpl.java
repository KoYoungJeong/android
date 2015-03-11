package com.tosslab.jandi.app.ui.search.messages.presenter;

import com.tosslab.jandi.app.ui.search.messages.model.MessageSearchModel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchPresenterImpl implements MessageSearchPresenter {

    @Bean
    MessageSearchModel messageSearchModel;

    private View view;

    @Override
    public void setView(View view) {

        this.view = view;
    }

    @Override
    public void onSearchRequest(String query) {

        messageSearchModel.requestSearchQuery(query);
    }

}
