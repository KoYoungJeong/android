package com.tosslab.jandi.app.ui.invites;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.model.InviteModel;
import com.tosslab.jandi.app.ui.invites.to.EmailTO;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.apache.log4j.Logger;

import java.util.Arrays;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EActivity(R.layout.activity_invite)
public class InviteActivity extends BaseAnalyticsActivity {

    private static final Logger logger = Logger.getLogger(InviteActivity.class);

    @Bean
    InviteModel inviteModel;

    @Bean
    InviteView inviteView;
    private PublishSubject<EmailTO> emailSendingSubject;

    @AfterViews
    void initView() {

        setUpActionbar();

        emailSendingSubject = PublishSubject.create();

        emailSendingSubject.observeOn(Schedulers.io())
                .subscribe(new Action1<EmailTO>() {
                    @Override
                    public void call(EmailTO o) {
                        try {
                            inviteModel.inviteMembers(Arrays.asList(o.getEmail()));
                            inviteView.updateSuccessInvite(o);
                            inviteView.addSendEmailSuccessText();

                        } catch (JandiNetworkException e) {
                            inviteView.showErrorToast(getString(R.string.err_invitation_failed));
                        } catch (Exception e) {
                            inviteView.showErrorToast(getString(R.string.err_invitation_failed));
                        }
                    }
                }, throwable -> logger.debug("Email Sending Fail : " + throwable.getMessage()));
    }

    private void setUpActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }


    @Override
    protected void onResume() {
        super.onResume();
        EntityManager entityManager = EntityManager.getInstance(InviteActivity.this);
        trackGaInviteMember(entityManager.getDistictId());
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        finish();
    }

    @AfterTextChange(R.id.edit_invite_email)
    void onEmailTextChange(Editable text) {

        String email = text.toString();

        if (!TextUtils.equals(email, email.toLowerCase())) {
            inviteView.setEmailText(email.toLowerCase());
            return;
        }

        boolean isValidEmail = inviteModel.isValidEmailFormat(email);
        inviteView.setEnableAddButton(isValidEmail);
    }

    @Click(R.id.btn_invite_send)
    void onInviteListAddClick() {
        String emailText = inviteView.getEmailText();
        if (!inviteView.getInvites().contains(emailText)) {
            if (!inviteModel.isInvitedEmail(emailText)) {
                EmailTO emailTO = EmailTO.create(emailText, false);
                inviteView.addEmail(emailTO);
                inviteView.moveToSelection(0);

                emailSendingSubject.onNext(emailTO);
            } else {
                inviteView.showWarnToast(getString(R.string.jandi_duplicate_email));
            }
        } else {
            inviteView.showWarnToast(getString(R.string.jandi_invitation_succeed));
        }
        inviteView.clearEmailTextView();
    }

}
