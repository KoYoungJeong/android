package com.tosslab.jandi.app.ui.profile.account.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2016. 9. 30..
 */

public class SettingAccountProfileModel {

    private Lazy<LoginApi> loginApi;

    private Lazy<AccountProfileApi> accountProfileApi;

    @Inject
    public SettingAccountProfileModel(Lazy<LoginApi> loginApi, Lazy<AccountProfileApi> accountProfileApi) {
        this.loginApi = loginApi;
        this.accountProfileApi = accountProfileApi;
    }

    public String getName() {
        return AccountRepository.getRepository().getAccountInfo().getName();
    }

    public String getPrimaryEmail() {
        String primaryEmail = AccountRepository.getRepository().getAccountEmails().get(0).getId();
        List<ResAccountInfo.UserEmail> emails = AccountRepository.getRepository().getAccountEmails();
        for (ResAccountInfo.UserEmail email : emails) {
            if (email.isPrimary()) {
                primaryEmail = email.getId();
            }
        }
        return primaryEmail;
    }

    public String[] getAccountEmails() {
        List<ResAccountInfo.UserEmail> userEmails =
                AccountRepository.getRepository().getAccountEmails();
        Iterator<String> confirmedEmails = Observable.from(userEmails)
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .map(ResAccountInfo.UserEmail::getId)
                .toBlocking()
                .getIterator();
        List<String> emails = new ArrayList<String>();
        while (confirmedEmails.hasNext()) {
            emails.add(confirmedEmails.next());
        }
        int size = emails.size();
        String[] emailArray = new String[size];
        for (int idx = 0; idx < size; idx++) {
            emailArray[idx] = emails.get(idx);
        }
        return emailArray;
    }

    public void updateProfileEmail(String email) {
        try {
            ResAccountInfo resAccountInfo =
                    accountProfileApi.get().changePrimaryEmail(new ReqAccountEmail(email));
            AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public Observable<ResCommon> getSignOutObservable() {
        return Observable.defer(() -> {
            String deviceId = TokenUtil.getTokenObject().getDeviceId();
            // deviceId 가 없는 경우에 대한 방어코드, deviceId 가 비어 있는 경우 400 error 가 떨어짐.
            // UUID RFC4122 규격 맞춘 아무 값이나 필요
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString();
            }

            return Observable.just(deviceId);
        })
                .observeOn(Schedulers.io())
                .concatMap(deviceId -> {
                    try {
                        ResCommon resCommon = loginApi.get()
                                .deleteToken(TokenUtil.getRefreshToken(), deviceId);
                        return Observable.just(resCommon);
                    } catch (RetrofitException e) {
                        LogUtil.d(e.getCause().getMessage());
                        return Observable.error(e);
                    }
                });
    }

}
