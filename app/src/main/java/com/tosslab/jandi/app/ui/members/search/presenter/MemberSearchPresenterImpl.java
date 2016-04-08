package com.tosslab.jandi.app.ui.members.search.presenter;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.ui.members.search.model.MemberSearchModel;
import com.tosslab.jandi.app.ui.members.search.view.MemberSearchView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public class MemberSearchPresenterImpl implements MemberSearchPresenter {

    private final MemberSearchableDataModel memberSearchableDataModel;
    private final MemberSearchModel memberSearchModel;
    private final MemberSearchView memberSearchView;

    @Inject
    public MemberSearchPresenterImpl(MemberSearchModel memberSearchModel,
                                     MemberSearchableDataModel memberSearchableDataModel,
                                     MemberSearchView memberSearchView) {
        this.memberSearchModel = memberSearchModel;
        this.memberSearchableDataModel = memberSearchableDataModel;
        this.memberSearchView = memberSearchView;

        initMemberSearchQueue();
    }

    private PublishSubject<String> searchQueryQueue;
    private Subscription searchQueryQueueSubscription;

    @Override
    public void initMemberSearchQueue() {
        searchQueryQueue = PublishSubject.create();
        searchQueryQueueSubscription =
                searchQueryQueue
                        .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .map(query -> Pair.create(query,
                                memberSearchModel.getSearchedMembers(
                                        query, memberSearchableDataModel.getInitializedMembers())))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            memberSearchableDataModel.clear();

                            List<FormattedEntity> members = pair.second;
                            if (members == null || members.isEmpty()) {
                                String query = pair.first;
                                memberSearchableDataModel.setEmptySearchedMember(query);
                            } else {
                                memberSearchableDataModel.addAll(members);
                            }

                            memberSearchView.notifyDataSetChanged();
                        }, throwable -> {
                            LogUtil.e(Log.getStackTraceString(throwable));
                        });
    }

    @Override
    public void onInitializeWholeMembers() {
        memberSearchView.showProgress();

        memberSearchModel.getEnabledMembersObservable()
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(members -> {
                    memberSearchView.hideProgress();

                    memberSearchableDataModel.clear();

                    if (members != null && !members.isEmpty()) {
                        memberSearchableDataModel.setInitializedMembers(members);
                        memberSearchableDataModel.addAll(members);
                    }
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    memberSearchView.hideProgress();
                }, memberSearchView::notifyDataSetChanged);
    }

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
