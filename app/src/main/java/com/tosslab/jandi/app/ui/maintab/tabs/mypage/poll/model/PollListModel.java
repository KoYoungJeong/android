package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.model;

import android.support.annotation.VisibleForTesting;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class PollListModel {

    public static final int DEFAULT_REQUEST_ITEM_COUNT = 50;

    @VisibleForTesting
    Lazy<PollApi> pollApi;

    public PollListModel(Lazy<PollApi> api) {
        pollApi = api;
    }

    public Observable<ResPollList> getPollListObservable(int count) {
        return Observable.defer(() -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                return Observable.error(new NullPointerException("has not selected team."));
            } else {
                try {
                    ResPollList resPollList = pollApi.get().getPollList(teamId, count);
                    return Observable.just(resPollList);
                } catch (RetrofitException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public Observable<ResPollList> getPollListObservable(int count, String finishedAt) {
        return Observable.defer(() -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                return Observable.error(new NullPointerException("has not selected team."));
            } else {
                try {
                    ResPollList resPollList = pollApi.get().getPollList(teamId, count, finishedAt);
                    return Observable.just(resPollList);
                } catch (RetrofitException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    public List<Poll> getMergedPollList(List<Poll> onGoing, List<Poll> finished) {
        if (onGoing == null) {
            onGoing = new ArrayList<>();
        }
        if (finished == null) {
            finished = new ArrayList<>();
        }
        return Observable.merge(Observable.from(onGoing), Observable.from(finished))
                .toList()
                .toBlocking()
                .firstOrDefault(new ArrayList<>());
    }

    public Observable<List<Poll>> getPollListFromDBObservable() {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();

        return Observable.from(PollRepository.getInstance().getPolls())
                .filter(poll -> poll.getTeamId() == teamId)
                .filter(poll -> {
                    TopicRoom topic = TeamInfoLoader.getInstance().getTopic(poll.getTopicId());
                    return topic != null && topic.isJoined();
                })
                .toSortedList((poll, poll2) -> {

                    Date poll1FinishedAt = poll.getFinishedAt();
                    Date poll2FinishedAt = poll2.getFinishedAt();

                    Date pollUpdatedAt = poll.getUpdatedAt();
                    Date poll2UpdatedAt = poll2.getUpdatedAt();

                    if (poll1FinishedAt != null
                            && poll2FinishedAt != null) {
                        return poll2FinishedAt.compareTo(poll1FinishedAt);
                    } else {
                        if (poll1FinishedAt != null) {
                            return poll2UpdatedAt.compareTo(poll1FinishedAt);
                        } else if (poll2FinishedAt != null) {
                            return poll2FinishedAt.compareTo(pollUpdatedAt);
                        } else {
                            return poll2UpdatedAt.compareTo(pollUpdatedAt);
                        }
                    }
                });
    }

    public void sortPollListByDueDate(List<Poll> onGoing) {
        Collections.sort(onGoing, (lhs, rhs) -> lhs.getDueDate().compareTo(rhs.getDueDate()));
    }

    public void sortPollListByFinishedAt(List<Poll> finished) {
        Collections.sort(finished, (lhs, rhs) -> rhs.getFinishedAt().compareTo(lhs.getFinishedAt()));
    }

    public void upsertPolls(List<Poll> pollList) {
        PollRepository.getInstance().upsertPollList(pollList);
    }

}
