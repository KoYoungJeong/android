package com.tosslab.jandi.app.ui.poll.create.presenter;

import java.util.Calendar;

/**
 * Created by tonyjs on 16. 6. 20..
 */
public interface PollCreatePresenter {

    void initializePollCreateBuilder(long topicId);

    void onPollSubjectChanged(String subject);

    void onPollDescriptionChanged(String description);

    void onPollItemInput(int position, String title);

    void onPollItemRemove(int position);

    void onPollDueDateSelected(Calendar dueDate);

    void onPollDueDateHourSelected(int hour);

    void onPollAnonymousOptionChanged(boolean anonymous);

    void onPollMultipleChoiceOptionChanged(boolean duplicate);

    void onCreatePoll();

    boolean isAvailablePoll();

    interface View {

        void showNotEnoughPollItemsToast();

        void showEmptySubjectToast();

        void showUnExpectedErrorToast();

        void finish();

        void showDueDateCannotBePastTimeToast();

        void showProgress();

        void dismissProgress();
    }
}
