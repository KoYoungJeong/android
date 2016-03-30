package com.tosslab.jandi.app.ui.invites.email.presenter;

import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;

/**
 * Created by tee on 15. 6. 8..
 */

public interface InviteByEmailPresenter {

    void onInviteListAddClick(String email);

    void onEmailTextChanged(String email);

    void invite(String email);

}
