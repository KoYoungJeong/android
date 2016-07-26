package com.tosslab.jandi.app.ui.maintab.file.presenter;

import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapterModel;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.maintab.file.to.SearchQueryTO;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 16. 6. 28..
 */

public class FileListPresenterImpl implements FileListPresenter {

    private FileListPresenter.View view;

    private FileListModel fileListModel;

    private long entityId;
    private SearchedFilesAdapterModel searchedFilesAdapterModel;
    private long selectedTeamId;
    private PublishSubject<Integer> searchSubject;
    private SearchQueryTO searchQuery;
    private PublishSubject<Object> previousLoadSubject;

    public FileListPresenterImpl(long entityId, FileListModel fileListModel, FileListPresenter.View view) {
        this.entityId = entityId;
        this.fileListModel = fileListModel;
        this.view = view;
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
        }

        searchSubject = PublishSubject.create();
        searchSubject.onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .concatMap(requestCount -> {
                    searchQuery.setToFirst();
                    view.clearListView();
                    return loadSearchObservable(requestCount);
                })
                .subscribe(o -> {
                    view.onSearchHeaderReset();
                }, throwable -> LogUtil.d("ReqSearch Fail : " + throwable.getMessage()));

        previousLoadSubject = PublishSubject.create();
        previousLoadSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .concatMap(o -> FileListPresenterImpl.this.loadPreviousFileObservable())
                .subscribe(o -> {
                }, throwable -> LogUtil.d("Load Fail : " + throwable.getMessage()));
    }

    private Observable<Object> loadSearchObservable(int requestCount) {

        if (!NetworkCheckUtil.isConnected()) {
            return Observable.empty();
        }

        return Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(i -> {
                    view.setInitLoadingViewVisible(android.view.View.VISIBLE);
                    view.setEmptyViewVisible(android.view.View.GONE);
                    view.setSearchEmptryViewVisible(android.view.View.GONE);
                })
                .observeOn(Schedulers.io())
                .map(i -> {
                    ReqSearchFile reqSearchFile = searchQuery.getRequestQuery();
                    reqSearchFile.teamId = selectedTeamId;
                    if (requestCount > ReqSearchFile.MAX) {
                        reqSearchFile.listCount = requestCount;
                    }
                    return reqSearchFile;
                })
                .concatMap(reqSearchFile -> {
                    try {
                        ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);
                        String keyword = reqSearchFile.keyword;
                        fileListModel.trackFileKeywordSearchSuccess(keyword);
                        return Observable.just(Pair.create(reqSearchFile, resSearchFile));
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                        return Observable.error(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> {
                    if (pair.second.fileCount < pair.first.listCount) {
                        searchedFilesAdapterModel.setNoMoreLoad();
                    } else {
                        searchedFilesAdapterModel.setReadyMore();
                    }
                    updateAdapterModel(pair.second);
                    view.setInitLoadingViewVisible(android.view.View.GONE);
                    if (fileListModel.isDefaultSearchQuery(searchQuery.getRequestQuery())) {
                        if (pair.second.fileCount > 0) {
                            view.setEmptyViewVisible(android.view.View.GONE);
                        } else {
                            view.setEmptyViewVisible(android.view.View.VISIBLE);
                        }
                        view.setSearchEmptryViewVisible(android.view.View.GONE);
                    } else {
                        if (pair.second.fileCount > 0) {
                            view.setSearchEmptryViewVisible(android.view.View.GONE);
                        } else {
                            view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
                        }
                        view.setEmptyViewVisible(android.view.View.GONE);
                    }
                    view.searchSucceed(pair.second);
                    if (fileListModel.isAllTypeFirstSearch(pair.first)) {
                        fileListModel.saveOriginFirstItems(selectedTeamId, pair.second);
                    }
                }).doOnError(e -> {
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
                }).map(pair -> new Object());
    }

    @Override
    public void getPreviousFile() {
        previousLoadSubject.onNext(new Object());
    }

    public Observable<Object> loadPreviousFileObservable() {
        if (!NetworkCheckUtil.isConnected()) {
            return Observable.empty();
        }

        return Observable.defer(() -> {
            ReqSearchFile reqSearchFile = searchQuery.getRequestQuery();
            reqSearchFile.teamId = selectedTeamId;
            return Observable.just(reqSearchFile);
        })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> view.showMoreProgressBar())
                .observeOn(Schedulers.io())
                .concatMap(reqSearchFile -> {
                    try {
                        ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);
                        return Observable.just(resSearchFile);
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(resSearchFile -> {
                    if (resSearchFile.fileCount > 0) {
                        searchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                        searchedFilesAdapterModel.add(fileListModel.descSortByCreateTime(resSearchFile.files));
                        view.justRefresh();
                    }
                    if (resSearchFile.fileCount < ReqSearchFile.MAX) {
                        view.showWarningToast(JandiApplication.getContext().getString(R.string.warn_no_more_files));
                        setListNoMoreLoad();
                    } else {
                        setListReadyLoadMore();
                    }
                    view.dismissMoreProgressBar();
                }).doOnError(e -> {
                    if (e instanceof RetrofitException) {
                        e.printStackTrace();
                        LogUtil.e("fail to get searched files.", e);
                        view.searchFailed(R.string.err_file_search);
                    } else {
                        e.printStackTrace();
                        view.searchFailed(R.string.err_file_search);
                    }
                }).map(i -> new Object());

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
        view.clearListView();
        searchSubject.onNext(-1);
    }

    @Override
    public void onFileShare(long teamId) {
        if (teamId != fileListModel.getSelectedTeamId()) {
            return;
        }
        int itemCount = searchedFilesAdapterModel.getItemCount();
        searchSubject.onNext(itemCount);
    }

    @Override
    public void onFileDeleted(long teamId, long fileId) {
        if (teamId != fileListModel.getSelectedTeamId()) {
            return;
        }

        int positionByFileId = searchedFilesAdapterModel.findPositionByFileId(fileId);
        if (positionByFileId >= 0) {
            removeItem(positionByFileId);
        }
    }

    @Override
    public void onFileTypeSelection(String query, String searchText) {
        searchQuery.setFileType(query);
        if (searchText != null) {
            searchQuery.setKeyword(searchText);
        }
        view.clearListView();
        searchSubject.onNext(-1);
    }

    @Override
    public void onMemberSelection(String userId, String searchText) {
        searchQuery.setWriter(userId);
        if (searchText != null) {
            searchQuery.setKeyword(searchText);
        }
        view.clearListView();
        searchSubject.onNext(-1);
    }

    @Override
    public void onEntitySelection(long sharedEntityId, String searchText) {
        searchQuery.setEntityId(sharedEntityId);
        if (searchText != null) {
            searchQuery.setKeyword(searchText);
        }
        view.clearListView();
        searchSubject.onNext(-1);
    }

    @Override
    public void onTopicDeleted(long teamId) {
        if (teamId != fileListModel.getSelectedTeamId()) {
            return;
        }
        // 토픽이 삭제되거나 나간 경우 해당 토픽의 파일 접근 여부를 알 수 없으므로
        // 리로드하도록 처리함
        int itemCount = searchedFilesAdapterModel.getItemCount();
        searchSubject.onNext(itemCount);
    }

    @Override
    public void onNetworkConnection() {
        if (searchedFilesAdapterModel.getItemCount() <= 0) {
            searchSubject.onNext(-1);
        }
    }

    @Override
    public void doKeywordSearch(String s) {
        searchQuery.setKeyword(s);
        view.justRefresh();
        searchSubject.onNext(-1);
    }

    private void removeItem(int position) {
        searchedFilesAdapterModel.remove(position);
        view.justRefresh();
        if (searchedFilesAdapterModel.getItemCount() <= 0) {
            if (fileListModel.isDefaultSearchQueryIgnoreMessageId(searchQuery.getRequestQuery())) {
                view.setEmptyViewVisible(android.view.View.VISIBLE);
                view.setSearchEmptryViewVisible(android.view.View.GONE);
            } else {
                view.setEmptyViewVisible(android.view.View.GONE);
                view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
            }
        }
    }

    @Override
    public void onRefreshFileInfo(int fileId, int commentCount) {
        int position = searchedFilesAdapterModel.findPositionByFileId(fileId);
        if (position < 0) {
            return;
        }

        ResMessages.FileMessage item = searchedFilesAdapterModel.getItem(position);
        if (item != null) {
            item.commentCount = commentCount;
            view.justRefresh();
        }
    }

}
