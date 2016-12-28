package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.messages.comments.CommentApi;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApi;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApi;
import com.tosslab.jandi.app.network.client.sticker.StickerApi;
import com.tosslab.jandi.app.network.client.teams.sendmessage.SendMessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSendMessages;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


public class MessageManipulator {
    public static final int NUMBER_OF_MESSAGES = 50;
    public static final int MAX_OF_MESSAGES = 50;


    int entityType;
    long entityId;
    Lazy<GroupMessageApi> groupMessageApi;
    Lazy<ChannelMessageApi> channelMessageApi;
    Lazy<DirectMessageApi> directMessageApi;
    Lazy<SendMessageApi> sendMessageApi;
    Lazy<CommentApi> commentApi;
    Lazy<StickerApi> stickerApi;
    Lazy<MessageApi> messageApi;
    private long selectedTeamId;
    private long roomId;

    @Inject
    public MessageManipulator(Lazy<GroupMessageApi> groupMessageApi,
                              Lazy<ChannelMessageApi> channelMessageApi,
                              Lazy<DirectMessageApi> directMessageApi,
                              Lazy<SendMessageApi> sendMessageApi,
                              Lazy<CommentApi> commentApi,
                              Lazy<StickerApi> stickerApi,
                              Lazy<MessageApi> messageApi) {
        this.groupMessageApi = groupMessageApi;
        this.channelMessageApi = channelMessageApi;
        this.directMessageApi = directMessageApi;
        this.sendMessageApi = sendMessageApi;
        this.commentApi = commentApi;
        this.stickerApi = stickerApi;
        this.messageApi = messageApi;
        initSelectedTeam();
    }

    void initSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return;
        }
        selectedTeamId = selectedTeamInfo.getTeamId();
    }

    public void initEntity(int entityType, long entityId) {
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public ResMessages getMessages(final long firstItemId, int count) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().getDirectMessages(selectedTeamId, entityId, firstItemId, count);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().getGroupMessages(selectedTeamId, entityId, firstItemId, count);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get()
                        .getPublicTopicMessages(selectedTeamId, entityId, firstItemId, count);

        }

    }

    public List<ResMessages.Link> updateMessages(final long fromCurrentId) throws RetrofitException {

        return messageApi.get().getRoomUpdateMessage(selectedTeamId, roomId, fromCurrentId);
    }

    public ResCommon setLastReadLinkId(final long lastLinkId) throws RetrofitException {

        String entityType;
        switch (this.entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                entityType = ReqSetMarker.USER;
                break;
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                entityType = ReqSetMarker.PRIVATEGROUP;
                break;
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                entityType = ReqSetMarker.CHANNEL;
                break;
        }
        ReqSetMarker reqSetMarker = new ReqSetMarker(selectedTeamId, lastLinkId, entityType);
        return messageApi.get().setMarker(entityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message, List<MentionObject> mentions) throws RetrofitException {

        final ReqSendMessages reqSendMessages = new ReqSendMessages();
        reqSendMessages.setText(message);
        reqSendMessages.setMentions(mentions);

        return sendMessageApi.get().sendMessage(selectedTeamId, entityId, reqSendMessages);
    }

    public ResCommon deleteMessage(final long messageId) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().deleteDirectMessage(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().deletePrivateGroupMessage(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get().deletePublicTopicMessage(selectedTeamId, entityId, messageId);
        }

    }

    public ResCommon deleteSticker(final long messageId) throws RetrofitException {
        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().deleteDirectMessage(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().deletePrivateGroupMessage(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get().deletePublicTopicMessage(selectedTeamId, entityId, messageId);
        }
    }

    public ResCommon deleteStickerComment(final long feedbackId, final long commentId) throws RetrofitException {
        return commentApi.get().deleteMessageComment(selectedTeamId, feedbackId, commentId);
    }

    public ResMessages getBeforeMarkerMessage(long linkId, int maxCount) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().getDirectMarkerMessages(selectedTeamId, entityId, linkId, maxCount);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().getGroupMarkerMessages(selectedTeamId, entityId, linkId, maxCount);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get().getPublicTopicMarkerMessages(selectedTeamId, entityId, linkId, maxCount);

        }

    }

    public ResMessages getAfterMarkerMessage(long linkId) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().getDirectMessagesUpdatedForMarker(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().getGroupMessagesUpdatedForMarker(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get().getPublicTopicUpdatedMessagesForMarker(selectedTeamId, entityId, linkId);

        }
    }

    public ResMessages getAfterMarkerMessage(long linkId, int count) throws RetrofitException {
        switch (entityType) {
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApi.get().getDirectMessagesUpdatedForMarker(selectedTeamId, entityId, linkId, count);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApi.get().getGroupMessagesUpdatedForMarker(selectedTeamId, entityId, linkId, count);
            case JandiConstants.TYPE_PUBLIC_TOPIC:
            default:
                return channelMessageApi.get().getPublicTopicUpdatedMessagesForMarker(selectedTeamId, entityId, linkId, count);

        }

    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitException {
        return messageApi.get().getMessage(teamId, messageId);
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setTeamId(long teamId) {
        this.selectedTeamId = teamId;
    }
}
