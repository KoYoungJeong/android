package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
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
import com.tosslab.jandi.app.services.socket.to.SocketPollCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollFinishedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollVotedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamDomainUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamJoinEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTeamNameUpdatedEvent;
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
import com.tosslab.jandi.app.services.socket.to.UnknownEventHistoryInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import rx.Observable;

public class SocketHistoryDeserializer extends JsonDeserializer<EventHistoryInfo> {

    @Override
    public EventHistoryInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        String eventTypeValue = getEventTypeValue(root);
        EventType eventType = getEventType(eventTypeValue);

        return createEventInfo(mapper, root, eventType);
    }

    public String getEventTypeValue(JsonNode root) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

        String key;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();

            key = next.getKey();
            if (TextUtils.equals(key, "event")) {
                return next.getValue().textValue();
            }
        }
        return "";
    }

    public EventType getEventType(String eventTypeValue) {
        if (TextUtils.isEmpty(eventTypeValue)) {
            return EventType.Unknown;
        }

        return Observable.from(EventType.values())
                .takeFirst(type -> TextUtils.equals(type.getRawType(), eventTypeValue))
                .toBlocking()
                .firstOrDefault(EventType.Unknown);
    }


    public EventHistoryInfo createEventInfo(ObjectMapper mapper, JsonNode root, EventType eventType) throws IOException {
        return mapper.treeToValue(root, eventType.getClazz());
    }

    public enum EventType {
        TeamCreated("team_created", SocketTeamCreatedEvent.class),
        TeamJoined("team_joined", SocketTeamJoinEvent.class),
        TeamLeft("team_left", SocketTeamLeaveEvent.class),
        TeamDeleted("team_deleted", SocketTeamDeletedEvent.class),
        TeamNameUpdated("team_name_updated", SocketTeamNameUpdatedEvent.class),
        TeamDomainUpdated("team_domain_updated", SocketTeamDomainUpdatedEvent.class),
        TeamUpdated("team_updated", SocketTeamUpdatedEvent.class),
        ChatClose("chat_closed", SocketChatCloseEvent.class),
        ChatCreated("chat_created", SocketChatCreatedEvent.class),
        ConnectCreated("connect_created", SocketConnectBotCreatedEvent.class),
        ConnectDeleted("connect_deleted", SocketConnectBotDeletedEvent.class),
        ConnectUpdated("connect_updated", SocketConnectBotUpdatedEvent.class),
        TopicLeft("topic_left", SocketTopicLeftEvent.class),
        TopicDeleted("topic_deleted", SocketTopicDeletedEvent.class),
        TopicCreated("topic_created", SocketTopicCreatedEvent.class),
        TopicInvited("topic_invited", SocketTopicInvitedEvent.class),
        TopicJoined("topic_joined", SocketTopicJoinedEvent.class),
        TopicUpdated("topic_updated", SocketTopicUpdatedEvent.class),
        TopicStarred("topic_starred", SocketTopicStarredEvent.class),
        TopicUnstarred("topic_unstarred", SocketTopicUnstarredEvent.class),
        TopicKickedOut("topic_kicked_out", SocketTopicKickedoutEvent.class),
        MemberStarred("member_starred", SocketMemberStarredEvent.class),
        MemberUnstarred("member_unstarred", SocketMemberUnstarredEvent.class),
        MemberProfileUpdated("member_profile_updated", SocketMemberUpdatedEvent.class),
        MemberUpdated("member_updated", SocketMemberUpdatedEvent.class),
        FileDeleted("file_deleted", SocketFileDeletedEvent.class),
        FileUnshared("file_unshared", SocketFileUnsharedEvent.class),
        CommentCreated("file_comment_created", SocketFileCommentCreatedEvent.class),
        CommentDeleted("file_comment_deleted", SocketFileCommentDeletedEvent.class),
        MessageDeleted("message_deleted", SocketMessageDeletedEvent.class),
        MessageCreated("message_created", SocketMessageCreatedEvent.class),
        MessageStarred("message_starred", SocketMessageStarredEvent.class),
        MessageUnstarred("message_unstarred", SocketMessageUnstarredEvent.class),
        RoomMarkerUpdated("room_marker_updated", SocketRoomMarkerEvent.class),
        AnnouncementDeleted("announcement_deleted", SocketAnnouncementDeletedEvent.class),
        AnnouncementStatusUpdated("announcement_status_updated", SocketAnnouncementUpdatedEvent.class),
        AnnouncementCreated("announcement_created", SocketAnnouncementCreatedEvent.class),
        LinkPreviewCreated("link_preview_created", SocketLinkPreviewMessageEvent.class),
        LinkPreviewImage("link_preview_image", SocketLinkPreviewThumbnailEvent.class),
        RoomSubscriptionUpdated("room_subscription_updated", SocketTopicPushEvent.class),
        FolderCreated("folder_created", SocketTopicFolderCreatedEvent.class),
        FolderUpdated("folder_updated", SocketTopicFolderUpdatedEvent.class),
        FolderDeleted("folder_deleted", SocketTopicFolderDeletedEvent.class),
        FolderItemCreated("folder_item_created", SocketTopicFolderItemCreatedEvent.class),
        FolderItemDeleted("folder_item_deleted", SocketTopicFolderItemDeletedEvent.class),
        PollCreated("poll_created", SocketPollCreatedEvent.class),
        PollFinished("poll_finished", SocketPollFinishedEvent.class),
        PollDeleted("poll_deleted", SocketPollDeletedEvent.class),
        PollVoted("poll_voted", SocketPollVotedEvent.class),
        Unknown("", UnknownEventHistoryInfo.class);

        private final String rawType;
        private final Class<? extends EventHistoryInfo> clazz;

        EventType(String rawType, Class<? extends EventHistoryInfo> clazz) {

            this.rawType = rawType;
            this.clazz = clazz;
        }

        public String getRawType() {
            return rawType;
        }

        public Class<? extends EventHistoryInfo> getClazz() {
            return clazz;
        }
    }

}