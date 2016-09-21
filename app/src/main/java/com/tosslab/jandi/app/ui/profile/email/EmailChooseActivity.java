package com.tosslab.jandi.app.ui.profile.email;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.DeleteEmailEvent;
import com.tosslab.jandi.app.events.profile.NewEmailEvent;
import com.tosslab.jandi.app.events.profile.RetryNewEmailEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.email.model.EmailChooseModel;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrChangeAccountPrimaryEmail;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrRequestVerificationEmail;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EActivity(R.layout.activity_email_choose)
@OptionsMenu(R.menu.email_choose)
public class EmailChooseActivity extends BaseAppCompatActivity {

    @Bean
    EmailChoosePresenter emailChoosePresenter;

    @Bean
    EmailChooseModel emailChooseModel;

    @AfterViews
    void initView() {

        setUpActionBar();

        List<AccountEmail> accountEmails = emailChooseModel.getAccountEmails();
        emailChoosePresenter.setEmails(accountEmails);

        getAccountEmailFromServer();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.ChooseAnEmail);
    }

    @OptionsItem(R.id.action_confirm)
    void onOkOptionSelected() {
        AccountEmail selectedAccountEmail = emailChoosePresenter.getSelectedEmail();

        if (selectedAccountEmail == null) {
            return;
        }

        String selectedEmail = selectedAccountEmail.getEmail();
        String originPrimaryEmail = emailChooseModel.getPrimaryEmail();
        if (!TextUtils.equals(originPrimaryEmail, selectedEmail)) {
            requestChangePrimaryEmail(selectedEmail);
        } else {
            finish();
        }
    }

    @Background
    void requestChangePrimaryEmail(String selectedEmail) {
        emailChoosePresenter.showProgressWheel();

        try {
            ResAccountInfo resAccountInfo = emailChooseModel.updatePrimaryEmail(selectedEmail);
            AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());

            SprinklrChangeAccountPrimaryEmail.sendLog(emailChooseModel.getPrimaryEmail());

            emailChoosePresenter.finishWithResultOK();
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            SprinklrChangeAccountPrimaryEmail.trackFail(errorCode);
            e.printStackTrace();
            emailChoosePresenter.showFailToast(getString(R.string.err_network));
        } finally {
            emailChoosePresenter.dismissProgressWheel();
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        finish();
    }


    @Background
    void getAccountEmailFromServer() {
        try {
            ResAccountInfo accountInfo = emailChooseModel.getAccountEmailsFromServer();
            AccountRepository.getRepository().upsertUserEmail(accountInfo.getEmails());
            List<AccountEmail> accountEmails = emailChooseModel.getAccountEmails();
            emailChoosePresenter.refreshEmails(accountEmails);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldReconnectSocketService(false);
        EventBus.getDefault().register(this);
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

    @ItemClick(R.id.lv_email_choose)
    void onEmailItemClick(AccountEmail clickedItem) {

        if (!(clickedItem instanceof AccountEmail.DummyEmail)) {

            AccountEmail selectedEmail = emailChoosePresenter.getSelectedEmail();

            if (clickedItem.isConfirmed()) {

                if (selectedEmail != null && clickedItem != selectedEmail) {
                    selectedEmail.setSelected(!selectedEmail.isSelected());
                    clickedItem.setSelected(!clickedItem.isSelected());
                    emailChoosePresenter.refreshListView();
                }

            } else {
                // non-case...
                emailChoosePresenter.showRetryEmailDialog(selectedEmail.getEmail());
            }

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseAnEmail, AnalyticsValue.Action.ChooseEmail);
        } else {
            emailChoosePresenter.showNewEmailDialog();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseAnEmail, AnalyticsValue.Action.AddNewEmail);
        }

    }

    @ItemLongClick(R.id.lv_email_choose)
    void onEmailItemLongClick(AccountEmail accountEmail) {
        if (!(accountEmail instanceof AccountEmail.DummyEmail)) {
            String primaryEmail = emailChooseModel.getPrimaryEmail();
            if (!TextUtils.equals(primaryEmail, accountEmail.getEmail()) && !accountEmail.isSelected()) {
                emailChoosePresenter.showDeleteEmail(accountEmail.getEmail());
            }
        }
    }

    public void onEvent(NewEmailEvent newEmailEvent) {
        if (!emailChoosePresenter.hasSameEmail(newEmailEvent.getEmail())) {
            if (!emailChooseModel.isConfirmedEmail(newEmailEvent.getEmail())) {
                requestNewEmail(newEmailEvent.getEmail());
            }
        } else {
            emailChoosePresenter.showFailToast(getString(R.string.jandi_already_linked_email));
        }
    }

    public void onEvent(RetryNewEmailEvent retryNewEmailEvent) {
        requestNewEmail(retryNewEmailEvent.getEmail());
    }

    public void onEvent(DeleteEmailEvent deleteEmailEvent) {
        requestDeleteEmail(deleteEmailEvent.getEmail());
    }

    @Background
    void requestDeleteEmail(String email) {
        emailChoosePresenter.showProgressWheel();

        try {
            ResAccountInfo resAccountInfo = emailChooseModel.requestDeleteEmail(email);
            AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());
            emailChoosePresenter.refreshEmails(emailChooseModel.getAccountEmails());
        } catch (RetrofitException e) {
            e.printStackTrace();
            emailChoosePresenter.showFailToast(getString(R.string.err_network));
        } finally {
            emailChoosePresenter.dismissProgressWheel();
        }
    }

    @Background
    void requestNewEmail(String email) {
        emailChoosePresenter.showProgressWheel();
        try {
            ResAccountInfo resAccountInfo = emailChooseModel.requestNewEmail(email);
            AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());

            SprinklrRequestVerificationEmail.sendLog(email);

            emailChoosePresenter.refreshEmails(emailChooseModel.getAccountEmails());
            emailChoosePresenter.showSuccessToast(getString(R.string.sent_auth_email));
        } catch (RetrofitException e) {
            e.printStackTrace();

            int errorCode = e.getResponseCode();
            SprinklrRequestVerificationEmail.sendFailLog(errorCode);

            String errorMessage = getString(R.string.err_team_creation_failed);
            if (errorCode == 40001) {
                errorMessage = getString(R.string.err_email_exists);
            }
            emailChoosePresenter.showFailToast(errorMessage);
        } finally {
            emailChoosePresenter.dismissProgressWheel();
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
