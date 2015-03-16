package com.tosslab.jandi.app.ui.search.main.presenter;

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

    private View view;
    private PublishSubject<String> objectPublishSubject;

    @AfterInject
    void initObject() {
        objectPublishSubject = PublishSubject.create();

        objectPublishSubject
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        searchModel.upsertQuery(0, text);
                        List<SearchKeyword> searchKeywords = searchModel.searchOldQuery(text);
                        view.setOldQueries(searchKeywords);
                    }
                });
    }

    @Override
    public void setView(View view) {

        this.view = view;
    }

    @Override
    public void onSearchTextChange(String s) {
        objectPublishSubject.onNext(s);
    }

    @Override
    public void onSearchVoice() {
        view.startVoiceActivity();
    }

    @Override
    public void onVoiceSearchResult(List<String> voiceSearchResults) {
        if (voiceSearchResults != null && !voiceSearchResults.isEmpty()) {
            String searchText = voiceSearchResults.get(0);
            view.setSearchText(searchText);
            view.sendNewQuery(searchText);
        } else {
            view.showNoVoiceSearchItem();
        }

    }
}
