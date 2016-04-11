package com.tosslab.jandi.app.ui.invites.email.presenter;

/**
 * Created by tee on 15. 6. 8..
 */

public interface InviteByEmailPresenter {

    void onInviteListAddClick(String email);

    void onEmailTextChanged(String email);

    void invite(String email);

}
