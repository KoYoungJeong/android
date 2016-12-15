package com.tosslab.jandi.app.ui.search.filter.room.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.model.RoomFilterDataModel;
import com.tosslab.jandi.app.ui.search.filter.room.model.RoomFilterModel;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
public class RoomFilterPresenterImpl implements RoomFilterPresenter {

    private final RoomFilterModel roomFilterModel;
    private final RoomFilterDataModel roomFilterDataModel;
    private final RoomFilterPresenter.View roomFilterView;

    private PublishSubject<String> searchUserSearchQueue;
    private Subscription searchUserQueueSubscription;
    private PublishSubject<String> searchTopicQueue;
    private Subscription searchTopicQueueSubscription;

    private boolean isShowDefaultTopic = true;

    @Inject
    public RoomFilterPresenterImpl(RoomFilterModel roomFilterModel,
                                   RoomFilterDataModel roomFilterDataModel,
                                   RoomFilterPresenter.View roomFilterView) {
        this.roomFilterModel = roomFilterModel;
        this.roomFilterDataModel = roomFilterDataModel;
        this.roomFilterView = roomFilterView;

        initTopicSearchQueue();
        initUserSearchQueue();
    }

    @Override
    public void setShowDefaultTopic(boolean showDefaultTopic) {
        isShowDefaultTopic = showDefaultTopic;
    }

    @Override
    public void initTopicSearchQueue() {
        searchTopicQueue = PublishSubject.create();
        searchTopicQueueSubscription =
                searchTopicQueue
                        .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .map(query -> {
                            List<MultiItemRecyclerAdapter.Row<?>> rows = new ArrayList<>();
                            if (TextUtils.isEmpty(query)) {
                                List<TopicFolder> initializedFolders = roomFilterDataModel.getTopicFolders();
                                List<TopicRoom> initializedRooms = roomFilterDataModel.getTopicRooms();
                                rows.addAll(roomFilterDataModel.getTopicWithFolderRows(initializedFolders));
                                rows.addAll(roomFilterDataModel.getTopicRows(initializedRooms));
                            } else {
                                List<TopicRoom> searchedTopics = roomFilterModel.getSearchedTopics(query, isShowDefaultTopic);
                                rows.addAll(roomFilterDataModel.getTopicRows(searchedTopics));
                            }
                            return Pair.create(query, rows);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            String query = pair.first;
                            List<MultiItemRecyclerAdapter.Row<?>> rows = pair.second;

                            roomFilterDataModel.clearAllRows();
                            roomFilterDataModel.addRows(rows);
                            roomFilterView.notifyDataSetChanged();
                        }, Throwable::printStackTrace);
    }

    @Override
    public void initUserSearchQueue() {
        searchUserSearchQueue = PublishSubject.create();
        searchUserQueueSubscription =
                searchUserSearchQueue
                        .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .map(query -> {
                            List<User> initializedUsers = roomFilterDataModel.getUsers();
                            List<User> searchedUsers =
                                    roomFilterModel.getSearchedDirectMessages(query, initializedUsers);

                            return Pair.create(query,
                                    roomFilterDataModel.getUserRows(searchedUsers));
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            String query = pair.first;
                            List<MultiItemRecyclerAdapter.Row<?>> rows = pair.second;

                            roomFilterDataModel.clearAllRows();
                            roomFilterDataModel.addRows(rows);
                            roomFilterView.notifyDataSetChanged();
                        }, Throwable::printStackTrace);
    }

    @Override
    public void onInitializeRooms(RoomType roomType) {
        switch (roomType) {
            case Topic:
                initTopicRooms(null);
                break;
            case DirectMessage:
                initUsers(null);
                break;
        }
    }

    private void initTopicRooms(@Nullable Action0 onCompleteAction) {
        List<TopicFolder> initializedTopicFolders = roomFilterDataModel.getTopicFolders();
        List<TopicRoom> initializedTopicRooms = roomFilterDataModel.getTopicRooms();

        if (initializedTopicFolders != null && !initializedTopicFolders.isEmpty()) {

            initTopicRoomsWithFolderFromCache(
                    initializedTopicFolders, initializedTopicRooms, onCompleteAction);

        } else if (initializedTopicRooms != null && !initializedTopicRooms.isEmpty()) {

            initTopicRoomsFromCache(initializedTopicRooms, onCompleteAction);

        } else {

            initTopicRoomsFromTeamInfo(onCompleteAction);

        }
    }

    private void initTopicRoomsFromCache(List<TopicRoom> initializedRooms,
                                         @Nullable Action0 onCompleteAction) {
        Observable.just(initializedRooms)
                .map(roomFilterDataModel::getTopicRows)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    roomFilterDataModel.clearAllRows();
                    roomFilterDataModel.addRows(rows);
                    roomFilterView.notifyDataSetChanged();
                }, Throwable::printStackTrace, () -> {
                    if (onCompleteAction != null) {
                        onCompleteAction.call();
                    }
                });
    }

    private void initTopicRoomsWithFolderFromCache(List<TopicFolder> initializedFolders,
                                                   List<TopicRoom> initializedRooms,
                                                   @Nullable Action0 onCompleteAction) {
        Observable.just(initializedFolders)
                .map(roomFilterDataModel::getTopicWithFolderRows)
                .map(rows -> {
                    rows.addAll(roomFilterDataModel.getTopicRows(initializedRooms));
                    return rows;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    roomFilterDataModel.clearAllRows();
                    roomFilterDataModel.addRows(rows);
                    roomFilterView.notifyDataSetChanged();
                }, Throwable::printStackTrace, () -> {
                    if (onCompleteAction != null) {
                        onCompleteAction.call();
                    }
                });
    }

    private void initTopicRoomsFromTeamInfo(@Nullable Action0 onCompleteAction) {
        roomFilterView.showProgress();

        List<TopicFolder> topicFolderList;

        if (!isShowDefaultTopic) {
            topicFolderList = roomFilterModel.getTopicRoomsWithFolderExceptDefaultTopic();
        } else {
            topicFolderList = roomFilterModel.getTopicRoomsWithFolder();
        }

        Observable.from(topicFolderList)
                .toSortedList(((folder, folder2) -> folder.getSeq() - folder2.getSeq()))
                .onErrorReturn(throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    return new ArrayList<>();
                })
                .defaultIfEmpty(new ArrayList<>(0))
                .doOnNext(roomFilterDataModel::setFolders)
                .map(topicFolders -> {
                    List<TopicRoom> unFoldedTopics =
                            roomFilterModel.getUnfoldedTopics(topicFolders, isShowDefaultTopic);
                    roomFilterDataModel.setTopicRooms(unFoldedTopics);
                    return Pair.create(topicFolders, unFoldedTopics);
                })
                .map(pair -> {
                    List<TopicFolder> topicFolders = pair.first;
                    List<TopicRoom> topicRooms = pair.second;

                    List<MultiItemRecyclerAdapter.Row<?>> rows = new ArrayList<>();
                    rows.addAll(roomFilterDataModel.getTopicWithFolderRows(topicFolders));

                    rows.addAll(roomFilterDataModel.getTopicRows(topicRooms));
                    return rows;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    roomFilterView.hideProgress();
                    roomFilterDataModel.clearAllRows();
                    roomFilterDataModel.addRows(rows);
                    roomFilterView.notifyDataSetChanged();
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    roomFilterView.hideProgress();
                }, () -> {
                    if (onCompleteAction != null) {
                        onCompleteAction.call();
                    }
                });
    }

    private void initUsers(@Nullable Action0 onCompeteAction) {
        List<User> initializedUsers = roomFilterDataModel.getUsers();
        if (initializedUsers != null && !initializedUsers.isEmpty()) {

            initializeUsersFromCache(initializedUsers, onCompeteAction);

        } else {

            initializeUsersFromTeamInfo(onCompeteAction);

        }
    }

    private void initializeUsersFromTeamInfo(@Nullable Action0 onCompeteAction) {
        roomFilterView.showProgress();

        Observable.from(roomFilterModel.getUserList())
                .filter(User::isEnabled)
                .filter(user -> user.getId() != TeamInfoLoader.getInstance().getMyId())
                .toSortedList((lhs, rhs) -> {
                    if (TeamInfoLoader.getInstance().isJandiBot(lhs.getId())) {
                        return -1;
                    } else if (TeamInfoLoader.getInstance().isJandiBot(rhs.getId())) {
                        return 1;
                    }
                    String lhsName = lhs.getName();
                    String rhsName = rhs.getName();

                    return StringCompareUtil.compare(lhsName, rhsName);
                })
                .doOnNext(roomFilterDataModel::setUsers)
                .map(roomFilterDataModel::getUserRows)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    roomFilterDataModel.clearAllRows();
                    roomFilterDataModel.addRows(rows);
                    roomFilterView.notifyDataSetChanged();
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    roomFilterView.hideProgress();
                }, () -> {
                    roomFilterView.hideProgress();
                    if (onCompeteAction != null) {
                        onCompeteAction.call();
                    }
                });
    }

    private void initializeUsersFromCache(List<User> initializedDirectMessages,
                                          @Nullable Action0 onCompeteAction) {

        Observable.just(initializedDirectMessages)
                .map(roomFilterDataModel::getUserRows)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    roomFilterDataModel.clearAllRows();
                    roomFilterDataModel.addRows(rows);
                    roomFilterView.notifyDataSetChanged();
                }, Throwable::printStackTrace, () -> {
                    if (onCompeteAction != null) {
                        onCompeteAction.call();
                    }
                });
    }

    @Override
    public void onSearchRooms(String query, RoomType roomType) {
        switch (roomType) {
            case Topic:
                if (!searchTopicQueueSubscription.isUnsubscribed()) {
                    searchTopicQueue.onNext(query);
                }
                break;
            case DirectMessage:
                if (!searchUserQueueSubscription.isUnsubscribed()) {
                    searchUserSearchQueue.onNext(query);
                }
                break;
        }
    }

    @Override
    public void stopDirectMessageSearchQueue() {
        if (!searchUserQueueSubscription.isUnsubscribed()) {
            searchUserQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void stopTopicSearchQueue() {
        if (!searchTopicQueueSubscription.isUnsubscribed()) {
            searchTopicQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onRoomTypeChanged(RoomType roomType, String enteredQuery) {
        if (TextUtils.isEmpty(enteredQuery)) {
            onInitializeRooms(roomType);
            return;
        }

        switch (roomType) {
            case Topic:
                List<TopicFolder> initializedTopicFolders = roomFilterDataModel.getTopicFolders();
                List<TopicRoom> initializedTopicRooms = roomFilterDataModel.getTopicRooms();

                boolean isInitializedTopicFolders =
                        initializedTopicFolders != null && !initializedTopicFolders.isEmpty();
                boolean isInitializedTopicRooms =
                        initializedTopicRooms != null && !initializedTopicRooms.isEmpty();

                if (isInitializedTopicFolders || isInitializedTopicRooms) {
                    onSearchRooms(enteredQuery, roomType);
                } else {
                    initTopicRooms(() -> onSearchRooms(enteredQuery, roomType));
                }
                break;

            case DirectMessage:
                List<User> initializedUsers = roomFilterDataModel.getUsers();
                if (initializedUsers != null && !initializedUsers.isEmpty()) {
                    onSearchRooms(enteredQuery, roomType);
                } else {
                    initUsers(() -> onSearchRooms(enteredQuery, roomType));
                }
                break;
        }
    }

    @Override
    public void onMemberClickActionForGetRoomId(long memberId) {
        roomFilterView.showProgress();

        roomFilterModel.getRoomIdFromMemberIdObservable(memberId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(roomId -> {
                    roomFilterView.hideProgress();
                    roomFilterView.setResult(false, roomId, memberId);
                    roomFilterView.finish();
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    roomFilterView.hideProgress();
                    roomFilterView.setResult(false, -1L, memberId);
                    roomFilterView.finish();
                });
    }

    @Override
    public void onInitializeSelectedRoomId(boolean isTopic, final long selectedRoomId) {
        if (selectedRoomId <= -1L) {
            return;
        }

        if (isTopic) {
            roomFilterDataModel.setSelectedTopicRoomId(selectedRoomId);
            roomFilterView.notifyDataSetChanged();
        } else {
            Observable.just(selectedRoomId)
                    .map(roomFilterModel::getUserIdFromRoomId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userId -> {
                        roomFilterDataModel.setSelectedUserId(userId);
                        roomFilterView.notifyDataSetChanged();
                    }, Throwable::printStackTrace);
        }
    }

}
