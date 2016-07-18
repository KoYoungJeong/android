package com.tosslab.jandi.app.ui.poll.participants.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResPollParticipants;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.poll.participants.model.PollParticipantsModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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

                    AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                            .event(Event.PollMemberOfVoted)
                            .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                            .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                            .property(PropertyKey.ResponseSuccess, true)
                            .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                            .property(PropertyKey.PollId, pollId)
                            .build());
                }, t -> {
                    view.dismissProgress();
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    view.showUnExpectedErrorToast();
                    view.finish();

                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                                .event(Event.PollMemberOfVoted)
                                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                                .property(PropertyKey.ResponseSuccess, true)
                                .property(PropertyKey.ErrorCode, e.getStatusCode())
                                .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                                .property(PropertyKey.PollId, pollId)
                                .build());
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
