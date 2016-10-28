package com.tosslab.jandi.app.ui.settings.call;


import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.f2prateek.dart.HensonNavigable;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@HensonNavigable
public class CallSettingActivity extends BaseAppCompatActivity {

    private static final int REQ_PERMISSIONS = 101;

    @Bind(R.id.switch_call_preview_setting_call)
    SwitchCompat switchCall;
    @Bind(R.id.switch_call_preview_setting_canvas)
    SwitchCompat switchCanvas;

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
        setCanvasPermission();
        setCallPermission();

        if (JandiPreference.isShowCallPermissionPopup()) {
            if (hasCanvasPermission() && hasCallPermission()) {
                JandiPreference.setShowCallPermissionPopup();
            }
        }

    }

    private void setCallPermission() {
        switchCall.setChecked(hasCallPermission());
    }

    private boolean hasCallPermission() {
        return SdkUtils.hasPermission(this, Manifest.permission.CALL_PHONE);
    }

    private void setCanvasPermission() {
        switchCanvas.setChecked(hasCanvasPermission());
    }

    private boolean hasCanvasPermission() {
        if (SdkUtils.isMarshmallow()) {
            return Settings.canDrawOverlays(this);
        } else {
            return true;
        }
    }

    @OnClick(R.id.vg_call_preview_setting_call)
    void onCallPermissionClick() {
        if (hasCallPermission()) {
            return;
        }

        if (!SdkUtils.isDeniedPermanently(this, Manifest.permission.CALL_PHONE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS}, REQ_PERMISSIONS);
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    @OnClick(R.id.vg_call_preview_setting_canvas)
    void onCanvasPermissionClick() {
        if (hasCanvasPermission()) {
            return;
        }
        if (SdkUtils.isMarshmallow()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", getPackageName(), null)));
        }
    }
}
