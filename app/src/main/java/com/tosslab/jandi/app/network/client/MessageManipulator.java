package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
// TODO Must be seperate logic
@EBean
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 20;

    @RootContext
    Context context;

    int mEntityType;
    int mEntityId;
    private int selectedTeamId;

    @AfterInject
    void initSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();
    }

    public void initEntity(int entityType, int entityId) {
        mEntityId = entityId;
        mEntityType = entityType;
    }

    public ResMessages getMessages(final int firstItemId, int count) throws JandiNetworkException {
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

    public ResUpdateMessages updateMessages(final int fromCurrentId) throws JandiNetworkException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return RequestApiManager.getInstance().getPublicTopicUpdatedMessagesByChannelMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return RequestApiManager.getInstance().getDirectMessagesUpdatedByDirectMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return RequestApiManager.getInstance().getGroupMessagesUpdatedByGroupMessageApi(selectedTeamId, mEntityId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon setMarker(final int lastLinkId) throws JandiNetworkException {

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

    public ResCommon sendMessage(String message) throws JandiNetworkException {
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

    public ResCommon deleteMessage(final int messageId) throws JandiNetworkException {

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

    public ResMessages getBeforeMarkerMessage(int linkId) throws JandiNetworkException {

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

    public ResMessages getAfterMarkerMessage(int linkId) throws JandiNetworkException {

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
}
