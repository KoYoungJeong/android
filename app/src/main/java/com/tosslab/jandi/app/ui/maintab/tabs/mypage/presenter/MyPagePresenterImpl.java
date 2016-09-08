package com.tosslab.jandi.app.ui.maintab.tabs.mypage.presenter;

import com.tosslab.jandi.app.team.TeamInfoLoader;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 2016. 8. 30..
 */
public class MyPagePresenterImpl implements MyPagePresenter {

    private final MyPagePresenter.View view;

    @Inject
    public MyPagePresenterImpl(View view) {
        this.view = view;
    }

    @Override
    public void onInitializePollBadge() {
        Observable.just(TeamInfoLoader.getInstance().getPollBadge())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setPollBadge);
    }
}
