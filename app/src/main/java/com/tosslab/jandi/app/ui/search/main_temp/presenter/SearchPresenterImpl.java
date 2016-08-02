package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;
import com.tosslab.jandi.app.ui.search.main_temp.model.SearchModel;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchHistoryData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageHeaderData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchPresenterImpl implements SearchPresenter {

    private final BehaviorSubject<Long> writerSubject;
    private final BehaviorSubject<Long> roomSubject;
    private final BehaviorSubject<String> accessTypeSubject;
    private final BehaviorSubject<Integer> pageSubject;
    private final BehaviorSubject<Date> endDateSubject;
    private final BehaviorSubject<String> keywordSubject;
    private final CompositeSubscription compositeSubscription;

    @Inject
    SearchModel searchModel;

    @Inject
    SearchPresenter.View view;

    private boolean hasMoreSearchResult = false;
    private SearchAdapterDataModel searchAdapterDataModel;
    private SearchMessageHeaderData.Builder
            searchedMessageHeaderDataBuilder = new SearchMessageHeaderData.Builder();

    @Inject
    public SearchPresenterImpl() {
        writerSubject = BehaviorSubject.create(-1L);
        roomSubject = BehaviorSubject.create(-1L);
        accessTypeSubject = BehaviorSubject.create("");
        pageSubject = BehaviorSubject.create(1);
        endDateSubject = BehaviorSubject.create(new Date());
        keywordSubject = BehaviorSubject.create("");

        compositeSubscription = new CompositeSubscription();

        compositeSubscription.add(
                Observable.combineLatest(
                        writerSubject,
                        roomSubject,
                        accessTypeSubject,
                        endDateSubject,
                        pageSubject,
                        keywordSubject,
                        (writerId, roomId, accessType, date, page, keyword) ->
                                new ReqSearch.Builder()
                                        .setRoomId(roomId)
                                        .setWriterId(writerId)
                                        .setType("message")
                                        .setAccessType(accessType)
                                        .setEndAt(date)
                                        .setPage(page)
                                        .setKeyword(keyword).build())
                        .filter(reqSearch -> !TextUtils.isEmpty(keywordSubject.getValue()))
                        .onBackpressureBuffer()
                        .throttleLast(100, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(it -> {
                            if (it.getPage() == 1) {
                                setTopicRoomDatas(false);
                                searchedMessageHeaderDataBuilder.setShowSearchedResultMessage(true);
                                searchedMessageHeaderDataBuilder.setShowProgress(true);
                                searchAdapterDataModel.clearSearchMessageDatas();
                            } else {
                                view.showMoreProgressBar();
                            }
                            searchAdapterDataModel.setMessageHeaderData(searchedMessageHeaderDataBuilder.build());
                            view.refreshSearchedAll();
                        })
                        .observeOn(Schedulers.io())
                        .map((Func1<ReqSearch, List<? extends ResSearch.SearchRecord>>) it -> {
                            try {
                                ResSearch results = searchModel.searchMessages(it);
                                hasMoreSearchResult = results.hasMore();
                                searchedMessageHeaderDataBuilder.setHasMore(hasMoreSearchResult);
                                searchedMessageHeaderDataBuilder.setShowSearchedResultMessage(true);
                                searchedMessageHeaderDataBuilder.setSearchedMessageCount(results.getTotalCount());
                                return results.getRecords();
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                            }
                            return new ArrayList<>();
                        })
                        .map(searchRecords -> {
                            List<SearchMessageData> searchMessageDatas = new ArrayList<>();
                            for (ResSearch.SearchRecord searchRecord : searchRecords) {
                                SearchMessageData searchMessageData = new SearchMessageData.Builder()
                                        .setRoomId(searchRecord.getRoomId())
                                        .setWriterId(searchRecord.getWriterId())
                                        .setCreatedAt(searchRecord.getCreatedAt())
                                        .setFeedbackType(searchRecord.getFeedbackType())
                                        .setLinkId(searchRecord.getLinkId())
                                        .setMentions(searchRecord.getMentions())
                                        .setText(searchRecord.getText())
                                        .setFile(searchRecord.getFile())
                                        .setPoll(searchRecord.getPoll())
                                        .build();
                                searchMessageDatas.add(searchMessageData);
                            }

                            return searchMessageDatas;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(searchMessageDatas -> {
                                    if (searchMessageDatas != null && !searchMessageDatas.isEmpty()) {
                                        if (pageSubject.getValue() == 1) {
                                            searchAdapterDataModel.setSearchMessageDatas(searchMessageDatas);
                                        } else {
                                            searchAdapterDataModel.addSearchMessageDatas(searchMessageDatas);
                                        }
                                    } else {
                                        searchedMessageHeaderDataBuilder.setShowSearchedResultMessage(false);
                                        searchAdapterDataModel.clearSearchMessageDatas();
                                    }

                                    if (pageSubject.getValue() == 1) {
                                        searchedMessageHeaderDataBuilder.setShowProgress(false);
                                    } else {
                                        view.dismissMoreProgressBar();
                                        searchAdapterDataModel.setMoreState(SearchAdapter.MoreState.Idle);
                                    }

                                    searchAdapterDataModel.setMessageHeaderData(
                                            searchedMessageHeaderDataBuilder.build());
                                    view.refreshSearchedAll();
                                },
                                t -> {
                                    t.printStackTrace();
                                    if (t instanceof RetrofitException) {
                                        RetrofitException e1 = (RetrofitException) t;
                                        e1.printStackTrace();
                                    } else {
                                        t.printStackTrace();
                                    }
                                    searchAdapterDataModel.clearSearchMessageDatas();
                                    if (pageSubject.getValue() == 1) {
                                        searchedMessageHeaderDataBuilder.setShowProgress(false);
                                    } else {
                                        view.dismissMoreProgressBar();
                                        searchAdapterDataModel.setMoreState(SearchAdapter.MoreState.Idle);
                                    }
                                    searchAdapterDataModel.setMessageHeaderData(
                                            searchedMessageHeaderDataBuilder.build());
                                    view.refreshSearchedAll();
                                }
                        ));


    }

    @Override
    public void sendSearchHistory() {
        List<SearchHistoryData> searchHistoryDatas = new ArrayList<>();
        List<String> searchKeywords = searchModel.getHistory();
        Observable.from(searchKeywords)
                .map(searchKeyword ->
                        searchHistoryDatas.add(
                                new SearchHistoryData.Builder()
                                        .setKeyword(searchKeyword)
                                        .build())
                ).subscribe();

        searchAdapterDataModel.setSearchHistoryDatas(searchHistoryDatas);
        view.refreshHistory();
    }

    @Override
    public void upsertKeywordHistory(String keyword) {
        searchModel.upsertSearchQuery(keyword);
    }

    private void setTopicRoomDatas(boolean isShowUnjoinedTopic) {
        List<SearchTopicRoomData> topicRoomDatas =
                searchModel.getSearchedTopics(keywordSubject.getValue(), isShowUnjoinedTopic);
        searchAdapterDataModel.setSearchTopicRoomDatas(topicRoomDatas);
    }

    @Override
    public void setChangeIsShowUnjoinedTopic(boolean isShowUnjoinedTopic) {
        setTopicRoomDatas(isShowUnjoinedTopic);
        view.refreshSearchedAll();
    }

    @Override
    public void sendMoreResults() {
        if (hasMoreSearchResult) {
            int nextPage = pageSubject.getValue() + 1;
            pageSubject.onNext(nextPage);
        }
    }

    public void setSearchAdapterDataModel(SearchAdapterDataModel searchAdapterDataModel) {
        this.searchAdapterDataModel = searchAdapterDataModel;
    }

    public void sendSearchQuery(String keyword) {
        keywordSubject.onNext(keyword);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
        upsertKeywordHistory(keyword);
    }

    @Override
    public void onDeleteaAllHistoryItem() {
        searchModel.removeHistoryAllItems();
        sendSearchHistory();
    }

    @Override
    public void onDeleteaHistoryItemByKeyword(String keyword) {
        searchModel.removeHistoryItemByKeyword(keyword);
        sendSearchHistory();
    }

    @Override
    public List<String> getOldQueryList(String keyword) {
        return searchModel.searchOldQuery(keyword);
    }

    public void sendTopicRoomSearchQuery(String keyword, boolean isShowUnjoinedTopic) {
        List<SearchTopicRoomData> topicRoomDatas = searchModel.getSearchedTopics(keyword, isShowUnjoinedTopic);
        searchAdapterDataModel.setSearchTopicRoomDatas(topicRoomDatas);
        view.refreshSearchedAll();
    }

}