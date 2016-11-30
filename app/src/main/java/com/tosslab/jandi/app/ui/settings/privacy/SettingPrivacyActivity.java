package com.tosslab.jandi.app.ui.settings.privacy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity_;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingPrivacyActivity extends BaseAppCompatActivity {

    public static final int REQUEST_SET_PASSCODE = 1;

    @Bind(R.id.switch_setting_privacy_passcode)
    SwitchCompat switchPassCode;

    @Bind(R.id.vg_setting_privacy_passcode_modify)
    ViewGroup vgModifyPassCode;
    @Bind(R.id.v_setting_privacy_divider_for_passcode_modify)
    View vModifyPassCodeDivider;

    @Bind(R.id.vg_setting_privacy_fingerprint)
    ViewGroup vgUseFingerPrint;
    @Bind(R.id.switch_setting_privacy_fingerprint)
    SwitchCompat switchFingerPrint;

    @Bind(R.id.tv_setting_privacy_passcode_detail)
    TextView tvPassCodeDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_privacy);
        ButterKnife.bind(this);
        initViews();
    }

    void initViews() {
        initToolbar();

        initPassCodeSwitch();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SetPasscode);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setTitle(R.string.jandi_privacy_protection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vg_setting_privacy_passcode)
    void setPassCode() {
        boolean checked = !switchPassCode.isChecked();
        if (checked) {
            PassCodeActivity_.intent(this)
                    .mode(PassCodeActivity.MODE_TO_SAVE_PASSCODE)
                    .startForResult(REQUEST_SET_PASSCODE);
        } else {
            JandiPreference.removePassCode(getApplicationContext());
            initPassCodeSwitch();
        }

        AnalyticsValue.Label label = checked ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PasscodeLock, AnalyticsValue.Action.Passcode, label);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_SET_PASSCODE) {
            initPassCodeSwitch();

        }
    }

    void initPassCodeSwitch() {
        String passCode = JandiPreference.getPassCode(getApplicationContext());
        boolean hasPassCode = !TextUtils.isEmpty(passCode);
        switchPassCode.setChecked(hasPassCode);
        tvPassCodeDetail.setVisibility(hasPassCode ? View.GONE : View.VISIBLE);
        vgModifyPassCode.setVisibility(hasPassCode ? View.VISIBLE : View.GONE);
        vModifyPassCodeDivider.setVisibility(hasPassCode ? View.VISIBLE : View.GONE);

        vgUseFingerPrint.setVisibility(hasPassCode ? View.VISIBLE : View.GONE);
        switchFingerPrint.setChecked(JandiPreference.isUseFingerprint());
    }

    @OnClick(R.id.vg_setting_privacy_fingerprint)
    void setUseFingerprint() {
        boolean futureChecked = !switchFingerPrint.isChecked();
        switchFingerPrint.setChecked(futureChecked);
        JandiPreference.setUseFingerprint(futureChecked);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PasscodeLock,
                AnalyticsValue.Action.UseFingerPrint, futureChecked ?
                        AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    @OnClick(R.id.vg_setting_privacy_passcode_modify)
    void startModifyPassCodeActivity() {
        PassCodeActivity_.intent(this)
                .mode(PassCodeActivity.MODE_TO_MODIFY_PASSCODE)
                .startForResult(REQUEST_SET_PASSCODE);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PasscodeLock, AnalyticsValue.Action.ChangePasscode);
    }
}
