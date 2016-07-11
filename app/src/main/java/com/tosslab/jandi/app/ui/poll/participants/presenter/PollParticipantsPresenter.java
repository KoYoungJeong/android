package com.tosslab.jandi.app.ui.poll.participants.presenter;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public interface PollParticipantsPresenter {

    String TAG = PollCreatePresenter.class.getSimpleName();

    void onInitializePollParticipants(long pollId, int selectedSequence, @Nullable String headerTitle);

    interface View {
        void showProgress();

        void dismissProgress();

        void showCheckNetworkDialog(boolean shouldFinishWhenConfirm);

        void showUnExpectedErrorToast();

        void finish();

        void addMembers(List<User> members);

        void setTitle(String headerTitle);
    }
}
