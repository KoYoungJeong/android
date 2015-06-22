package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.account.devices.AccountDeviceApiV2Client;
import com.tosslab.jandi.app.network.client.file.FileApiClient;
import com.tosslab.jandi.app.network.client.file.FileApiClient_;
import com.tosslab.jandi.app.network.client.messages.MessagesApiV2Client;
import com.tosslab.jandi.app.network.client.messages.comments.CommentsApiV2Client;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApiV2Client;
import com.tosslab.jandi.app.network.client.profile.ProfileApiV2Client;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiV2Client;
import com.tosslab.jandi.app.network.client.settings.StarredEntityApiV2Client;
import com.tosslab.jandi.app.network.client.teams.TeamApiV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EBean
@Deprecated
public class JandiEntityClient {

    @RootContext
    Context context;
    RestAdapter restAdapter;
    private int selectedTeamId;

    @AfterInject
    void initAuthentication() {

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        selectedTeamId = selectedTeamInfo.getTeamId();

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api")
                .build();

    }

    /**
     * *********************************************************
     * Entity (Channel, Private Group, Direct Message) 관련
     * **********************************************************
     */

    public ResLeftSideMenu getTotalEntitiesInfo() throws JandiNetworkException {
        try {
            return RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon createPublicTopic(String entityName) throws JandiNetworkException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.createChannel(reqCreateTopic);
                    return restAdapter.create(ChannelApiV2Client.class).createChannel(reqCreateTopic);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon createPrivateGroup(String entityName) throws JandiNetworkException {
        final ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = selectedTeamId;
        reqCreateTopic.name = entityName;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    GroupApiClient groupApiClient = new GroupApiClient_(context);
//                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return groupApiClient.createPrivateGroup(reqCreateTopic);
                    return restAdapter.create(GroupApiV2Client.class).createPrivateGroup(reqCreateTopic);
                }
            }).request();


        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon joinChannel(final ResLeftSideMenu.Channel channel) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.joinTopic(channel.id, new ReqDeleteTopic(selectedTeamId));
                    return restAdapter.create(ChannelApiV2Client.class).joinTopic(channel.id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();


        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon leaveChannel(final int id) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.leaveTopic(id, new ReqDeleteTopic(selectedTeamId));
                    return restAdapter.create(ChannelApiV2Client.class).leaveTopic(id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon leavePrivateGroup(final int id) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    GroupApiClient groupApiClient = new GroupApiClient_(context);
//                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return groupApiClient.leaveGroup(id, new ReqTeam(selectedTeamId));
                    return restAdapter.create(GroupApiV2Client.class).leaveGroup(id, new ReqTeam(selectedTeamId));
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon modifyChannelName(final int id, String name) throws JandiNetworkException {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.modifyPublicTopicName(entityInfo, id);
                    return restAdapter.create(ChannelApiV2Client.class).modifyPublicTopicName(entityInfo, id);
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon modifyPrivateGroupName(final int id, String name) throws JandiNetworkException {
        final ReqCreateTopic entityInfo = new ReqCreateTopic();
        entityInfo.teamId = selectedTeamId;
        entityInfo.name = name;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    GroupApiClient groupApiClient = new GroupApiClient_(context);
//                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return groupApiClient.modifyGroup(entityInfo, id);
                    return restAdapter.create(GroupApiV2Client.class).modifyGroup(entityInfo, id);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon deleteChannel(final int id) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.deleteTopic(id, new ReqDeleteTopic(selectedTeamId));
                    return restAdapter.create(ChannelApiV2Client.class).deleteTopic(id, new ReqDeleteTopic(selectedTeamId));
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon deletePrivateGroup(final int id) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    GroupApiClient groupApiClient = new GroupApiClient_(context);
//                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return groupApiClient.deleteGroup(selectedTeamId, id);
                    return restAdapter.create(GroupApiV2Client.class).deleteGroup(selectedTeamId, id);
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon inviteChannel(final int id, final List<Integer> invitedUsers) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ChannelApiClient channelApiClient = new ChannelApiClient_(context);
//                    channelApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return channelApiClient.invitePublicTopic(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                    return restAdapter.create(ChannelApiV2Client.class).invitePublicTopic(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon invitePrivateGroup(final int id, final List<Integer> invitedUsers) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    GroupApiClient groupApiClient = new GroupApiClient_(context);
//                    groupApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return groupApiClient.inviteGroup(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                    return restAdapter.create(GroupApiV2Client.class).inviteGroup(id, new ReqInviteTopicUsers(invitedUsers, selectedTeamId));
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    /**
     * *********************************************************
     * Entity 즐겨찾기 등록 / 해제
     * **********************************************************
     */
    public ResCommon enableFavorite(final int entityId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    StarredEntityApiClient starredEntityApiClient = new StarredEntityApiClient_(context);
//                    starredEntityApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return starredEntityApiClient.enableFavorite(new ReqTeam(selectedTeamId), entityId);
                    return restAdapter.create(StarredEntityApiV2Client.class).enableFavorite(new ReqTeam(selectedTeamId), entityId);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon disableFavorite(final int entityId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {

                @Override
                public ResCommon request() throws JandiNetworkException {
//                    StarredEntityApiClient starredEntityApiClient = new StarredEntityApiClient_(context);
//                    starredEntityApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return starredEntityApiClient.disableFavorite(selectedTeamId, entityId);
                    return restAdapter.create(StarredEntityApiV2Client.class).disableFavorite(selectedTeamId, entityId);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필
     * **********************************************************
     */
    public ResLeftSideMenu.User getUserProfile(final int entityId) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
                @Override
                public ResLeftSideMenu.User request() throws JandiNetworkException {

//                    TeamsApiClient teamsApiClient = new TeamsApiClient_(context);
//                    teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return teamsApiClient.getMemberProfile(selectedTeamId, entityId);
                    return restAdapter.create(TeamApiV2Client.class).getMemberProfile(selectedTeamId, entityId);
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResLeftSideMenu.User updateUserProfile(final int entityId, final ReqUpdateProfile reqUpdateProfile) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
                @Override
                public ResLeftSideMenu.User request() throws JandiNetworkException {
//                    ProfileApiClient profileApiClient = new ProfileApiClient_(context);
//                    profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return profileApiClient.updateMemberProfile(entityId, reqUpdateProfile);
                    return restAdapter.create(ProfileApiV2Client.class).updateMemberProfile(entityId, reqUpdateProfile);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon updateMemberName(final int entityId, final ReqProfileName profileName) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    ProfileApiClient profileApiClient = new ProfileApiClient_(context);
//                    profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return profileApiClient.updateMemberName(entityId, profileName);
                    return restAdapter.create(ProfileApiV2Client.class).updateMemberName(entityId, profileName);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResLeftSideMenu.User updateMemberEmail(int entityId, String email) throws JandiNetworkException {
        return RequestManager.newInstance(context, new Request<ResLeftSideMenu.User>() {
            @Override
            public ResLeftSideMenu.User request() throws JandiNetworkException {

//                ProfileApiClient profileApiClient = new ProfileApiClient_(context);
//                profileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                return profileApiClient.updateMemberEmail(entityId, new ReqAccountEmail(email));
                return restAdapter.create(ProfileApiV2Client.class).updateMemberEmail(entityId, new ReqAccountEmail(email));
            }
        }).request();
    }

    /**
     * *********************************************************
     * Push Notification Token
     * **********************************************************
     */
    @Deprecated
    public ResAccountInfo registerNotificationToken(String oldDevToken, String newDevToken) throws JandiNetworkException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", newDevToken);
        try {
//            AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
//            accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//            return accountDevicesApiClient.registerNotificationToken(req);
            return restAdapter.create(AccountDeviceApiV2Client.class).registerNotificationToken(req);
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    @Deprecated
    public ResAccountInfo deleteNotificationToken(String regId) throws JandiNetworkException {
        try {
//            AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
//            accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//            return accountDevicesApiClient.deleteNotificationToken(new ReqDeviceToken(regId));
            return restAdapter.create(AccountDeviceApiV2Client.class).deleteNotificationToken(new ReqDeviceToken(regId));
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResAccountInfo subscribeNotification(final String regId, final boolean isSubscribe) throws JandiNetworkException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);

        try {
            return RequestManager.newInstance(context, new Request<ResAccountInfo>() {
                @Override
                public ResAccountInfo request() throws JandiNetworkException {
//                    AccountDevicesApiClient accountDevicesApiClient = new AccountDevicesApiClient_(context);
//                    accountDevicesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return accountDevicesApiClient.subscribeStateNotification(new ReqSubscibeToken(regId, isSubscribe));
                    return restAdapter.create(AccountDeviceApiV2Client.class).subscribeStateNotification(new ReqSubscibeToken(regId, isSubscribe));
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    /**
     * *********************************************************
     * File 관련
     * **********************************************************
     */
    public ResFileDetail getFileDetail(final int messageId) throws JandiNetworkException {
        try {
            return RequestManager.newInstance(context, new Request<ResFileDetail>() {
                @Override
                public ResFileDetail request() throws JandiNetworkException {
//                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
//                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return messagesApiClient.getFileDetail(selectedTeamId, messageId);
                    return restAdapter.create(MessagesApiV2Client.class).getFileDetail(selectedTeamId, messageId);
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon sendMessageComment(final int messageId, String comment) throws JandiNetworkException {
        final ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.teamId = selectedTeamId;
        reqSendComment.comment = comment;
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
//                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return commentsApiClient.sendMessageComment(reqSendComment, messageId);
                    return restAdapter.create(CommentsApiV2Client.class).sendMessageComment(reqSendComment, messageId);
                }
            }).request();
        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon shareMessage(final int messageId, int cdpIdToBeShared) throws JandiNetworkException {
        final ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        reqShareMessage.teamId = selectedTeamId;
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
//                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return messagesApiClient.shareMessage(reqShareMessage, messageId);
                    return restAdapter.create(MessagesApiV2Client.class).shareMessage(reqShareMessage, messageId);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon unshareMessage(final int messageId, int cdpIdToBeunshared) throws JandiNetworkException {
        final ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(selectedTeamId, cdpIdToBeunshared);
        try {
            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    MessagesApiClient messagesApiClient = new MessagesApiClient_(context);
//                    messagesApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return messagesApiClient.unshareMessage(reqUnshareMessage, messageId);
                    return restAdapter.create(MessagesApiV2Client.class).unshareMessage(reqUnshareMessage, messageId);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon modifyMessageComment(final int messageId, String comment, final int feedbackId)
            throws JandiNetworkException {
        final ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.teamId = selectedTeamId;
        reqModifyComment.comment = comment;

        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
//                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return commentsApiClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
                    return restAdapter.create(CommentsApiV2Client.class).modifyMessageComment(reqModifyComment, feedbackId, messageId);
                }
            }).request();


        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    public ResCommon deleteMessageComment(final int messageId, final int feedbackId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
//                    CommentsApiClient commentsApiClient = new CommentsApiClient_(context);
//                    commentsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//                    return commentsApiClient.deleteMessageComment(selectedTeamId, feedbackId, messageId);
                    return restAdapter.create(CommentsApiV2Client.class).deleteMessageComment(selectedTeamId, feedbackId, messageId);
                }
            }).request();

        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }

    @Deprecated
    public ResCommon deleteFile(final int fileId) throws JandiNetworkException {
        try {

            return RequestManager.newInstance(context, new Request<ResCommon>() {
                @Override
                public ResCommon request() throws JandiNetworkException {
                    FileApiClient fileApiClient = new FileApiClient_(context);
                    fileApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                    return fileApiClient.deleteFile(selectedTeamId, fileId);

                }
            }).request();


        } catch (RetrofitError e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new JandiNetworkException(RetrofitError.unexpectedError(null, e));
        }
    }
}
