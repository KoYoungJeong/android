package com.tosslab.jandi.app.ui.profile.account;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.profile.account.dagger.DaggerSettingAccountProfileComponent;
import com.tosslab.jandi.app.ui.profile.account.dagger.SettingAccountProfileModule;
import com.tosslab.jandi.app.ui.profile.account.presenter.SettingAccountProfilePresenter;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.NameStatusActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignOut;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by tee on 2016. 9. 30..
 */

public class SettingAccountProfileActivity extends BaseAppCompatActivity implements
        SettingAccountProfilePresenter.View {

    @Inject
    SettingAccountProfilePresenter presenter;

    @Bind(R.id.tv_edit_account_name)
    TextView tvEditAccountName;

    @Bind(R.id.tv_edit_primary_email)
    TextView tvEditPrimaryEmail;

    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_main_account_info);
        setUpActionBar();
        DaggerSettingAccountProfileComponent.builder()
                .settingAccountProfileModule(new SettingAccountProfileModule(this))
                .build()
                .inject(this);
        ButterKnife.bind(this);
        progressWheel = new ProgressWheel(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setAccountName();
        presenter.setPrimaryEmail();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setTitle(R.string.common_accthome_jandiacct);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @Override
    public void setName(String name) {
        tvEditAccountName.setText(name);
    }

    @Override
    public void setPrimaryEmail(String email) {
        tvEditPrimaryEmail.setText(email);
    }

    @OnClick(R.id.vg_edit_account_name)
    void onClickEditAccountName() {
        startActivity(Henson.with(this)
                .gotoNameStatusActivity()
                .type(NameStatusActivity.EXTRA_TYPE_NAME_FOR_MAIN_ACCOUNT)
                .build());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditAccount, AnalyticsValue.Action.AccountName);
    }

    @OnClick(R.id.vg_edit_primary_email)
    void onClickEditPrimaryEmail() {
        presenter.onEmailChoose();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditAccount, AnalyticsValue.Action.ChooseAnEmail);
    }

    @OnClick(R.id.vg_log_out)
    void onClickLogOut() {
        showSignOutDialog();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.EditAccount, AnalyticsValue.Action.SignOut);
    }

    @Override
    public void showEmailChooseDialog(String[] emails) {
        int checkedIdx = 0;
        for (int idx = 0; idx < emails.length; idx++) {
            if (TextUtils.equals(emails[idx], tvEditPrimaryEmail.getText())) {
                checkedIdx = idx;
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_choose_email)
                .setSingleChoiceItems(emails, checkedIdx, null)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    int checkedItemPosition =
                            ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String primaryEmail = emails[checkedItemPosition];
                    presenter.updatePrimaryEmail(primaryEmail);
                })
                .create().show();
    }

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out,
                        (dialog, which) -> {
                            presenter.onSignOutAction();
                            SprinklrSignOut.sendLog();
                            AnalyticsUtil.flushSprinkler();
                        })
                .create().show();
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void moveLoginActivity() {
        IntroActivity.startActivity(this, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
