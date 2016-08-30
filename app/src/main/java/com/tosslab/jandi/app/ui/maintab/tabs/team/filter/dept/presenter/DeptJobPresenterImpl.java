package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataModel;
import com.tosslab.jandi.app.utils.FirstCharacterUtil;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class DeptJobPresenterImpl implements DeptJobPresenter {

    private View view;
    private DeptJobDataModel deptJobDataModel;
    private int type;
    private BehaviorSubject<String> deptJobSubject;
    private CompositeSubscription subscription;
    private HighlightSpannable highlightSpan;

    @Inject
    public DeptJobPresenterImpl(View view, DeptJobDataModel deptJobDataModel) {
        this.view = view;
        this.deptJobDataModel = deptJobDataModel;
        subscription = new CompositeSubscription();

        int highlighteColor = JandiApplication.getContext().getResources().getColor(R.color.rgb_00abe8);
        highlightSpan = new HighlightSpannable(Color.TRANSPARENT, highlighteColor);
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
                        .toSortedList(StringCompareUtil::compare)
                        .compose(textToSpan(it)))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(dataMap())
                .subscribe(this::addDatas));


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
                        .toSortedList(StringCompareUtil::compare)
                        .compose(textToSpan(it)))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(dataMap())
                .subscribe(this::addDatas));

    }

    private Observable.Transformer<? super List<String>, ? extends List<CharSequence>> textToSpan(final String it) {
        return observable -> observable.concatMap(new Func1<List<String>, Observable<? extends List<CharSequence>>>() {
            @Override
            public Observable<? extends ArrayList<CharSequence>> call(List<String> its) {
                if (TextUtils.isEmpty(it)) {
                    return Observable.just(new ArrayList<CharSequence>(its));
                } else {
                    return Observable.from(its)
                            .map(text -> {

                                int index = text.toLowerCase().indexOf(it.toLowerCase());
                                if (index >= 0) {
                                    SpannableStringBuilder builder = new SpannableStringBuilder(text);
                                    builder.setSpan(highlightSpan, index, index + it.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    return builder;
                                } else {
                                    return text;
                                }
                            })
                            .collect(ArrayList::new, List::add);
                }
            }
        });
    }

    protected void addDatas(List<Pair<CharSequence, String>> its) {
        deptJobDataModel.clear();
        if (!its.isEmpty()) {
            view.dismissEmptyView();
            deptJobDataModel.addAll(its);
            view.refreshDataView();
        } else {
            view.showEmptyView(deptJobSubject.getValue());
        }
    }

    private Observable.Transformer<? super List<CharSequence>, ? extends List<Pair<CharSequence, String>>> dataMap() {
        return ob -> ob.map(its -> {
            List<Pair<CharSequence, String>> list = new ArrayList<>();
            for (CharSequence it : its) {
                list.add(Pair.create(it, FirstCharacterUtil.firstCharacter(it.toString())));
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
