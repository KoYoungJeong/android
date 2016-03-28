package com.tosslab.jandi.app.ui.settings.account.model;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import retrofit.RetrofitError;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 23..
 */
public class SettingAccountModel {

    public Observable<ResAccountInfo> getAccountInfoObservable() {
        return Observable.just(AccountRepository.getRepository().getAccountInfo())
                .subscribeOn(Schedulers.io());
    }

    public Observable<ResAccountInfo.UserEmail> getAccountEmailObservable() {
        return Observable.from(AccountRepository.getRepository().getAccountEmails())
                .subscribeOn(Schedulers.io())
                .filter(ResAccountInfo.UserEmail::isPrimary)
                .firstOrDefault(new ResAccountInfo.UserEmail());
    }

    public Observable<ResAccountInfo> getUpdateAccountNameObservable(final String newName) {
        return Observable.<ResAccountInfo>create(subscriber -> {
            try {
                ReqProfileName reqProfileName = new ReqProfileName(newName);
                ResAccountInfo resAccountInfo =
                        RequestApiManager.getInstance().changeNameByAccountProfileApi(reqProfileName);
                subscriber.onNext(resAccountInfo);
            } catch (RetrofitError error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public Observable<String> getUpdateAccountRepositoryObservable(String newName) {
        return Observable.just(newName)
                .subscribeOn(Schedulers.io())
                .map(name -> {
                    AccountRepository.getRepository().updateAccountName(name);
                    return name;
                });
    }
}
