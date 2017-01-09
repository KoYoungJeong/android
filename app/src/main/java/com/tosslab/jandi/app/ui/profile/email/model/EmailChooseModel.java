package com.tosslab.jandi.app.ui.profile.email.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.utils.LanguageUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EBean
public class EmailChooseModel {


    public List<AccountEmail> getAccountEmails() {

        List<AccountEmail> accountEmails = new ArrayList<>();

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {

            if (!TextUtils.equals(userEmail.getStatus(), "confirmed")) {
                continue;
            }

            AccountEmail object = new AccountEmail(userEmail.getEmail(), true);

            if (userEmail.isPrimary()) {
                object.setSelected(true);
            }

            accountEmails.add(object);
        }

        accountEmails.add(new AccountEmail.DummyEmail());

        return accountEmails;
    }

    public ResAccountInfo requestNewEmail(String email) throws RetrofitException {
        ReqAccountEmail reqAccountEmail = new ReqAccountEmail(email, LanguageUtil.getLanguage());
        return new AccountEmailsApi(RetrofitBuilder.getInstance()).requestAddEmail(reqAccountEmail);
    }

    public boolean isConfirmedEmail(String email) {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(email, userEmail.getEmail())) {
                return TextUtils.equals(userEmail.getStatus(), "confirmed");
            }
        }

        return false;
    }

    public String getPrimaryEmail() {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail.getEmail();
            }
        }
        return "";

    }

    public ResAccountInfo requestDeleteEmail(String email) throws RetrofitException {
        ReqAccountEmail reqAccountEmail = new ReqAccountEmail(email, LanguageUtil.getLanguage());
        return new AccountEmailsApi(RetrofitBuilder.getInstance()).deleteEmail(reqAccountEmail);
    }

    public ResAccountInfo getAccountEmailsFromServer() throws RetrofitException {
        return new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();

    }

    public ResAccountInfo updatePrimaryEmail(String selectedEmail) throws RetrofitException {
        return new AccountApi(RetrofitBuilder.getInstance()).updatePrimaryEmail(new ReqUpdatePrimaryEmailInfo(selectedEmail));
    }

}
