package com.tosslab.jandi.app.ui.search.main.presenter;

import com.tosslab.jandi.app.ui.search.main.model.SearchModel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class SearchPresenterImpl implements SearchPresenter {

    @Bean
    SearchModel searchModel;

    private View view;

    @Override
    public void setView(View view) {

        this.view = view;
    }
}
