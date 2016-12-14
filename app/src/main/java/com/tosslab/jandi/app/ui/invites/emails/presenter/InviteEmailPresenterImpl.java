package com.tosslab.jandi.app.ui.invites.emails.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapterDataModel;
import com.tosslab.jandi.app.ui.invites.emails.model.InviteEmailModel;
import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2016. 12. 12..
 */

public class InviteEmailPresenterImpl implements InviteEmailPresenter {

    @Inject
    InviteEmailListAdapterDataModel adapterDataModel;

    @Inject
    InviteEmailModel inviteEmailmodel;

    @Inject
    InviteEmailPresenter.View view;
    private int invitationUserCnt = -1;

    @Inject
    public InviteEmailPresenterImpl() {
    }

    @Override
    public void addEmail(String email) {
        if (inviteEmailmodel.isValidEmailFormat(email) && !isAlreadyInsertedEmail(email)) {
            InviteEmailVO.Status status = getInviteEmailStatus(email);
            if (status != null) {
                InviteEmailVO item = new InviteEmailVO();
                item.setEmail(email);
                item.setStatus(status);
                adapterDataModel.addItem(item);
                onInvitedUsersChanged();
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
                if (inviteEmailmodel.isNotEnableUser(email)) {
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
                Observable.defer(() -> {
                    List<ResInvitationMembers> invitationMembers =
                            inviteEmailmodel.sendInviteEmailForAssociate(
                                    adapterDataModel.getItems(), selectedTopicId);
                    return Observable.just(invitationMembers);
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resInvitations -> {
                            view.dismissProgressWheel();
                            view.finished();
                        }, t -> {
                            view.dismissProgressWheel();
                            view.finished();
                        });
            }
        } else {
            view.setErrorInputSelectedEmail();
        }
    }

    @Override
    public void startInvitation() {
        if (invitationUserCnt > 0) {
            view.showProgressWheel();
            Observable.defer(() -> {
                List<ResInvitationMembers> invitationMembers =
                        inviteEmailmodel.sendInviteEmailForMember(adapterDataModel.getItems());
                return Observable.just(invitationMembers);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resInvitations -> {
                        view.dismissProgressWheel();
                        view.finished();
                    }, t -> {
                        view.dismissProgressWheel();
                        view.finished();
                    });
        } else {
            view.setErrorInputSelectedEmail();
        }
    }
}