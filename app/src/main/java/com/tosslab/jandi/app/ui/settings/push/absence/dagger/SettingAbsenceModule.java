package com.tosslab.jandi.app.ui.settings.push.absence.dagger;

import com.tosslab.jandi.app.ui.settings.push.absence.presenter.SettingAbsencePresenter;
import com.tosslab.jandi.app.ui.settings.push.absence.presenter.SettingAbsencePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 2017. 5. 23..
 */

@Module
public class SettingAbsenceModule {

    private SettingAbsencePresenter.View view;

    public SettingAbsenceModule(SettingAbsencePresenter.View view) {
        this.view = view;
    }

    @Provides
    SettingAbsencePresenter.View provideView() {
        return view;
    }

    @Provides
    SettingAbsencePresenter provideSettingAbsencePresenter(
            SettingAbsencePresenterImpl SettingAbsencePresenter) {
        return SettingAbsencePresenter;
    }

}
