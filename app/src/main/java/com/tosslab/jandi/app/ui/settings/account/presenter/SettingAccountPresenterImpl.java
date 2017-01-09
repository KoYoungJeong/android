package com.tosslab.jandi.app.ui.settings.account.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.settings.account.model.SettingAccountModel;
import com.tosslab.jandi.app.ui.settings.account.view.SettingAccountView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 23..
 */
public class SettingAccountPresenterImpl implements SettingAccountPresenter {

    private final SettingAccountModel model;
    private final SettingAccountView view;

    public SettingAccountPresenterImpl(SettingAccountModel model, SettingAccountView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void initializeAccountName() {
        model.getAccountInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resAccountInfo -> {
                    view.setAccountName(resAccountInfo.getName());
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                });
    }

    @Override
    public void initializeAccountEmail() {
        model.getAccountEmailObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userEmail -> {
                    if (!TextUtils.isEmpty(userEmail.getEmail())) {
                        view.setAccountEmail(userEmail.getEmail());
                    }
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                });
    }

    @Override
    public void onChangeAccountNameAction(String newName) {
        view.showProgressWheel();

        model.getUpdateAccountNameObservable(newName)
                .concatMap(resAccountInfo -> model.getUpdateAccountRepositoryObservable(newName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> {
                    view.dismissProgressWheel();

                    view.showChangeAccountNameSuccessToast();

                    view.setAccountName(name);
                }, throwable -> {
                    view.dismissProgressWheel();
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));

                    if (throwable instanceof RetrofitException) {
                        if (((RetrofitException) throwable).getStatusCode() >= 500) {
                            view.showChangeAccountNameFailToast();
                        }
                    }

                });

    }
}
