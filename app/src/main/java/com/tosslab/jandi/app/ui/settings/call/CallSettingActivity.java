package com.tosslab.jandi.app.ui.settings.call;


import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.dart.HensonNavigable;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@HensonNavigable
public class CallSettingActivity extends BaseAppCompatActivity {

    private static final int REQ_PERMISSIONS = 101;

    @Bind(R.id.tv_call_preview_setting_call)
    TextView tvCallPermission;
    @Bind(R.id.tv_call_preview_setting_canvas)
    TextView tvCanvasPermission;

    @Bind(R.id.tv_call_preview_setting_guide2)
    TextView tvGuide2;
    @Bind(R.id.tv_call_preview_setting_guide3)
    TextView tvGuide3;

    @Bind(R.id.vg_call_preview_setting_call)
    ViewGroup vgCallPermission;
    @Bind(R.id.vg_call_preview_setting_canvas)
    ViewGroup vgCanvasPermission;
    @Bind(R.id.vg_call_preview_setting_caller_id)
    ViewGroup vgCallerId;
    @Bind(R.id.switch_call_preview_setting_caller_id)
    SwitchCompat switchCallerId;

    @Bind(R.id.layout_dept_job_group_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_preview_setting);
        ButterKnife.bind(this);

        setUpAction();
    }

    private void setUpAction() {
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SdkUtils.hasCanvasPermission() && hasCallPermission()) {
            vgCallPermission.setVisibility(View.GONE);
            vgCanvasPermission.setVisibility(View.GONE);
            tvGuide2.setVisibility(View.GONE);
            tvGuide3.setVisibility(View.GONE);
            vgCallerId.setVisibility(View.VISIBLE);
            switchCallerId.setChecked(JandiPreference.isShowCallPopup());
        } else {
            vgCallPermission.setVisibility(View.VISIBLE);
            vgCanvasPermission.setVisibility(View.VISIBLE);
            tvGuide2.setVisibility(View.VISIBLE);
            tvGuide3.setVisibility(View.VISIBLE);
            vgCallerId.setVisibility(View.GONE);

            setCanvasPermission();
            setCallPermission();
        }


    }

    private void setCallPermission() {
        boolean hasCallPermission = hasCallPermission();
        tvCallPermission.setSelected(hasCallPermission);
        if (hasCallPermission) {
            tvCallPermission.setText(R.string.common_calleridnotifier_status_allowed);
        } else {
            tvCallPermission.setText(R.string.common_calleridnotifier_status_allow);
        }
    }

    private boolean hasCallPermission() {
        return SdkUtils.hasPermission(Manifest.permission.CALL_PHONE);
    }

    private void setCanvasPermission() {
        boolean hasCanvasPermission = SdkUtils.hasCanvasPermission();
        tvCanvasPermission.setSelected(hasCanvasPermission);
        if (hasCanvasPermission) {
            tvCanvasPermission.setText(R.string.common_calleridnotifier_status_allowed);
        } else {
            tvCanvasPermission.setText(R.string.common_calleridnotifier_status_allow);
        }
    }

    @OnClick(R.id.vg_call_preview_setting_call)
    void onCallPermissionClick() {
        if (hasCallPermission()) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS}, REQ_PERMISSIONS);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamPhoneNumberSetting, AnalyticsValue.Action.AllowPhoneCalls);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Permissions.getResult()
                .activity(this)
                .addRequestCode(REQ_PERMISSIONS)
                .addPermission(Manifest.permission.CALL_PHONE, () -> {
                })
                .neverAskAgain(() -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }).resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));

    }

    @OnClick(R.id.vg_call_preview_setting_canvas)
    void onCanvasPermissionClick() {
        if (SdkUtils.hasCanvasPermission()) {
            return;
        }
        if (SdkUtils.isOverMarshmallow()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", getPackageName(), null)));
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamPhoneNumberSetting, AnalyticsValue.Action.DrawOverApps);
    }

    @OnClick(R.id.vg_call_preview_setting_caller_id)
    void onCallIdSetting() {
        switchCallerId.setChecked(!switchCallerId.isChecked());
        JandiPreference.setShowCallPopup(switchCallerId.isChecked());
    }
}
