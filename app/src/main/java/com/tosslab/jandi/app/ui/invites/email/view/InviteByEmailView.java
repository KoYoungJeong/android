package com.tosslab.jandi.app.ui.invites.email.view;

import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;

/**
 * Created by tonyjs on 16. 3. 30..
 */
public interface InviteByEmailView {
    void showSendEmailSuccessView();

    void moveToPosition(int i);

    void setEmailTextView(String s);

    void setEnableAddButton(boolean isValidEmail);

    String getString(int err_invitation_failed);

    void showInviteFailToast();

    void showKickedMemberFailDialog();

    void showInviteAgainDialog(String email);

    void showAlreadyInTeamToast(String teamName);

    void notifyDataSetChanged();

    void notifyItemChanged(int position);

    void showInviteSuccessToast();

    void clearEmailInput();

    void notifyItemInserted(int position);
}
