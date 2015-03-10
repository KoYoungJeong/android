package com.tosslab.jandi.app.ui.search.messages.presenter;

import org.androidannotations.annotations.EBean;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class MessageSearchPresenterImpl implements MessageSearchPresenter {

    private View view;

    @Override
    public void setView(View view) {

        this.view = view;
    }
}
