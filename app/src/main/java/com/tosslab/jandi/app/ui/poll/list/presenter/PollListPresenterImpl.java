package com.tosslab.jandi.app.ui.poll.list.presenter;

import android.util.Log;
import android.util.Pair;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.poll.list.adapter.model.PollListDataModel;
import com.tosslab.jandi.app.ui.poll.list.model.PollListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class PollListPresenterImpl implements PollListPresenter {

    private final PollListModel pollListModel;
    private final PollListDataModel pollListDataModel;
    private final PollListPresenter.View pollListView;

    @Inject
    public PollListPresenterImpl(PollListModel pollListModel,
                                 PollListDataModel pollListDataModel,
                                 View pollListView) {
        this.pollListModel = pollListModel;
        this.pollListDataModel = pollListDataModel;
        this.pollListView = pollListView;
    }

    @Override
    public void onInitializePollList() {
        if (!NetworkCheckUtil.isConnected()) {
            initializeFromDatabase();
            return;
        }

        pollListView.showProgress();
        pollListModel.getPollListObservable(PollListModel.DEFAULT_REQUEST_ITEM_COUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resPollList -> {
                    if (resPollList == null) {
                        return new ResPollList();
                    }

                    List<Poll> onGoing = resPollList.getOnGoing();
                    pollListModel.sortPollListByDueDate(onGoing);
                    List<Poll> finished = resPollList.getFinished();
                    pollListModel.sortPollListByFinishedAt(finished);
                    List<Poll> mergedPollList =
                            pollListModel.getMergedPollList(onGoing, finished);
                    resPollList.setPollList(mergedPollList);
                    return resPollList;
                })
                .subscribe(resPollList -> {
                    pollListView.dismissProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.showEmptyView();
                        pollListView.setHasMore(false);
                    } else {
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                        pollListView.notifyDataSetChanged();
                    }
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    initializeFromDatabase();
                });
    }

    @Override
    public void reInitializePollList() {
        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        pollListModel.getPollListObservable(PollListModel.DEFAULT_REQUEST_ITEM_COUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resPollList -> {
                    if (resPollList == null) {
                        return new ResPollList();
                    }

                    List<Poll> onGoing = resPollList.getOnGoing();
                    pollListModel.sortPollListByDueDate(onGoing);
                    List<Poll> finished = resPollList.getFinished();
                    pollListModel.sortPollListByFinishedAt(finished);
                    List<Poll> mergedPollList =
                            pollListModel.getMergedPollList(onGoing, finished);
                    resPollList.setPollList(mergedPollList);
                    return resPollList;
                })
                .subscribe(resPollList -> {
                    pollListView.dismissProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.showEmptyView();
                        pollListView.setHasMore(false);
                    } else {
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                        pollListView.notifyDataSetChanged();
                    }
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    initializeFromDatabase();
                });
    }

    private void initializeFromDatabase() {
        pollListView.showProgress();
        pollListModel.getPollListFromDBObservable()
                .map(polls -> {
                    ResPollList resPollList = new ResPollList();
                    resPollList.setHasMore(false);
                    resPollList.setPollList(polls);
                    return resPollList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resPollList -> {
                    pollListView.dismissProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.showEmptyView();
                        pollListView.setHasMore(false);
                    } else {
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                        pollListView.notifyDataSetChanged();
                    }
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    pollListView.dismissProgress();
                    pollListView.showUnExpectedErrorToast();
                    pollListView.finish();
                });
    }

    @Override
    public void onLoadMorePollList(Date lastItemFinishedAt) {
        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        pollListView.showLoadMoreProgress();
        int count = PollListModel.DEFAULT_REQUEST_ITEM_COUNT;
        String finishedAt = ISO8601Utils.format(lastItemFinishedAt);
        pollListModel.getPollListObservable(count, finishedAt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(resPollList -> {
                    if (resPollList == null) {
                        resPollList = new ResPollList();
                        resPollList.setHasMore(false);
                        return resPollList;
                    }

                    List<Poll> finished = resPollList.getFinished();
                    if (finished == null || finished.isEmpty()) {
                        return resPollList;
                    }

                    pollListModel.sortPollListByFinishedAt(finished);
                    resPollList.setPollList(finished);
                    return resPollList;
                })
                .subscribe(resPollList -> {
                    pollListView.dismissLoadMoreProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.setHasMore(false);
                    } else {
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                        pollListView.notifyDataSetChanged();
                    }
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    pollListView.dismissLoadMoreProgress();
                });
    }

    @Override
    public void onPollDataChanged(SocketPollEvent.Type type, Poll poll) {
        LogUtil.d("jsp", type.name() + " && " + poll.toString());
        switch (type) {
            case CREATED:
                addPoll(poll);
                break;
            case VOTED:
                changePoll(poll);
                break;
            case FINISHED:
                changePollToFinished(poll);
                break;
            case DELETED:
                deletePoll(poll);
                break;
        }
    }

    private void addPoll(Poll poll) {
        Observable.just(poll)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(poll1 -> {
                    pollListDataModel.addPoll(0, poll);
                    pollListView.notifyDataSetChanged();
                });
    }

    private void changePoll(Poll poll) {
        Observable.just(poll)
                .map(poll1 -> pollListDataModel.getIndexById(poll.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(index -> {
                    if (index < 0) {
                        return;
                    }

                    pollListDataModel.setPoll(index, poll);
                    pollListView.notifyDataSetChanged();
                });
    }

    private void changePollToFinished(Poll poll) {
        Observable.just(poll)
                .map(poll1 -> {
                    int targetToAddIndex = pollListDataModel.getIndexOfFirstFinishedPoll();
                    return Pair.create(targetToAddIndex, poll1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Poll poll1 = pair.second;
                    LogUtil.e("jsp2", pair.first + " & " + poll1.toString());
                    if (poll1 == null || poll1.getId() <= 0) {
                        return;
                    }

                    int targetToAddIndex = pair.first;
                    if (targetToAddIndex < 0) {
                        targetToAddIndex = 0;
                    }

                    pollListDataModel.addPoll(targetToAddIndex, poll1);
                    pollListDataModel.removePollByIdAndStats(poll1.getId(), "created");
                    pollListView.notifyDataSetChanged();
                });
    }

    private void deletePoll(Poll poll) {
        Observable.just(poll)
                .map(poll1 -> pollListDataModel.getIndexById(poll.getId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(index -> {
                    try {
                        pollListDataModel.removePollByIndex(index);
                        LogUtil.i("jsp", "index = " + index);
                        pollListView.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

    }
}
