package com.tosslab.jandi.app.ui.maintab.team.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamModel {

    public Observable<Team> getTeamObservable() {
        final EntityManager entityManager = EntityManager.getInstance();
        return Observable.from(entityManager.getFormattedUsers())
                .filter(FormattedEntity::isTeamOwner)
                .firstOrDefault(EntityManager.UNKNOWN_USER_ENTITY)
                .map(ownerEntity -> {
                    long teamId = entityManager.getTeamId();
                    String teamName = entityManager.getTeamName();
                    String teamDomain = getFullDomain(entityManager.getTeamDomain());

                    List<FormattedEntity> teamMembers = MembersModel.getEnabledTeamMember();
                    return Team.create(teamId, teamName, teamDomain, ownerEntity.getUser(), teamMembers);
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
