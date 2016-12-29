package com.tosslab.jandi.app.ui.profile.modify.dagger;

import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenter;
import com.tosslab.jandi.app.ui.profile.modify.presenter.ModifyProfilePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class ModifyProfileModule {

    private final ModifyProfilePresenter.View view;
    private final long memberId;

    public ModifyProfileModule(ModifyProfilePresenter.View view, long memberId) {
        this.view = view;
        if (memberId <= 0) {
            this.memberId = TeamInfoLoader.getInstance().getMyId();
        } else {
            this.memberId = memberId;
        }
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
        impl.setMemberId(memberId);
        return impl;
    }

}
