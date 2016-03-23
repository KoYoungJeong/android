package com.tosslab.jandi.app.ui.maintab.team.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;
import com.tosslab.jandi.app.utils.AccountUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamModel {

    private List<FormattedEntity> currentMembers;

    public List<FormattedEntity> getCurrentMembers() {
        return currentMembers;
    }

    public void setCurrentMembers(List<FormattedEntity> currentMembers) {
        this.currentMembers = currentMembers;
    }

    public Observable<Team> getTeamObservable() {
        final EntityManager entityManager = EntityManager.getInstance();
        return Observable.from(entityManager.getFormattedUsers())
                .subscribeOn(Schedulers.trampoline())
                .filter(FormattedEntity::isTeamOwner)
                .firstOrDefault(EntityManager.UNKNOWN_USER_ENTITY)
                .first()
                .map(ownerEntity -> {
                    long teamId = entityManager.getTeamId();
                    String teamName = entityManager.getTeamName();
                    String teamDomain = entityManager.getTeamDomain();
                    return Team.create(teamId, teamName, teamDomain, ownerEntity.getUser());
                })
                .concatMap(team -> {
                    List<FormattedEntity> teamMembers = getTeamMember();
                    team.setMembers(teamMembers);

                    setCurrentMembers(teamMembers);

                    return Observable.just(team);
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
