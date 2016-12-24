package com.tosslab.jandi.app.ui.poll.participants.model;

import android.support.annotation.VisibleForTesting;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResPollParticipants;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollParticipantsModel {

    @VisibleForTesting
    Lazy<PollApi> pollApi;

    @Inject
    public PollParticipantsModel(Lazy<PollApi> api) {
        pollApi = api;
    }

    public Observable<ResPollParticipants> getAllParticipantsObservable(long pollId) {
        return Observable.<ResPollParticipants>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollParticipants pollParticipants = pollApi.get().getAllPollParticipants(teamId, pollId);
                    subscriber.onNext(pollParticipants);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollParticipants> getParticipantsObservable(long pollId, int selectedSequence) {
        return Observable.<ResPollParticipants>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollParticipants pollParticipants= pollApi.get().getPollParticipants(teamId, pollId, selectedSequence);
                    subscriber.onNext(pollParticipants);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

}
