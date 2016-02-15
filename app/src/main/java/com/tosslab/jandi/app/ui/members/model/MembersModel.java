package com.tosslab.jandi.app.ui.members.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */

@EBean
public class MembersModel {

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
                                    .enabled(TextUtils.equals(entity.getUser().status, "enabled"))
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
                            .enabled(TextUtils.equals(entity.getUser().status, "enabled"))
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
                            .enabled(TextUtils.equals(unjoinedEntity.getUser().status, "enabled"))
                            .owner(unjoinedEntity.isTeamOwner())
                            .name(unjoinedEntity.getName());
                })
                .filter(ChatChooseItem::isEnabled)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        return chatChooseItems;

    }

    public void kickUser(long teamId, long topicId, long userEntityId) throws RetrofitError {
        RequestApiManager.getInstance().kickUserFromTopic(teamId, topicId, new ReqMember(userEntityId));
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
        RequestApiManager.getInstance().assignToTopicOwner(teamId, entityId, new ReqOwner(memberId));
    }
}
