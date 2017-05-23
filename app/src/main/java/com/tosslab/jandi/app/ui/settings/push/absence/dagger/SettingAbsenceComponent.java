package com.tosslab.jandi.app.ui.settings.push.absence.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.settings.push.absence.SettingAbsenceActivity;

import dagger.Component;

/**
 * Created by tee on 2017. 5. 23..
 */

@Component(modules = {ApiClientModule.class, SettingAbsenceModule.class})
public interface SettingAbsenceComponent {
    void inject(SettingAbsenceActivity activity);
}
