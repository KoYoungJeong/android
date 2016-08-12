package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
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
import rx.functions.Func0;
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

    @Inject
    SearchAdapterDataModel searchAdapterDataModel;

    private boolean hasMoreSearchResult = false;

    private SearchMessageHeaderData.Builder
            searchedMessageHeaderDataBuilder = new SearchMessageHeaderData.Builder();

    private MoreState moreState = MoreState.Idle;

    @Inject
    public SearchPresenterImpl() {
        writerSubject = BehaviorSubject.create(-1l);
        roomSubject = BehaviorSubject.create(-1l);
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
                                searchAdapterDataModel.setLoading(true);
                                searchAdapterDataModel.clearSearchMessageDatas();
                            } else {
                                view.showMoreProgressBar();
                            }
                            searchAdapterDataModel.setMessageHeaderData(searchedMessageHeaderDataBuilder.build());
                            view.refreshSearchedAll();
                            view.hideKeyboard();
                        })
                        .observeOn(Schedulers.io())
                        .map(it -> {
                            try {
                                ResSearch results = searchModel.searchMessages(it);
                                hasMoreSearchResult = results.hasMore();
                                searchedMessageHeaderDataBuilder.setHasMore(hasMoreSearchResult);
                                searchedMessageHeaderDataBuilder.setShowSearchedResultMessage(true);
                                searchedMessageHeaderDataBuilder.setSearchedMessageCount(results.getTotalCount());
                                return results;
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                            }
                            return new ResSearch();
                        })
                        .map(searchRecords -> {
                            List<SearchMessageData> searchMessageDatas = new ArrayList<>();
                            for (ResSearch.SearchRecord searchRecord : searchRecords.getRecords()) {
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
                                        .setTokens(searchRecords.getTokens())
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
                                        searchAdapterDataModel.setLoading(false);
                                        searchAdapterDataModel.clearSearchMessageDatas();
                                    }

                                    if (pageSubject.getValue() == 1) {
                                        searchedMessageHeaderDataBuilder.setShowProgress(false);
                                        searchAdapterDataModel.setLoading(false);
                                    } else {
                                        view.dismissMoreProgressBar();
                                        moreState = MoreState.Idle;
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
                                        searchAdapterDataModel.setLoading(false);
                                    } else {
                                        view.dismissMoreProgressBar();
                                        moreState = MoreState.Idle;
                                    }
                                    searchAdapterDataModel.setMessageHeaderData(
                                            searchedMessageHeaderDataBuilder.build());
                                    view.refreshSearchedAll();
                                }
                        ));


    }

    @Override
    public void sendSearchHistory() {
        Observable.from(searchModel.getHistory())
                .map(searchKeyword -> new SearchHistoryData.Builder()
                        .setKeyword(searchKeyword)
                        .build())
                .collect((Func0<ArrayList<SearchHistoryData>>) ArrayList::new, List::add)
                .subscribe(searchHistoryDatas -> {
                    searchAdapterDataModel.setSearchHistoryDatas(searchHistoryDatas);
                    view.refreshHistory();
                });
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
        if (hasMoreSearchResult && moreState == MoreState.Idle) {
            moreState = MoreState.Loading;
            int nextPage = pageSubject.getValue() + 1;
            pageSubject.onNext(nextPage);
        }
    }

    public void sendSearchQuery(String keyword) {
        keywordSubject.onNext(keyword);
        pageSubject.onNext(1);
        endDateSubject.onNext(new Date());
        hasMoreSearchResult = false;
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

    @Override
    public void onLaunchTopicRoom(long topicId, boolean isJoined) {
        TopicRoom topicRoom = searchModel.getTopicRoomById(topicId);
        if (isJoined) {
            int type = topicRoom.isPublicTopic() ?
                    JandiConstants.TYPE_PUBLIC_TOPIC : JandiConstants.TYPE_PRIVATE_TOPIC;
            view.moveToMessageActivity(topicId, type);
        } else {
            view.showTopicInfoDialog(topicRoom);
        }
    }

    @Override
    public void onJoinTopic(long topicId, int topicType) {
        Observable.create(subscriber -> {
            try {
                searchModel.joinTopic(topicId);
                subscriber.onNext(topicId);
            } catch (RetrofitException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(o -> view.moveToMessageActivity(topicId, topicType))
                .subscribe(o -> {
                        }, e -> {
                            e.printStackTrace();
                        }
                );
    }

    @Override
    public void onRoomChanged(long roomId, boolean isDirectMessageRoom) {
        String roomName = "";

        if (isDirectMessageRoom) {
            long companionId = TeamInfoLoader.getInstance().getChat(roomId).getCompanionId();
            roomName = TeamInfoLoader.getInstance().getMemberName(companionId);
        } else {
            roomName = TeamInfoLoader.getInstance().getTopic(roomId).getName();
        }

        if (!TextUtils.isEmpty(roomName)) {
            searchedMessageHeaderDataBuilder.setRoomName(roomName);
        }

        if (roomSubject.getValue() != roomId) {
            roomSubject.onNext(roomId);
            pageSubject.onNext(1);
            endDateSubject.onNext(new Date());
            hasMoreSearchResult = false;
        }

        if (searchAdapterDataModel.isHistoryMode()) {
            searchAdapterDataModel.setMessageHeaderData(searchedMessageHeaderDataBuilder.build());
            view.refreshHistory();
        }
    }

    @Override
    public void onWriterChanged(long writerId) {
        String writerName = searchModel.getWriterName(writerId);
        if (!TextUtils.isEmpty(writerName)) {
            searchedMessageHeaderDataBuilder.setMemberName(writerName);
        } else {
            searchedMessageHeaderDataBuilder.setMemberName(
                    JandiApplication.getContext().getString(R.string.jandi_search_all_member));
        }

        if (writerSubject.getValue() != writerId) {
            writerSubject.onNext(writerId);
            pageSubject.onNext(1);
            endDateSubject.onNext(new Date());
            hasMoreSearchResult = false;
        }

        if (searchAdapterDataModel.isHistoryMode()) {
            searchAdapterDataModel.setMessageHeaderData(searchedMessageHeaderDataBuilder.build());
            view.refreshHistory();
        }
    }

    @Override
    public void onAccessTypeChanged(String accessType) {
        if (TextUtils.equals(accessType, "accessible")) {
            searchedMessageHeaderDataBuilder.setRoomName(
                    JandiApplication.getContext().getString(R.string.jandi_search_all_room)
            );
        } else if (TextUtils.equals(accessType, "joined")) {
            searchedMessageHeaderDataBuilder.setRoomName(
                    JandiApplication.getContext().getString(R.string.jandi_joined_room)
            );
        }

        if (searchAdapterDataModel.isHistoryMode()) {
            searchAdapterDataModel.setMessageHeaderData(searchedMessageHeaderDataBuilder.build());
            view.refreshHistory();
        }

        if (!TextUtils.equals(accessTypeSubject.getValue(), accessType)) {
            pageSubject.onNext(1);
            roomSubject.onNext(-1l);
            endDateSubject.onNext(new Date());
            accessTypeSubject.onNext(accessType);
        }
    }

    @Override
    public void onDestroy() {
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    public enum MoreState {
        Idle, Loading
    }

}