package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
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

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;

import rx.Observable;
import rx.functions.Func0;

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

    public boolean isTopic(FormattedEntity entity) {
        return entity != EntityManager.UNKNOWN_USER_ENTITY && !entity.isUser();
    }

    public void setEntityInfo(int entityType, long entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public ResMessages getOldMessage(long linkId, int count) throws IOException {
        return messageManipulator.getMessages(linkId, count);
    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) {
        ResMessages.OriginalMessage message = null;
        try {
            message = messageManipulator.getMessage(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return message;
    }

    public boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text.toString().trim());
    }

    public List<ResMessages.Link> getNewMessage(long linkId) throws IOException {
        return messageManipulator.updateMessages(linkId);
    }

    public void deleteMessage(long messageId) throws IOException {
        messageManipulator.deleteMessage(messageId);
    }

    public void deleteSticker(long messageId, int messageType) throws IOException {
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

    public MenuCommand getMenuCommand(Fragment fragmet, long teamId, long entityId, MenuItem item) {
        return MenuCommandBuilder.init(activity)
                .with(fragmet)
                .teamId(teamId)
                .entityId(entityId)
                .build(item);
    }

    public long getRoomId() {
        try {
            ResMessages oldMessage = getOldMessage(-1, 1);
            return oldMessage.entityId;
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setRoomId(long roomId) {
        messageManipulator.setRoomId(roomId);
    }

    public long sendMessage(long localId, String message, List<MentionObject> mentions) {

        SendingMessage sendingMessage = new SendingMessage(localId, new ReqSendMessageV3(message, mentions));
        try {
            ResCommon resCommon = messageManipulator.sendMessage(sendingMessage.getMessage(), sendingMessage.getMentions());

            SendMessageRepository.getRepository().updateSendMessageStatus(
                    sendingMessage.getLocalId(), resCommon.id, SendMessage.Status.COMPLETE);

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

    public long insertSendingMessage(long roomId, String message, List<MentionObject> mentions, long stickerGroupId, String stickerId) {
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

    public boolean isMyMessage(long writerId) {
        return EntityManager.getInstance().getMe().getId() == writerId;
    }

    @Deprecated
    public JsonObject uploadFile(ConfirmFileUploadEvent event, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_FILE_UPLOAD_URL + "inner-api/file";
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

    public void updateLastLinkId(long lastUpdateLinkId) throws IOException {
        messageManipulator.setLastReadLinkId(lastUpdateLinkId);
    }

    public void saveTempMessage(long roomId, String sendEditText) {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(roomId);
        readyMessage.setText(sendEditText);
        ReadyMessageRepository.getRepository().upsertReadyMessage(readyMessage);
    }

    public void deleteTopic(long entityId, int entityType) throws IOException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
    }

    public void modifyTopicName(int entityType, long entityId, String inputName) throws IOException {
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

    public List<ResMessages.Link> getDummyMessages(long roomId) {
        List<SendMessage> sendMessage = SendMessageRepository.getRepository().getSendMessage(roomId);
        long id = EntityManager.getInstance().getMe().getId();
        List<ResMessages.Link> links = new ArrayList<>();
        for (SendMessage link : sendMessage) {

            DummyMessageLink dummyMessageLink = getDummyMessageLink(id, link);
            links.add(dummyMessageLink);
        }
        return links;
    }

    private DummyMessageLink getDummyMessageLink(long id, SendMessage link) {
        List<MentionObject> mentionObjects = new ArrayList<>();

        Collection<MentionObject> savedMention = link.getMentionObjects();
        if (savedMention != null) {
            for (MentionObject mentionObject : savedMention) {
                mentionObjects.add(mentionObject);
            }
        }

        DummyMessageLink dummyMessageLink;
        if (link.getStickerGroupId() > 0 && !TextUtils.isEmpty(link.getStickerId())) {

            dummyMessageLink = new DummyMessageLink(link.getId(), link.getStatus(),
                    link.getStickerGroupId(), link.getStickerId());
            dummyMessageLink.message.writerId = id;
            dummyMessageLink.message.createTime = new Date();
        } else {
            dummyMessageLink = new DummyMessageLink(link.getId(), link.getMessage(),
                    link.getStatus(), mentionObjects);
            dummyMessageLink.message.writerId = id;
            dummyMessageLink.message.createTime = new Date();
        }
        return dummyMessageLink;
    }

    public boolean isFailedDummyMessage(DummyMessageLink dummyMessageLink) {
        return TextUtils.equals(dummyMessageLink.getStatus(), SendMessage.Status.FAIL.name());
    }

    public void deleteDummyMessageAtDatabase(long localId) {
        SendMessageRepository.getRepository().deleteSendMessage(localId);
    }

    public void removeNotificationSameEntityId(long entityId) {

        int chatIdFromPush = JandiPreference.getChatIdFromPush(activity);
        if (chatIdFromPush == entityId) {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }

    }

    public boolean isEnabledIfUser(long entityId) {

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY && entity.isUser()) {
            return entity.isEnabled();
        } else {
            return true;
        }

    }

    public ResMessages getBeforeMarkerMessage(long linkId) throws IOException {

        return messageManipulator.getBeforeMarkerMessage(linkId);
    }

    public ResMessages getAfterMarkerMessage(long linkId) throws IOException {
        return messageManipulator.getAfterMarkerMessage(linkId);
    }

    public ResMessages getAfterMarkerMessage(long linkId, int count) throws IOException {
        return messageManipulator.getAfterMarkerMessage(linkId, count);
    }

    public void updateMarkerInfo(long teamId, long roomId) {
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

    public void upsertMyMarker(long roomId, long lastLinkId) {
        long myId = EntityManager.getInstance().getMe().getId();
        long teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
        MarkerRepository.getRepository().upsertRoomMarker(teamId, roomId, myId, lastLinkId);
    }

    public int sendStickerMessage(long teamId, long entityId, StickerInfo stickerInfo, long localId) {

        String type = null;

        ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), teamId, entityId, type, "", new ArrayList<>());

        try {
            ResCommon resCommon = RequestApiManager.getInstance()
                    .sendStickerByStickerApi(reqSendSticker);

            SendMessageRepository.getRepository()
                    .updateSendMessageStatus(localId, resCommon.id, SendMessage.Status.COMPLETE);

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

    public void trackMessageDeleteSuccess(long messageId) {
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

    public void registStarredMessage(long teamId, long messageId) {
        try {
            RequestApiManager.getInstance()
                    .registStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, true);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void unregistStarredMessage(long teamId, long messageId) {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean isUser(long entityId) {
        return EntityManager
                .getInstance()
                .getEntityById(entityId).isUser() || EntityManager.getInstance().isJandiBot(entityId);
    }

    public String getReadyMessage(long roomId) {
        return ReadyMessageRepository.getRepository().getReadyMessage(roomId).getText();
    }

    public ResMessages.Link getLastLinkMessage(long roomId) {
        return MessageRepository.getRepository().getLastMessage(roomId);
    }

    public void clearLinks(long teamId, long roomId) {
        MessageRepository.getRepository().clearLinks(teamId, roomId);
    }

    public void upsertMessages(ResMessages messages) {
        Observable.from(messages.records)
                .subscribe(link -> {
                    link.roomId = messages.entityId;
                });

        MessageRepository.getRepository().upsertMessages(messages.records);

    }

    public long getLastReadLinkId(long roomId, long entityId) {
        if (roomId > 0) {
            // 기존의 마커 정보 가져오기
            ResRoomInfo.MarkerInfo myMarker = MarkerRepository.getRepository()
                    .getMyMarker(roomId, entityId);

            if (myMarker != null && myMarker.getLastLinkId() > 0) {
                return myMarker.getLastLinkId();
            }
        }

        // 엔티티 기준으로 정보 가져오기
        ResLeftSideMenu.User myUser = EntityManager.getInstance().getMe()
                .getUser();

        Long lastLinkId = Observable.from(myUser.u_messageMarkers)
                .filter(messageMarker -> messageMarker.entityId == entityId)
                .map(messageMarker -> messageMarker.lastLinkId)
                .firstOrDefault(-1L)
                .toBlocking()
                .first();

        return lastLinkId;
    }

    @Nullable
    public List<ResMessages.Link> loadOldMessages(long roomId, long linkId,
                                                  boolean firstLoad,
                                                  int offset) {

        List<ResMessages.Link> oldMessages;
        if (firstLoad) {
            // 처음 로드면 현재 링크 ~ 이전 20개 로드
            oldMessages =
                    MessageRepository.getRepository().getOldMessages(roomId, linkId + 1, offset);
        } else {
            // 처음 로드 아니면 현재 링크 - 1 ~ 이전 itemCount 로드
            oldMessages =
                    MessageRepository.getRepository().getOldMessages(roomId, linkId, offset);
        }

        return oldMessages;
    }

    public void sortByTime(List<ResMessages.Link> records) {
        Collections.sort(records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
    }

    public void deleteCompletedSendingMessage(long roomId) {
        SendMessageRepository.getRepository().deleteCompletedMessageOfRoom(roomId);
    }

    public long getMyId() {
        return EntityManager.getInstance().getMe().getId();
    }

    public boolean isTeamOwner() {
        return EntityManager.getInstance().getMe().isTeamOwner();
    }

    public boolean isCurrentTeam(long teamId) {
        return AccountRepository.getRepository().getSelectedTeamId() == teamId;
    }

    public void upsertMessages(long roomId, List<ResMessages.Link> messages) {
        Observable.from(messages)
                .doOnNext(link -> link.roomId = roomId)
                .doOnNext(link -> {
                    // event 가 아니고 삭제된 파일/코멘트/메세지만 처리
                    if (!TextUtils.equals(link.status, "event")
                            && TextUtils.equals(link.status, "archived")) {
                        if (!(link.message instanceof ResMessages.FileMessage)) {
                            MessageRepository.getRepository().deleteMessage(link.messageId);
                        } else {
                            MessageRepository.getRepository()
                                    .upsertFileMessage((ResMessages.FileMessage) link.message);
                        }
                    }
                })
                .filter(link -> {
                    // 이벤트와 삭제된 메세지는 처리 됐으므로..
                    return TextUtils.equals(link.status, "event")
                            || !TextUtils.equals(link.status, "archived");
                })
                .collect((Func0<List<ResMessages.Link>>) ArrayList::new, List::add)
                .subscribe(links -> {

                    List<Long> messageIds = new ArrayList<>();
                    for (ResMessages.Link link : links) {
                        messageIds.add(link.messageId);
                    }

                    // sending 메세지 삭제
                    SendMessageRepository.getRepository().deleteCompletedMessages(messageIds);

                    MessageRepository.getRepository().upsertMessages(links);
                });
    }

    public AnalyticsValue.Screen getScreen(long entityId) {
        return isUser(entityId) ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
    }

    public long insertSendingMessageIfCan(long entityId, long roomId, String message, List<MentionObject> mentions) {
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

    public long insertSendingMessageIfCan(long entityId, long roomId, StickerInfo stickerInfo) {

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

    /**
     * @param cursorPosition
     * @param message
     * @return
     * @see com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel#needSpace(int, String)
     */
    public boolean needSpace(int cursorPosition, String message) {
        int selectionStart = cursorPosition;
        if (selectionStart > 0) {
            CharSequence charSequence = message.substring(selectionStart - 1, selectionStart);
            return !TextUtils.isEmpty(charSequence.toString().trim());
        }
        return false;
    }

    public void deleteReadyMessage(long roomId) {
        ReadyMessageRepository.getRepository().deleteReadyMessage(roomId);
    }
    public boolean isInactiveUser(long entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isInavtived();
    }
}
