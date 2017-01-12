package com.tosslab.jandi.app.services.socket;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.BotRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.local.orm.repositories.socket.SocketEventRepository;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.domain.SocketStart;
import com.tosslab.jandi.app.services.socket.annotations.Version;
import com.tosslab.jandi.app.services.socket.model.SocketModelExtractor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCloseEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketConnectBotUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileShareEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamJoinEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderItemCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderItemDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicInvitedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicJoinedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicKickedoutEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicLeftEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicUpdatedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;

import dagger.Component;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(AndroidJUnit4.class)
public class JandiSocketServiceModelTest {

    private static long teamId;
    private static String initializeInfo;
    @Inject
    JandiSocketServiceModel model;

    @Inject
    Lazy<ChannelMessageApi> channelMessageApi;

    private boolean accept;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        DaggerJandiSocketServiceModelTest_JandiSocketServiceModelTestComponent.builder()
                .build()
                .inject(JandiSocketServiceModelTest.this);
        accept = false;

        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();

        model.eventPublisher.subscribe(o -> {
            EventBus.getDefault().post(o);
        });
    }

    @Test
    public void testGetStartInfo() throws Exception {
        SocketStart startInfo = model.getStartInfo();
        assertThat(startInfo).isNotNull();
        assertThat(startInfo.getToken()).isEqualToIgnoringCase(TokenUtil.getAccessToken());

    }

    @Test
    public void testOnFileDeleted() throws Exception {
        final Pair<Long, Long>[] longLongPair = new Pair[1];
        register((DeleteFileEvent event) -> {
            longLongPair[0] = Pair.create(event.getTeamId(), event.getId());
        });
        SocketFileDeletedEvent event = createEvent(SocketFileDeletedEvent.class);
        SocketFileDeletedEvent.EventFileInfo file = new SocketFileDeletedEvent.EventFileInfo();
        int fileId = 1;
        file.setId(fileId);
        event.setFile(file);
        model.onFileDeleted(event);

        assertThat(longLongPair[0].first).isEqualTo(teamId);
        assertThat(longLongPair[0].second).isEqualTo(fileId);
        assertThat(SocketEventRepository.getInstance().hasEvent(event)).isTrue();
    }

    @Test
    public void testOnFileCommentCreated() throws Exception {
        register((FileCommentRefreshEvent event) -> accept = true);
        SocketFileCommentCreatedEvent event = createEvent(SocketFileCommentCreatedEvent.class);
        event.setFile(new SocketFileCommentCreatedEvent.EventFileInfo());
        event.setEvent("file_comment_created");
        event.setComment(new SocketFileCommentCreatedEvent.EventCommentInfo());
        model.onFileCommentCreated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnFileCommentDeleted() throws Exception {

        register((FileCommentRefreshEvent event) -> accept = true);

        SocketFileCommentDeletedEvent event = createEvent(SocketFileCommentDeletedEvent.class);
        event.setFile(new SocketFileCommentDeletedEvent.EventFileInfo());
        event.setRooms(new ArrayList<>());
        event.setComment(new SocketFileCommentDeletedEvent.EventCommentInfo());
        model.onFileCommentDeleted(event);

        assertThat(accept).isTrue();

    }

    @Test
    public void testOnMessageDeleted() throws Exception {

        final SocketMessageDeletedEvent[] acceptEvent = new SocketMessageDeletedEvent[1];
        register((SocketMessageDeletedEvent event) -> {
            accept = true;
            acceptEvent[0] = event;
        });
        SocketMessageDeletedEvent event = createEvent(SocketMessageDeletedEvent.class);
        SocketMessageDeletedEvent.Data data = new SocketMessageDeletedEvent.Data();
        data.setLinkId(1);
        data.setMessageId(2);
        data.setRoomId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setData(data);
        model.onMessageDeleted(event);

        assertThat(accept).isTrue();
        assertThat(acceptEvent[0]).isEqualTo(event);
    }

    @Test
    public void testOnTopicUpdated() throws Exception {

        final long[] acceptId = new long[1];
        register((TopicInfoUpdateEvent event) -> {
            accept = true;
            acceptId[0] = event.getId();
        });

        SocketTopicUpdatedEvent event = createEvent(SocketTopicUpdatedEvent.class);
        SocketTopicUpdatedEvent.Data data = new SocketTopicUpdatedEvent.Data();
        Topic topic = TopicRepository.getInstance().getTopic(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setTopic(topic);
        event.setData(data);
        model.onTopicUpdated(event);

        assertThat(accept).isTrue();
        assertThat(acceptId[0]).isEqualTo(topic.getId());

    }

    @Test
    public void testOnChatClosed() throws Exception {
        register((ChatListRefreshEvent event) -> {
            accept = true;
        });
        SocketChatCloseEvent event = createEvent(SocketChatCloseEvent.class);
        SocketChatCloseEvent.Data data = new SocketChatCloseEvent.Data();
        data.setChatId(TeamInfoLoader.getInstance().getChatId(TeamInfoLoader.getInstance().getJandiBot().getId()));
        event.setData(data);
        model.onChatClosed(event);

        assertThat(accept).isTrue();
        assertThat(TeamInfoLoader.getInstance().getRoom(TeamInfoLoader.getInstance().getChatId(TeamInfoLoader.getInstance().getJandiBot().getId())).isJoined())
                .isFalse();

    }

    @Test
    public void testOnChatCreated() throws Exception {
        register((ChatListRefreshEvent event) -> {
            accept = true;
        });

        SocketChatCreatedEvent event = getChatCreatedObject(teamId);
        model.onChatCreated(event);

        assertThat(accept).isTrue();
        assertThat(ChatRepository.getInstance().getChat(event.getData().getChat().getId())).isNotNull();
    }

    private SocketChatCreatedEvent getChatCreatedObject(long teamId) {
        String content = "{    \n" +
                "    \"event\": \"chat_created\",\n" +
                "    \"version\": 1,\n" +
                "    \"teamId\": 11680043,\n" +
                "    \"data\": {\n" +
                "        \"chat\": {\n" +
                "            \"id\": 11903032,\n" +
                "            \"teamId\": 11680043,\n" +
                "            \"type\": \"chat\",\n" +
                "            \"status\": \"active\",\n" +
                "            \"lastLinkId\": -1,\n" +
                "            \"members\": [\n" +
                "                11680045,\n" +
                "                11903030\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "    \"ts\": 1464241715673\n" +
                "}";
        try {
            SocketChatCreatedEvent socketChatCreatedEvent = JacksonMapper.getInstance().getObjectMapper().readValue(content, SocketChatCreatedEvent.class);
            socketChatCreatedEvent.setTeamId(teamId);
            socketChatCreatedEvent.getData().getChat().setTeamId(teamId);
            return socketChatCreatedEvent;
        } catch (IOException e) {
            return null;
        }
    }

    @Test
    public void testOnTopicLeft() throws Exception {
        final long[] leftTopicId = new long[1];
        final long[] leftTeamId = new long[1];
        register((TopicDeleteEvent event) -> {
            accept = true;
            leftTopicId[0] = event.getTopicId();
            leftTeamId[0] = event.getTeamId();
        });

        SocketTopicLeftEvent event = createEvent(SocketTopicLeftEvent.class);
        SocketTopicLeftEvent.Data data = new SocketTopicLeftEvent.Data();
        data.setMemberId(TeamInfoLoader.getInstance().getMyId());
        data.setTopicId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setData(data);
        model.onTopicLeft(event);

        assertThat(accept).isTrue();
        assertThat(leftTeamId[0]).isEqualTo(teamId);
        assertThat(leftTopicId[0]).isEqualTo(data.getTopicId());
    }

    @Test
    public void testOnMemberStarred() throws Exception {
        register((MemberStarredEvent event) -> {
            accept = true;
        });
        SocketMemberStarredEvent event = createEvent(SocketMemberStarredEvent.class);
        SocketMemberStarredEvent.Member member = new SocketMemberStarredEvent.Member();
        member.setId(TeamInfoLoader.getInstance().getJandiBot().getId());
        event.setMember(member);
        model.onMemberStarred(event);

        assertThat(accept).isTrue();
//        assertThat(TeamInfoLoader.initiate().isStarredUser(TeamInfoLoader.initiate().getJandiBot().getTopicId()))
//                .isTrue();
    }

    @Test
    public void testOnFileUnshared() throws Exception {
        register((UnshareFileEvent event) -> {
            accept = true;
        });

        SocketFileUnsharedEvent event = createEvent(SocketFileUnsharedEvent.class);
        SocketFileUnsharedEvent.EventFileInfo file = new SocketFileUnsharedEvent.EventFileInfo();
        file.setId(1);
        event.setFile(file);
        event.room = new SocketFileUnsharedEvent.Room_();
        event.room.id = 1;
        model.onFileUnshared(event);

        assertThat(accept).isTrue();

    }

    @Test
    public void testOnFileShared() throws Exception {
        register((ShareFileEvent event) -> {
            accept = true;
        });
        SocketFileShareEvent event = createEvent(SocketFileShareEvent.class);
        event.setFile(new SocketFileShareEvent.FileObject());
        model.onFileShared(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnRoomMarkerUpdated() throws Exception {
        register((RoomMarkerEvent event) -> {
            accept = true;
        });
        SocketRoomMarkerEvent event = createEvent(SocketRoomMarkerEvent.class);
        event.setRoom(new SocketRoomMarkerEvent.MarkerRoom());
        event.setMarker(new SocketRoomMarkerEvent.Marker());
        model.onRoomMarkerUpdated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnLinkPreviewCreated() throws Exception {
        register((LinkPreviewUpdateEvent event) -> {
            accept = true;
        });
        SocketLinkPreviewMessageEvent event = createEvent(SocketLinkPreviewMessageEvent.class);
        SocketLinkPreviewMessageEvent.Data data = new SocketLinkPreviewMessageEvent.Data();
        data.setLinkPreview(new ResMessages.LinkPreview());
        event.setData(data);

        model.onLinkPreviewCreated(event);
        assertThat(accept).isTrue();
    }

    @NonNull
    private <T extends EventHistoryInfo> T createEvent(Class<T> clazz) {
        try {
            EventHistoryInfo event = clazz.newInstance();
            Field version = clazz.getDeclaredField("version");
            version.setAccessible(true);
            version.setInt(event, clazz.getAnnotation(Version.class).value());
            version.setAccessible(false);

            Field teamId = clazz.getDeclaredField("teamId");
            teamId.setAccessible(true);
            teamId.setLong(event, JandiSocketServiceModelTest.teamId);
            teamId.setAccessible(false);


            Field unique = clazz.getDeclaredField("unique");
            unique.setAccessible(true);
            unique.set(event, UUID.randomUUID().toString());
            unique.setAccessible(false);

            Field ts = clazz.getDeclaredField("ts");
            ts.setAccessible(true);
            ts.setLong(event, System.currentTimeMillis());
            ts.setAccessible(false);

            return (T) event;
        } catch (Exception e) {
        }
        return null;
    }

    @Ignore
    @Test
    public void testOnLinkPreviewImage() throws Exception {
        register((LinkPreviewUpdateEvent event) -> {
            accept = true;
        });

        SocketLinkPreviewThumbnailEvent event = createEvent(SocketLinkPreviewThumbnailEvent.class);
        SocketLinkPreviewThumbnailEvent.Data data = new SocketLinkPreviewThumbnailEvent.Data();
        ResMessages.LinkPreview linkPreview = new ResMessages.LinkPreview();
        data.setLinkPreview(linkPreview);
        event.setData(data);
        model.onLinkPreviewImage(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnAnnouncementCreated() throws Exception {

        register((SocketAnnouncementCreatedEvent event) -> {
            accept = true;
        });

        SocketAnnouncementCreatedEvent event = createEvent(SocketAnnouncementCreatedEvent.class);
        SocketAnnouncementCreatedEvent.Data data = new SocketAnnouncementCreatedEvent.Data();
        data.setAnnouncement(new Announcement());
        event.setData(data);

        model.onAnnouncementCreated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnAnnouncementDeleted() throws Exception {

        register((SocketAnnouncementDeletedEvent event) -> {
            accept = true;
        });
        SocketAnnouncementDeletedEvent event = createEvent(SocketAnnouncementDeletedEvent.class);
        SocketAnnouncementDeletedEvent.Data data = new SocketAnnouncementDeletedEvent.Data();
        event.setData(data);
        model.onAnnouncementDeleted(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnRoomSubscriptionUpdated() throws Exception {


        register((SocketTopicPushEvent e) -> {
            accept = true;
        });

        SocketTopicPushEvent event = createEvent(SocketTopicPushEvent.class);
        SocketTopicPushEvent.Data data = new SocketTopicPushEvent.Data();
        data.setRoomId(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setSubscribe(false);
        event.setData(data);
        model.onRoomSubscriptionUpdated(event);

        assertThat(accept).isTrue();

//        assertThat(TeamInfoLoader.initiate()
//                .getTopic(TeamInfoLoader.initiate().getDefaultTopicId())
//                .isPushSubscribe())
//                .isFalse();
    }

    @Test
    public void testOnMessageUnstarred() throws Exception {

        register((MessageStarEvent event) -> {
            accept = true;
        });
        SocketMessageUnstarredEvent event = createEvent(SocketMessageUnstarredEvent.class);
        SocketMessageUnstarredEvent.StarredInfo starredInfo = new SocketMessageUnstarredEvent.StarredInfo();
        starredInfo.setMessageId(1);
        event.setStarredInfo(starredInfo);
        model.onMessageUnstarred(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnMessageStarred() throws Exception {
        register((MessageStarEvent event) -> {
            accept = true;
        });
        SocketMessageStarredEvent event = createEvent(SocketMessageStarredEvent.class);
        SocketMessageStarredEvent.StarredInfo starredInfo = new SocketMessageStarredEvent.StarredInfo();
        starredInfo.setMessageId(1);
        event.setStarredInfo(starredInfo);
        model.onMessageStarred(event);
    }

    @Test
    public void testOnFolderDeleted() throws Exception {

        register((TopicFolderRefreshEvent e) -> {
            accept = true;
        });
        SocketTopicFolderDeletedEvent event = createEvent(SocketTopicFolderDeletedEvent.class);

        SocketTopicFolderDeletedEvent.Data data = new SocketTopicFolderDeletedEvent.Data();
        data.setFolderId(1);
        event.setData(data);
        model.onFolderDeleted(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnFolderItemCreated() throws Exception {
        register((TopicFolderRefreshEvent event) -> {
            accept = true;
        });

        Folder folder = new Folder();
        folder.setId(1);
        FolderRepository.getInstance().addFolder(folder);


        SocketTopicFolderItemCreatedEvent event = createEvent(SocketTopicFolderItemCreatedEvent.class);
        SocketTopicFolderItemCreatedEvent.Data data = new SocketTopicFolderItemCreatedEvent.Data();
        data.setFolderId(1);
        data.setRoomId(2);
        event.setData(data);
        model.onFolderItemCreated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnFolderItemDeleted() throws Exception {
        register((TopicFolderRefreshEvent event) -> {
            accept = true;
        });
        Folder folder = new Folder();
        folder.setId(1);
        FolderRepository.getInstance().addFolder(folder);
        SocketTopicFolderItemDeletedEvent event = createEvent(SocketTopicFolderItemDeletedEvent.class);
        SocketTopicFolderItemDeletedEvent.Data data = new SocketTopicFolderItemDeletedEvent.Data();
        data.setFolderId(1);
        data.setRoomId(2);
        event.setData(data);
        model.onFolderItemDeleted(event);
        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicFolderCreated() throws Exception {
        register((TopicFolderRefreshEvent event) -> {
            accept = true;
        });
        SocketTopicFolderCreatedEvent event = createEvent(SocketTopicFolderCreatedEvent.class);
        SocketTopicFolderCreatedEvent.Data data = new SocketTopicFolderCreatedEvent.Data();
        Folder folder = new Folder();
        folder.setSeq(1);
        folder.setId(1);
        folder.setName("haha");
        folder.setRooms(Arrays.asList(1L, 2L, 3L));
        data.setFolder(folder);
        event.setData(data);
        model.onTopicFolderCreated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicFolderUpdated() throws Exception {
        register((TopicFolderRefreshEvent event) -> {
            accept = true;
        });

        Folder folder = new Folder();
        folder.setId(1);
        FolderRepository.getInstance().addFolder(folder);

        SocketTopicFolderUpdatedEvent event = createEvent(SocketTopicFolderUpdatedEvent.class);
        SocketTopicFolderUpdatedEvent.Data data = new SocketTopicFolderUpdatedEvent.Data();
        Folder folder1 = new Folder();
        folder1.setId(1);
        folder1.setSeq(2);
        folder1.setName("jaj");
        data.setFolder(folder1);
        event.setData(data);
        model.onTopicFolderUpdated(event);
        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTeamLeft() throws Exception {
        register((TeamLeaveEvent teamLeaveEvent) -> {
            accept = true;
        });
        SocketTeamLeaveEvent event = createEvent(SocketTeamLeaveEvent.class);
        SocketTeamLeaveEvent.Data data = new SocketTeamLeaveEvent.Data();
        data.setMemberId(1);
        event.setData(data);
        model.onTeamLeft(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTeamDeleted() throws Exception {
        register((TeamDeletedEvent e) -> {
            accept = true;
        });
        SocketTeamDeletedEvent event = createEvent(SocketTeamDeletedEvent.class);
        SocketTeamDeletedEvent.Data data = new SocketTeamDeletedEvent.Data();
        data.setTeamId(1L);
        event.setData(data);
        model.onTeamDeleted(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicKickOut() throws Exception {
        register((TopicKickedoutEvent e) -> {
            accept = true;
        });

        SocketTopicKickedoutEvent event = createEvent(SocketTopicKickedoutEvent.class);
        event.setData(new SocketTopicKickedoutEvent.Data());
        event.getData().setRoomId(1);

        model.onTopicKickOut(event);
        assertThat(accept).isTrue();
    }

    @Test
    public void testOnMessageCreated() throws Exception {

        register((SocketMessageCreatedEvent e) -> {
            accept = true;
        });

        ResMessages message = channelMessageApi.get().getPublicTopicMessages(TeamInfoLoader.getInstance().getTeamId(), TeamInfoLoader.getInstance().getDefaultTopicId(), -1, 1);
        ResMessages.Link link = message.records.get(0);
        SocketMessageCreatedEvent event = createEvent(SocketMessageCreatedEvent.class);
        SocketMessageCreatedEvent.Data data = new SocketMessageCreatedEvent.Data();
        data.setLinkMessage(link);
        event.setData(data);
        model.onMessageCreated(event, false);

        assertThat(accept).isTrue();
    }

    @Test
    public void testThrowExceptionIfInvaildVersion() throws Exception {
        SocketMessageCreatedEvent event = new SocketMessageCreatedEvent();
        event.setVersion(0);
        try {
            SocketModelExtractor.throwExceptionIfInvaildVersion(event);
            fail("It cannot be occured");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOnConnectBotCreated() throws Exception {
        register((RefreshConnectBotEvent e) -> {
            accept = true;
        });
        SocketConnectBotCreatedEvent event = createEvent(SocketConnectBotCreatedEvent.class);
        SocketConnectBotCreatedEvent.Data data = new SocketConnectBotCreatedEvent.Data();
        Bot bot = new Bot();
        bot.setType("connect");
        bot.setTeamId(TeamInfoLoader.getInstance().getTeamId());
        bot.setName("hello");
        bot.setId(1);
        bot.setStatus("enabled");
        data.setBot(bot);
        event.setData(data);
        model.onConnectBotCreated(event);

        Bot bot1 = BotRepository.getInstance().getBot(1);
        assertThat(bot1).isNotNull();
        assertThat(accept).isTrue();
    }

    @Test
    public void testOnConnectBotDeleted() throws Exception {
        register((RefreshConnectBotEvent e) -> {
            accept = true;
        });

        Bot bot = new Bot();
        bot.setId(1);
        bot.setName("asd");
        bot.setTeamId(TeamInfoLoader.getInstance().getTeamId());
        BotRepository.getInstance().addBot(bot);
        SocketConnectBotDeletedEvent event = createEvent(SocketConnectBotDeletedEvent.class);
        event.setData(new SocketConnectBotDeletedEvent.Data());
        event.getData().setBotId(1);
        model.onConnectBotDeleted(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnConnectBotUpdated() throws Exception {
        register((RefreshConnectBotEvent e) -> {
            accept = true;
        });


        Bot bot1 = new Bot();
        bot1.setId(1);
        bot1.setName("asd");
        BotRepository.getInstance().addBot(bot1);

        SocketConnectBotUpdatedEvent event = createEvent(SocketConnectBotUpdatedEvent.class);

        event.setData(new SocketConnectBotUpdatedEvent.Data());
        Bot bot = new Bot();
        bot.setType("connect");
        bot.setTeamId(TeamInfoLoader.getInstance().getTeamId());
        bot.setName("hello");
        bot.setId(1);
        bot.setStatus("enabled");

        event.getData().setBot(bot);

        model.onConnectBotUpdated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTeamJoin() throws Exception {
        register((TeamJoinEvent e) -> {
            accept = true;
        });

        SocketTeamJoinEvent event = createEvent(SocketTeamJoinEvent.class);
        event.setData(new SocketTeamJoinEvent.Data());
        Human member = new Human();
        member.setId(1);
        event.getData().setTeamId(teamId);
        event.getData().setMember(member);

        model.onTeamJoin(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicCreated() throws Exception {
        register((RetrieveTopicListEvent e) -> {
            accept = true;
        });

        Topic topic = TopicRepository.getInstance().getDefaultTopic();
        topic.setId(1);

        SocketTopicCreatedEvent event = createEvent(SocketTopicCreatedEvent.class);
        event.setData(new SocketTopicCreatedEvent.Data());
        event.getData().setTopic(topic);

        model.onTopicCreated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicJoined() throws Exception {
        register((RetrieveTopicListEvent e) -> {
            accept = true;
        });

        SocketTopicJoinedEvent event = createEvent(SocketTopicJoinedEvent.class);
        SocketTopicJoinedEvent.Data data = new SocketTopicJoinedEvent.Data();
        data.setTopicId(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setMemberId(TeamInfoLoader.getInstance().getMyId());
        event.setData(data);
        model.onTopicJoined(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicInvitedListener() throws Exception {
        register((RetrieveTopicListEvent e) -> {
            accept = true;
        });

        Topic defaultTopic = TopicRepository.getInstance().getDefaultTopic();
        TopicRepository.getInstance().removeMember(defaultTopic.getId(), TeamInfoLoader.getInstance().getMyId());
        SocketTopicInvitedEvent event = createEvent(SocketTopicInvitedEvent.class);
        event.setData(new SocketTopicInvitedEvent.Data());
        event.getData().setTopic(defaultTopic);
        model.onTopicInvited(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnMemberUpdated() throws Exception {
        register((ProfileChangeEvent e) -> {
            accept = true;
        });

        Human human = HumanRepository.getInstance().getHuman(TeamInfoLoader.getInstance().getMyId());
        SocketMemberUpdatedEvent event = createEvent(SocketMemberUpdatedEvent.class);
        event.setData(new SocketMemberUpdatedEvent.Data());
        event.getData().setMember(human);
        model.onMemberUpdated(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicDeleted() throws Exception {
        register((RetrieveTopicListEvent e) -> {
            accept = true;
        });
        SocketTopicDeletedEvent event = createEvent(SocketTopicDeletedEvent.class);
        event.setData(new SocketTopicDeletedEvent.Data());
        event.getData().setTopicId(TeamInfoLoader.getInstance().getDefaultTopicId());
        model.onTopicDeleted(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicStarred() throws Exception {
        register((TopicInfoUpdateEvent e) -> {
            accept = true;
        });
        SocketTopicStarredEvent event = createEvent(SocketTopicStarredEvent.class);
        SocketTopicStarredEvent.Topic topic = new SocketTopicStarredEvent.Topic();
        topic.setId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setTopic(topic);
        model.onTopicStarred(event);

        assertThat(accept).isTrue();
    }

    @Test
    public void testOnTopicUnstarred() throws Exception {
        register((TopicInfoUpdateEvent e) -> {
            accept = true;
        });

        SocketTopicUnstarredEvent event = createEvent(SocketTopicUnstarredEvent.class);
        SocketTopicUnstarredEvent.Topic topic = new SocketTopicUnstarredEvent.Topic();
        topic.setId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setTopic(topic);
        model.onTopicUnstarred(event);

        assertThat(accept).isTrue();

    }

    @Test
    public void testOnMemberUnstarred() throws Exception {
        register((MemberStarredEvent e) -> {
            accept = true;
        });

        SocketMemberUnstarredEvent event = createEvent(SocketMemberUnstarredEvent.class);
        SocketMemberUnstarredEvent.Member member = new SocketMemberUnstarredEvent.Member();
        member.setId(1);
        event.setMember(member);
        model.onMemberUnstarred(event);
        assertThat(accept).isTrue();
    }

    @Test
    public void testOnAnnouncementStatusUpdated() throws Exception {

        final Pair<Long, Boolean>[] pair = new Pair[1];
        register((AnnouncementUpdatedEvent event) -> {
            accept = true;
            long topicId = event.getTopicId();
            boolean opened = event.isOpened();

            pair[0] = Pair.create(topicId, opened);
        });

        SocketAnnouncementUpdatedEvent event = createEvent(SocketAnnouncementUpdatedEvent.class);
        SocketAnnouncementUpdatedEvent.Data data = new SocketAnnouncementUpdatedEvent.Data();
        data.setTopicId(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setOpened(true);
        event.setData(data);
        model.onAnnouncementStatusUpdated(event);

        assertThat(accept).isTrue();
        assertThat(pair[0].first).isEqualTo(TeamInfoLoader.getInstance().getDefaultTopicId());
        assertThat(pair[0].second).isTrue();
    }

    @Test
    public void testOnTeamUpdated() throws Exception {

        register((TeamInfoChangeEvent event) -> {
            accept = true;
        });

        SocketTeamUpdatedEvent event = createEvent(SocketTeamUpdatedEvent.class);
        event.setData(new SocketTeamUpdatedEvent.Data());
        Team team = TeamRepository.getInstance(TeamInfoLoader.getInstance().getTeamId()).getTeam();
        String name = "hello";
        team.setName(name);
        event.getData().setTeam(team);
        model.onTeamUpdated(event);

        assertThat(accept).isTrue();
//        assertThat(TeamInfoLoader.initiate().getTeamName()).isEqualToIgnoringCase(name);
    }


    private <T> void register(Call<T> call) {
        EventBus.getDefault().register(new EventBusListener<T>(call));
    }

    @Component(modules = {ApiClientModule.class})
    public interface JandiSocketServiceModelTestComponent {
        void inject(JandiSocketServiceModelTest model);
    }

    interface Call<T> {
        void call(T t);
    }

    public static class EventBusListener<T> {
        Call<T> call;

        public EventBusListener(Call<T> call) {
            this.call = call;
        }

        public void onEvent(T t) {
            if (call != null) {
                try {
                    call.call(t);
                } catch (Exception e) {
                    System.out.println("Wrong Event " + t.getClass());
                }
            }
        }
    }
}