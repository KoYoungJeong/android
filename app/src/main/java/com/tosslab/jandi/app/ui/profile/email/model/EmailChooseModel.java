package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;

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
            AccountEmail object = new AccountEmail(userEmail.getId(), TextUtils.equals(userEmail.getStatus(), "confirmed"));

            if (userEmail.isPrimary()) {
                object.setSelected(true);
            }

            accountEmails.add(object);
        }

        accountEmails.add(new AccountEmail.DummyEmail());

        return accountEmails;
    }

}
