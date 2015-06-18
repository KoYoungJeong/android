package com.tosslab.jandi.app.ui.invites.email.presenter;

import com.tosslab.jandi.app.ui.invites.email.adapter.InviteEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;

/**
 * Created by tee on 15. 6. 8..
 */

public interface InviteEmailPresenter {

    void onInviteListAddClick(String email);

    void onEmailTextChanged(String email);

    public interface View {
        void setEmailTextView(String email);

        void clearEmailTextView();

        void setEnableAddButton(Boolean enable);

        void showToast(String message);

        InviteEmailListAdapter getAdapter();

        void addEmailToList(EmailVO emailTO);

        void removeEmailFromList(EmailVO emailVO);

        void updateSuccessInvite(EmailVO o, int isSuccess);

        void moveToSelection(int position);
    }

}
