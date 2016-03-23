package com.tosslab.jandi.app.ui.settings.account.view;

/**
 * Created by tonyjs on 16. 3. 23..
 */
public interface SettingAccountView {

    void setAccountName(String name);

    void setAccountEmail(String userEmail);

    void showProgressWheel();

    void dismissProgressWheel();

    void showChangeAccountNameSuccessToast();

    void showChangeAccountNameFailToast();

}
