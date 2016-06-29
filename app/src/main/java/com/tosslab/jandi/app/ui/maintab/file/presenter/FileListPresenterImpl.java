package com.tosslab.jandi.app.ui.maintab.file.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapterModel;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.file.to.SearchQueryTO;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 16. 6. 28..
 */

public class FileListPresenterImpl implements FileListPresenterV3 {

    View view;

    FileListModel fileListModel;

    private long entityId;
    private SearchedFilesAdapterModel searchedFilesAdapterModel;
    private long selectedTeamId;
    private String entityName;
    private PublishSubject<Integer> searchSubject;
    private SearchQueryTO searchQuery;

    public FileListPresenterImpl(long entityId, FileListModel fileListModel) {
        this.entityId = entityId;
        this.fileListModel = fileListModel;
        selectedTeamId = fileListModel.getSelectedTeamId();
    }

    @Override
    public void setSearchedFilesAdapterModel(SearchedFilesAdapterModel adapterModel) {
        this.searchedFilesAdapterModel = adapterModel;
    }

    @Override
    public void initSearchQuery() {
        searchQuery = new SearchQueryTO();
        if (entityId >= 0) {
            searchQuery.setEntityId(entityId);
            this.entityName = TeamInfoLoader.getInstance()
                    .getName(entityId);
        }

        searchSubject = PublishSubject.create();
        searchSubject.observeOn(Schedulers.io())
                .subscribe(index -> {
                    searchQuery.setToFirst();
                    view.clearListView();
                    doSearchInBackground(index);
                }, throwable -> LogUtil.d("Search Fail : " + throwable.getMessage()));
    }

    private void doSearchInBackground(int requestCount) {
        Boolean[] isFinish = new Boolean[1];
        isFinish[0] = false;

        //TODO 수정
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    view.setInitLoadingViewVisible(android.view.View.VISIBLE);
                    view.setEmptyViewVisible(android.view.View.GONE);
                    view.setSearchEmptryViewVisible(android.view.View.GONE);

                    if (!NetworkCheckUtil.isConnected()) {
                        view.setInitLoadingViewVisible(android.view.View.GONE);
                        view.setEmptyViewVisible(android.view.View.GONE);
                        view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
                        isFinish[0] = true;
                    }
                });

        if (isFinish[0]) {
            return;
        }

        ReqSearchFile reqSearchFile = searchQuery.getRequestQuery();
        reqSearchFile.teamId = selectedTeamId;
        if (requestCount > ReqSearchFile.MAX) {
            reqSearchFile.listCount = requestCount;
        }

        Observable.create((Observable.OnSubscribe<ResSearchFile>) subscriber -> {
            try {
                ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);
                String keyword = reqSearchFile.keyword;
                fileListModel.trackFileKeywordSearchSuccess(keyword);
                if (resSearchFile.fileCount < reqSearchFile.listCount) {
                    searchedFilesAdapterModel.setNoMoreLoad();
                } else {
                    searchedFilesAdapterModel.setReadyMore();
                }

                updateAdapterModel(resSearchFile);
                subscriber.onNext(resSearchFile);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(resSearchFile -> {
                    view.setInitLoadingViewVisible(android.view.View.GONE);
                    if (fileListModel.isDefaultSearchQuery(searchQuery.getRequestQuery())) {
                        if (resSearchFile.fileCount > 0) {
                            view.setEmptyViewVisible(android.view.View.GONE);
                        } else {
                            view.setEmptyViewVisible(android.view.View.VISIBLE);
                        }
                        view.setSearchEmptryViewVisible(android.view.View.GONE);
                    } else {
                        if (resSearchFile.fileCount > 0) {
                            view.setSearchEmptryViewVisible(android.view.View.GONE);
                        } else {
                            view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
                        }
                        view.setEmptyViewVisible(android.view.View.GONE);
                    }
                    view.searchSucceed(resSearchFile);
                    if (fileListModel.isAllTypeFirstSearch(reqSearchFile)) {
                        fileListModel.saveOriginFirstItems(selectedTeamId, resSearchFile);
                    }
                })
                .subscribe(o -> {
                }, e -> {
                    if (e instanceof RetrofitException) {
                        RetrofitException e1 = (RetrofitException) e;
                        int errorCode = e1.getStatusCode();
                        fileListModel.trackFileKeywordSearchFail(errorCode);
                        e.printStackTrace();
                        LogUtil.e("fail to get searched files.", e);
                        view.searchFailed(R.string.err_file_search);
                    } else {
                        e.printStackTrace();
                        fileListModel.trackFileKeywordSearchFail(-1);
                        view.searchFailed(R.string.err_file_search);
                    }
                });


    }

    void getPreviousFile() {
        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        Observable.create(new Observable.OnSubscribe<ResSearchFile>() {
            @Override
            public void call(Subscriber<? super ResSearchFile> subscriber) {
                try {
                    ReqSearchFile reqSearchFile = searchQuery.getRequestQuery();
                    reqSearchFile.teamId = selectedTeamId;
                    ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);
                    subscriber.onNext(resSearchFile);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> {
                    view.showMoreProgressBar();
                })
                .doOnUnsubscribe(() -> {
                    view.dismissMoreProgressBar();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(resSearchFile -> {
                    if (resSearchFile.fileCount > 0) {
                        searchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                        searchedFilesAdapterModel.add(fileListModel.descSortByCreateTime(resSearchFile.files));
                    }
                    if (resSearchFile.fileCount < ReqSearchFile.MAX) {
                        view.showWarningToast(JandiApplication.getContext().getString(R.string.warn_no_more_files));
                        setListNoMoreLoad();
                    } else {
                        setListReadyLoadMore();
                    }
                })
                .subscribe(o -> {

                }, e -> {
                    if (e instanceof RetrofitException) {
                        e.printStackTrace();
                        LogUtil.e("fail to get searched files.", e);
                        view.searchFailed(R.string.err_file_search);
                    } else {
                        e.printStackTrace();
                        view.searchFailed(R.string.err_file_search);
                    }
                });
    }

    private void updateAdapterModel(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount > 0) {
            searchedFilesAdapterModel.add(fileListModel.descSortByCreateTime(resSearchFile.files));
            searchQuery.setNext(resSearchFile.firstIdOfReceivedList);
        }
    }

    @Override
    public void setListNoMoreLoad() {
        searchedFilesAdapterModel.setNoMoreLoad();
    }

    @Override
    public void setListReadyLoadMore() {
        searchedFilesAdapterModel.setReadyMore();
    }

    @Override
    public long getSearchedEntityId() {
        return searchQuery.getEntityId();
    }

    @Override
    public boolean isDefaultSeachQuery() {
        return searchQuery.getEntityId() == ReqSearchFile.ALL_ENTITIES
                && searchQuery.getSearchFileType().equals("all")
                && searchQuery.getSearchUser().equals("all");
    }

    @Override
    public void doSearchAll() {
        searchSubject.onNext(-1);
    }

    @Override
    public void doSearchByCnt(int cnt) {
        searchSubject.onNext(cnt);
    }


}
