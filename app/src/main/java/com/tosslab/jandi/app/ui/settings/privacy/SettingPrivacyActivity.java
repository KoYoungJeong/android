package com.tosslab.jandi.app.ui.settings.privacy;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 10. 13..
 */
@EActivity(R.layout.activity_setting_privacy)
public class SettingPrivacyActivity extends BaseAppCompatActivity {

    public static final int REQUEST_SET_PASSCODE = 1;

    @ViewById(R.id.switch_setting_privacy_passcode)
    SwitchCompat switchPassCode;

    @ViewById(R.id.vg_setting_privacy_passcode_modify)
    ViewGroup vgModifyPassCode;
    @ViewById(R.id.v_setting_privacy_divider_for_passcode_modify)
    View vModifyPassCodeDivider;

    @ViewById(R.id.vg_setting_privacy_fingerprint)
    ViewGroup vgUseFingerPrint;
    @ViewById(R.id.switch_setting_privacy_fingerprint)
    SwitchCompat switchFingerPrint;

    @ViewById(R.id.tv_setting_privacy_passcode_detail)
    TextView tvPassCodeDetail;

    @AfterViews
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

    @OptionsItem(android.R.id.home)
    @Override
    public void finish() {
        super.finish();
    }

    @Click(R.id.vg_setting_privacy_passcode)
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

    @OnActivityResult(REQUEST_SET_PASSCODE)
    void handleResultIfSetPassCode(int resultCode) {
        if (resultCode != RESULT_OK) {
            return;
        }
        initPassCodeSwitch();
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

    @Click(R.id.vg_setting_privacy_fingerprint)
    void setUseFingerprint() {
        boolean futureChecked = !switchFingerPrint.isChecked();
        switchFingerPrint.setChecked(futureChecked);
        JandiPreference.setUseFingerprint(futureChecked);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PasscodeLock,
                AnalyticsValue.Action.UseFingerPrint, futureChecked ?
                        AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    @Click(R.id.vg_setting_privacy_passcode_modify)
    void startModifyPassCodeActivity() {
        PassCodeActivity_.intent(this)
                .mode(PassCodeActivity.MODE_TO_MODIFY_PASSCODE)
                .startForResult(REQUEST_SET_PASSCODE);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.PasscodeLock, AnalyticsValue.Action.ChangePasscode);
    }
}
