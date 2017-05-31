package com.tosslab.jandi.app.local.orm.repositories.info;


import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.LastMessage;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Profile;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.TopicFolder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@org.junit.runner.RunWith(AndroidJUnit4.class)
public class IntegrationTest {

    private static String initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(InnerApiRetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void transactionOfBot() throws Exception {
        int botId = 1;
        {
            Bot bot;
            bot = new Bot();
            bot.setId(botId);
            bot.setTeamId(teamId);
            String name = "Hello";
            bot.setName(name);
            BotRepository.getInstance().addBot(bot);

            WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(botId);
            assertThat(webhookBot).isNotNull();
            assertThat(webhookBot.getName()).isEqualToIgnoringCase(name);
        }

        {
            Bot bot = new Bot();
            bot.setId(botId);
            bot.setTeamId(teamId);
            String name = "Hello2";
            bot.setName(name);
            BotRepository.getInstance().updateBot(bot);

            WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(botId);
            assertThat(webhookBot).isNotNull();
            assertThat(webhookBot.getName())
                    .isNotEqualTo("Hello")
                    .isEqualToIgnoringCase(name);

        }

        {
            BotRepository.getInstance().updateBotStatus(botId, "status");

            WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(botId);
            assertThat(webhookBot).isNotNull();
            assertThat(webhookBot.isEnabled()).isFalse();

            BotRepository.getInstance().updateBotStatus(botId, "enabled");
            webhookBot = TeamInfoLoader.getInstance().getBot(botId);
            assertThat(webhookBot).isNotNull();
            assertThat(webhookBot.isEnabled()).isTrue();
        }

        {
            BotRepository.getInstance().clear();

            WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(botId);
            assertThat(webhookBot).isNull();

        }
    }

    @Test
    public void transactionOfChat() throws Exception {

        long roomId = -1;
        List<DirectMessageRoom> rooms = TeamInfoLoader.getInstance().getDirectMessageRooms();
        for (DirectMessageRoom room : rooms) {
            if (room.isEnabled()) {
                roomId = room.getId();
                break;
            }
        }
        if (roomId <= 0) {
            fail("It cannot test");
            return;
        }


        int chatId = 1;
        {
            Chat chat = new Chat();
            chat.setId(chatId);
            chat.setStatus("active");
            chat.setReadLinkId(11);
            chat.setIsOpened(true);
            chat.setType("type");
            chat.setLastLinkId(12);
            chat.setUnreadCount(10);
            chat.setMembers(Arrays.asList(1L, 2L));
            chat.setTeamId(teamId);

            Marker marker = new Marker();
            marker.setMemberId(1);
            marker.setReadLinkId(10);

            Marker marker2 = new Marker();
            marker2.setMemberId(2);
            marker.setReadLinkId(11);

            LastMessage lastMessage = new LastMessage();
            lastMessage.setId(100);
            lastMessage.setStatus("created");
            lastMessage.setText("hello");
            chat.setLastMessage(lastMessage);

            chat.setMarkers(Arrays.asList(marker, marker2));

            ChatRepository.getInstance().addChat(chat);

            DirectMessageRoom room = TeamInfoLoader.getInstance().getChat(chatId);
            assertThat(room).isNotNull();
            assertThat(room.getUnreadCount()).isEqualTo(10);
            assertThat(room.getLastLinkId()).isEqualTo(12);
            assertThat(room.getReadLinkId()).isEqualTo(11);
            assertThat(room.getTeamId()).isEqualTo(teamId);
            assertThat(room.isJoined()).isTrue();
            assertThat(room.isEnabled()).isTrue();
            assertThat(room.getType()).isEqualTo("type");
            assertThat(room.getMarkers().size()).isEqualTo(2);

            boolean has1 = false, has2 = false;
            for (Marker marker1 : room.getMarkers()) {
                if (marker1.getMemberId() == 1) {
                    has1 = true;
                }
                if (marker1.getMemberId() == 2) {
                    has2 = true;
                }
            }

            assertThat(has1 & has2).isTrue();

            assertThat(room.getLastMessage()).isEqualTo("hello");
            assertThat(room.getLastMessageStatus()).isEqualTo("created");
            assertThat(room.getLastMessageId()).isEqualTo(100);
        }

        {
            ChatRepository.getInstance().incrementUnreadCount(chatId);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getUnreadCount())
                    .isGreaterThan(10)
                    .isEqualTo(11);
        }

        {
            ChatRepository.getInstance().updateChatOpened(chatId, false);

            assertThat(TeamInfoLoader.getInstance().getChat(chatId).isJoined()).isFalse();
        }

        {
            ChatRepository.getInstance().updateLastLinkId(chatId, 20);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getLastLinkId()).isEqualTo(20);
        }

        {
            ChatRepository.getInstance().updateLastMessage(chatId, 1000, "hahaha", "deleted");
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getLastMessageId()).isEqualTo(1000);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getLastMessage()).isEqualTo("hahaha");
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getLastMessageStatus()).isEqualTo("deleted");
        }

        {
            ChatRepository.getInstance().updateReadLinkId(chatId, -10);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getReadLinkId()).isGreaterThan(-10);
        }

        {
            ChatRepository.getInstance().updateUnreadCount(chatId, 1023);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId).getUnreadCount()).isEqualTo(1023);
        }

        {
            ChatRepository.getInstance().deleteChat(chatId);
            assertThat(TeamInfoLoader.getInstance().getChat(chatId)).isNull();
        }

    }

    @Test
    public void transactionOfFolder() throws Exception {

        long folderId = 1000;
        {
            Folder folder = new Folder();
            folder.setSeq(2);
            folder.setId(folderId);
            folder.setName("hello");
            folder.setOpened(true);

            FolderRepository.getInstance().addFolder(folder);

            TopicFolder topicFolder1 = getTopicFolder(folderId);

            assertThat(topicFolder1).isNotNull();
            assertThat(topicFolder1.getFolder()).isNotNull();
            assertThat(topicFolder1.getFolder().getName()).isEqualTo("hello");
            assertThat(topicFolder1.getFolder().getSeq()).isEqualTo(2);
            assertThat(topicFolder1.getFolder().isOpened()).isTrue();

        }

        {
            FolderRepository.getInstance().addTopic(folderId, TeamInfoLoader.getInstance().getDefaultTopicId());
            assertThat(getTopicFolder(folderId).getRooms().size()).isEqualTo(1);
            assertThat(getTopicFolder(folderId).getRooms().get(0).getId()).isEqualTo(TeamInfoLoader.getInstance().getDefaultTopicId());
        }

        {
            FolderRepository.getInstance().removeTopic(folderId, TeamInfoLoader.getInstance().getDefaultTopicId());
            assertThat(getTopicFolder(folderId).getRooms().size()).isZero();
        }

        {
            FolderRepository.getInstance().addTopic(folderId, TeamInfoLoader.getInstance().getDefaultTopicId());
            FolderRepository.getInstance().removeTopicOfTeam(teamId, Arrays.asList(TeamInfoLoader.getInstance().getDefaultTopicId()));
            assertThat(getTopicFolder(folderId).getRooms().size()).isZero();
        }

        {
            FolderRepository.getInstance().updateFolderName(folderId, "ha2");
            assertThat(getTopicFolder(folderId).getName()).isEqualToIgnoringCase("ha2");
        }

        {
            FolderRepository.getInstance().updateFolderSeq(teamId, folderId, 3);
            assertThat(getTopicFolder(folderId).getSeq()).isEqualTo(3);
        }

        {
            FolderRepository.getInstance().deleteFolder(folderId);
            assertThat(getTopicFolder(folderId)).isNull();
        }
    }

    @Test
    public void transactionOfHuman() throws Exception {
        int memberId = 1;

        {
            Human member = new Human();
            member.setTeamId(teamId);
            member.setId(memberId);
            member.setName("member");
            member.setStatus("enabled");

            HumanRepository.getInstance().addHuman(member);
            User user = TeamInfoLoader.getInstance().getUser(memberId);
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("member");
            assertThat(user.isEnabled()).isTrue();
            assertThat(user.isStarred()).isFalse();
            assertThat(user.getDivision()).isNullOrEmpty();
            assertThat(user.getPhoneNumber()).isNullOrEmpty();
            assertThat(user.getStatusMessage()).isNullOrEmpty();
            assertThat(user.getPosition()).isNullOrEmpty();
            assertThat(user.isProfileUpdated()).isFalse();
            assertThat(user.getEmail()).isNullOrEmpty();
            assertThat(user.isBot()).isFalse();


        }

        {
            Human member = new Human();
            member.setId(memberId);
            member.setTeamId(teamId);
            member.setName("member2");
            Profile profile = new Profile();
            profile.setPhoneNumber("123");
            profile.setDepartment("dept");
            profile.setPosition("pos");
            profile.setEmail("email");
            member.setProfile(profile);

            HumanRepository.getInstance().updateHuman(member);

            User user = TeamInfoLoader.getInstance().getUser(memberId);
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("member2");
            assertThat(user.getEmail()).isEqualToIgnoringCase("email");
            assertThat(user.getPhoneNumber()).isEqualToIgnoringCase("123");
            assertThat(user.getDivision()).isEqualToIgnoringCase("dept");
            assertThat(user.getPosition()).isEqualToIgnoringCase("pos");
        }

        {
            HumanRepository.getInstance().updatePhotoUrl(memberId, "photo");

            assertThat(TeamInfoLoader.getInstance().getUser(memberId).getPhotoUrl()).isEqualTo("photo");
        }

        {
            HumanRepository.getInstance().updateStarred(memberId, true);
            assertThat(TeamInfoLoader.getInstance().getUser(memberId).isStarred()).isTrue();
        }

        {
            HumanRepository.getInstance().updateStatus(memberId, "disabled");
            assertThat(TeamInfoLoader.getInstance().getUser(memberId).isEnabled()).isFalse();
        }

        {

            HumanRepository.getInstance().clear();

            assertThat(TeamInfoLoader.getInstance().getUser(memberId)).isNull();
        }
    }

    @Test
    public void transactionOfInitialInfo() throws Exception {
        InitialInfoRepository.getInstance().removeInitialInfo(teamId);
        RawInitialInfo rawInitialInfo = InitialInfoRepository.getInstance().getRawInitialInfo(teamId);
        assertThat(rawInitialInfo).isNull();
    }

    @Test
    public void transactionOfPoll() throws Exception {
        int votableCount = InitialPollInfoRepository.getInstance().getVotableCount();
        InitialPollInfoRepository.getInstance().increaseVotableCount();
        assertThat(TeamInfoLoader.getInstance().getPollBadge()).isEqualTo(votableCount + 1);

        InitialPollInfoRepository.getInstance().decreaseVotableCount();
        assertThat(TeamInfoLoader.getInstance().getPollBadge()).isEqualTo(votableCount);

        InitialPollInfoRepository.getInstance().updateVotableCount(votableCount + 10);
        assertThat(TeamInfoLoader.getInstance().getPollBadge()).isEqualTo(votableCount + 10);

    }

    @Test
    public void transactionOfRoomMarker() throws Exception {
        long chatId = 1;
        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setStatus("active");
        chat.setReadLinkId(11);
        chat.setIsOpened(true);
        chat.setType("type");
        chat.setLastLinkId(12);
        chat.setUnreadCount(10);
        chat.setMembers(Arrays.asList(1L, 2L));
        chat.setTeamId(teamId);
        ChatRepository.getInstance().addChat(chat);

        RoomMarkerRepository.getInstance().upsertRoomMarker(chatId, 1, 2);

        Collection<Marker> markers = TeamInfoLoader.getInstance().getRoom(chatId).getMarkers();
        assertThat(markers).isNotNull().hasSize(1);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(2L);

        RoomMarkerRepository.getInstance().upsertRoomMarker(1, 1, 3);
        markers = TeamInfoLoader.getInstance().getRoom(chatId).getMarkers();
        assertThat(markers).isNotNull().hasSize(1);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(3L);

        RoomMarkerRepository.getInstance().upsertRoomMarker(1, 2, 2);
        markers = TeamInfoLoader.getInstance().getRoom(chatId).getMarkers();
        assertThat(markers).isNotNull().hasSize(2);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L, 2L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(2L, 3L);

        RoomMarkerRepository.getInstance().deleteMarker(1, 1);
        markers = TeamInfoLoader.getInstance().getRoom(chatId).getMarkers();
        assertThat(markers).isNotNull().hasSize(1);
        assertThat(markers).extracting(Marker::getMemberId).doesNotContain(1L);
        assertThat(markers).extracting(Marker::getReadLinkId).doesNotContain(3L);

        RoomMarkerRepository.getInstance().deleteMarkers(1);
        markers = TeamInfoLoader.getInstance().getRoom(chatId).getMarkers();
        assertThat(markers).isNotNull().isEmpty();


        long topicId = 2;
        Topic topic = new Topic();
        topic.setId(topicId);
        chat.setTeamId(teamId);
        TopicRepository.getInstance().addTopic(topic);

        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(0);

        RoomMarkerRepository.getInstance().upsertRoomMarker(topicId, 1, 1);
        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(1);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(1L);

        RoomMarkerRepository.getInstance().upsertRoomMarker(topicId, 2, 2);
        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(2);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L, 2L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(1L, 2L);

        RoomMarkerRepository.getInstance().upsertRoomMarker(topicId, 1, 3);
        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(2);
        assertThat(markers).extracting(Marker::getMemberId).contains(1L, 2L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(2L, 3L).doesNotContain(1L);

        RoomMarkerRepository.getInstance().deleteMarker(topicId, 1);
        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(1);
        assertThat(markers).extracting(Marker::getMemberId).contains(2L);
        assertThat(markers).extracting(Marker::getReadLinkId).contains(2L);

        RoomMarkerRepository.getInstance().deleteMarkers(topicId);
        markers = TeamInfoLoader.getInstance().getRoom(topicId).getMarkers();
        assertThat(markers).hasSize(0);

    }

    @Test
    public void transactionOfTeam() throws Exception {
        Team team = new Team();
        team.setId(teamId);
        team.setName("hello");
        TeamRepository.getInstance(teamId).updateTeam(team);
        assertThat(TeamInfoLoader.getInstance().getTeamName()).isEqualTo("hello");
    }

    @Test
    public void transactionOfTopic() throws Exception {
        int topicId = 101;
        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setTeamId(teamId);
        topic.setName("hello");
        topic.setMembers(new ArrayList<>());
        topic.setUnreadCount(10);
        topic.getMembers().add(1L);
        topic.getMembers().add(2L);

        TopicRepository.getInstance().addTopic(topic);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId)).isNotNull();
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getName()).isEqualTo("hello");

        TopicRepository.getInstance().incrementUnreadCount(topicId);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getUnreadCount()).isEqualTo(11);

        TopicRepository.getInstance().addMember(topicId, Arrays.asList(1L));
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getMemberCount()).isEqualTo(2);

        TopicRepository.getInstance().addMember(topicId, Arrays.asList(3L));
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getMemberCount()).isEqualTo(3);

        TopicRepository.getInstance().removeMember(topicId, 3L);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getMemberCount()).isEqualTo(2);

        Announcement announcement = new Announcement();
        announcement.setIsOpened(true);
        announcement.setContent("content");
        TopicRepository.getInstance().createAnnounce(topicId, announcement);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement()).isNotNull();
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement().isOpened()).isTrue();
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement().getContent()).isEqualTo("content");

        TopicRepository.getInstance().updateAnnounceOpened(topicId, false);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement().isOpened()).isFalse();

        TopicRepository.getInstance().removeAnnounce(topicId);
        assertThat(TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement()).isNull();

    }

    @Test
    public void multiThread() throws Exception {


        TestSubscriber<User> subscriber = TestSubscriber.create();
        Observable.just(TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()))
                .observeOn(Schedulers.computation())
                .doOnNext(it -> {
                    assertThat(it.getEmail()).isNotNull();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> {
                    assertThat(it.getEmail()).isNotNull();
                })
                .doOnError(throwable -> fail("It cannot be happened"))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
    }

    @Nullable
    private TopicFolder getTopicFolder(long folderId) {
        TopicFolder topicFolder1 = null;
        for (TopicFolder topicFolder : TeamInfoLoader.getInstance().getTopicFolders()) {
            if (topicFolder.getId() == folderId) {
                topicFolder1 = topicFolder;
                break;
            }
        }
        return topicFolder1;
    }
}
