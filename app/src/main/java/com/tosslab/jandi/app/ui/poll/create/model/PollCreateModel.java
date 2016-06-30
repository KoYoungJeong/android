package com.tosslab.jandi.app.ui.poll.create.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 20..
 */
public class PollCreateModel {

    Lazy<PollApi> pollApi;

    public PollCreateModel(Lazy<PollApi> api) {
        pollApi = api;
    }

    public boolean hasSubject(String subject) {
        return !TextUtils.isEmpty(subject) && TextUtils.getTrimmedLength(subject) > 0;
    }

    // 2개 이상
    public boolean hasEnoughItems(List<String> items) {
        return items != null && items.size() >= 2;
    }

    public boolean hasDueDate(int hour) {
        return hour >= 0;
    }

    public boolean hasTargetTopic(long topicId) {
        return topicId > 0;
    }

    public Observable<ResCreatePoll> getCreatePollObservable(final ReqCreatePoll reqCreatePoll) {
        return Observable.<ResCreatePoll>create(subscriber -> {
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            try {
                ResCreatePoll resCreatePoll = pollApi.get().createPoll(teamId, reqCreatePoll);
                subscriber.onNext(resCreatePoll);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public void buildDueDate(ReqCreatePoll.Builder createPollBuilder) {
        Calendar dueDate = createPollBuilder.getDueDate();
        dueDate.set(Calendar.HOUR_OF_DAY, createPollBuilder.getHour());
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        createPollBuilder.dueDate(dueDate);
    }

    public boolean isAvailableDueDate(Calendar dueDate) {
        return new Date().compareTo(dueDate.getTime()) < 0;
    }

    public List<String> getFilteredItems(Map<Integer, String> items) {
        if (items == null || items.size() <= 0) {
            return new ArrayList<>();
        }

        return Observable.from(items.values())
                .filter(item -> !TextUtils.isEmpty(item) && !TextUtils.isEmpty(item.trim()))
                .toList()
                .toBlocking()
                .firstOrDefault(new ArrayList<>());

    }
}
