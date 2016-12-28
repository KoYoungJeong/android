package com.tosslab.jandi.app.ui.profile.email.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapterDataModel;
import com.tosslab.jandi.app.ui.profile.email.model.EmailChooseModel;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrChangeAccountPrimaryEmail;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrRequestVerificationEmail;

import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EmailChoosePresenterImpl implements EmailChoosePresenter {

    @Inject
    EmailChooseAdapterDataModel adapterDataModel;

    @Inject
    EmailChoosePresenter.View view;

    @Inject
    EmailChooseModel emailChooseModel;

    @Inject
    EmailChoosePresenterImpl() {
    }

    @Override
    public void requestDeleteEmail(String email) {
        view.showProgressWheel();
        Completable.fromCallable(() -> {
            ResAccountInfo resAccountInfo = emailChooseModel.requestDeleteEmail(email);
            AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());
            adapterDataModel.setAccountEmails(emailChooseModel.getAccountEmails());
            return Completable.complete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.refreshListView();
                    view.dismissProgressWheel();
                }, t -> {
                    if (t instanceof RetrofitException) {
                        t.printStackTrace();
                        view.showFailToast(R.string.err_network);
                    }
                    view.dismissProgressWheel();
                });
    }

    @Override
    public void requestNewEmail(String email) {
        if (!hasSameEmail(email)) {
            if (!emailChooseModel.isConfirmedEmail(email)) {
                view.showProgressWheel();

                Completable.fromCallable(() -> {
                    ResAccountInfo resAccountInfo = emailChooseModel.requestNewEmail(email);
                    AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());
                    SprinklrRequestVerificationEmail.sendLog(email);
                    adapterDataModel.setAccountEmails(emailChooseModel.getAccountEmails());
                    return Completable.complete();
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            view.refreshListView();
                            view.showSuccessToast(R.string.sent_auth_email);
                            view.dismissProgressWheel();
                        }, t -> {
                            t.printStackTrace();
                            if (t instanceof RetrofitException) {
                                int errorCode = ((RetrofitException) t).getResponseCode();
                                SprinklrRequestVerificationEmail.sendFailLog(errorCode);
                                int errorMessageId = R.string.err_team_creation_failed;
                                if (errorCode == 40001) {
                                    errorMessageId = R.string.err_email_exists;
                                }
                                view.showFailToast(errorMessageId);
                            }
                            view.dismissProgressWheel();
                        });
            } else {
                view.showFailToast(R.string.jandi_already_linked_email);
            }
        }
    }

    public AccountEmail getSelectedEmail() {
        int count = adapterDataModel.getCount();
        for (int idx = 0; idx < count; ++idx) {
            AccountEmail item = adapterDataModel.getItem(idx);
            if (item.isSelected()) {
                return item;
            }
        }
        return null;
    }

    public boolean hasSameEmail(String email) {
        int count = adapterDataModel.getCount();
        for (int idx = 0; idx < count; ++idx) {
            AccountEmail item = adapterDataModel.getItem(idx);
            if (TextUtils.equals(email, item.getEmail())) {
                return true;
            }
        }

        return false;
    }

    public void getAccountEmailFromServer() {
        Completable.fromCallable(() -> {
            ResAccountInfo accountInfo = emailChooseModel.getAccountEmailsFromServer();
            AccountRepository.getRepository().upsertUserEmail(accountInfo.getEmails());
            List<AccountEmail> accountEmails = emailChooseModel.getAccountEmails();
            adapterDataModel.setAccountEmails(accountEmails);
            return Completable.complete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.refreshListView();
                }, t -> {
                    t.printStackTrace();
                });
    }

    @Override
    public void setChangePrimaryEmail() {
        AccountEmail selectedAccountEmail = getSelectedEmail();
        if (selectedAccountEmail == null) {
            return;
        }
        String selectedEmail = selectedAccountEmail.getEmail();
        String originPrimaryEmail = emailChooseModel.getPrimaryEmail();
        if (!TextUtils.equals(originPrimaryEmail, selectedEmail)) {
            view.showProgressWheel();
            Completable.fromCallable(() -> {
                ResAccountInfo resAccountInfo = emailChooseModel.updatePrimaryEmail(selectedEmail);
                AccountRepository.getRepository().upsertUserEmail(resAccountInfo.getEmails());
                SprinklrChangeAccountPrimaryEmail.sendLog(emailChooseModel.getPrimaryEmail());
                return Completable.complete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        view.finishWithResultOK();
                        view.dismissProgressWheel();
                    }, t -> {
                        if (t instanceof RetrofitException) {
                            int errorCode = ((RetrofitException) t).getStatusCode();
                            SprinklrChangeAccountPrimaryEmail.trackFail(errorCode);
                            t.printStackTrace();
                            view.showFailToast(R.string.err_network);
                        }
                        view.dismissProgressWheel();
                    });
        } else {
            view.activityFinish();
        }
    }

    @Override
    public void onEmailItemSelected(int position) {
        AccountEmail clickedItem = adapterDataModel.getItem(position);
        if (!(clickedItem instanceof AccountEmail.DummyEmail)) {
            AccountEmail selectedEmail = getSelectedEmail();
            if (clickedItem.isConfirmed()) {
                if (clickedItem != selectedEmail) {
                    if (selectedEmail != null) {
                        selectedEmail.setSelected(!selectedEmail.isSelected());
                    }
                    clickedItem.setSelected(!clickedItem.isSelected());
                    view.refreshListView();
                }
            }
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseAnEmail, AnalyticsValue.Action.ChooseEmail);
        } else {
            view.showNewEmailDialog();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.ChooseAnEmail, AnalyticsValue.Action.AddNewEmail);
        }
    }

    @Override
    public void onEmailItemLongClicked(int position) {
        AccountEmail clickedItem = adapterDataModel.getItem(position);
        if (!(clickedItem instanceof AccountEmail.DummyEmail)) {
            String primaryEmail = emailChooseModel.getPrimaryEmail();
            if (!TextUtils.equals(primaryEmail, clickedItem.getEmail()) && !clickedItem.isSelected()) {
                view.showDeleteEmail(clickedItem.getEmail());
            }
        }
    }

}