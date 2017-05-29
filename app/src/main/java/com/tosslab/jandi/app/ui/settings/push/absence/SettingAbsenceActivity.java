package com.tosslab.jandi.app.ui.settings.push.absence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.CalendarDialogFragment;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.push.absence.dagger.DaggerSettingAbsenceComponent;
import com.tosslab.jandi.app.ui.settings.push.absence.dagger.SettingAbsenceModule;
import com.tosslab.jandi.app.ui.settings.push.absence.presenter.SettingAbsencePresenter;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2017. 5. 23..
 */

public class SettingAbsenceActivity extends BaseAppCompatActivity implements SettingAbsencePresenter.View {

    @Bind(R.id.vg_setting_absence_checkbox)
    SettingsBodyCheckView vgSettingAbsenceCheckBox;

    @Bind(R.id.vg_setting_absence_detail)
    ViewGroup vgSettingAbsenceDetail;

    @Bind(R.id.tv_select_absence_start_time)
    TextView tvSelectAbsenceStartTime;

    @Bind(R.id.tv_select_absence_end_time)
    TextView tvSelectAbsenceEndTime;

    @Bind(R.id.vg_setting_push_alarm_enable_checkbox)
    SettingsBodyCheckView vgSettingPushAlarmEnableCheckBox;

    @Bind(R.id.et_absence_option_message)
    AutoCompleteTextView etAbsenceOptionMessage;

    @Bind(R.id.tv_absence_option_message_length)
    TextView tvAbsenceOptionMessageLength;

    private boolean isEnablePushAlarm = false;

    public static void launchActivity(Activity activity) {
        activity.startActivity(
                new Intent(activity, SettingAbsenceActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_absence);
        ButterKnife.bind(this);
        DaggerSettingAbsenceComponent.builder()
                .settingAbsenceModule(new SettingAbsenceModule(this))
                .build()
                .inject(this);
        initViews();
    }

    private void initViews() {
        vgSettingAbsenceDetail.setVisibility(View.GONE);
        etAbsenceOptionMessage.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAbsenceOptionMessageLength.setText(etAbsenceOptionMessage.length() + "/60");
            }
        });
    }

    @OnClick(R.id.vg_setting_absence_checkbox)
    void onSettingAbsenceCheckboxClicked() {
        boolean isChecked = vgSettingAbsenceCheckBox.isChecked();
        vgSettingAbsenceCheckBox.setChecked(!isChecked);
        if (!isChecked) {
            vgSettingAbsenceDetail.setVisibility(View.VISIBLE);
        } else {
            vgSettingAbsenceDetail.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.vg_setting_push_alarm_enable_checkbox)
    void onPushAlarmEnableCheckboxClicked() {
        isEnablePushAlarm = vgSettingPushAlarmEnableCheckBox.isChecked();
        vgSettingPushAlarmEnableCheckBox.setChecked(!isEnablePushAlarm);
    }

    @OnClick(R.id.tv_select_absence_start_time)
    void showStartDateChoiceView() {
        CalendarDialogFragment fragment = new CalendarDialogFragment();
        fragment.show(getSupportFragmentManager(), "calendar_dialog");
    }
}