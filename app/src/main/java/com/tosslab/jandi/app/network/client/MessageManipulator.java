package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.client.sticker.StickerApiClient;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
// TODO Must be seperate logic
@EBean
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 20;

    @RootContext
    Context context;

    @RestService
    DirectMessageApiClient directMessageApiClient;

    @RestService
    GroupMessageApiClient groupMessageApiClient;

    @RestService
    ChannelMessageApiClient channelMessageApiClient;

    @RestService
    TeamsApiClient teamsApiClient;

    @RestService
    StickerApiClient stickerApiClient;

    @RestService
    JandiRestClient jandiRestClient;

    int entityType;
    int entityId;
    private int selectedTeamId;

    @AfterInject
    void initSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();
    }

    public void initEntity(int entityType, int entityId) {
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public ResMessages getMessages(final int firstItemId, int count) throws JandiNetworkException {
        final int requestCount = Math.max(NUMBER_OF_MESSAGES, count);
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    if (firstItemId > 0) {
                        return channelMessageApiClient.getPublicTopicMessages(selectedTeamId, entityId, firstItemId, requestCount);
                    } else {
                        return channelMessageApiClient.getPublicTopicMessages(selectedTeamId, entityId);

                    }
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    if (firstItemId > 0) {
                        return directMessageApiClient.getDirectMessages(selectedTeamId, entityId, firstItemId, requestCount);
                    } else {
                        return directMessageApiClient.getDirectMessages(selectedTeamId, entityId);
                    }
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    if (firstItemId > 0) {
                        return groupMessageApiClient.getGroupMessages(selectedTeamId, entityId, firstItemId, requestCount);
                    } else {
                        return groupMessageApiClient.getGroupMessages(selectedTeamId, entityId);
                    }
                default:
                    return null;

            }
        }).request();
    }

    public ResUpdateMessages updateMessages(final int fromCurrentId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    return channelMessageApiClient.getPublicTopicUpdatedMessages(selectedTeamId, entityId, fromCurrentId);
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    return directMessageApiClient.getDirectMessagesUpdated(selectedTeamId, entityId, fromCurrentId);
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    return groupMessageApiClient.getGroupMessagesUpdated(selectedTeamId, entityId, fromCurrentId);
                default:
                    return null;
            }
        }).request();
    }

    public ResCommon setMarker(final int lastLinkId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            jandiRestClient.setAuthentication(requestAuthentication);
            String entityType;
            switch (this.entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    entityType = ReqSetMarker.CHANNEL;
                    break;
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    entityType = ReqSetMarker.USER;
                    break;
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                default:
                    entityType = ReqSetMarker.PRIVATEGROUP;
                    break;
            }
            ReqSetMarker reqSetMarker = new ReqSetMarker(selectedTeamId, lastLinkId, entityType);
            return jandiRestClient.setMarker(entityId, reqSetMarker);
        }).request();
    }

    public ResCommon sendMessage(String message) throws JandiNetworkException {
        final ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.teamId = selectedTeamId;
        sendingMessage.type = "string";
        sendingMessage.content = message;

        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    return channelMessageApiClient.sendPublicTopicMessage(sendingMessage, entityId);
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    return directMessageApiClient.sendDirectMessage(sendingMessage, entityId);
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    return groupMessageApiClient.sendGroupMessage(sendingMessage, entityId);
                default:
                    return null;
            }
        }).request();
    }

    public ResCommon deleteMessage(final int messageId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    return channelMessageApiClient.deletePublicTopicMessage(selectedTeamId, entityId, messageId);
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    return directMessageApiClient.deleteDirectMessage(selectedTeamId, entityId, messageId);
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    return groupMessageApiClient.deletePrivateGroupMessage(selectedTeamId, entityId, messageId);
                default:
                    return null;
            }
        }).request();
    }

    public ResCommon deleteSticker(final int messageId, int messageType) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            stickerApiClient.setAuthentication(requestAuthentication);
            switch (messageType) {
                case MessageItem.TYPE_STICKER:
                    return stickerApiClient.deleteSticker(messageId, selectedTeamId);
                case MessageItem.TYPE_STICKER_COMMNET:
                    return stickerApiClient.deleteStickerComment(messageId, selectedTeamId);
                default:
                    return null;
            }
        }).request();
    }

    public ResMessages getBeforeMarkerMessage(int linkId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    return channelMessageApiClient.getPublicTopicMarkerMessages(selectedTeamId, entityId, linkId);
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    return directMessageApiClient.getDirectMarkerMessages(selectedTeamId, entityId, linkId);
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    return groupMessageApiClient.getGroupMarkerMessages(selectedTeamId, entityId, linkId);
                default:
                    return null;

            }
        }).request();
    }

    public ResMessages getAfterMarkerMessage(int linkId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {

            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

            switch (entityType) {
                case JandiConstants.TYPE_PUBLIC_TOPIC:
                    channelMessageApiClient.setAuthentication(requestAuthentication);
                    return channelMessageApiClient.getPublicTopicUpdatedMessagesForMarker(selectedTeamId, entityId, linkId);
                case JandiConstants.TYPE_DIRECT_MESSAGE:
                    directMessageApiClient.setAuthentication(requestAuthentication);
                    return directMessageApiClient.getDirectMessagesUpdatedForMarker(selectedTeamId, entityId, linkId);
                case JandiConstants.TYPE_PRIVATE_TOPIC:
                    groupMessageApiClient.setAuthentication(requestAuthentication);
                    return groupMessageApiClient.getGroupMessagesUpdatedForMarker(selectedTeamId, entityId, linkId);
                default:
                    return null;

            }
        }).request();
    }

    public ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);
            teamsApiClient.setAuthentication(requestAuthentication);
            return teamsApiClient.getMessage(teamId, messageId);
        }).request();
    }
}
