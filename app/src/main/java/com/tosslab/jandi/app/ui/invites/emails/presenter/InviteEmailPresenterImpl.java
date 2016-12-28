package com.tosslab.jandi.app.ui.invites.emails.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapterDataModel;
import com.tosslab.jandi.app.ui.invites.emails.model.InviteEmailModel;
import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrInvitationTeam;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2016. 12. 12..
 */

public class InviteEmailPresenterImpl implements InviteEmailPresenter {

    InviteEmailListAdapterDataModel adapterDataModel;

    InviteEmailModel inviteEmailmodel;

    InviteEmailPresenter.View view;
    private int invitationUserCnt = -1;

    @Inject
    public InviteEmailPresenterImpl(InviteEmailListAdapterDataModel adapterDataModel,
                                    InviteEmailModel inviteEmailmodel,
                                    View view) {
        this.adapterDataModel = adapterDataModel;
        this.inviteEmailmodel = inviteEmailmodel;
        this.view = view;
    }

    @Override
    public void addEmail(String email) {
        if (inviteEmailmodel.isValidEmailFormat(email) && !isAlreadyInsertedEmail(email)) {
            int availableCount = 0;
            for (InviteEmailVO inviteEmailVO : adapterDataModel.getItems()) {
                if (inviteEmailVO.getStatus() == InviteEmailVO.Status.AVAILABLE) {
                    availableCount++;
                }

                if (availableCount >= 10) {
                    break;
                }
            }

            if(availableCount < 10) {
                InviteEmailVO.Status status = getInviteEmailStatus(email);
                if (status != null) {
                    InviteEmailVO item = new InviteEmailVO();
                    item.setEmail(email);
                    item.setStatus(status);
                    adapterDataModel.addItem(item);
                    onInvitedUsersChanged();
                }
            } else {
                view.showDialogOver10();
            }

        }
    }

    @Override
    public void onInvitedUsersChanged() {
        invitationUserCnt = rx.Observable.from(adapterDataModel.getItems())
                .filter(vo -> vo.getStatus() == InviteEmailVO.Status.AVAILABLE)
                .count()
                .toBlocking()
                .first();
        view.changeContentInvitationButton(invitationUserCnt);
    }

    @Override
    public void onTopicSelected() {
        view.changeContentInvitationButton(invitationUserCnt);
    }

    private boolean isAlreadyInsertedEmail(String email) {
        int count = rx.Observable.from(adapterDataModel.getItems())
                .filter(vo -> vo.getEmail().equals(email))
                .count()
                .toBlocking()
                .first();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }


    private InviteEmailVO.Status getInviteEmailStatus(String email) {
        if (!TextUtils.isEmpty(email)) {
            if (!inviteEmailmodel.isInvitedEmail(email)) {
                if (inviteEmailmodel.isDisableMember(email)) {
                    return InviteEmailVO.Status.BLOCKED;
                } else {
                    return InviteEmailVO.Status.AVAILABLE;
                }
            } else {
                if (inviteEmailmodel.isInactivedUser(email)) {
                    return InviteEmailVO.Status.DUMMY;
                } else {
                    return InviteEmailVO.Status.JOINED;
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public void setStatusByEmailValid(String email) {
        if (inviteEmailmodel.isValidEmailFormat(email)) {
            view.enableAddButton(true);
        } else {
            view.enableAddButton(false);
        }
    }

    @Override
    public void startInvitationForAssociate(long selectedTopicId) {
        if (invitationUserCnt > 0) {
            if (selectedTopicId == -1) {
                view.setErrorSelectedTopic();
            } else {
                view.showProgressWheel();
                long teamId = TeamInfoLoader.getInstance().getTeamId();
                SprinklrInvitationTeam.sendLog(teamId, adapterDataModel.getItems().size(),"associate");

                Observable.defer(() -> {
                    List<ResInvitationMembers> invitationMembers =
                            inviteEmailmodel.sendInviteEmailForAssociate(
                                    adapterDataModel.getItems(), selectedTopicId);
                    return Observable.just(invitationMembers);
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resInvitations -> {
                            view.dismissProgressWheel();
                            if (checkSuccessSendingEmails(resInvitations)) {
                                view.showSuccessDialog();
                            } else {
                                view.changeInvitationButtonIfPatiallyFailed();
                                view.showPartiallyFailedDialog();
                            }
                        }, t -> {
                            view.dismissProgressWheel();
                            view.showUnkownFailedDialog();
                        });
            }
        } else {
            view.setErrorInputSelectedEmail();
        }
    }

    @Override
    public void startInvitation() {
        if (invitationUserCnt > 0) {

            long teamId = TeamInfoLoader.getInstance().getTeamId();
            SprinklrInvitationTeam.sendLog(teamId, adapterDataModel.getItems().size(),"member");

            view.showProgressWheel();
            Observable.defer(() -> {
                List<ResInvitationMembers> invitationMembers =
                        inviteEmailmodel.sendInviteEmailForMember(adapterDataModel.getItems());
                return Observable.just(invitationMembers);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resInvitations -> {
                        view.dismissProgressWheel();
                        if (checkSuccessSendingEmails(resInvitations)) {
                            view.showSuccessDialog();
                        } else {
                            view.changeInvitationButtonIfPatiallyFailed();
                            view.showPartiallyFailedDialog();
                        }
                    }, t -> {
                        view.dismissProgressWheel();
                        view.showUnkownFailedDialog();
                    });
        } else {
            view.setErrorInputSelectedEmail();
        }
    }

    private boolean checkSuccessSendingEmails(List<ResInvitationMembers> resInvitations) {
        boolean[] isSuccess = new boolean[1];
        isSuccess[0] = true;
        Observable.from(resInvitations)
                .subscribe(resInvitation -> {
                    if (!resInvitation.isSuccess()) {
                        if (isSuccess[0]) {
                            isSuccess[0] = false;
                            adapterDataModel.removeAllItems();
                        }
                        InviteEmailVO item = new InviteEmailVO();
                        item.setEmail(resInvitation.getEmail());
                        item.setStatus(getStatusByCode(resInvitation.getCode()));
                        adapterDataModel.addItem(item);
                    }
                });
        return isSuccess[0];
    }

    private InviteEmailVO.Status getStatusByCode(int code) {
        // 차단된 멤버 초대
        if (code == 40301) {
            return InviteEmailVO.Status.ACCOUNT_BLOCKED;
        }// 이미 팀 멤버
        else if (code == 40304) {
            return InviteEmailVO.Status.ACCOUNT_JOIN;
        }// 이미 초대 중인 이메일 or 이전 초대와 다른 Level로 초대
        else if (code == 40306 || code == 40023) {
            return InviteEmailVO.Status.ACCOUNT_DUMMY;
        }
        return InviteEmailVO.Status.ACCOUNT_JOIN;
    }

}