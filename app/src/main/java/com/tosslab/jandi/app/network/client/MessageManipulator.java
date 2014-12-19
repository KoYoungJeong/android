package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.RestClientException;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
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
    JandiRestClient jandiRestClient;

    int mEntityType;
    int mEntityId;
    private int selectedTeamId;

    @AfterInject
    void initSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = JandiDatabaseManager.getInstance(context).getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();
    }


    public void initEntity(int entityType, int entityId) {

        JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

        directMessageApiClient.setAuthentication(requestAuthentication);
        groupMessageApiClient.setAuthentication(requestAuthentication);
        channelMessageApiClient.setAuthentication(requestAuthentication);
        jandiRestClient.setAuthentication(requestAuthentication);

        mEntityId = entityId;
        mEntityType = entityType;
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.getPublicTopicMessages(selectedTeamId, mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.getDirectMessages(selectedTeamId, mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.getGroupMessages(selectedTeamId, mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResUpdateMessages updateMessages(int fromCurrentId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.getPublicTopicUpdatedMessages(fromCurrentId, selectedTeamId, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.getDirectMessagesUpdated(selectedTeamId, mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.getGroupMessagesUpdated(selectedTeamId, mEntityId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon setMarker(int lastLinkId) throws RestClientException {
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
        return jandiRestClient.setMarker(mEntityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message) throws RestClientException {
        ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.sendPublicTopicMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.sendDirectMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.sendGroupMessage(sendingMessage, mEntityId);
            default:
                return null;
        }
    }

    public ResCommon deleteMessage(int messageId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.deletePublicTopicMessage(selectedTeamId, mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.deleteDirectMessage(selectedTeamId, mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.deletePrivateGroupMessage(selectedTeamId, mEntityId, messageId);
            default:
                return null;
        }
    }
}
