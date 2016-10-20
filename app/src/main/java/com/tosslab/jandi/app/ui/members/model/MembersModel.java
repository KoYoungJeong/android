package com.tosslab.jandi.app.ui.members.model;

import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.functions.Func0;

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

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);
        Collection<Long> members = topic.getMembers();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
        Observable.from(members)
                .filter(memberId -> TeamInfoLoader.getInstance().isUser(memberId))
                .filter(memberId -> !TeamInfoLoader.getInstance().isJandiBot(memberId))
                .map(memberId -> TeamInfoLoader.getInstance().getUser(memberId))
                .map(user -> ChatChooseItem.create(user)
                        .owner(topic.getCreatorId() == user.getId()))
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        return chatChooseItems;
    }

    public List<ChatChooseItem> getTeamMembers() {

        List<User> formattedUsers = TeamInfoLoader.getInstance().getUserList();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();
        Observable.from(formattedUsers)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .map(ChatChooseItem::create)
                .filter(ChatChooseItem::isEnabled)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        if (TeamInfoLoader.getInstance().hasJandiBot()) {
            User jandiBot = TeamInfoLoader.getInstance().getJandiBot();
            ChatChooseItem bot = new ChatChooseItem();
            bot.entityId(jandiBot.getId())
                    .name(jandiBot.getName())
                    .isBot(true)
                    .enabled(jandiBot.isEnabled());
            chatChooseItems.add(bot);
        }

        return chatChooseItems;
    }

    public List<ChatChooseItem> getUnjoinedTopicMembers(long entityId) {

        TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(entityId);
        Collection<Long> topicMembers = topicRoom.getMembers();
        List<User> userList = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(userList)
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(user -> !topicMembers.contains(user.getId()))
                .map(ChatChooseItem::create)
                .collect((Func0<ArrayList<ChatChooseItem>>) ArrayList::new, ArrayList::add)
                .toBlocking()
                .firstOrDefault(new ArrayList<>());
    }

    public void kickUser(long teamId, long topicId, long userEntityId) throws RetrofitException {
        roomsApi.get().kickUserFromTopic(teamId, topicId, new ReqMember(userEntityId));
    }

    public boolean isTeamOwner() {
        return TeamInfoLoader.getInstance()
                .getUser(TeamInfoLoader.getInstance().getMyId())
                .isTeamOwner();
    }

    public boolean isMyTopic(long entityId) {
        return TeamInfoLoader.getInstance().isTopicOwner(entityId, TeamInfoLoader.getInstance().getMyId());
    }

    public boolean isTopicOwner(long topicId, long memberId) {
        return TeamInfoLoader.getInstance().isTopicOwner(topicId, memberId);
    }

    public boolean removeMember(long topicId, long memberId) {
        boolean result = TopicRepository.getInstance().removeMember(topicId, memberId);
        TeamInfoLoader.getInstance().refresh();
        return result;
    }

    public void assignToTopicOwner(long teamId, long entityId, long memberId) throws Exception {
        roomsApi.get().assignToTopicOwner(teamId, entityId, new ReqOwner(memberId));
    }

}
