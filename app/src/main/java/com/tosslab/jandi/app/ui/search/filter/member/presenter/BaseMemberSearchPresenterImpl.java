package com.tosslab.jandi.app.ui.search.filter.member.presenter;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.search.filter.member.model.MemberSearchModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 2016. 7. 26..
 */
public abstract class BaseMemberSearchPresenterImpl implements BaseMemberSearchPresenter {

    protected final MemberSearchModel memberSearchModel;

    private PublishSubject<String> searchQueryQueue;
    private Subscription searchQueryQueueSubscription;

    public BaseMemberSearchPresenterImpl(MemberSearchModel model) {
        this.memberSearchModel = model;
        initMemberSearchQueue();
    }

    @Override
    public void initMemberSearchQueue() {
        searchQueryQueue = PublishSubject.create();
        searchQueryQueueSubscription =
                searchQueryQueue
                        .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .map(query -> Pair.create(query,
                                memberSearchModel.getSearchedMembers(query, getInitializedMembers())))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            onMemberSearched(pair.first, pair.second);
                        }, throwable -> {
                            LogUtil.e(Log.getStackTraceString(throwable));
                        });
    }

    @Override
    public abstract void onInitializeWholeMembers();

    public abstract void onMemberSearched(String query, List<User> members);

    public abstract List<User> getInitializedMembers();

    @Override
    public void stopMemberSearchQueue() {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onSearchMember(String query) {
        if (!searchQueryQueueSubscription.isUnsubscribed()) {
            searchQueryQueue.onNext(query);
        }
    }

}
