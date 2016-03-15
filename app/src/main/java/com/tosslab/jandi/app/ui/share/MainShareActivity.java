package com.tosslab.jandi.app.ui.share;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.share.model.MainShareModel;
import com.tosslab.jandi.app.ui.share.multi.MultiShareFragment;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */

@EActivity(R.layout.activity_main_share)
@OptionsMenu(R.menu.share_menu)
public class MainShareActivity extends BaseAppCompatActivity {

    public static final int REQ_STORAGE_PERMISSION = 101;

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
            // Check Shared Info Type
            startIntro();
            return;
        }

        if (!mainShareModel.hasTeamInfo() || !mainShareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(getString(R.string.err_profile_get_info));
            startIntro();
            return;
        }

        if (intentType == IntentType.Text) {
            setUpFragment(intent, intentType);
        } else {
            Permissions.getChecker()
                    .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .hasPermission(() -> setUpFragment(intent, intentType))
                    .noPermission(() -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE_PERMISSION))
                    .check();
        }


    }

    private void setUpMultiUploadFragment(Intent intent) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> {
                    new Handler().postDelayed(() -> {
                        Intent intent = getIntent();
                        setUpFragment(intent, mainShareModel.getIntentType(intent.getAction(), intent.getType()));
                    }, 300);
                }, this::finish)
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    private void setUpFragment(Intent intent, IntentType intentType) {
        String fragmentTag = "share";

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if (fragment != null) {
            return;
        }

        Share share;
        if (intentType == IntentType.Text) {
            TextShareFragment textShareFragment = TextShareFragment_.builder()
                    .subject(mainShareModel.handleSendSubject(intent))
                    .text(mainShareModel.handleSendText(intent)).build();
            fragment = textShareFragment;
            share = textShareFragment;
        } else if (intentType == IntentType.Multiple) {
            MultiShareFragment multiShareFragment = MultiShareFragment
                    .create(mainShareModel.handleSendImages(intent));
            fragment = multiShareFragment;
            share = multiShareFragment;
        } else {
            ImageShareFragment imageShareFragment = ImageShareFragment_.builder()
                    .uriString(mainShareModel.handleSendImage(intent).toString())
                    .build();
            fragment = imageShareFragment;
            share = imageShareFragment;
        }

        this.share = share;
        fragmentManager.beginTransaction()
                .add(R.id.vg_share_container, fragment, fragmentTag)
                .commit();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SharetoJandi);
    }

    private void startIntro() {
        IntroActivity_.intent(MainShareActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(MainShareActivity.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                share.startShare();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.Send);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        Image, Text, Multiple, Etc
    }

    public interface Share {
        void startShare();
    }

}
