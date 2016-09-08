package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.model;

import android.support.annotation.VisibleForTesting;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.ArrayList;
import java.util.Collections;
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
        return Observable.<ResPollList>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollList resPollList = pollApi.get().getPollList(teamId, count);
                    subscriber.onNext(resPollList);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollList> getPollListObservable(int count, String finishedAt) {
        return Observable.<ResPollList>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollList resPollList = pollApi.get().getPollList(teamId, count, finishedAt);
                    subscriber.onNext(resPollList);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
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
                .toList();
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
