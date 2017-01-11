package com.tosslab.jandi.app.ui.poll.create.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.poll.create.model.PollCreateModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPollCreated;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 20..
 */
public class PollCreatePresenterImpl implements PollCreatePresenter {

    private final PollCreateModel pollCreateModel;
    private final PollCreatePresenter.View pollCreateView;

    private ReqCreatePoll.Builder createPollBuilder;

    @Inject
    public PollCreatePresenterImpl(PollCreateModel pollCreateModel,
                                   PollCreatePresenter.View pollCreateView) {
        this.pollCreateModel = pollCreateModel;
        this.pollCreateView = pollCreateView;
    }

    @Override
    public void initializePollCreateBuilder(long topicId) {
        createPollBuilder = new ReqCreatePoll.Builder(topicId);
    }

    @Override
    public void onPollSubjectChanged(String subject) {
        createPollBuilder.subject(subject);
    }

    @Override
    public void onPollItemInput(int position, String title) {
        if (TextUtils.isEmpty(title)) {
            createPollBuilder.removeItemFromMap(position);
        } else {
            createPollBuilder.putItemToMap(position, title);
        }
    }

    @Override
    public void onPollItemRemove(int position) {
        createPollBuilder.removeItemFromMap(position);
    }

    @Override
    public void onPollDueDateSelected(Calendar dueDate) {
        createPollBuilder.dueDate(dueDate);
    }

    @Override
    public void onPollDueDateHourSelected(int hour) {
        createPollBuilder.hour(hour);
    }

    @Override
    public void onPollAnonymousOptionChanged(boolean anonymous) {
        createPollBuilder.anonymous(anonymous);
    }

    @Override
    public void onPollMultipleChoiceOptionChanged(boolean multipleChoice) {
        createPollBuilder.multipleChoice(multipleChoice);
    }

    @Override
    public boolean isAvailablePoll() {
        if (createPollBuilder.getDueDate() == null) {
            createPollBuilder.dueDate(Calendar.getInstance());
        }

        if (!pollCreateModel.hasSubject(createPollBuilder.getSubject())) {
            return false;
        }

        List<String> filteredItems = pollCreateModel.getFilteredItems(createPollBuilder.getItemsMap());
        if (!pollCreateModel.hasEnoughItems(filteredItems)) {
            return false;
        }

        pollCreateModel.buildDueDate(createPollBuilder);

        if (!pollCreateModel.isAvailableDueDate(createPollBuilder.getDueDate())) {
            return false;
        }

        return true;
    }

    @Override
    public void onCreatePoll() {
        if (createPollBuilder.getDueDate() == null) {
            createPollBuilder.dueDate(Calendar.getInstance());
        }

        if (!pollCreateModel.hasTargetTopic(createPollBuilder.getTopicId())) {
            return;
        }

        if (!pollCreateModel.hasSubject(createPollBuilder.getSubject())) {
            pollCreateView.showEmptySubjectToast();
            return;
        }

        List<String> filteredItems = pollCreateModel.getFilteredItems(createPollBuilder.getItemsMap());
        if (!pollCreateModel.hasEnoughItems(filteredItems)) {
            pollCreateView.showNotEnoughPollItemsToast();
            return;
        } else {
            createPollBuilder.items(filteredItems);
        }

        pollCreateModel.buildDueDate(createPollBuilder);

        if (!pollCreateModel.isAvailableDueDate(createPollBuilder.getDueDate())) {
            pollCreateView.showDueDateCannotBePastTimeToast();
            return;
        }

        pollCreateView.showProgress();
        ReqCreatePoll reqCreatePoll = createPollBuilder.build();
        pollCreateModel.getCreatePollObservable(reqCreatePoll)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resCreatePoll -> {
                    pollCreateView.dismissProgress();
                    pollCreateView.finish();

                    SprinklrPollCreated.sendLog(TeamInfoLoader.getInstance().getMyId(),
                            resCreatePoll.getLinkMessage().pollId,
                            TeamInfoLoader.getInstance().getTeamId(),
                            reqCreatePoll.getTopicId());

                }, throwable -> {
                    pollCreateView.dismissProgress();
                    LogUtil.e(Log.getStackTraceString(throwable));
                    pollCreateView.showUnExpectedErrorToast();
                    if (throwable instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) throwable;
                        SprinklrPollCreated.sendFailLog(e.getResponseCode());
                    }
                });
    }
}
