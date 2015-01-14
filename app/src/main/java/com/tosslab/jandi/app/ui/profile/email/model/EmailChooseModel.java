package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EBean
public class EmailChooseModel {

    @RootContext
    Context context;

    public List<AccountEmail> getAccountEmails() {

        List<AccountEmail> accountEmails = new ArrayList<AccountEmail>();

        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {

            if (!TextUtils.equals(userEmail.getStatus(), "confirmed")) {
                continue;
            }

            AccountEmail object = new AccountEmail(userEmail.getId(), TextUtils.equals(userEmail.getStatus(), "confirmed"));

            if (userEmail.isPrimary()) {
                object.setSelected(true);
            }

            accountEmails.add(object);
        }

        accountEmails.add(new AccountEmail.DummyEmail());

        return accountEmails;
    }

    public ResAccountInfo requestNewEmail(String email) throws JandiNetworkException {
        return RequestManager.newInstance(context, EmailAddRequest.create(context, email)).request();
    }

    public boolean isConfirmedEmail(String email) {

        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(email, userEmail.getId())) {
                return TextUtils.equals(userEmail.getStatus(), "confirmed");
            }
        }

        return false;
    }

    public String getPrimaryEmail() {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail.getId();
            }
        }
        return "";

    }

    public ResAccountInfo requestDeleteEmail(String email) throws JandiNetworkException {
        return RequestManager.newInstance(context, EmailDeleteRequest.create(context, email)).request();
    }

    public ResAccountInfo getAccountEmailsFromServer() throws JandiNetworkException {
        return RequestManager.newInstance(context, AccountInfoRequest.create(context)).request();

    }

    public ResAccountInfo updatePrimaryEmail(String selectedEmail) throws JandiNetworkException {
        return RequestManager.newInstance(context, EmailChooseRequest.create(context, selectedEmail)).request();
    }
}
