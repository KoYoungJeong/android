package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.manager.Request;
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
    JandiRestClient jandiRestClient;

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

    public ResMessages getMessages(final int firstItemId) throws JandiNetworkException {

        return RequestManager.newInstance(context, new Request<ResMessages>() {
            @Override
            public ResMessages request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        if (firstItemId > 0) {
                            return channelMessageApiClient.getPublicTopicMessages(selectedTeamId, mEntityId, firstItemId);
                        } else {
                            return channelMessageApiClient.getPublicTopicMessages(selectedTeamId, mEntityId);

                        }
                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        if (firstItemId > 0) {
                            return directMessageApiClient.getDirectMessages(selectedTeamId, mEntityId, firstItemId);
                        } else {
                            return directMessageApiClient.getDirectMessages(selectedTeamId, mEntityId);
                        }
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        if (firstItemId > 0) {
                            return groupMessageApiClient.getGroupMessages(selectedTeamId, mEntityId, firstItemId);
                        } else {
                            return groupMessageApiClient.getGroupMessages(selectedTeamId, mEntityId);
                        }
                    default:
                        return null;

                }
            }
        }).request();

    }

    public ResUpdateMessages updateMessages(final int fromCurrentId) throws JandiNetworkException {
        return RequestManager.newInstance(context, new Request<ResUpdateMessages>() {
            @Override
            public ResUpdateMessages request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        return channelMessageApiClient.getPublicTopicUpdatedMessages(selectedTeamId, mEntityId, fromCurrentId);
                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        return directMessageApiClient.getDirectMessagesUpdated(selectedTeamId, mEntityId, fromCurrentId);
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        return groupMessageApiClient.getGroupMessagesUpdated(selectedTeamId, mEntityId, fromCurrentId);
                    default:
                        return null;
                }
            }
        }).request();
    }

    public ResCommon setMarker(final int lastLinkId) throws JandiNetworkException {

        return RequestManager.newInstance(context, new Request<ResCommon>() {
            @Override
            public ResCommon request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                jandiRestClient.setAuthentication(requestAuthentication);
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
        }).request();
    }

    public ResCommon sendMessage(String message) throws JandiNetworkException {
        final ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.teamId = selectedTeamId;
        sendingMessage.type = "string";
        sendingMessage.content = message;

        return RequestManager.newInstance(context, new Request<ResCommon>() {
            @Override
            public ResCommon request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        return channelMessageApiClient.sendPublicTopicMessage(sendingMessage, mEntityId);
                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        return directMessageApiClient.sendDirectMessage(sendingMessage, mEntityId);
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        return groupMessageApiClient.sendGroupMessage(sendingMessage, mEntityId);
                    default:
                        return null;
                }
            }
        }).request();

    }

    public ResCommon deleteMessage(final int messageId) throws JandiNetworkException {

        return RequestManager.newInstance(context, new Request<ResCommon>() {
            @Override
            public ResCommon request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        return channelMessageApiClient.deletePublicTopicMessage(selectedTeamId, mEntityId, messageId);
                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        return directMessageApiClient.deleteDirectMessage(selectedTeamId, mEntityId, messageId);
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        return groupMessageApiClient.deletePrivateGroupMessage(selectedTeamId, mEntityId, messageId);
                    default:
                        return null;
                }
            }
        }).request();

    }

    public ResMessages getBeforeMarkerMessage(int linkId) throws JandiNetworkException {
        return RequestManager.newInstance(context, new Request<ResMessages>() {
            @Override
            public ResMessages request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        return channelMessageApiClient.getPublicTopicMarkerMessages(selectedTeamId, mEntityId, linkId);

                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        return directMessageApiClient.getDirectMarkerMessages(selectedTeamId, mEntityId, linkId);
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        return groupMessageApiClient.getGroupMarkerMessages(selectedTeamId, mEntityId, linkId);
                    default:
                        return null;

                }
            }
        }).request();

    }

    public ResMessages getAfterMarkerMessage(int linkId) throws JandiNetworkException {
        return RequestManager.newInstance(context, new Request<ResMessages>() {
            @Override
            public ResMessages request() throws JandiNetworkException {
                JandiV2HttpAuthentication requestAuthentication = TokenUtil.getRequestAuthentication(context);

                switch (mEntityType) {
                    case JandiConstants.TYPE_PUBLIC_TOPIC:
                        channelMessageApiClient.setAuthentication(requestAuthentication);
                        return channelMessageApiClient.getPublicTopicUpdatedMessagesForMarker(selectedTeamId, mEntityId, linkId);

                    case JandiConstants.TYPE_DIRECT_MESSAGE:
                        directMessageApiClient.setAuthentication(requestAuthentication);
                        return directMessageApiClient.getDirectMessagesUpdatedForMarker(selectedTeamId, mEntityId, linkId);
                    case JandiConstants.TYPE_PRIVATE_TOPIC:
                        groupMessageApiClient.setAuthentication(requestAuthentication);
                        return groupMessageApiClient.getGroupMessagesUpdatedForMarker(selectedTeamId, mEntityId, linkId);
                    default:
                        return null;

                }
            }
        }).request();
    }
}
