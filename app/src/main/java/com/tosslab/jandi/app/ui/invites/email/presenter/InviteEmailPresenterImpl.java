package com.tosslab.jandi.app.ui.invites.email.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.invites.email.InviteEmailActivity;
import com.tosslab.jandi.app.ui.invites.email.model.InviteEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Arrays;
import java.util.List;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tee on 15. 6. 8..
 */

@EBean
public class InviteEmailPresenterImpl implements InviteEmailPresenter {

    @Bean
    InviteEmailModel inviteModel;

    @RootContext
    InviteEmailActivity view;

    private PublishSubject<EmailVO> emailSendingSubject;

    @AfterViews
    void initView() {
        initEmailSendingSubject();
    }

    public void initEmailSendingSubject() {
        emailSendingSubject = PublishSubject.create();
        emailSendingSubject.observeOn(Schedulers.io()).subscribe(object -> {
                    try {
                        inviteModel.inviteMembers(Arrays.asList(object.getEmail()));
                        view.updateSuccessInvite(object, 1);
                        view.addSendEmailSuccessText();
                    } catch (RetrofitException e) {
                        LogUtil.d("Email Sending Fail : " + e.getMessage());
                        view.removeEmailFromList(object);
                        view.showToast(view.getString(R.string.err_invitation_failed));
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.d("Email Sending Fail : " + e.getMessage());
                        view.removeEmailFromList(object);
                        view.showToast(view.getString(R.string.err_invitation_failed));
                    }
                }, throwable -> LogUtil.e("Email Sending Fail : " + throwable.getMessage())
        );
    }

    public void onInviteListAddClick(String email) {

        List<String> emailLists = view.getAdapter().getEmailLists();
        if (!emailLists.contains(email)) {
            if (!inviteModel.isInvitedEmail(email)) {
                EmailVO emailVO = EmailVO.create(email);
                view.addEmailToList(emailVO);
                view.moveToSelection(0);
                emailSendingSubject.onNext(emailVO);
            } else {
                if (inviteModel.isInactivedUser(email)) {
                    view.showSendInviteAgain(email);
                } else {
                    String teamName = inviteModel.getCurrentTeamName();

                    view.showToast(JandiApplication.getContext().getString(R.string
                            .jandi_already_in_team, teamName));
                }
            }
        } else {
            view.showToast(view.getString(R.string.jandi_invitation_succeed));
        }

        view.clearEmailTextView();
    }

    @Override
    public void invite(String email) {
        EmailVO emailVO = EmailVO.create(email);
        view.addEmailToList(emailVO);
        view.moveToSelection(0);
        emailSendingSubject.onNext(emailVO);
    }

    @Override
    public void onEmailTextChanged(String email) {
        if (!TextUtils.equals(email, email.toLowerCase())) {
            view.setEmailTextView(email.toLowerCase());
            return;
        }

        boolean isValidEmail = inviteModel.isValidEmailFormat(email);
        view.setEnableAddButton(isValidEmail);
    }
}
