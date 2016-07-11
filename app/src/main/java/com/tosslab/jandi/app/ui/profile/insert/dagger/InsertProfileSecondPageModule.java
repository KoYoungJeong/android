package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileSecondPagePresenter;
import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileSecondPagePresenterImpl;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;

import dagger.Module;
import dagger.Provides;

@Module
public class InsertProfileSecondPageModule {

    private InsertProfileSecondPagePresenter.View view;

    public InsertProfileSecondPageModule(InsertProfileSecondPagePresenter.View view) {
        this.view = view;
    }

    @Provides
    ModifyProfileModel provideModifyProfileModel() {
        return new ModifyProfileModel();
    }

    @Provides
    InsertProfileSecondPagePresenter.View provideViewOfSetProfileSecondPagePresenter() {
        return view;
    }

    @Provides
    InsertProfileSecondPagePresenter provideSetProfileSecondPagePresenter(
            InsertProfileSecondPagePresenterImpl presenter) {
        return presenter;
    }

}
