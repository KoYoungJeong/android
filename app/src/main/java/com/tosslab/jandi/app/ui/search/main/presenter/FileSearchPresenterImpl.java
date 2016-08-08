package com.tosslab.jandi.app.ui.search.main.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.ui.search.main.model.FileSearchModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class FileSearchPresenterImpl implements FileSearchPresenter {

    FileSearchModel fileSearchModel;
    View view;
    private PublishSubject<String> objectPublishSubject;

    @Inject
    public FileSearchPresenterImpl(FileSearchPresenter.View view, FileSearchModel fileSearchModel) {
        this.view = view;
        this.fileSearchModel = fileSearchModel;
        initObject();
    }

    void initObject() {
        objectPublishSubject = PublishSubject.create();
        objectPublishSubject
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSearchText);
    }

    void onSearchText(String text) {
        List<String> searchKeywords = fileSearchModel.searchOldQuery(text);
        view.setOldQueries(searchKeywords);
    }

    @Override
    public void onSearchTextChange(String text) {
        if (!TextUtils.isEmpty(text)) {
            objectPublishSubject.onNext(text);
            view.setMicToClearImage();
        } else {
            view.setClearToMicImage();
        }
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
            fileSearchModel.upsertQuery(searchText);
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
        fileSearchModel.upsertQuery(text);
    }
}
