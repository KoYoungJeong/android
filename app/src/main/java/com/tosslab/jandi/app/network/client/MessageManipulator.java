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
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
// TODO Must be seperate logic
@EBean
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 20;

    @RootContext
    Context context;

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

    public ResMessages getMessages(final int firstItemId, int count) throws RetrofitError {
        final int requestCount = Math.max(NUMBER_OF_MESSAGES, count);

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance().getPublicTopicMessagesByChannelMessageApi(selectedTeamId, mEntityId, firstItemId, requestCount);
                } else {
                    return RequestApiManager.getInstance().getPublicTopicMessagesByChannelMessageApi(selectedTeamId, mEntityId);
                }
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(selectedTeamId, mEntityId, firstItemId, requestCount);
                } else {
                    return RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(selectedTeamId, mEntityId);
                }
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance().getGroupMessagesByGroupMessageApi(selectedTeamId, mEntityId, firstItemId, requestCount);
                } else {
                    return RequestApiManager.getInstance().getGroupMessagesByGroupMessageApi(selectedTeamId, mEntityId);
                }
            default:
                return null;

        }

    }

    public ResUpdateMessages updateMessages(final int fromCurrentId) throws RetrofitError {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                LogUtil.e(String.valueOf("selectedTeamId:" + selectedTeamId + "EntityId:" + mEntityId + "fromCurrentId:" + fromCurrentId));
                return RequestApiManager.getInstance().getPublicTopicUpdatedMessagesByChannelMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMessagesUpdatedByDirectMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMessagesUpdatedByGroupMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon setMarker(final int lastLinkId) throws RetrofitError {

        String entityType;
        switch (mEntityType) {
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
        return RequestApiManager.getInstance().setMarkerByMainRest(mEntityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message) throws RetrofitError {
        final ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.teamId = selectedTeamId;
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().sendPublicTopicMessageByChannelMessageApi(sendingMessage, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().sendDirectMessageByDirectMessageApi(sendingMessage, mEntityId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().sendGroupMessageByGroupMessageApi(sendingMessage, mEntityId);
            default:
                return null;
        }

    }

    public ResCommon deleteMessage(final int messageId) throws RetrofitError {

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().deletePublicTopicMessageByChannelMessageApi(selectedTeamId, mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().deleteDirectMessageByDirectMessageApi(selectedTeamId, mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().deletePrivateGroupMessageByGroupMessageApi(selectedTeamId, mEntityId, messageId);
            default:
                return null;
        }

    }

    public ResCommon deleteSticker(final int messageId, int messageType) throws RetrofitError {

        switch (messageType) {
            case MessageItem.TYPE_STICKER:
                return RequestApiManager.getInstance().deleteStickerByStickerApi(messageId, selectedTeamId);
            case MessageItem.TYPE_STICKER_COMMNET:
                return RequestApiManager.getInstance().deleteStickerCommentByStickerApi(messageId, selectedTeamId);
            default:
                return null;
        }

    }


    public ResMessages getBeforeMarkerMessage(int linkId) throws RetrofitError {

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().getPublicTopicMarkerMessagesByChannelMessageApi(selectedTeamId, mEntityId, linkId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMarkerMessagesByDirectMessageApi(selectedTeamId, mEntityId, linkId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMarkerMessagesByGroupMessageApi(selectedTeamId, mEntityId, linkId);
            default:
                return null;

        }

    }

    public ResMessages getAfterMarkerMessage(int linkId) throws RetrofitError {

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(selectedTeamId, mEntityId, linkId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMessagesUpdatedForMarkerByDirectMessageApi(selectedTeamId, mEntityId, linkId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMessagesUpdatedForMarkerByGroupMessageApi(selectedTeamId, mEntityId, linkId);
            default:
                return null;

        }
    }

    public ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);
            teamsApiClient.setAuthentication(requestAuthentication);
            return teamsApiClient.getMessage(teamId, messageId);
        }).request();
    }
}
