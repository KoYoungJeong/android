package com.tosslab.jandi.app.ui.members.model;

import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.member.MemberApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.member.MemberInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

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

public class MembersModel {
    private Lazy<RoomsApi> roomsApi;
    private Lazy<MemberApi> memberApi;

    @Inject
    public MembersModel(Lazy<RoomsApi> roomsApi, Lazy<MemberApi> memberApi) {
        this.roomsApi = roomsApi;
        this.memberApi = memberApi;
    }


    public List<ChatChooseItem> getTopicMembers(long entityId) {

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);
        Collection<Long> members = topic.getMembers();

        long myId = TeamInfoLoader.getInstance().getMyId();

        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
        Observable.from(members)
                .filter(memberId -> TeamInfoLoader.getInstance().isUser(memberId))
                .filter(memberId -> !TeamInfoLoader.getInstance().isJandiBot(memberId))
                .map(memberId -> TeamInfoLoader.getInstance().getUser(memberId))
                .map(user -> {
                    return ChatChooseItem.create(user)
                            .myId(myId == user.getId())
                            .owner(topic.getCreatorId() == user.getId());
                })
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        return chatChooseItems;
    }

    public List<ChatChooseItem> getTeamMembers() {

        List<User> formattedUsers = TeamInfoLoader.getInstance().getUserList();

        long myId = TeamInfoLoader.getInstance().getMyId();
        List<ChatChooseItem> chatChooseItems = new ArrayList<>();
        Observable.from(formattedUsers)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .map((user1) -> ChatChooseItem.create(user1)
                        .myId(myId == user1.getId()))
                .filter(ChatChooseItem::isEnabled)
                .subscribe(chatChooseItems::add, Throwable::printStackTrace);

        if (TeamInfoLoader.getInstance().hasJandiBot()) {
            User jandiBot = TeamInfoLoader.getInstance().getJandiBot();
            ChatChooseItem bot = new ChatChooseItem();
            bot.entityId(jandiBot.getId())
                    .name(jandiBot.getName())
                    .level(Level.Member)
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
        long myId = TeamInfoLoader.getInstance().getMyId();
        return Observable.from(userList)
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(user -> !topicMembers.contains(user.getId()))
                .map((user1) -> {
                    return ChatChooseItem.create(user1)
                            .myId(myId == user1.getId());
                })
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
        return TopicRepository.getInstance().removeMember(topicId, memberId);
    }

    public void assignToTopicOwner(long teamId, long entityId, long memberId) throws Exception {
        roomsApi.get().assignToTopicOwner(teamId, entityId, new ReqOwner(memberId));
    }

    public MemberInfo getMemberInfo(long teamId, long entityId) throws RetrofitException {
        return memberApi.get().getMemberInfo(teamId, entityId);
    }
}
