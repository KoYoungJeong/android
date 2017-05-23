package com.tosslab.jandi.app.ui.settings.push.absence;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.push.absence.presenter.SettingAbsencePresenter;

/**
 * Created by tee on 2017. 5. 23..
 */

public class SettingAbsenceActivity extends BaseAppCompatActivity implements SettingAbsencePresenter.View {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_absence);
    }

}
