package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.ReadyMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListModel {

    @Bean
    MessageManipulator messageManipulator;
    @Bean
    EntityClientManager entityClientManager;
    @RootContext
    AppCompatActivity activity;


    public void setEntityInfo(int entityType, int entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public ResMessages getOldMessage(int position, int count) throws RetrofitError {
        return messageManipulator.getMessages(position, count);
    }

    public ResMessages.OriginalMessage getMessage(int teamId, int messageId) {
        ResMessages.OriginalMessage message = null;
        try {
            message = messageManipulator.getMessage(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return message;
    }

    public boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text);
    }

    public List<ResMessages.Link> getNewMessage(int linkId) throws RetrofitError {
        return messageManipulator.updateMessages(linkId);
    }

    public void deleteMessage(int messageId) throws RetrofitError {
        messageManipulator.deleteMessage(messageId);
    }

    public void deleteSticker(int messageId, int messageType) throws RetrofitError {
        messageManipulator.deleteSticker(messageId, messageType);
    }

    public boolean isFileType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.FileMessage;
    }

    public boolean isCommentType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.CommentMessage;
    }

    public boolean isStickerType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.StickerMessage;
    }

    public boolean isStickerCommentType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.CommentStickerMessage;
    }

    public boolean isPublicTopic(int entityType) {
        return (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) ? true : false;
    }

    public boolean isDirectMessage(int entityType) {
        return (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? true : false;
    }

    public MenuCommand getMenuCommand(Fragment fragmet, ChattingInfomations
            chattingInfomations, MenuItem item) {
        return MenuCommandBuilder.init(activity)
                .with(fragmet)
                .with(entityClientManager)
                .with(chattingInfomations)
                .build(item);
    }

    public int sendMessage(long localId, String message, List<MentionObject> mentions) {

        SendingMessage sendingMessage = new SendingMessage(localId, new ReqSendMessageV3(message, mentions));
        try {
            ResCommon resCommon = messageManipulator.sendMessage(sendingMessage.getMessage(), sendingMessage.getMentions());

            SendMessageRepository.getRepository().deleteSendMessage(sendingMessage.getLocalId());

            trackMessagePostSuccess();

            EventBus.getDefault().post(new SendCompleteEvent(sendingMessage.getLocalId(), resCommon.id));
            return resCommon.id;
        } catch (RetrofitError e) {
            SendMessageRepository.getRepository().updateSendMessageStatus(
                    sendingMessage.getLocalId(), SendMessage.Status.FAIL);
            EventBus.getDefault().post(new SendFailEvent(sendingMessage.getLocalId()));

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            trackMessagePostFail(errorCode);
            return -1;
        } catch (Exception e) {

            SendMessageRepository.getRepository().updateSendMessageStatus(
                    sendingMessage.getLocalId(), SendMessage.Status.FAIL);
            EventBus.getDefault().post(new SendFailEvent(sendingMessage.getLocalId()));

            trackMessagePostFail(-1);
            return -1;
        }
    }

    public long insertSendingMessage(int roomId, String message, List<MentionObject> mentions, int stickerGroupId, String stickerId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setRoomId(roomId);
        sendMessage.setMessage(message);
        sendMessage.setStickerGroupId(stickerGroupId);
        sendMessage.setStickerId(stickerId);
        if (mentions != null) {
            for (MentionObject mention : mentions) {
                mention.setSendMessageOf(sendMessage);
            }
        }
        sendMessage.setMentionObjects(mentions);
        SendMessageRepository.getRepository().insertSendMessage(sendMessage);
        return sendMessage.getId();
    }

    public boolean isMyMessage(int writerId) {
        return EntityManager.getInstance().getMe().getId() == writerId;
    }

    @Deprecated
    public JsonObject uploadFile(ConfirmFileUploadEvent event, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_FILE_UPLOAD_URL + "/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(activity)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress((downloaded, total) -> progressDialog.setProgress((int) (downloaded / total)))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(activity))
                .setMultipartParameter("title", event.title)
                .setMultipartParameter("share", "" + event.entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(AccountRepository.getRepository().getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (event.comment != null && !event.comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", event.comment);
        }

        ResponseFuture<JsonObject> requestFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        progressDialog.setOnCancelListener(dialog -> requestFuture.cancel());

        return requestFuture.get();
    }

    public void updateMarker(int lastUpdateLinkId) throws RetrofitError {
        messageManipulator.setMarker(lastUpdateLinkId);
    }

    public void saveTempMessage(int roomId, String sendEditText) {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(roomId);
        readyMessage.setText(sendEditText);
        ReadyMessageRepository.getRepository().upsertReadyMessage(readyMessage);
    }

    public void deleteTopic(int entityId, int entityType) throws RetrofitError {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
    }

    public void modifyTopicName(int entityType, int entityId, String inputName) throws RetrofitError {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.modifyChannelName(entityId, inputName);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entityClientManager.modifyPrivateGroupName(entityId, inputName);
        }
    }

    public void trackChangingEntityName(int entityType) {

        try {
            String distictId = EntityManager.getInstance().getDistictId();

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, distictId)
                    .trackChangingEntityName(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public void trackDeletingEntity(int entityType) {
        String distictId = EntityManager.getInstance().getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(activity, distictId)
                    .trackDeletingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public List<ResMessages.Link> getDummyMessages(int roomId) {
        List<SendMessage> sendMessage = SendMessageRepository.getRepository().getSendMessage
                (roomId);
        int id = EntityManager.getInstance().getMe().getId();
        List<ResMessages.Link> links = new ArrayList<>();
        for (SendMessage link : sendMessage) {

            List<MentionObject> mentionObjects = new ArrayList<>();

            Collection<MentionObject> savedMention = link.getMentionObjects();
            if (savedMention != null) {
                for (MentionObject mentionObject : savedMention) {
                    mentionObjects.add(mentionObject);
                }
            }

            if (link.getStickerGroupId() > 0 && !TextUtils.isEmpty(link.getStickerId())) {

                DummyMessageLink dummyMessageLink = new DummyMessageLink(link.getId(), link.getStatus(),
                        link.getStickerGroupId(), link.getStickerId());
                dummyMessageLink.message.writerId = id;
                dummyMessageLink.message.createTime = new Date();
                links.add(dummyMessageLink);
            } else {
                DummyMessageLink dummyMessageLink = new DummyMessageLink(link.getId(), link.getMessage(),
                        link.getStatus(), mentionObjects);
                dummyMessageLink.message.writerId = id;
                dummyMessageLink.message.createTime = new Date();
                links.add(dummyMessageLink);
            }
        }
        return links;
    }

    public boolean isFailedDummyMessage(DummyMessageLink dummyMessageLink) {
        return TextUtils.equals(dummyMessageLink.getStatus(), SendMessage.Status.FAIL.name());
    }

    public void deleteDummyMessageAtDatabase(long localId) {
        SendMessageRepository.getRepository().deleteSendMessage(localId);
    }

    public void removeNotificationSameEntityId(int entityId) {

        int chatIdFromPush = JandiPreference.getChatIdFromPush(activity);
        if (chatIdFromPush == entityId) {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }

    }

    public boolean isEnabledIfUser(int entityId) {

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY && entity.isUser()) {
            return TextUtils.equals(entity.getUser().status, "enabled");
        } else {
            return true;
        }

    }

    public ResMessages getBeforeMarkerMessage(int linkId) throws RetrofitError {

        return messageManipulator.getBeforeMarkerMessage(linkId);
    }


    public ResMessages getAfterMarkerMessage(int linkId) throws RetrofitError {
        return messageManipulator.getAfterMarkerMessage(linkId);
    }

    public ResMessages getAfterMarkerMessage(int linkId, int count) throws RetrofitError {
        return messageManipulator.getAfterMarkerMessage(linkId, count);
    }

    @Background
    public void updateMarkerInfo(int teamId, int roomId) {

        if (teamId <= 0 || roomId <= 0) {
            return;
        }

        try {
            ResRoomInfo resRoomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId, roomId);
            MarkerRepository.getRepository().upsertRoomInfo(resRoomInfo);
            EventBus.getDefault().post(new RoomMarkerEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    public void upsertMyMarker(int roomId, int lastLinkId) {
        int myId = EntityManager.getInstance().getMe().getId();
        int teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
        MarkerRepository.getRepository().upsertRoomMarker(teamId, roomId, myId, lastLinkId);
    }

    public int sendStickerMessage(int teamId, int entityId, StickerInfo stickerInfo, long localId) {

        String type = null;

        ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), teamId, entityId, type, "", new ArrayList<>());

        try {
            ResCommon resCommon = RequestApiManager.getInstance().sendStickerByStickerApi(reqSendSticker);
            SendMessageRepository.getRepository().deleteSendMessage(localId);

            trackMessagePostSuccess();
            EventBus.getDefault().post(new SendCompleteEvent(localId, resCommon.id));

            return 1;
        } catch (RetrofitError e) {
            e.printStackTrace();

            SendMessageRepository.getRepository()
                    .updateSendMessageStatus(localId, SendMessage.Status.FAIL);
            EventBus.getDefault().post(new SendFailEvent(localId));

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            trackMessagePostFail(errorCode);
            return -1;
        }

    }

    private void trackMessagePostSuccess() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessagePost)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .build())
                .flush();

    }

    private void trackMessagePostFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessagePost)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build())
                .flush();
    }

    public void trackMessageDeleteSuccess(int messageId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessageDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.MessageId, messageId)
                        .build());

    }

    public void trackMessageDeleteFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.MessageDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());
    }

    public void registStarredMessage(int teamId, int messageId) {
        try {
            RequestApiManager.getInstance()
                    .registStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, true);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void unregistStarredMessage(int teamId, int messageId) {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean isUser(int entityId) {
        return EntityManager
                .getInstance()
                .getEntityById(entityId).isUser();
    }

    public String getReadyMessage(int roomId) {
        return ReadyMessageRepository.getRepository().getReadyMessage(roomId).getText();
    }

    public void upsertMessages(ResMessages messages) {
        Observable.from(messages.records)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(link -> {
                    link.roomId = messages.entityId;
                    MessageRepository.getRepository().upsertMessage(link);
                });


    }

    public void setRoomId(int roomId) {
        messageManipulator.setRoomId(roomId);
    }

    public int getLastReadLinkId(int roomId, int entityId) {
        if (roomId > 0) {
            // 기존의 마커 정보 가져오기
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            ResRoomInfo.MarkerInfo myMarker = MarkerRepository.getRepository()
                    .getMyMarker(roomId, entityId);

            if (myMarker != null && myMarker.getLastLinkId() > 0) {
                return myMarker.getLastLinkId();
            }
        }

        // 엔티티 기준으로 정보 가져오기
        ResLeftSideMenu.User myUser = EntityManager.getInstance().getMe()
                .getUser();

        Integer lastLinkId = Observable.from(myUser.u_messageMarkers)
                .filter(messageMarker -> messageMarker.entityId == entityId)
                .map(messageMarker -> messageMarker.lastLinkId)
                .firstOrDefault(-1)
                .toBlocking()
                .first();

        return lastLinkId;
    }

    public int getMyId() {
        return EntityManager.getInstance().getMe().getId();
    }


    public boolean isTeamOwner() {
        return TextUtils.equals(EntityManager.getInstance().getMe().getUser().u_authority, "owner");
    }

    public boolean isCurrentTeam(int teamId) {
        return AccountRepository.getRepository().getSelectedTeamId() == teamId;
    }

    public AnalyticsValue.Screen getScreen(int entityId) {
        return isUser(entityId) ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
    }

    public long insertSendingMessageIfCan(int entityId, int roomId, String message, List<MentionObject> mentions) {
        long localId;
        if (isUser(entityId)) {
            if (roomId > 0) {
                localId = insertSendingMessage(roomId, message, mentions, -1, "");
            } else {
                // roomId 를 할당받지 못하면 메세지를 보내지 않음
                localId = -1;
            }
        } else {
            localId = insertSendingMessage(entityId, message, mentions, -1, "");
        }
        return localId;
    }

    public long insertSendingMessageIfCan(int entityId, int roomId, StickerInfo stickerInfo) {

        long localId;
        if (isUser(entityId)) {
            if (roomId > 0) {
                localId = insertSendingMessage(roomId, "", new ArrayList<>(), stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
            } else {
                // roomId 를 할당받지 못하면 메세지를 보내지 않음
                localId = -1;
            }
        } else {
            localId = insertSendingMessage(entityId, "", new ArrayList<>(), stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
        }

        return localId;
    }

    public boolean isBefore30Days(Date time) {
        return DateComparatorUtil.isBefore30Days(time);
    }

    public String getTopicName(int entityId) {
        return EntityManager.getInstance().getEntityNameById(entityId);
    }
}
