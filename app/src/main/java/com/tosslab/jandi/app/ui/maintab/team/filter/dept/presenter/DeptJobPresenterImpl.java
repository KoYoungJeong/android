package com.tosslab.jandi.app.ui.maintab.team.filter.dept.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter.DeptJobDataModel;
import com.tosslab.jandi.app.utils.FirstCharacterUtil;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class DeptJobPresenterImpl implements DeptJobPresenter {

    private View view;
    private DeptJobDataModel deptJobDataModel;
    private int type;
    private BehaviorSubject<String> deptJobSubject;
    private CompositeSubscription subscription;

    @Inject
    public DeptJobPresenterImpl(View view, DeptJobDataModel deptJobDataModel) {
        this.view = view;
        this.deptJobDataModel = deptJobDataModel;
        subscription = new CompositeSubscription();
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onCreate() {
        deptJobSubject = BehaviorSubject.create("");


        subscription.add(deptJobSubject.filter(it -> type == DeptJobFragment.EXTRA_TYPE_DEPT)
                .onBackpressureBuffer()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .map(String::toLowerCase)
                .observeOn(Schedulers.io())
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .filter(user -> !TextUtils.isEmpty(user.getDivision()))
                        .map(User::getDivision)
                        .filter(division -> {
                            if (TextUtils.isEmpty(it)) {
                                return true;
                            } else {
                                return division.toLowerCase().contains(it);
                            }
                        })
                        .distinct()
                        .toSortedList(StringCompareUtil::compare))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(dataMap())
                .subscribe(its -> {
                    addDatas(its);
                }));


        subscription.add(deptJobSubject.filter(it -> type == DeptJobFragment.EXTRA_TYPE_JOB)
                .onBackpressureBuffer()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .map(String::toLowerCase)
                .observeOn(Schedulers.io())
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .filter(user -> !TextUtils.isEmpty(user.getPosition()))
                        .map(User::getPosition)
                        .filter(division -> {
                            if (TextUtils.isEmpty(it)) {
                                return true;
                            } else {
                                return division.toLowerCase().contains(it);
                            }
                        })
                        .distinct()
                        .toSortedList(StringCompareUtil::compare))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(dataMap())
                .subscribe(its -> {
                    addDatas(its);
                }));

    }

    protected void addDatas(List<Pair<String, String>> its) {
        deptJobDataModel.clear();
        if (!its.isEmpty()) {
            view.dismissEmptyView();
            deptJobDataModel.addAll(its);
            view.refreshDataView();
        } else {
            view.showEmptyView(deptJobSubject.getValue());
        }
    }

    private Observable.Transformer<? super List<String>, ? extends List<Pair<String, String>>> dataMap() {
        return ob -> ob.map(its -> {
            List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
            for (String it : its) {
                list.add(Pair.create(it, FirstCharacterUtil.firstCharacter(it)));
            }
            return list;
        });
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
