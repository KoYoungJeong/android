package com.tosslab.jandi.app.ui.settings.account.presenter;

/**
 * Created by tonyjs on 16. 3. 23..
 */
public interface SettingAccountPresenter {

    String TAG = SettingAccountPresenter.class.getSimpleName();

    void initializeAccountName();

    void initializeAccountEmail();

    void onChangeAccountNameAction(String newName);
}
