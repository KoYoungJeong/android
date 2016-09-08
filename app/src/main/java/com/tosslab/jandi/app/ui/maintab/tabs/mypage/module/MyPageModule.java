package com.tosslab.jandi.app.ui.maintab.tabs.mypage.module;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.presenter.MyPagePresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.presenter.MyPagePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 2016. 8. 30..
 */
@Module
public class MyPageModule {

    private MyPagePresenter.View view;

    public MyPageModule(MyPagePresenter.View view) {
        this.view = view;
    }

    @Provides
    public MyPagePresenter.View providesMyPageView() {
        return view;
    }

    @Provides
    public MyPagePresenter providesMyPagePresenter(MyPagePresenterImpl myPagePresenter) {
        return myPagePresenter;
    }

}
