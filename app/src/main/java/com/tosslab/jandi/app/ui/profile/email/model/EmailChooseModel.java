package com.tosslab.jandi.app.ui.profile.email.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.utils.LanguageUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EBean
public class EmailChooseModel {

    @RootContext
    Context context;

    public List<AccountEmail> getAccountEmails() {

        List<AccountEmail> accountEmails = new ArrayList<AccountEmail>();

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

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

    public ResAccountInfo requestNewEmail(String email) throws RetrofitError {
        ReqAccountEmail reqAccountEmail = new ReqAccountEmail(email, LanguageUtil.getLanguage(context.getApplicationContext()));
        return RequestApiManager.getInstance().requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    public boolean isConfirmedEmail(String email) {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(email, userEmail.getId())) {
                return TextUtils.equals(userEmail.getStatus(), "confirmed");
            }
        }

        return false;
    }

    public String getPrimaryEmail() {
        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail.getId();
            }
        }
        return "";

    }

    public ResAccountInfo requestDeleteEmail(String email) throws RetrofitError {
        ReqAccountEmail reqAccountEmail = new ReqAccountEmail(email, LanguageUtil.getLanguage(context.getApplicationContext()));
        return RequestApiManager.getInstance().deleteEmailByAccountEmailApi(reqAccountEmail);
    }

    public ResAccountInfo getAccountEmailsFromServer() throws RetrofitError {
        return RequestApiManager.getInstance().getAccountInfoByMainRest();

    }

    public ResAccountInfo updatePrimaryEmail(String selectedEmail) throws RetrofitError {
        return RequestApiManager.getInstance().updatePrimaryEmailByMainRest(new ReqUpdatePrimaryEmailInfo(selectedEmail));
    }

}
