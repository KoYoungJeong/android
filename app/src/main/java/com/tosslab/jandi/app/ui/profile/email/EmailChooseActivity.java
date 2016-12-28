package com.tosslab.jandi.app.ui.profile.email;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.DeleteEmailEvent;
import com.tosslab.jandi.app.events.profile.NewEmailEvent;
import com.tosslab.jandi.app.events.profile.RetryNewEmailEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapter;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapterViewModel;
import com.tosslab.jandi.app.ui.profile.email.dagger.DaggerEmailChooseComponent;
import com.tosslab.jandi.app.ui.profile.email.dagger.EmailChooseModule;
import com.tosslab.jandi.app.ui.profile.email.presenter.EmailChoosePresenter;
import com.tosslab.jandi.app.ui.profile.email.presenter.EmailChoosePresenterImpl;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class EmailChooseActivity extends BaseAppCompatActivity
        implements EmailChoosePresenter.View {

    @Bind(R.id.lv_email_choose)
    ListView emailListView;

    @Inject
    EmailChooseAdapterViewModel adapterViewModel;

    @Inject
    EmailChoosePresenterImpl emailChoosePresenter;

    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_choose);
        setShouldReconnectSocketService(false);
        setUpActionBar();
        ButterKnife.bind(this);

        EmailChooseAdapter emailChooseAdapter = new EmailChooseAdapter(this);
        DaggerEmailChooseComponent.builder().emailChooseModule(
                new EmailChooseModule(this, emailChooseAdapter))
                .build()
                .inject(this);
        setListView(emailChooseAdapter);

        emailChoosePresenter.getAccountEmailFromServer();

        progressWheel = new ProgressWheel(this);
        EventBus.getDefault().register(this);
        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.ChooseAnEmail);
    }

    private void setListView(EmailChooseAdapter emailChooseAdapter) {
        emailListView.setAdapter(emailChooseAdapter);

        emailListView.setOnItemClickListener((parent, view, position, id) -> {
            emailChoosePresenter.onEmailItemSelected(position);
        });

        emailListView.setOnItemLongClickListener((parent, view, position, id) -> {
            emailChoosePresenter.onEmailItemLongClicked(position);
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.email_choose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                emailChoosePresenter.setChangePrimaryEmail();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    public void onEvent(NewEmailEvent newEmailEvent) {
        emailChoosePresenter.requestNewEmail(newEmailEvent.getEmail());
    }

    public void onEvent(RetryNewEmailEvent retryNewEmailEvent) {
        emailChoosePresenter.requestNewEmail(retryNewEmailEvent.getEmail());
    }

    public void onEvent(DeleteEmailEvent deleteEmailEvent) {
        emailChoosePresenter.requestDeleteEmail(deleteEmailEvent.getEmail());
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
        if (progressWheel != null) {
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
    public void showSuccessToast(int messageResourceId) {
        ColoredToast.show(getString(messageResourceId));
    }

    @Override
    public void showFailToast(int messageResourceId) {
        ColoredToast.showError(getString(messageResourceId));
    }

    @Override
    public void showWarning(int messageResourceId) {
        ColoredToast.showWarning(getString(messageResourceId));
    }

    @Override
    public void showDeleteEmail(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(R.string.jandi_r_u_sure_to_delete_email)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) ->
                        EventBus.getDefault().post(new DeleteEmailEvent(email)))
                .create()
                .show();
    }

    @Override
    public void showNewEmailDialog() {
        EditTextDialogFragment editTextDialogFragment =
                EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_NEW_EMAIL, "");
        editTextDialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void finishWithResultOK() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void refreshListView() {
        adapterViewModel.refresh();
    }

    @Override
    public void activityFinish() {
        finish();
    }
}