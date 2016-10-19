package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
import com.tosslab.jandi.app.services.socket.to.SocketMentionMarkerUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollFinishedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollVotedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
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
import com.tosslab.jandi.app.services.socket.to.SocketTopicUpdatedEvent;
import com.tosslab.jandi.app.services.socket.to.UnknownEventHistoryInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonTypeInfo(
        defaultImpl = UnknownEventHistoryInfo.class,
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "event")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "team_joined", value = SocketTeamJoinEvent.class),
        @JsonSubTypes.Type(name = "team_left", value = SocketTeamLeaveEvent.class),
        @JsonSubTypes.Type(name = "team_deleted", value = SocketTeamDeletedEvent.class),
        @JsonSubTypes.Type(name = "team_name_updated", value = SocketTeamNameUpdatedEvent.class),
        @JsonSubTypes.Type(name = "team_domain_updated", value = SocketTeamDomainUpdatedEvent.class),
        @JsonSubTypes.Type(name = "team_updated", value = SocketTeamUpdatedEvent.class),
        @JsonSubTypes.Type(name = "chat_closed", value = SocketChatCloseEvent.class),
        @JsonSubTypes.Type(name = "chat_created", value = SocketChatCreatedEvent.class),
        @JsonSubTypes.Type(name = "connect_created", value = SocketConnectBotCreatedEvent.class),
        @JsonSubTypes.Type(name = "connect_deleted", value = SocketConnectBotDeletedEvent.class),
        @JsonSubTypes.Type(name = "connect_updated", value = SocketConnectBotUpdatedEvent.class),
        @JsonSubTypes.Type(name = "topic_left", value = SocketTopicLeftEvent.class),
        @JsonSubTypes.Type(name = "topic_deleted", value = SocketTopicDeletedEvent.class),
        @JsonSubTypes.Type(name = "topic_created", value = SocketTopicCreatedEvent.class),
        @JsonSubTypes.Type(name = "topic_invited", value = SocketTopicInvitedEvent.class),
        @JsonSubTypes.Type(name = "topic_joined", value = SocketTopicJoinedEvent.class),
        @JsonSubTypes.Type(name = "topic_updated", value = SocketTopicUpdatedEvent.class),
        @JsonSubTypes.Type(name = "topic_starred", value = SocketTopicStarredEvent.class),
        @JsonSubTypes.Type(name = "topic_unstarred", value = SocketTopicStarredEvent.class),
        @JsonSubTypes.Type(name = "topic_kicked_out", value = SocketTopicKickedoutEvent.class),
        @JsonSubTypes.Type(name = "member_starred", value = SocketMemberStarredEvent.class),
        @JsonSubTypes.Type(name = "member_unstarred", value = SocketMemberUnstarredEvent.class),
        @JsonSubTypes.Type(name = "member_updated", value = SocketMemberUpdatedEvent.class),
        @JsonSubTypes.Type(name = "file_deleted", value = SocketFileDeletedEvent.class),
        @JsonSubTypes.Type(name = "file_unshared", value = SocketFileUnsharedEvent.class),
        @JsonSubTypes.Type(name = "file_shared", value = SocketFileShareEvent.class),
        @JsonSubTypes.Type(name = "file_comment_created", value = SocketFileCommentCreatedEvent.class),
        @JsonSubTypes.Type(name = "file_comment_deleted", value = SocketFileCommentDeletedEvent.class),
        @JsonSubTypes.Type(name = "message_deleted", value = SocketMessageDeletedEvent.class),
        @JsonSubTypes.Type(name = "message_created", value = SocketMessageCreatedEvent.class),
        @JsonSubTypes.Type(name = "message_starred", value = SocketMessageStarredEvent.class),
        @JsonSubTypes.Type(name = "message_unstarred", value = SocketMessageUnstarredEvent.class),
        @JsonSubTypes.Type(name = "room_marker_updated", value = SocketRoomMarkerEvent.class),
        @JsonSubTypes.Type(name = "announcement_deleted", value = SocketAnnouncementDeletedEvent.class),
        @JsonSubTypes.Type(name = "announcement_status_updated", value = SocketAnnouncementUpdatedEvent.class),
        @JsonSubTypes.Type(name = "announcement_created", value = SocketAnnouncementCreatedEvent.class),
        @JsonSubTypes.Type(name = "link_preview_created", value = SocketLinkPreviewMessageEvent.class),
        @JsonSubTypes.Type(name = "link_preview_image", value = SocketLinkPreviewThumbnailEvent.class),
        @JsonSubTypes.Type(name = "room_subscription_updated", value = SocketTopicPushEvent.class),
        @JsonSubTypes.Type(name = "folder_created", value = SocketTopicFolderCreatedEvent.class),
        @JsonSubTypes.Type(name = "folder_updated", value = SocketTopicFolderUpdatedEvent.class),
        @JsonSubTypes.Type(name = "folder_deleted", value = SocketTopicFolderDeletedEvent.class),
        @JsonSubTypes.Type(name = "folder_item_created", value = SocketTopicFolderItemCreatedEvent.class),
        @JsonSubTypes.Type(name = "folder_item_deleted", value = SocketTopicFolderItemDeletedEvent.class),
        @JsonSubTypes.Type(name = "poll_created", value = SocketPollCreatedEvent.class),
        @JsonSubTypes.Type(name = "poll_finished", value = SocketPollFinishedEvent.class),
        @JsonSubTypes.Type(name = "poll_deleted", value = SocketPollDeletedEvent.class),
        @JsonSubTypes.Type(name = "poll_voted", value = SocketPollVotedEvent.class),
        @JsonSubTypes.Type(name = "poll_comment_created", value = SocketPollCommentCreatedEvent.class),
        @JsonSubTypes.Type(name = "poll_comment_deleted", value = SocketPollCommentDeletedEvent.class),
        @JsonSubTypes.Type(name = "mention_marker_updated", value = SocketMentionMarkerUpdatedEvent.class),
})
public interface EventHistoryInfo {

    long getTs();

    String getEvent();

    int getVersion();

    long getTeamId();

    String getUnique();
}
