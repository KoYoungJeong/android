package com.tosslab.jandi.app.ui.share;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.share.model.MainShareModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */

@EActivity(R.layout.activity_main_share)
@OptionsMenu(R.menu.share_menu)
public class MainShareActivity extends AppCompatActivity {

    public static final int MODE_SHARE_TEXT = 1;
    public static final int MODE_SHARE_FILE = 2;

    @Bean
    MainShareModel mainShareModel;

    private ShareDialogFragment fragment;

    @AfterViews
    void initViews() {
        setupActionbar();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        IntentType intentType = mainShareModel.getIntentType(action, type);

        if (intentType == null) {
            // Check Shared Info Type
            startIntro();
            return;
        }

        if (!mainShareModel.hasTeamInfo() || !mainShareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(MainShareActivity.this, getString(R.string.err_profile_get_info));
            startIntro();
            return;
        }

        switch (intentType) {
            case Text:
                fragment = ShareDialogFragment_
                        .builder()
                        .subject(mainShareModel.handleSendSubject(intent))
                        .text(mainShareModel.handleSendText(intent))
                        .mode(MODE_SHARE_TEXT)
                        .build();
                break;
            default:
                fragment = ShareDialogFragment_
                        .builder()
                        .mode(MODE_SHARE_FILE)
                        .uriString(mainShareModel.handleSendImage(intent).toString())
                        .build();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.vg_share_container, fragment, "detail")
                .commit();


    }

    private void startIntro() {
        IntroActivity_.intent(MainShareActivity.this)
                .start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                fragment.startShare();
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
        Image, Text, Etc
    }

}
