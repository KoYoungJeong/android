package com.tosslab.jandi.app.ui.invites.email.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.ui.invites.email.model.InviteByEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.InvitedEmailDataModel;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;
import com.tosslab.jandi.app.ui.invites.email.view.InviteByEmailView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 15. 6. 8..
 */
public class InviteByEmailPresenterImpl implements InviteByEmailPresenter {

    private InviteByEmailModel inviteByEmailModel;
    private InviteByEmailView inviteByEmailView;
    private InvitedEmailDataModel invitedEmailDataModel;

    @Inject
    public InviteByEmailPresenterImpl(InviteByEmailModel inviteByEmailModel,
                                      InviteByEmailView inviteByEmailView,
                                      InvitedEmailDataModel invitedEmailDataModel) {
        this.inviteByEmailModel = inviteByEmailModel;
        this.inviteByEmailView = inviteByEmailView;
        this.invitedEmailDataModel = invitedEmailDataModel;
    }

    public void onInviteListAddClick(String email) {
        EmailVO searchedEmailVO = invitedEmailDataModel.findEmailVoByEmail(email);
        if (searchedEmailVO == null) {
            if (!inviteByEmailModel.isInvitedEmail(email)) {
                if (inviteByEmailModel.isNotEnableUser(email)) {
                    inviteByEmailView.showKickedMemberFailDialog();
                } else {
                    invite(email);
                }
            } else {
                if (inviteByEmailModel.isInactivedUser(email)) {
                    inviteByEmailView.showInviteAgainDialog(email);
                } else {
                    String teamName = inviteByEmailModel.getCurrentTeamName();
                    inviteByEmailView.showAlreadyInTeamToast(teamName);
                }
            }
        } else {
            inviteByEmailView.showInviteSuccessToast();
        }

        inviteByEmailView.clearEmailInput();
    }

    @Override
    public void invite(String email) {
        EmailVO emailVO = EmailVO.create(email);
        int position = invitedEmailDataModel.add(emailVO);
        inviteByEmailView.notifyItemInserted(position);
        inviteByEmailView.moveToPosition(0);

        invite(emailVO);
    }

    private void invite(EmailVO emailVo) {
        String email = emailVo.getEmail();
        inviteByEmailModel.getInviteMemberObservable(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    int position = invitedEmailDataModel
                            .updateEmailToInviteSuccessAndGetPosition(email);

                    if (position >= 0) {
                        inviteByEmailView.notifyItemChanged(position);
                    }
                    inviteByEmailView.showSendEmailSuccessView();
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));
                    int position =
                            invitedEmailDataModel.updateEmailToInviteFailAndGetPosition(email);
                    if (position >= 0) {
                        inviteByEmailView.notifyItemChanged(position);
                    }
                    inviteByEmailView.showInviteFailToast();
                });
    }

    @Override
    public void onEmailTextChanged(String email) {
        if (!TextUtils.equals(email, email.toLowerCase())) {
            inviteByEmailView.setEmailTextView(email.toLowerCase());
            return;
        }

        boolean isValidEmail = inviteByEmailModel.isValidEmailFormat(email);
        inviteByEmailView.setEnableAddButton(isValidEmail);
    }
}
