package com.tosslab.jandi.app.ui.members.model;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */

@EBean
public class MembersModel {
    @Inject
    Lazy<RoomsApi> roomsApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public List<ChatChooseItem> getTopicMembers(long entityId) {
        final EntityManager entityManager = EntityManager.getInstance();

        Collection<Long> members = entityManager.getEntityById(entityId).getMembers();
        List<FormattedEntity> formattedUsers = entityManager.getFormattedUsers();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
        Observable.from(members)
                .map(memberEntityId -> Observable.from(formattedUsers)
                        .filter(entity -> entity.getId() == memberEntityId)
                        .map(entity -> {

                            ChatChooseItem chatChooseItem = new ChatChooseItem();
                            return chatChooseItem.entityId(entity.getId())
                                    .statusMessage(entity.getUserStatusMessage())
                                    .photoUrl(entity.getUserLargeProfileUrl())
                                    .starred(entity.isStarred)
                                    .enabled(entity.isEnabled())
                                    .inactive(entity.isInavtived())
                                    .email(entity.getUserEmail())
                                    .owner(entityManager.isTopicOwner(entityId, entity.getId()))
                                    .name(entity.getName());

                        })
                        .toBlocking()
                        .firstOrDefault(new ChatChooseItem().entityId(-1)))
                .filter(chatChooseItem -> chatChooseItem.getEntityId() != -1)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        return chatChooseItems;
    }

    public List<ChatChooseItem> getTeamMembers() {

        List<FormattedEntity> formattedUsers = EntityManager.getInstance().getFormattedUsers();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();
        Observable.from(formattedUsers)
                .map(entity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();
                    return chatChooseItem.entityId(entity.getId())
                            .statusMessage(entity.getUserStatusMessage())
                            .photoUrl(entity.getUserLargeProfileUrl())
                            .starred(entity.isStarred)
                            .enabled(entity.isEnabled())
                            .inactive(entity.isInavtived())
                            .email(entity.getUserEmail())
                            .owner(entity.isTeamOwner())
                            .name(entity.getName());
                })
                .filter(ChatChooseItem::isEnabled)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        if (EntityManager.getInstance().hasJandiBot()) {
            BotEntity botEntity = (BotEntity) EntityManager.getInstance().getJandiBot();
            ChatChooseItem bot = new ChatChooseItem();
            bot.entityId(botEntity.getId())
                    .name(botEntity.getName())
                    .isBot(true)
                    .enabled(botEntity.isEnabled());
            chatChooseItems.add(bot);
        }

        return chatChooseItems;
    }

    public List<ChatChooseItem> getUnjoinedTopicMembers(long entityId) {

        EntityManager entityManager = EntityManager.getInstance();

        FormattedEntity entity = entityManager.getEntityById(entityId);

        int entityType = entity.isPublicTopic()
                ? JandiConstants.TYPE_PUBLIC_TOPIC
                : entity.isPrivateGroup()
                ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;

        List<FormattedEntity> unjoinedMembersOfEntity = entityManager.getUnjoinedMembersOfEntity(entityId, entityType);

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        Observable.from(unjoinedMembersOfEntity)
                .map(unjoinedEntity -> {
                    ChatChooseItem chatChooseItem = new ChatChooseItem();
                    return chatChooseItem.entityId(unjoinedEntity.getId())
                            .statusMessage(unjoinedEntity.getUserStatusMessage())
                            .photoUrl(unjoinedEntity.getUserLargeProfileUrl())
                            .starred(unjoinedEntity.isStarred)
                            .enabled(unjoinedEntity.isEnabled())
                            .inactive(unjoinedEntity.isInavtived())
                            .email(unjoinedEntity.getUserEmail())
                            .owner(unjoinedEntity.isTeamOwner())
                            .name(unjoinedEntity.getName());
                })
                .filter(ChatChooseItem::isEnabled)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        return chatChooseItems;

    }

    public void kickUser(long teamId, long topicId, long userEntityId) throws RetrofitException {
        roomsApi.get().kickUserFromTopic(teamId, topicId, new ReqMember(userEntityId));
    }

    public boolean isTeamOwner() {
        return EntityManager.getInstance().getMe().isTeamOwner();
    }

    public boolean isMyTopic(long entityId) {
        return EntityManager.getInstance().isMyTopic(entityId);
    }

    public boolean isTopicOwner(long topicId, long memberId) {
        return EntityManager.getInstance().isTopicOwner(topicId, memberId);
    }

    public boolean removeMember(long topicId, long userEntityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(topicId);
        if (entity.isPublicTopic()) {
            return entity.getChannel().ch_members.remove(userEntityId);

        } else if (entity.isPrivateGroup()) {
            return entity.getPrivateGroup().pg_members.remove(userEntityId);
        }

        return false;
    }

    public void assignToTopicOwner(long teamId, long entityId, long memberId) throws Exception {
        roomsApi.get().assignToTopicOwner(teamId, entityId, new ReqOwner(memberId));
    }


}
