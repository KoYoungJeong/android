package com.tosslab.jandi.app.ui.invites;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.adapter.InviteListAdapter;
import com.tosslab.jandi.app.ui.invites.model.InviteModel;
import com.tosslab.jandi.app.ui.invites.to.EmailTO;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.apache.log4j.Logger;

import java.util.List;

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

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @Override
    protected void onResume() {
        super.onResume();
        EntityManager entityManager = ((JandiApplication) getApplicationContext()).getEntityManager();
        trackGaInviteMember(entityManager.getDistictId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();

        switch (invitePresenter.getMenuStatus()) {
            case ADD:
                getMenuInflater().inflate(R.menu.invite_add, menu);
                break;
            case DELETE:
                getMenuInflater().inflate(R.menu.invite_delete, menu);
                break;
        }


        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_invitation)
    void onInviteOptionSelected() {
        List<String> invites = invitePresenter.getInvites();
        if (invites != null && !invites.isEmpty()) {
            invitePresenter.showProgressWheel();
            invite(invites);
        } else {
            // No Item Dialog
            invitePresenter.showNoEmailDialog();
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        finish();
    }

    @Background
    void invite(List<String> invites) {

        try {
            inviteModel.inviteMembers(invites);
        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody);
        }


        // invite success dialog
        invitePresenter.clearItems();
        invitePresenter.dismissProgressWheel();

        invitePresenter.showSuccessDialog();
    }

    @OptionsItem(R.id.action_delete)
    void onDeleteOptionSelected() {
        invitePresenter.deleteSelectedEmail();

        invitePresenter.setMenuStatus(InviteListAdapter.MenuStatus.ADD);
        invitePresenter.setUnselectedAll();
        invalidateOptionsMenu();
    }

    @AfterTextChange(R.id.et_invitation_email)
    void onEmailTextChange(Editable text) {

        String email = text.toString();

        if (!TextUtils.equals(email, email.toLowerCase())) {
            invitePresenter.setEmailText(email.toLowerCase());
            return ;
        }

        boolean isValidEmail = inviteModel.isValidEmailFormat(email);
        invitePresenter.setEnableAddButton(isValidEmail);
    }

    @Click(R.id.btn_invitation_confirm)
    void onInviteListAddClick() {
        String emailText = invitePresenter.getEmailText();
        if (!invitePresenter.getInvites().contains(emailText)) {
            invitePresenter.addEmailAtFirst(EmailTO.create(emailText));
        }
        invitePresenter.clearEmailTextView();

    }

    @ItemClick(R.id.lv_invite)
    void onEmailItemClick(EmailTO emailTO) {
        if (invitePresenter.getMenuStatus() == InviteListAdapter.MenuStatus.DELETE) {
            emailTO.setSelected(!emailTO.isSelected());
            invitePresenter.notifyDatasetChanged();
        }
    }

    @ItemLongClick(R.id.lv_invite)
    void onEmailItemLongClick(EmailTO emailTO) {
        InviteListAdapter.MenuStatus menuStatus = invitePresenter.getMenuStatus();

        if (menuStatus == InviteListAdapter.MenuStatus.ADD) {
            invitePresenter.setMenuStatus(InviteListAdapter.MenuStatus.DELETE);
            emailTO.setSelected(!emailTO.isSelected());
            invalidateOptionsMenu();
        } else {
            emailTO.setSelected(!emailTO.isSelected());
        }
        invitePresenter.notifyDatasetChanged();
    }

    @Override
    public void onBackPressed() {
        if (invitePresenter.getMenuStatus() == InviteListAdapter.MenuStatus.DELETE) {
            invitePresenter.setMenuStatus(InviteListAdapter.MenuStatus.ADD);
            invitePresenter.setUnselectedAll();
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }
}
