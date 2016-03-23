package com.tosslab.jandi.app.ui.maintab.team.presenter;

import com.tosslab.jandi.app.ui.maintab.team.model.TeamModel;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamPresenterImpl implements TeamPresenter {

    private final TeamModel model;
    private final TeamView view;

    private PublishSubject<String> searchQueryQueue;
    private Subscription searchQueryQueueSubscription;

    @Inject
    public TeamPresenterImpl(TeamModel model, TeamView view) {
        this.model = model;
        this.view = view;

        initSearchQueue();
    }

    @Override
    public void initSearchQueue() {
        searchQueryQueue = PublishSubject.create();
        searchQueryQueueSubscription =
                searchQueryQueue
                        .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .map(model::getSearchedMembers)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setSearchedMembers);
    }

    @Override
    public void stopSearchQueue() {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onInitialize() {
        view.showProgress();

        model.getTeamObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team -> {
                    view.hideProgress();
                    view.initTeamInfo(team);
                });
    }

    @Override
    public void onSearchMember(String query) {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueue.onNext(query);
        }
    }

}
