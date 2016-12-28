package com.tosslab.jandi.app.ui.profile.email.dagger;

import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapter;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapterDataModel;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapterViewModel;
import com.tosslab.jandi.app.ui.profile.email.model.EmailChooseModel;
import com.tosslab.jandi.app.ui.profile.email.presenter.EmailChoosePresenter;
import com.tosslab.jandi.app.ui.profile.email.presenter.EmailChoosePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 2016. 12. 22..
 */

@Module
public class EmailChooseModule {

    private EmailChoosePresenter.View view;
    private EmailChooseAdapter adapter;

    public EmailChooseModule(EmailChoosePresenter.View view, EmailChooseAdapter adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    @Provides
    EmailChoosePresenter provideEmailChoosePresenter(EmailChoosePresenterImpl presenter) {
        return presenter;
    }

    @Provides
    EmailChooseAdapterViewModel provideEmailChooseAdapterViewModel() {
        return adapter;
    }

    @Provides
    EmailChooseAdapterDataModel provideEmailChooseAdapterDataModel() {
        return adapter;
    }

    @Provides
    EmailChoosePresenter.View provideViewOfEmailChoosePresenter() {
        return view;
    }

    @Provides
    EmailChooseModel provideEmailChooseModel() {
        return new EmailChooseModel();
    }

}
