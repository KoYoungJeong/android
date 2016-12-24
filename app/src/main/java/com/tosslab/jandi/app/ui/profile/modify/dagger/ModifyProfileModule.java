package com.tosslab.jandi.app.ui.profile.modify.dagger;

import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenter;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class ModifyProfileModule {

    private final ModifyProfilePresenter.View view;

    public ModifyProfileModule(ModifyProfilePresenter.View view) {
        this.view = view;
    }

    @Provides
    ModifyProfilePresenter.View provideView() {
        return view;
    }

    @Provides
    ProfileFileUploadControllerImpl provideFileUploadController() {
        return new ProfileFileUploadControllerImpl();
    }

    @Provides
    ModifyProfilePresenter provideModifyProfilePresenter(ModifyProfilePresenterImpl impl) {
        return impl;
    }
}
