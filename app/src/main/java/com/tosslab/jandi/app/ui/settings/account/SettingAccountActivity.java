package com.tosslab.jandi.app.ui.settings.account;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.email.EmailChooseActivity;
import com.tosslab.jandi.app.ui.settings.account.component.DaggerSettingAccountComponent;
import com.tosslab.jandi.app.ui.settings.account.module.SettingAccountModule;
import com.tosslab.jandi.app.ui.settings.account.presenter.SettingAccountPresenter;
import com.tosslab.jandi.app.ui.settings.account.view.SettingAccountView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class SettingAccountActivity extends BaseAppCompatActivity implements SettingAccountView {

    public static final int REQUEST_EMAIL_CHOOSE = 323;
    @Inject
    SettingAccountPresenter presenter;

    @Bind(R.id.vg_setting_account_name)
    SettingsBodyView sbvAccountName;

    @Bind(R.id.vg_settin_account_email)
    SettingsBodyView sbvAccountEmail;

    @Bind(R.id.layout_search_bar)
    Toolbar toolbar;

    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerSettingAccountComponent.builder()
                .settingAccountModule(new SettingAccountModule(this))
                .build()
                .inject(this);

        setContentView(R.layout.activity_setting_account);

        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        setUpActionBar();

        progressWheel = new ProgressWheel(this);

        presenter.initializeAccountName();
        presenter.initializeAccountEmail();
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setTitle(R.string.jandi_setting_account);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @OnClick(R.id.vg_setting_account_name)
    void onClickAccountName() {
        String currentName = sbvAccountName.getTitle();
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_ACCOUNT_NAME, currentName);
        newFragment.show(getFragmentManager(), "dialog");

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountSetting, AnalyticsValue.Action.AccountName);
    }

    public void onEvent(ConfirmModifyProfileEvent event) {
        presenter.onChangeAccountNameAction(event.inputMessage);
    }

    @OnClick(R.id.vg_settin_account_email)
    void onClickAccountEmail() {
        Intent intent = new Intent(this, EmailChooseActivity.class);
        startActivityForResult(intent, REQUEST_EMAIL_CHOOSE);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountSetting, AnalyticsValue.Action.ChooseAnEmail);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            presenter.initializeAccountEmail();
        }
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
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void setAccountName(String name) {
        sbvAccountName.setTitle(name);
    }

    @Override
    public void setAccountEmail(String userEmail) {
        sbvAccountEmail.setTitle(userEmail);
    }

    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showChangeAccountNameSuccessToast() {
        ColoredToast.show(JandiApplication.getContext()
                .getResources().getString(R.string.jandi_success_update_account_profile));
    }

    @Override
    public void showChangeAccountNameFailToast() {
        ColoredToast.showError(JandiApplication.getContext()
                .getResources().getString(R.string.err_network));
    }
}
