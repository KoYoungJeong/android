package com.tosslab.jandi.app.ui.share;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.share.file.FileShareFragment;
import com.tosslab.jandi.app.ui.share.model.MainShareModel;
import com.tosslab.jandi.app.ui.share.multi.MultiShareFragment;
import com.tosslab.jandi.app.ui.share.text.TextShareFragment;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EActivity(R.layout.activity_main_share)
@OptionsMenu(R.menu.share_menu)
public class MainShareActivity extends BaseAppCompatActivity {

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final String FRAGMENT_TAG = "share";

    @Bean
    MainShareModel mainShareModel;

    private Share share;

    @AfterViews
    void initViews() {
        setupActionbar();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        boolean used = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        IntentType intentType = mainShareModel.getIntentType(action, type);

        if (intentType == null || used) {
            ColoredToast.show(R.string.jandi_unsupported_share_contents);
            finish();
            return;
        }

        if (!mainShareModel.hasTeamInfo() || !mainShareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(getString(R.string.err_profile_get_info));
            startIntro();
            finish();
            return;
        }

        if (intentType == IntentType.Text) {
            setUpFragment(intent, intentType);
        } else {
            Permissions.getChecker()
                    .activity(MainShareActivity.this)
                    .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .hasPermission(() -> setUpFragment(intent, intentType))
                    .noPermission(() ->
                            ActivityCompat.requestPermissions(MainShareActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQ_STORAGE_PERMISSION))
                    .check();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(MainShareActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> {
                    new Handler().postDelayed(() -> {
                        Intent intent = getIntent();
                        setUpFragment(intent, mainShareModel.getIntentType(intent.getAction(), intent.getType()));
                    }, 300);
                }, this::finish)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(MainShareActivity.this);
                })
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    private void setUpFragment(Intent intent, IntentType intentType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (fragment != null) {
            return;
        }

        boolean attached = false;
        if (intentType == IntentType.Text) {

            attached = attachTextShareFragment(intent);

        } else if (intentType == IntentType.Multiple) {

            attached = attachMultiShareFragment(intent);

        } else if (intentType == IntentType.File) {

            attached = attachFileShareFragment(intent);

        }

        if (attached) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SharetoJandi);
        } else {
            ColoredToast.show(R.string.jandi_unsupported_share_contents);
            finish();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof Share) {
            this.share = (Share) fragment;
        }
    }

    private boolean attachTextShareFragment(Intent intent) {
        String subject = mainShareModel.getShareSubject(intent);
        CharSequence text = mainShareModel.getShareText(intent);
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        TextShareFragment fragment = TextShareFragment.create(this, subject, text.toString());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();

        return true;
    }

    private boolean attachFileShareFragment(Intent intent) {
        Uri uri = mainShareModel.getShareFile(intent);
        if (uri == null) {
            return false;
        }

        FileShareFragment fragment = FileShareFragment.create(this, uri.toString());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();

        return true;
    }

    private boolean attachMultiShareFragment(Intent intent) {
        List<Uri> uris = mainShareModel.getShareFiles(intent);
        if (uris == null || uris.isEmpty()) {
            return false;
        }

        MultiShareFragment fragment = MultiShareFragment.create(uris);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, FRAGMENT_TAG)
                .commit();
        return true;
    }

    private void startIntro() {
        IntroActivity.startActivity(MainShareActivity.this, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                if (share != null) {
                    share.startShare();
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.Send);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

        return super.dispatchTouchEvent(ev);
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_share);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(R.string.jandi_share_to_jandi);
    }

    public enum IntentType {
        Text, Multiple, File
    }

    public interface Share {
        void startShare();
    }

}
