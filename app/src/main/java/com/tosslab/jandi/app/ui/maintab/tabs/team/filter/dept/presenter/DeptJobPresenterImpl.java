package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.model.DeptJobModel;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class DeptJobPresenterImpl implements DeptJobPresenter {

    final DeptJobModel deptJobModel;
    View view;
    DeptJobDataModel deptJobDataModel;
    private int type;
    BehaviorSubject<String> deptJobSubject;
    CompositeSubscription subscription;

    @Inject
    public DeptJobPresenterImpl(View view, DeptJobDataModel deptJobDataModel, DeptJobModel deptJobModel) {
        this.view = view;
        this.deptJobDataModel = deptJobDataModel;
        this.deptJobModel = deptJobModel;
        subscription = new CompositeSubscription();
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onCreate() {
        deptJobSubject = BehaviorSubject.create("");
        Observable<String> searchObservable = deptJobSubject.onBackpressureBuffer()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged();


        subscription.add(searchObservable.filter(it -> type == DeptJobFragment.EXTRA_TYPE_DEPT)
                .map(String::toLowerCase)
                .observeOn(Schedulers.io())
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .filter(user -> !TextUtils.isEmpty(user.getDivision()))
                        .map(User::getDivision)
                        .compose(deptJobModel.containFilter(it))
                        .distinct()
                        .toSortedList(StringCompareUtil::compare)
                        .compose(deptJobModel.textToSpan(it)))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deptJobModel.dataMap())
                .subscribe(this::addDatas));


        subscription.add(searchObservable.filter(it -> type == DeptJobFragment.EXTRA_TYPE_JOB)
                .map(String::toLowerCase)
                .observeOn(Schedulers.io())
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .filter(user -> !TextUtils.isEmpty(user.getPosition()))
                        .map(User::getPosition)
                        .compose(deptJobModel.containFilter(it))
                        .distinct()
                        .toSortedList(StringCompareUtil::compare)
                        .compose(deptJobModel.textToSpan(it)))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deptJobModel.dataMap())
                .subscribe(this::addDatas));

    }


    protected void addDatas(List<Pair<CharSequence, String>> its) {
        deptJobDataModel.clear();
        if (!its.isEmpty()) {
            view.dismissEmptyView();
            deptJobDataModel.addAll(its);
        } else {
            view.showEmptyView(deptJobSubject.getValue());
        }
        view.refreshDataView();
    }

    @Override
    public void onDestroy() {
        deptJobSubject.onCompleted();

        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onSearchKeyword(String text) {
        deptJobSubject.onNext(text);
    }

    @Override
    public void onPickUser(long userId) {
        long roomId = TeamInfoLoader.getInstance().getChatId(userId);
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long lastLinkId;
        if (roomId > 0) {
            lastLinkId = TeamInfoLoader.getInstance().getRoom(roomId).getLastLinkId();
        } else {
            lastLinkId = -1;
        }


        view.moveDirectMessage(teamId, userId, roomId, lastLinkId);

    }

    @Override
    public void onItemClick(int position) {
        String value = deptJobSubject.getValue();
        if (value.length() > 0) {
            MemberRecentKeywordRepository.getInstance().upsertKeyword(value);
        }
    }
}
