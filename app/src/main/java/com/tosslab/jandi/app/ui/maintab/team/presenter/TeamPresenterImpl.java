package com.tosslab.jandi.app.ui.maintab.team.presenter;

import android.util.Log;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.team.model.TeamModel;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamPresenterImpl implements TeamPresenter {

    private final MemberSearchableDataModel memberSearchableDataModel;
    private final TeamModel teamModel;
    private final TeamView teamView;

    @Inject
    public TeamPresenterImpl(TeamModel teamModel,
                             MemberSearchableDataModel memberSearchableDataModel,
                             TeamView teamView) {
        this.teamModel = teamModel;
        this.memberSearchableDataModel = memberSearchableDataModel;
        this.teamView = teamView;
    }

    @Override
    public void onInitializeTeam() {
        teamView.showProgress();

        teamModel.getTeamObservable()
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team -> {
                    teamView.hideProgress();

                    teamView.initTeamInfo(team);

                    memberSearchableDataModel.clear();

                    List<User> members = team.getMembers();
                    if (members != null && !members.isEmpty()) {
                        memberSearchableDataModel.addAll(members);
                    }
                }, throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    teamView.hideProgress();
                }, teamView::notifyDataSetChanged);
    }

    @Override
    public void reInitializeTeam() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    onInitializeTeam();
                });
    }

}
