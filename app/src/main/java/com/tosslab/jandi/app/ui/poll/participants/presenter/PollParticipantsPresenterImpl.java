package com.tosslab.jandi.app.ui.poll.participants.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;
import com.tosslab.jandi.app.ui.poll.participants.model.PollParticipantsModel;
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

        model.getParticipantsObservable(pollId, selectedSequence)
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

                }, e -> {
                    view.dismissProgress();
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    view.showUnExpectedErrorToast();
                    view.finish();
                });

    }

}
