package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import com.tosslab.jandi.app.ui.search.main_temp.model.SearchModel;

import javax.inject.Inject;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchPresenterImpl extends SearchPresenter {

    @Inject
    SearchModel searchModel;

    @Inject
    SearchPresenter.View view;

    @Inject
    public SearchPresenterImpl() {
    }

}
