package com.tosslab.jandi.app.ui.maintab.team.model;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamModel {

    private List<FormattedEntity> currentMembers;

    public List<FormattedEntity> getCurrentMembers() {
        return currentMembers;
    }

    public synchronized void setCurrentMembers(List<FormattedEntity> currentMembers) {
        this.currentMembers = currentMembers;
    }

    public Observable<Team> getTeamObservable() {
        final EntityManager entityManager = EntityManager.getInstance();
        return Observable.from(entityManager.getFormattedUsers())
                .filter(FormattedEntity::isTeamOwner)
                .firstOrDefault(EntityManager.UNKNOWN_USER_ENTITY)
                .map(ownerEntity -> {
                    long teamId = entityManager.getTeamId();
                    String teamName = entityManager.getTeamName();
                    String teamDomain = entityManager.getTeamDomain();
                    List<FormattedEntity> teamMembers = getTeamMember();

                    setCurrentMembers(teamMembers);

                    return Team.create(teamId, teamName, teamDomain, ownerEntity.getUser(), teamMembers);
                });
    }

    public List<FormattedEntity> getTeamMember() {
        List<FormattedEntity> members = new ArrayList<>();

        EntityManager entityManager = EntityManager.getInstance();
        members.addAll(entityManager.getFormattedUsers());
        if (entityManager.hasJandiBot()) {
            BotEntity botEntity = (BotEntity) entityManager.getJandiBot();
            members.add(botEntity);
        }

        Observable.from(members)
                .filter(entity -> entity instanceof BotEntity
                        || TextUtils.equals(entity.getUser().status, "enabled"))
                .toSortedList((entity, entity2) -> {
                    if (entity instanceof BotEntity) {
                        return -1;
                    } else if (entity2 instanceof BotEntity) {
                        return 1;
                    } else {
                        return entity.getName().toLowerCase()
                                .compareTo(entity2.getName().toLowerCase());
                    }
                })
                .subscribe(entities -> {
                    members.clear();
                    members.addAll(entities);
                });
        return members;
    }

    public List<FormattedEntity> getSearchedMembers(final String query) {
        List<FormattedEntity> searchedMembers = new ArrayList<>();
        List<FormattedEntity> currentMembers = getCurrentMembers();
        if (currentMembers == null || currentMembers.isEmpty()) {
            return searchedMembers;
        }

        Observable.from(currentMembers)
                .filter(member -> {
                    if (TextUtils.isEmpty(query)) {
                        return true;
                    }

                    return member.getName().toLowerCase().contains(query.toLowerCase());
                })
                .toSortedList((entity, entity2) -> {
                    if (entity instanceof BotEntity) {
                        return -1;
                    } else if (entity2 instanceof BotEntity) {
                        return 1;
                    } else {
                        return entity.getName().toLowerCase()
                                .compareTo(entity2.getName().toLowerCase());
                    }
                })
                .subscribe(searchedMembers::addAll);

        return searchedMembers;
    }

}
