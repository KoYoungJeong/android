package com.tosslab.jandi.app.ui.team.create.teaminfo.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.team.create.teaminfo.InsertTeamInfoFragment;
import com.tosslab.jandi.app.ui.team.create.teaminfo.model.InsertTeamInfoModel;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrCreateTeam;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 6. 24..
 */

public class InsertTeamInfoPresenterImpl implements InsertTeamInfoPresenter {

    private InsertTeamInfoModel teamInsertInfoModel;

    private InsertTeamInfoPresenter.View view;

    @Inject
    InsertTeamInfoPresenterImpl(InsertTeamInfoModel teamInsertInfoModel, View view) {
        this.teamInsertInfoModel = teamInsertInfoModel;
        this.view = view;
    }

    @Override
    public void checkEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = teamInsertInfoModel.initUserEmailInfo();
        if (userEmails == null || userEmails.isEmpty()) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            view.finish();
        }
    }

    @Override
    public void createTeam(String teamName, String teamDomain, int mode) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            return;
        }

        if ((teamName == null || teamName.length() <= 0)) {
            view.showTeamNameLengthError();
            return;
        }

        if (!teamInsertInfoModel.isValidDomainCharacters(teamDomain)) {
            view.showTeamDomainInvalidUrlError();
            return;
        }

        if (!teamInsertInfoModel.isValidDomainLength(teamDomain)) {
            view.showTeamDomainLengthError();
            return;
        }

        Observable.fromCallable(() -> {
            ResValidation validation = teamInsertInfoModel.validDomain(teamDomain);
            if (!validation.isValidate()) {
                throw new Exception();
            }
            ResTeamDetailInfo newTeam = teamInsertInfoModel.createNewTeam(teamName, teamDomain);
            long teamId = newTeam.getInviteTeam().getTeamId();
            teamInsertInfoModel.updateTeamInfo(teamId);
            SprinklrCreateTeam.sendLog(teamId);

            return teamId;
        }).subscribeOn(Schedulers.io())
                .switchMap(teamId -> Observable.fromCallable(() -> {
                    try {
                        teamInsertInfoModel.updateEntityInfo(teamId);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                        if (e.getStatusCode() == 403) {
                            ResAccessToken resAccessToken = teamInsertInfoModel.refreshToken();
                            TokenUtil.saveTokenInfoByRefresh(resAccessToken);
                            teamInsertInfoModel.updateEntityInfo(teamId);
                        } else {
                            throw e;
                        }
                    }
                    teamInsertInfoModel.updateRank(teamId);
                    teamInsertInfoModel.updateOnlineStatus(teamId);
                    TeamInfoLoader.getInstance().refresh();

                    return teamId;
                }))
                .doOnSubscribe(() -> view.showProgressWheel())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    if (mode == InsertTeamInfoFragment.MODE_FROM_MAIN_LIST) {
                        view.onMoveInsertProfilePage();
                    } else {
                        view.onMoveMainTabActivity();
                    }
                }, e1 -> {
                    if (e1 instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) e1;
                        e.printStackTrace();
                        int errorCode = e.getStatusCode();
                        SprinklrCreateTeam.sendFailLog(e.getResponseCode());
                        if (errorCode >= 500) {
                            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
                        } else if (errorCode == 403) {
                            // 서버에서 딜레이로 인해 잘못된 처리로 판단하고 AccountHome 으로 전환
                            view.moveTeamListActivity();
                        } else {
                            view.failCreateTeam(errorCode);
                        }
                    } else {
                        view.showTeamInvalidOrSameDomainError();
                    }
                });
    }

}