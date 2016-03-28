package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.List;


@EBean
public class MessageManipulator {
    public static final int NUMBER_OF_MESSAGES = 20;
    public static final int MAX_OF_MESSAGES = 100;


    int entityType;
    long entityId;
    private long selectedTeamId;
    private long roomId;

    @AfterInject
    void initSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return;
        }
        selectedTeamId = selectedTeamInfo.getTeamId();
    }

    public void initEntity(int entityType, long entityId) {
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public ResMessages getMessages(final long firstItemId, int count) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance()
                            .getPublicTopicMessagesByChannelMessageApi(selectedTeamId, entityId,
                                    firstItemId, count);
                } else {
                    return RequestApiManager.getInstance()
                            .getPublicTopicMessagesByChannelMessageApi(selectedTeamId, entityId, firstItemId, count);
                }
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi
                            (selectedTeamId, entityId, firstItemId, count);
                } else {
                    return RequestApiManager.getInstance().getDirectMessagesByDirectMessageApi(selectedTeamId, entityId, firstItemId, count);
                }
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                if (firstItemId > 0) {
                    return RequestApiManager.getInstance().getGroupMessagesByGroupMessageApi
                            (selectedTeamId, entityId, firstItemId, count);
                } else {
                    return RequestApiManager.getInstance().getGroupMessagesByGroupMessageApi(selectedTeamId, entityId, firstItemId, count);
                }
            default:
                return null;

        }

    }

    public List<ResMessages.Link> updateMessages(final long fromCurrentId) throws RetrofitException {

        return RequestApiManager.getInstance().getRoomUpdateMessageByMessagesApiAuth
                (selectedTeamId, roomId, fromCurrentId);
    }

    public ResCommon setLastReadLinkId(final long lastLinkId) throws RetrofitException {

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
        return RequestApiManager.getInstance().setMarkerByMainRest(entityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message, List<MentionObject> mentions) throws RetrofitException {
        final ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.teamId = selectedTeamId;
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().sendPublicTopicMessageByChannelMessageApi(entityId, selectedTeamId, new ReqSendMessageV3(message, mentions));
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().sendDirectMessageByDirectMessageApi(entityId, selectedTeamId, new ReqSendMessageV3(message, null));
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().sendGroupMessageByGroupMessageApi(entityId, selectedTeamId, new ReqSendMessageV3(message, mentions));
            default:
                return null;
        }

    }

    public ResCommon deleteMessage(final long messageId) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().deletePublicTopicMessageByChannelMessageApi(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().deleteDirectMessageByDirectMessageApi(selectedTeamId, entityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().deletePrivateGroupMessageByGroupMessageApi(selectedTeamId, entityId, messageId);
            default:
                return null;
        }

    }

    public ResCommon deleteSticker(final long messageId, int messageType) throws RetrofitException {

        switch (messageType) {
            case MessageItem.TYPE_STICKER:
                return RequestApiManager.getInstance().deleteStickerByStickerApi(messageId, selectedTeamId);
            case MessageItem.TYPE_STICKER_COMMNET:
                return RequestApiManager.getInstance().deleteStickerCommentByStickerApi(messageId, selectedTeamId);
            default:
                return null;
        }

    }


    public ResMessages getBeforeMarkerMessage(long linkId) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().getPublicTopicMarkerMessagesByChannelMessageApi(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMarkerMessagesByDirectMessageApi(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMarkerMessagesByGroupMessageApi(selectedTeamId, entityId, linkId);
            default:
                return null;

        }

    }

    public ResMessages getAfterMarkerMessage(long linkId) throws RetrofitException {

        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMessagesUpdatedForMarkerByDirectMessageApi(selectedTeamId, entityId, linkId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMessagesUpdatedForMarkerByGroupMessageApi(selectedTeamId, entityId, linkId);
            default:
                return null;

        }
    }

    public ResMessages getAfterMarkerMessage(long linkId, int count) {
        RequestApiManager apiManager = RequestApiManager.getInstance();
        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return apiManager.getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(selectedTeamId, entityId, linkId, count);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return apiManager.getDirectMessagesUpdatedForMarkerByDirectMessageApi(selectedTeamId, entityId, linkId, count);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return apiManager.getGroupMessagesUpdatedForMarkerByGroupMessageApi(selectedTeamId, entityId, linkId, count);
            default:
                return null;

        }

    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) throws RetrofitException {
        return RequestApiManager.getInstance().getMessage(teamId, messageId);
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setTeamId(long teamId) {
        this.selectedTeamId = teamId;
    }
}
