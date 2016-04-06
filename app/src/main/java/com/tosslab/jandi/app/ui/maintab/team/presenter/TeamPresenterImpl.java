package com.tosslab.jandi.app.ui.maintab.team.presenter;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.maintab.team.model.TeamModel;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
                        .map(query -> Pair.create(query, model.getSearchedMembers(query)))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            view.clearMembers();

                            List<FormattedEntity> members = pair.second;
                            if (members == null || members.isEmpty()) {
                                String query = pair.first;
                                view.setEmptySearchedMember(query);
                            } else {
                                view.setSearchedMembers(members);
                            }

                            view.notifyDataSetChanged();
                        }, throwable -> {
                            LogUtil.e(Log.getStackTraceString(throwable));
                        });
    }

    @Override
    public void stopSearchQueue() {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onInitializeTeam() {
        view.showTeamLayout();

        view.showProgress();

        model.getTeamObservable()
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team -> {
                    view.hideProgress();

                    view.initTeamInfo(team);

                    view.clearMembers();

                    List<FormattedEntity> members = team.getMembers();
                    if (members != null && !members.isEmpty()) {
                        view.initTeamMembers(members);
                    }
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    view.hideProgress();
                }, () -> {
                    view.notifyDataSetChanged();

                    view.doSearchIfNeed();
                });
    }

    @Override
    public void reInitializeTeam() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    onInitializeTeam();
                });
    }

    @Override
    public void onSearchMember(String query) {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueue.onNext(query);
        }
    }

}
