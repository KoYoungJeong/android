package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
import com.tosslab.jandi.app.local.database.rooms.marker.JandiMarkerDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListModel {

    @Bean
    MessageManipulator messageManipulator;
    @Bean
    EntityClientManager entityClientManager;
    @Bean
    MessageListTimer messageListTimer;

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

    public List<ResMessages.Link> sortById(List<ResMessages.Link> messages) {

        Collections.sort(messages, (lhs, rhs) -> lhs.id - rhs.id);
        return messages;
    }

    public boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text);
    }

    public ResUpdateMessages getNewMessage(int linkId) throws RetrofitError {
        return messageManipulator.updateMessages(linkId);
    }

    public void stopRefreshTimer() {
//        messageListTimer.stop();
    }

    public void startRefreshTimer() {
//        messageListTimer.start();
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

    public boolean isPrivateTopic(int entityType) {
        return (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) ? true : false;
    }

    public boolean isDirectMessage(int entityType) {
        return (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? true : false;
    }

    public boolean isMyTopic(int entityId) {
        return EntityManager.getInstance(activity).isMyTopic(entityId);
    }

    public MenuCommand getMenuCommand(ChattingInfomations chattingInfomations, MenuItem item) {
        return MenuCommandBuilder.init(activity)
                .with(entityClientManager)
                .with(chattingInfomations)
                .build(item);
    }

    public int sendMessage(long localId, String message) {
        SendingMessage sendingMessage = new SendingMessage(localId, message);
        try {
            ResCommon resCommon = messageManipulator.sendMessage(sendingMessage.getMessage());
            JandiMessageDatabaseManager.getInstance(activity).deleteSendMessage(sendingMessage.getLocalId());
            EventBus.getDefault().post(new SendCompleteEvent(sendingMessage.getLocalId(), resCommon.id));
            return resCommon.id;
        } catch (RetrofitError e) {
            e.printStackTrace();
            JandiMessageDatabaseManager.getInstance(activity).updateSendState(sendingMessage.getLocalId(), SendingState.Fail);
            EventBus.getDefault().post(new SendFailEvent(sendingMessage.getLocalId()));
            return -1;
        } catch (Exception e) {
            JandiMessageDatabaseManager.getInstance(activity).updateSendState(sendingMessage.getLocalId(), SendingState.Fail);
            EventBus.getDefault().post(new SendFailEvent(sendingMessage.getLocalId()));
            return -1;
        }
    }

    public long insertSendingMessage(int teamId, int entityId, String message) {
        return JandiMessageDatabaseManager.getInstance(activity).insertSendMessage(teamId, entityId, message);
    }

    public boolean isMyMessage(int writerId) {
        return EntityManager.getInstance(activity).getMe().getId() == writerId;
    }

    public JsonObject uploadFile(ConfirmFileUploadEvent event, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_INNER_API_URL + "/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(activity)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress((downloaded, total) -> progressDialog.setProgress((int) (downloaded / total)))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", event.title)
                .setMultipartParameter("share", "" + event.entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(activity).getSelectedTeamInfo().getTeamId()));

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

    public void saveMessages(int teamId, int entityId, List<ResMessages.Link> lastItems) {
        JandiMessageDatabaseManager.getInstance(activity).upsertMessage(teamId, entityId, lastItems);
    }

    public void saveTempMessage(int teamId, int entityId, String sendEditText) {
        JandiMessageDatabaseManager.getInstance(activity).upsertTempMessage(teamId, entityId, sendEditText);
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

    public List<ResMessages.Link> sortDescById(List<ResMessages.Link> messages) {
        Collections.sort(messages, (lhs, rhs) -> lhs.id - rhs.id);
        return messages;
    }

    public void trackGetOldMessage(int entityType) {
        String gaPath = (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) ? BaseAnalyticsActivity.GA_PATH_CHANNEL
                : (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? BaseAnalyticsActivity.GA_PATH_DIRECT_MESSAGE
                : BaseAnalyticsActivity.GA_PATH_PRIVATE_GROUP;

        Tracker screenViewTracker = ((JandiApplication) activity.getApplicationContext())
                .getTracker(JandiApplication.TrackerName.APP_TRACKER);
        screenViewTracker.set("&uid", EntityManager.getInstance(activity).getDistictId());
        screenViewTracker.setScreenName(gaPath);
        screenViewTracker.send(new HitBuilders.AppViewBuilder().build());
    }

    public void trackChangingEntityName(int entityType) {

        try {
            String distictId = EntityManager.getInstance(activity).getDistictId();

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, distictId)
                    .trackChangingEntityName(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public void trackDeletingEntity(int entityType) {
        String distictId = EntityManager.getInstance(activity).getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(activity, distictId)
                    .trackDeletingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
        }
    }

    public void deleteSendingMessage(long localId) {
        JandiMessageDatabaseManager.getInstance(activity).deleteSendMessage(localId);
    }

    public List<ResMessages.Link> getDummyMessages(int teamId, int entityId, String name, String userLargeProfileUrl) {
        List<ResMessages.Link> sendMessage = JandiMessageDatabaseManager.getInstance(activity).getSendMessage(teamId, entityId);
        int id = EntityManager.getInstance(activity).getMe().getId();
        for (ResMessages.Link link : sendMessage) {

            link.message.writerId = id;
            link.message.createTime = new Date();
        }
        return sendMessage;
    }

    public boolean isFailedDummyMessage(DummyMessageLink dummyMessageLink) {
        return dummyMessageLink.getSendingState() == SendingState.Fail;
    }

    public void deleteDummyMessageAtDatabase(long localId) {
        JandiMessageDatabaseManager.getInstance(activity).deleteSendMessage(localId);
    }

    public boolean isDefaultTopic(int entityId) {
        return EntityManager.getInstance(activity).getDefaultTopicId() == entityId;
    }

    public void removeNotificationSameEntityId(int entityId) {

        int chatIdFromPush = JandiPreference.getChatIdFromPush(activity);
        if (chatIdFromPush == entityId) {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }

    }

    public boolean isEnabledIfUser(int entityId) {

        FormattedEntity entity = EntityManager.getInstance(activity).getEntityById(entityId);

        if (entity != null && entity.isUser()) {
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

    @Background
    public void updateMarkerInfo(int teamId, int roomId) {

        if (teamId <= 0 || roomId <= 0) {
            return;
        }

        try {
            ResRoomInfo resRoomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId, roomId);
            JandiMarkerDatabaseManager.getInstance(activity.getApplicationContext()).upsertMarkers(resRoomInfo);
            EventBus.getDefault().post(new RoomMarkerEvent());
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    public void deleteMarker(int teamId, int roomId, int memberId) {
        JandiMarkerDatabaseManager.getInstance(activity).deleteMarker(teamId, roomId, memberId);
    }

    public void insertMarker(int teamId, int roomId, int memberId) {
        JandiMarkerDatabaseManager.getInstance(activity).updateMarker(teamId, roomId, memberId, -1);
    }

    public void updateEntityInfo() {
        try {
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(activity).upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance(activity).refreshEntity(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(activity, totalUnreadCount);
            BadgeUtils.setBadge(activity, totalUnreadCount);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public void upsertMyMarker(int roomId, int lastLinkId) {
        int myId = EntityManager.getInstance(activity).getMe().getId();
        int teamId = JandiAccountDatabaseManager.getInstance(activity).getSelectedTeamInfo().getTeamId();
        JandiMarkerDatabaseManager.getInstance(activity).updateMarker(teamId, roomId, myId, lastLinkId);
    }

    public int sendStickerMessage(int teamId, int entityId, StickerInfo stickerInfo, String message) {

        FormattedEntity entity = EntityManager.getInstance(activity.getApplicationContext()).getEntityById(entityId);
        String type = null;
        if (!TextUtils.isEmpty(message)) {
            type = entity.isPublicTopic() ? JandiConstants.RoomType.TYPE_PUBLIC : entity.isPrivateGroup() ? JandiConstants.RoomType.TYPE_PRIVATE : JandiConstants.RoomType.TYPE_USER;
        }

        ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), teamId, entityId, type, message);

        try {
            RequestApiManager.getInstance().sendStickerByStickerApi(reqSendSticker);
            return 1;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return -1;
        }

    }

}
