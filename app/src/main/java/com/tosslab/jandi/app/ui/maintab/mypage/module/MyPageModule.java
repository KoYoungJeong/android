package com.tosslab.jandi.app.ui.maintab.mypage.module;

import com.tosslab.jandi.app.ui.maintab.mypage.model.MyPageModel;
import com.tosslab.jandi.app.ui.maintab.mypage.presenter.MyPagePresenter;
import com.tosslab.jandi.app.ui.maintab.mypage.presenter.MyPagePresenterImpl;
import com.tosslab.jandi.app.ui.maintab.mypage.view.MyPageView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 17..
 */
@Module
public class MyPageModule {

    private final MyPageView view;

    public MyPageModule(MyPageView view) {
        this.view = view;
    }

    @Provides
    @Singleton
    public MyPageModel provideMyPageModel() {
        return new MyPageModel();
    }

    @Provides
    public MyPageView provideMypageView() {
        return view;
    }

    @Provides
    public MyPagePresenter provideMyPagePresenter(MyPagePresenterImpl presenter) {
        return presenter;
    }


}
