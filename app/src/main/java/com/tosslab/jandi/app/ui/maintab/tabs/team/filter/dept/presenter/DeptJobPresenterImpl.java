package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
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
    private final String undefinedMember;
    View view;
    DeptJobDataModel deptJobDataModel;
    BehaviorSubject<String> deptJobSubject;
    CompositeSubscription subscription;
    private int type;

    @Inject
    public DeptJobPresenterImpl(View view, DeptJobDataModel deptJobDataModel, DeptJobModel deptJobModel) {
        this.view = view;
        this.deptJobDataModel = deptJobDataModel;
        this.deptJobModel = deptJobModel;
        subscription = new CompositeSubscription();
        undefinedMember = JandiApplication.getContext().getString(R.string.jandi_undefined_member);
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onCreate() {
        deptJobSubject = BehaviorSubject.create("");
        Observable<String> searchObservable = deptJobSubject.onBackpressureBuffer()
                .throttleLast(100, TimeUnit.MILLISECONDS);


        subscription.add(searchObservable.filter(it -> type == DeptJobFragment.EXTRA_TYPE_DEPT)
                .map(String::toLowerCase)
                .observeOn(Schedulers.io())
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .map((user) -> {
                            if (!TextUtils.isEmpty(user.getDivision())) {

                                return user.getDivision();
                            } else {
                                return undefinedMember;
                            }
                        })
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
                        .map((user) -> {
                            if (!TextUtils.isEmpty(user.getPosition())) {
                                return user.getPosition();
                            } else {
                                return undefinedMember;
                            }
                        })
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

    @Override
    public void onRefresh() {
        deptJobSubject.onNext(deptJobSubject.getValue());
    }
}
