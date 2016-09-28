package com.tosslab.jandi.app.ui.poll.participants.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResPollParticipants;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.poll.participants.model.PollParticipantsModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPollMemberOfVoted;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollParticipantsPresenterImpl implements PollParticipantsPresenter {

    private final PollParticipantsModel model;
    private final PollParticipantsPresenter.View view;

    @Inject
    public PollParticipantsPresenterImpl(PollParticipantsModel model,
                                         PollParticipantsPresenter.View view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void onInitializePollParticipants(long pollId, int selectedSequence,
                                             @Nullable final String headerTitle) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog(true);
            return;
        }

        getParticipantsObservable(pollId, selectedSequence)
                .concatMap(resPollParticipants ->
                        Observable.from(resPollParticipants.getMemberIds())
                                .map(id -> TeamInfoLoader.getInstance().getUser(id))
                                .toList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(members -> {
                    view.dismissProgress();

                    view.addMembers(members);

                    if (!TextUtils.isEmpty(headerTitle)) {
                        view.setTitle(headerTitle);
                    }

                    SprinklrPollMemberOfVoted.sendLog(pollId, TeamInfoLoader.getInstance().getTeamId());

                }, t -> {
                    view.dismissProgress();
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    view.showUnExpectedErrorToast();
                    view.finish();

                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        SprinklrPollMemberOfVoted.sendFailLog(e.getResponseCode());
                    }
                });

    }

    private Observable<ResPollParticipants> getParticipantsObservable(long pollId, int selectedSequence) {
        if (selectedSequence >= 0) {
            return model.getParticipantsObservable(pollId, selectedSequence);
        } else {
            return model.getAllParticipantsObservable(pollId);
        }
    }

}
