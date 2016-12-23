package com.tosslab.jandi.app.ui.profile.insert.dagger;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileFirstPagePresenter;
import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileFirstPagePresenterImpl;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;

import dagger.Module;
import dagger.Provides;

@Module
public class InsertProfileFirstPageModule {

    private InsertProfileFirstPagePresenter.View view;

    public InsertProfileFirstPageModule(InsertProfileFirstPagePresenter.View view) {
        this.view = view;
    }

    @Provides
    ModifyProfileModel provideModifyProfileModel() {
        return new ModifyProfileModel();
    }

    @Provides
    FileUploadController provideProfileFileUploadController() {
        return new ProfileFileUploadControllerImpl();
    }

    @Provides
    InsertProfileFirstPagePresenter.View provideViewOfSetProfileFirstPagePresenter() {
        return view;
    }

    @Provides
    InsertProfileFirstPagePresenter provideSetProfileFirstPagePresenter(InsertProfileFirstPagePresenterImpl presenter) {
        return presenter;
    }

}
