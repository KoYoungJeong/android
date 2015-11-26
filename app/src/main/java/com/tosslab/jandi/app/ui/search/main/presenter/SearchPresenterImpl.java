package com.tosslab.jandi.app.ui.search.main.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.ui.search.main.model.SearchModel;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class SearchPresenterImpl implements SearchPresenter {

    @Bean
    SearchModel searchModel;

    View view;
    private PublishSubject<String> objectPublishSubject;

    @AfterInject
    void initObject() {
        objectPublishSubject = PublishSubject.create();
        objectPublishSubject
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSearchText);
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    void onSearchText(String text) {
        List<SearchKeyword> searchKeywords = searchModel.searchOldQuery(text);
        view.setOldQueries(searchKeywords);
    }

    @Override
    public void onSearchTextChange(String text) {
        if (!TextUtils.isEmpty(text)) {
            publishNext(text);
            view.setMicToClearImage();
        } else {
            view.setClearToMicImage();
        }
    }

    public void publishNext(String text) {
        objectPublishSubject.onNext(text);
    }

    @Override
    public void onSearchVoice() {
        if (TextUtils.isEmpty(view.getSearchText())) {
            view.dismissDropDown();
            view.hideSoftInput();
            view.startVoiceActivity();
        } else {
            view.setSearchText("");
            view.showSoftInput();
        }
    }

    @Override
    public void onVoiceSearchResult(List<String> voiceSearchResults) {
        if (voiceSearchResults != null && !voiceSearchResults.isEmpty()) {
            String searchText = voiceSearchResults.get(0);
            searchModel.upsertQuery(0, searchText);
            view.setSearchText(searchText);
            view.sendNewQuery(searchText);
        } else {
            view.showNoVoiceSearchItem();
        }
    }

    @Override
    public void onSearchAction(String text) {
        view.dismissDropDown();
        view.hideSoftInput();
        searchModel.upsertQuery(0, text);
    }
}
