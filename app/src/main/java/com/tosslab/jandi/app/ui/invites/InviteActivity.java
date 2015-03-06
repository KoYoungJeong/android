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
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EActivity(R.layout.activity_invite)
public class InviteActivity extends BaseAnalyticsActivity {

    private static final Logger logger = Logger.getLogger(InviteActivity.class);

    @Bean
    InviteModel inviteModel;

    @Bean
    InvitePresenter invitePresenter;

    @AfterViews
    void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
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

    @AfterTextChange(R.id.et_invitation_email)
    void onEmailTextChange(Editable text) {

        String email = text.toString();

        if (!TextUtils.equals(email, email.toLowerCase())) {
            invitePresenter.setEmailText(email.toLowerCase());
            return;
        }

        boolean isValidEmail = inviteModel.isValidEmailFormat(email);
        invitePresenter.setEnableAddButton(isValidEmail);
    }

    @Background
    @Click(R.id.btn_invitation_confirm)
    void onInviteListAddClick() {
        String emailText = invitePresenter.getEmailText();
        if (!invitePresenter.getInvites().contains(emailText) && inviteModel.isNotMyEmail(emailText)) {
            invitePresenter.showProgressWheel();
            try {
                inviteModel.inviteMembers(Arrays.asList(emailText));
                invitePresenter.addEmailAtFirst(EmailTO.create(emailText));
            } catch (JandiNetworkException e) {
                logger.debug(e.getErrorInfo() + " : " + e.httpBody);
                invitePresenter.showErrorToast(getString(R.string.err_team_creation_failed));
            } finally {
                invitePresenter.dismissProgressWheel();

            }
        }
        invitePresenter.clearEmailTextView();

    }

}
