package com.tosslab.jandi.app.ui.settings.absence.dagger;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.settings.absence.presenter.SettingAbsencePresenter;
import com.tosslab.jandi.app.ui.settings.absence.presenter.SettingAbsencePresenterImpl;

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

    @Provides
    InputMethodManager provideInputMethodManager() {
        return (InputMethodManager) JandiApplication
                .getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

}
