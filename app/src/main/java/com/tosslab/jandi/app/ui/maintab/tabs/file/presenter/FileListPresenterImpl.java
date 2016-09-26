package com.tosslab.jandi.app.ui.maintab.tabs.file.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.SearchedFilesAdapterModel;
import com.tosslab.jandi.app.ui.maintab.tabs.file.model.FileListModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tee on 16. 6. 28..
 */

public class FileListPresenterImpl implements FileListPresenter {

    private static final int DEFAULT_COUNT = 20;
    private final BehaviorSubject<Integer> pageSubject;
    private final BehaviorSubject<Long> writerSubject;
    private final BehaviorSubject<Long> entitySubject;
    private final boolean inSearchActivity;
    private final BehaviorSubject<String> fileTypeSubject;
    private final BehaviorSubject<Date> endDateSubject;
    private final BehaviorSubject<String> keywordSubject;
    private final CompositeSubscription compositeSubscription;
    private FileListPresenter.View view;
    private FileListModel fileListModel;
    private long entityId;
    private SearchedFilesAdapterModel searchedFilesAdapterModel;

    public FileListPresenterImpl(long entityId, FileListModel fileListModel, View view, boolean inSearchActivity) {
        this.entityId = entityId;
        this.fileListModel = fileListModel;
        this.view = view;

        this.inSearchActivity = inSearchActivity;
        entitySubject = BehaviorSubject.create(entityId);
        writerSubject = BehaviorSubject.create(-1L);
        fileTypeSubject = BehaviorSubject.create("all");
        endDateSubject = BehaviorSubject.create(new Date());
        pageSubject = BehaviorSubject.create(1);
        keywordSubject = BehaviorSubject.create("");

        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(
                Observable.combineLatest(
                        entitySubject.doOnNext(it -> FileListPresenterImpl.this.entityId = it)
                                .map(entity -> {
                                    if (entity > 0) {
                                        if (TeamInfoLoader.getInstance().isTopic(entity)) {
                                            return entity;
                                        } else {
                                            return TeamInfoLoader.getInstance().getChatId(entity);
                                        }
                                    }
                                    return entity;
                                }).distinctUntilChanged(),
                        writerSubject.distinctUntilChanged(),
                        fileTypeSubject.distinctUntilChanged(),
                        endDateSubject.distinctUntilChanged(),
                        pageSubject.distinctUntilChanged(),
                        keywordSubject.distinctUntilChanged(),
                        (entity, writerId, fileType, date, page, keyword) -> {
                            return new ReqSearch.Builder()
                                    .setRoomId(entity)
                                    .setWriterId(writerId)
                                    .setType("file")
                                    .setFileType(fileType)
                                    .setPage(page)
                                    .setCount(DEFAULT_COUNT)
                                    .setKeyword(keyword).build();
                        })
                        .throttleLast(100, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .filter(it -> !inSearchActivity || TextUtils.isEmpty(it.getKeyword()) || it.getKeyword().length() >= 2)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(it -> {
                            if (it.getPage() > 1) {
                                view.showMoreProgressBar();
                            } else {
                                view.setSearchEmptryViewVisible(android.view.View.GONE);
                                view.setInitLoadingViewVisible(android.view.View.VISIBLE);
                            }
                            if (it.getPage() == 1) {
                                searchedFilesAdapterModel.clearList();
                                view.justRefresh();
                            }
                        })
                        .observeOn(Schedulers.newThread())
                        .map(it -> {
                            try {
                                ResSearch results = fileListModel.getResults(it);
                                fileListModel.trackFileKeywordSearchSuccess(it.getKeyword());
                                return Pair.create(it, results.getRecords());
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                            }
                            return Pair.create(it, new ArrayList<ResSearch.SearchRecord>());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            if (pair.first.getPage() == 1) {
                                searchedFilesAdapterModel.clearList();
                                view.justRefresh();
                            }
                            searchedFilesAdapterModel.add(pair.second);
                            view.justRefresh();
                            afterProccess(pair.first, pair.second);
                        }, t -> {
                            t.printStackTrace();
                            if (t instanceof RetrofitException) {
                                RetrofitException e1 = (RetrofitException) t;
                                int errorCode = e1.getStatusCode();
                                fileListModel.trackFileKeywordSearchFail(errorCode);
                                view.searchFailed(R.string.err_file_search);
                            } else {
                                fileListModel.trackFileKeywordSearchFail(-1);
                                view.searchFailed(R.string.err_file_search);
                            }
                        }));


    }

    private void afterProccess(ReqSearch first, List<ResSearch.SearchRecord> its) {
        int totalItemCount = searchedFilesAdapterModel.getItemCount();
        if (fileListModel.isDefaultSearchQuery(pageSubject.getValue(),
                entitySubject.getValue(), writerSubject.getValue(),
                keywordSubject.getValue(), fileTypeSubject.getValue())) {

            if (totalItemCount <= 0) {
                // 첫 검색
                view.setEmptyViewVisible(android.view.View.VISIBLE);
            } else {
                view.setEmptyViewVisible(android.view.View.GONE);
            }
            view.setSearchEmptryViewVisible(android.view.View.GONE);
        } else {
            if (totalItemCount <= 0) {
                // 첫 검색
                view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
            } else {
                view.setSearchEmptryViewVisible(android.view.View.GONE);
            }
            view.setEmptyViewVisible(android.view.View.GONE);
        }

        view.setInitLoadingViewVisible(android.view.View.GONE);


        if (its.size() < DEFAULT_COUNT) {
            if (pageSubject.getValue() > 1) {
                view.showWarningToast(JandiApplication.getContext().getString(R.string.warn_no_more_files));
            }
            setListNoMoreLoad();
        } else {
            setListReadyLoadMore();
        }

        view.dismissMoreProgressBar();

    }

    @Override
    public void setSearchedFilesAdapterModel(SearchedFilesAdapterModel adapterModel) {
        this.searchedFilesAdapterModel = adapterModel;
    }

    @Override
    public void getPreviousFile() {
        pageSubject.onNext(pageSubject.getValue() + 1);
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
        return entityId;
    }

    @Override
    public boolean isDefaultSeachQuery() {
        return fileListModel.isDefaultSearchQuery(pageSubject.getValue(),
                entitySubject.getValue(), writerSubject.getValue(),
                keywordSubject.getValue(), fileTypeSubject.getValue());
    }

    @Override
    public void doSearchAll() {
        searchedFilesAdapterModel.clearList();
        view.justRefresh();
        entitySubject.onNext(-1L);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
    }

    @Override
    public void onFileShare(long teamId) {
        if (teamId != fileListModel.getSelectedTeamId()) {
            return;
        }
        searchedFilesAdapterModel.clearList();
        view.justRefresh();
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
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
        searchedFilesAdapterModel.clearList();
        view.justRefresh();
        fileTypeSubject.onNext(query);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
    }

    @Override
    public void onMemberSelection(long userId, String searchText) {
        view.justRefresh();
        writerSubject.onNext(userId);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
    }

    @Override
    public void onEntitySelection(long sharedEntityId, String searchText) {
        view.justRefresh();
        entitySubject.onNext(sharedEntityId);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
    }

    @Override
    public void onTopicDeleted(long teamId) {
        if (teamId != fileListModel.getSelectedTeamId()) {
            return;
        }
        // 토픽이 삭제되거나 나간 경우 해당 토픽의 파일 접근 여부를 알 수 없으므로
        // 리로드하도록 처리함
        view.justRefresh();
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
    }

    @Override
    public void onNetworkConnection() {
        if (searchedFilesAdapterModel.getItemCount() <= 0) {
            pageSubject.onNext(1);
            endDateSubject.onNext(new Date());
        }
    }

    @Override
    public void onNewQuery(String s) {
        if (!TextUtils.isEmpty(s) && s.length() >= 2) {
            view.justRefresh();
            keywordSubject.onNext(s);
            pageSubject.onNext(1);
            endDateSubject.onNext(new Date());
        } else {
            view.showWarningToast(JandiApplication.getContext().getString(R.string.jandi_search_available_length_of_keyword));
        }
    }

    private void removeItem(int position) {
        searchedFilesAdapterModel.remove(position);
        view.justRefresh();
        if (searchedFilesAdapterModel.getItemCount() <= 0) {
            if (fileListModel.isDefaultSearchQuery(pageSubject.getValue(),
                    entitySubject.getValue(), writerSubject.getValue(),
                    keywordSubject.getValue(), fileTypeSubject.getValue())) {
                view.setEmptyViewVisible(android.view.View.VISIBLE);
                view.setSearchEmptryViewVisible(android.view.View.GONE);
            } else {
                view.setEmptyViewVisible(android.view.View.GONE);
                view.setSearchEmptryViewVisible(android.view.View.VISIBLE);
            }
        }
    }

    @Override
    public void onRefreshFileInfo(long fileId, int commentCount) {
        int position = searchedFilesAdapterModel.findPositionByFileId(fileId);
        if (position < 0) {
            return;
        }

        ResSearch.SearchRecord item = searchedFilesAdapterModel.getItem(position);
        if (item != null) {
            item.getFile().setCommentCount(commentCount);
            view.justRefresh();
        }
    }

    @Override
    public void onDestory() {
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public void getImageDetail(long fileId) {
        view.showProgress();
        Observable.just(fileId)
                .observeOn(Schedulers.newThread())
                .map(it -> {
                    try {
                        return ((ResMessages.FileMessage) fileListModel.getImageFile(fileId));
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgress();
                    view.moveToCarousel(it);
                }, t -> {
                    view.dismissProgress();
                    String message = JandiApplication.getContext().getString(R.string.jandi_err_unexpected);
                    view.showWarningToast(message);
                });

    }
}
