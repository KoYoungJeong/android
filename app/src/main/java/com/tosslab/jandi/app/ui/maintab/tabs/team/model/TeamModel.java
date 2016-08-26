package com.tosslab.jandi.app.ui.maintab.tabs.team.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.tabs.team.vo.Team;
import com.tosslab.jandi.app.ui.members.model.MembersModel;

import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamModel {

    public Observable<Team> getTeamObservable() {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isTeamOwner)
                .map(user -> {
                    long teamId = TeamInfoLoader.getInstance().getTeamId();
                    String teamName = TeamInfoLoader.getInstance().getTeamName();
                    String teamDomain = getFullDomain(TeamInfoLoader.getInstance().getTeamDomain());

                    List<User> teamMembers = MembersModel.getEnabledTeamMember();
                    return Team.create(teamId, teamName, teamDomain, user, teamMembers);
                });
    }

    public String getFullDomain(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return "";
        }

        if (domain.contains(".jandi.com")) {
            return domain;
        }

        return domain + ".jandi.com";
    }

}
