package com.tosslab.jandi.app.services.socket;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.domain.SocketStart;
import com.tosslab.jandi.app.services.socket.annotations.Version;
import com.tosslab.jandi.app.services.socket.dagger.SocketServiceModule;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCloseEvent;
import com.tosslab.jandi.app.services.socket.to.SocketChatCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileShareEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMemberStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamNameUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicLeftEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
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

import javax.inject.Inject;

import dagger.Component;
import de.greenrobot.event.EventBus;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class JandiSocketServiceModelTest {

    private static long teamId;
    @Inject
    JandiSocketServiceModel model;
    private boolean[] accept;

    @BeforeClass
    public static void beforeClass() {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
    }

    @Before
    public void setUp() throws Exception {
        DaggerJandiSocketServiceModelTest_JandiSocketServiceModelTestComponent.builder()
                .socketServiceModule(new SocketServiceModule(JandiApplication.getContext()))
                .build()
                .inject(JandiSocketServiceModelTest.this);
        accept[0] = false;
    }

    @Test
    public void testGetStartInfo() throws Exception {
        SocketStart startInfo = model.getStartInfo();
        assertThat(startInfo).isNotNull();
        assertThat(startInfo.getToken()).isEqualToIgnoringCase(TokenUtil.getAccessToken());

    }

    @Test
    public void testOnTeamNameUpdated() throws Exception {

        register((TeamInfoChangeEvent o) -> accept[0] = true);

        SocketTeamNameUpdatedEvent event = createEvent(SocketTeamNameUpdatedEvent.class);
        event.setTs(-1);
        SocketTeamNameUpdatedEvent.Team team = new SocketTeamNameUpdatedEvent.Team();
        team.setId(teamId);
        String name = "hello-name";
        team.setName(name);
        event.setTeam(team);
        model.onTeamNameUpdated(event);

        assertThat(accept[0]).isTrue();
        assertThat(TeamInfoLoader.getInstance().getTeamName()).isEqualToIgnoringCase(name);
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
    }

    @Test
    public void testOnFileCommentCreated() throws Exception {
        register((FileCommentRefreshEvent event) -> accept[0] = true);
        SocketFileCommentCreatedEvent event = createEvent(SocketFileCommentCreatedEvent.class);
        event.setFile(new SocketFileCommentCreatedEvent.EventFileInfo());
        event.setEvent("file_comment_created");
        event.setComment(new SocketFileCommentCreatedEvent.EventCommentInfo());
        model.onFileCommentCreated(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnFileCommentDeleted() throws Exception {

        register((FileCommentRefreshEvent event) -> accept[0] = true);

        SocketFileCommentDeletedEvent event = createEvent(SocketFileCommentDeletedEvent.class);
        event.setFile(new SocketFileCommentDeletedEvent.EventFileInfo());
        event.setRooms(new ArrayList<>());
        event.setComment(new SocketFileCommentDeletedEvent.EventCommentInfo());
        model.onFileCommentDeleted(event);

        assertThat(accept[0]).isTrue();

    }

    @Test
    public void testOnMessageDeleted() throws Exception {

        final SocketMessageDeletedEvent[] acceptEvent = new SocketMessageDeletedEvent[1];
        register((SocketMessageDeletedEvent event) -> {
            accept[0] = true;
            acceptEvent[0] = event;
        });
        SocketMessageDeletedEvent event = createEvent(SocketMessageDeletedEvent.class);
        SocketMessageDeletedEvent.Data data = new SocketMessageDeletedEvent.Data();
        data.setLinkId(1);
        data.setMessageId(2);
        data.setRoomId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setData(data);
        model.onMessageDeleted(event);

        assertThat(accept[0]).isTrue();
        assertThat(acceptEvent[0]).isEqualTo(event);
    }

    @Test
    public void testOnTopicUpdated() throws Exception {

        final long[] acceptId = new long[1];
        register((TopicInfoUpdateEvent event) -> {
            accept[0] = true;
            acceptId[0] = event.getId();
        });

        SocketTopicUpdatedEvent event = createEvent(SocketTopicUpdatedEvent.class);
        SocketTopicUpdatedEvent.Data data = new SocketTopicUpdatedEvent.Data();
        Topic topic = TopicRepository.getInstance().getTopic(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setTopic(topic);
        event.setData(data);
        model.onTopicUpdated(event);

        assertThat(accept[0]).isTrue();
        assertThat(acceptId[0]).isEqualTo(topic.getId());

    }

    @Test
    public void testOnChatClosed() throws Exception {
        register((ChatListRefreshEvent event) -> {
            accept[0] = true;
        });
        SocketChatCloseEvent event = createEvent(SocketChatCloseEvent.class);
        SocketChatCloseEvent.Data chat = new SocketChatCloseEvent.Data();
        chat.setId(TeamInfoLoader.getInstance().getChatId(TeamInfoLoader.getInstance().getJandiBot().getId()));
        event.setChat(chat);
        model.onChatClosed(event);

        assertThat(accept[0]).isTrue();
        assertThat(TeamInfoLoader.getInstance().getRoom(TeamInfoLoader.getInstance().getChatId(TeamInfoLoader.getInstance().getJandiBot().getId())).isJoined())
                .isFalse();

    }

    @Test
    public void testOnChatCreated() throws Exception {
        register((ChatListRefreshEvent event) -> {
            accept[0] = true;
        });

        SocketChatCreatedEvent event = getChatCreatedObject(teamId);
        model.onChatCreated(event);

        assertThat(accept[0]).isTrue();
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
            accept[0] = true;
            leftTopicId[0] = event.getId();
            leftTeamId[0] = event.getTeamId();
        });

        SocketTopicLeftEvent event = createEvent(SocketTopicLeftEvent.class);
        SocketTopicLeftEvent.Data data = new SocketTopicLeftEvent.Data();
        data.setMemberId(TeamInfoLoader.getInstance().getMyId());
        data.setTopicId(TeamInfoLoader.getInstance().getDefaultTopicId());
        event.setData(data);
        model.onTopicLeft(event);

        assertThat(accept[0]).isTrue();
        assertThat(leftTeamId[0]).isEqualTo(teamId);
        assertThat(leftTopicId[0]).isEqualTo(data.getTopicId());
    }

    @Test
    public void testOnMemberStarred() throws Exception {
        register((MemberStarredEvent event) -> {
            accept[0] = true;
        });
        SocketMemberStarredEvent event = createEvent(SocketMemberStarredEvent.class);
        SocketMemberStarredEvent.Member member = new SocketMemberStarredEvent.Member();
        member.setId(TeamInfoLoader.getInstance().getJandiBot().getId());
        event.setMember(member);
        model.onMemberStarred(event);

        assertThat(accept[0]).isTrue();
        assertThat(TeamInfoLoader.getInstance().isStarredUser(TeamInfoLoader.getInstance().getJandiBot().getId()))
                .isTrue();
    }

    @Test
    public void testOnFileUnshared() throws Exception {
        register((UnshareFileEvent event) -> {
            accept[0] = true;
        });

        SocketFileUnsharedEvent event = createEvent(SocketFileUnsharedEvent.class);
        SocketFileUnsharedEvent.EventFileInfo file = new SocketFileUnsharedEvent.EventFileInfo();
        file.setId(1);
        event.setFile(file);
        event.room = new SocketFileUnsharedEvent.Room_();
        event.room.id = 1;
        model.onFileUnshared(event);

        assertThat(accept[0]).isTrue();

    }

    @Test
    public void testOnFileShared() throws Exception {
        register((ShareFileEvent event) -> {
            accept[0] = true;
        });
        SocketFileShareEvent event = createEvent(SocketFileShareEvent.class);
        event.setFile(new SocketFileShareEvent.FileObject());
        model.onFileShared(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnRoomMarkerUpdated() throws Exception {
        register((SocketRoomMarkerEvent event) -> {
            accept[0] = true;
        });
        SocketRoomMarkerEvent event = createEvent(SocketRoomMarkerEvent.class);
        event.setRoom(new SocketRoomMarkerEvent.MarkerRoom());
        event.setMarker(new SocketRoomMarkerEvent.Marker());
        model.onRoomMarkerUpdated(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnLinkPreviewCreated() throws Exception {
        register((LinkPreviewUpdateEvent event) -> {
            accept[0] = true;
        });
        SocketLinkPreviewMessageEvent event = createEvent(SocketLinkPreviewMessageEvent.class);
        SocketLinkPreviewMessageEvent.Data data = new SocketLinkPreviewMessageEvent.Data();
        data.setLinkPreview(new ResMessages.LinkPreview());
        event.setData(data);

        model.onLinkPreviewCreated(event);
        assertThat(accept[0]).isTrue();
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

            return (T) event;
        } catch (Exception e) {
        }
        return null;
    }

    @Ignore
    @Test
    public void testOnLinkPreviewImage() throws Exception {
        register((LinkPreviewUpdateEvent event) -> {
            accept[0] = true;
        });

        SocketLinkPreviewThumbnailEvent event = createEvent(SocketLinkPreviewThumbnailEvent.class);
        SocketLinkPreviewThumbnailEvent.Data data = new SocketLinkPreviewThumbnailEvent.Data();
        ResMessages.LinkPreview linkPreview = new ResMessages.LinkPreview();
        data.setLinkPreview(linkPreview);
        event.setData(data);
        model.onLinkPreviewImage(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnAnnouncementCreated() throws Exception {

        register((SocketAnnouncementCreatedEvent event) -> {
            accept[0] = true;
        });

        SocketAnnouncementCreatedEvent event = createEvent(SocketAnnouncementCreatedEvent.class);
        SocketAnnouncementCreatedEvent.Data data = new SocketAnnouncementCreatedEvent.Data();
        data.setAnnouncement(new Topic.Announcement());
        event.setData(data);

        model.onAnnouncementCreated(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnAnnouncementDeleted() throws Exception {

        register((SocketAnnouncementDeletedEvent event) -> {
            accept[0] = true;
        });
        SocketAnnouncementDeletedEvent event = createEvent(SocketAnnouncementDeletedEvent.class);
        SocketAnnouncementDeletedEvent.Data data = new SocketAnnouncementDeletedEvent.Data();
        event.setData(data);
        model.onAnnouncementDeleted(event);

        assertThat(accept[0]).isTrue();
    }

    @Test
    public void testOnRoomSubscriptionUpdated() throws Exception {


        register((SocketTopicPushEvent e) -> {
            accept[0] = true;
        });

        SocketTopicPushEvent event = createEvent(SocketTopicPushEvent.class);
        SocketTopicPushEvent.Data data = new SocketTopicPushEvent.Data();
        data.setRoomId(TeamInfoLoader.getInstance().getDefaultTopicId());
        data.setSubscribe(false);
        event.setData(data);
        model.onRoomSubscriptionUpdated(event);

        assertThat(accept[0]).isTrue();

        assertThat(TeamInfoLoader.getInstance()
                .getTopic(TeamInfoLoader.getInstance().getDefaultTopicId())
                .isPushSubscribe())
                .isFalse();
    }

    @Test
    public void testStartMarkerObserver() throws Exception {

    }

    @Test
    public void testStopMarkerObserver() throws Exception {

    }

    @Test
    public void testOnMessageUnstarred() throws Exception {

    }

    @Test
    public void testOnMessageStarred() throws Exception {

    }

    @Test
    public void testOnFolderDeleted() throws Exception {

    }

    @Test
    public void testOnFolderItemCreated() throws Exception {

    }

    @Test
    public void testOnFolderItemDeleted() throws Exception {

    }

    @Test
    public void testOnTopicFolderCreated() throws Exception {

    }

    @Test
    public void testOnTopicFolderUpdated() throws Exception {

    }

    @Test
    public void testOnTeamLeft() throws Exception {

    }

    @Test
    public void testOnTeamDeleted() throws Exception {

    }

    @Test
    public void testOnTopicKickOut() throws Exception {

    }

    @Test
    public void testOnMessageCreated() throws Exception {

    }

    @Test
    public void testThrowExceptionIfInvaildVersion() throws Exception {

    }

    @Test
    public void testOnConnectBotCreated() throws Exception {

    }

    @Test
    public void testOnConnectBotDeleted() throws Exception {

    }

    @Test
    public void testOnConnectBotUpdated() throws Exception {

    }

    @Test
    public void testOnTeamJoin() throws Exception {

    }

    @Test
    public void testUpdateEventHistory() throws Exception {

    }

    @Test
    public void testOnTopicCreated() throws Exception {

    }

    @Test
    public void testOnTopicJoined() throws Exception {

    }

    @Test
    public void testOnTopicInvitedListener() throws Exception {

    }

    @Test
    public void testOnMemberUpdated() throws Exception {

    }

    @Test
    public void testOnTopicDeleted() throws Exception {

    }

    @Test
    public void testOnTopicStarred() throws Exception {

    }

    @Test
    public void testOnTopicUnstarred() throws Exception {

    }

    @Test
    public void testOnMemberUnstarred() throws Exception {

    }

    @Test
    public void testOnTeamDomainUpdated() throws Exception {

    }

    @Test
    public void testOnAnnouncementStatusUpdated() throws Exception {

        final Pair<Long, Boolean>[] pair = new Pair[1];
        register((AnnouncementUpdatedEvent event) -> {
            accept[0] = true;
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

        assertThat(accept[0]).isTrue();
        assertThat(pair[0].first).isEqualTo(TeamInfoLoader.getInstance().getDefaultTopicId());
        assertThat(pair[0].second).isTrue();
    }

    @Test
    public void testOnTeamUpdated() throws Exception {

        register((TeamInfoChangeEvent event) -> {
            accept[0] = true;
        });

        SocketTeamUpdatedEvent event = createEvent(SocketTeamUpdatedEvent.class);
        event.setData(new SocketTeamUpdatedEvent.Data());
        Team team = TeamRepository.getInstance().getTeam(TeamInfoLoader.getInstance().getTeamId());
        String name = "hello";
        team.setName(name);
        event.getData().setTeam(team);
        model.onTeamUpdated(event);

        assertThat(accept[0]).isTrue();
        assertThat(TeamInfoLoader.getInstance().getTeamName()).isEqualToIgnoringCase(name);
    }


    private <T> void register(Call<T> call) {
        EventBus.getDefault().register(new EventBusListener<T>(call));
    }

    @Component(modules = SocketServiceModule.class)
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