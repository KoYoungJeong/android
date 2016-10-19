package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter;

import android.util.Log;
import android.util.Pair;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialPollInfoRepository;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.model.PollListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.model.PollListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
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
                .subscribeOn(Schedulers.newThread())
                .doOnNext(resPollList -> {
                    if (resPollList != null) {
                        int votableCount = resPollList.getVotableCount();
                        InitialPollInfoRepository.getInstance().updateVotableCount(votableCount);
                        TeamInfoLoader.getInstance().refreshPollCount();
                        RefreshPollBadgeCountEvent event = new RefreshPollBadgeCountEvent(votableCount);
                        EventBus.getDefault().post(event);
                    }
                })
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
                    pollListModel.upsertPolls(mergedPollList);
                    resPollList.setPollList(mergedPollList);
                    return resPollList;
                })
                .subscribe(resPollList -> {
                    pollListView.dismissProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    pollListDataModel.clearPoll();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.setHasMore(false);
                    } else {
                        Log.d(TAG, "onInitializePollList: initPoll");
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                    }

                    pollListView.notifyDataSetChanged();
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
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resPollList -> {
                    pollListView.dismissProgress();

                    List<Poll> pollList = resPollList.getPollList();
                    pollListDataModel.clearPoll();
                    if (pollList == null || pollList.isEmpty()) {
                        pollListView.setHasMore(false);
                    } else {
                        pollListDataModel.addPolls(pollList);
                        pollListView.setHasMore(resPollList.hasMore());
                    }
                    pollListView.notifyDataSetChanged();
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    pollListView.dismissProgress();
                    pollListView.showUnExpectedErrorToast();
                    pollListView.notifyDataSetChanged();
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
                .subscribeOn(Schedulers.newThread())
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

    @Override
    public void removeOfTopics(long topicId) {

        Completable.fromAction(() -> {
            for (int idx = pollListDataModel.size() - 1; idx >= 0; idx--) {
                if (pollListDataModel.getPoll(idx).getTopicId() == topicId) {
                    pollListDataModel.removePollByIndex(idx);
                }
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pollListView::notifyDataSetChanged, Throwable::printStackTrace);


    }

    private void addPoll(Poll poll) {
        Observable.just(poll)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(it -> {
                    int size = pollListDataModel.size();
                    for (int idx = 0; idx < size; idx++) {
                        if (pollListDataModel.getPoll(idx).getId() == poll.getId()) {
                            return false;
                        }
                    }

                    return true;
                })
                .subscribe(poll1 -> {
                    Log.d(TAG, "addPoll: ");
                    pollListDataModel.addPoll(0, poll);
                    pollListView.notifyDataSetChanged();
                });
    }

    private void changePoll(Poll poll) {
        Observable.just(poll)
                .map(poll1 -> pollListDataModel.getIndexById(poll.getId()))
                .filter(it -> it >= 0)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(index -> {
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
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Poll poll1 = pair.second;
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
        Completable.fromAction(() -> {
            try {
                int index = pollListDataModel.getIndexById(poll.getId());
                if (index >= 0) {
                    pollListDataModel.removePollByIndex(index);
                    pollListView.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }
}
