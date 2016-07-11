package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
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
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.ReadyMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.sticker.StickerApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;
import com.tosslab.jandi.app.network.models.dynamicl10n.PollFinished;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.poll.util.PollUtil;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Func0;

@EBean
public class MessageListModel {

    @Bean
    MessageManipulator messageManipulator;
    @Bean
    EntityClientManager entityClientManager;
    @RootContext
    AppCompatActivity activity;

    @Inject
    Lazy<RoomsApi> roomsApi;
    @Inject
    Lazy<StickerApi> stickerApi;
    @Inject
    Lazy<MessageApi> messageApi;
    @Inject
    Lazy<PollApi> pollApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public boolean isTopic(long entityid) {
        return TeamInfoLoader.getInstance().isTopic(entityid);
    }

    public void setEntityInfo(int entityType, long entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public ResMessages getOldMessage(long linkId, int count) throws RetrofitException {
        return messageManipulator.getMessages(linkId, count);
    }

    public ResMessages.OriginalMessage getMessage(long teamId, long messageId) {
        ResMessages.OriginalMessage message = null;
        try {
            message = messageManipulator.getMessage(teamId, messageId);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
        return message;
    }

    public boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text.toString().trim());
    }

    public void deleteMessage(long messageId) throws RetrofitException {
        messageManipulator.deleteMessage(messageId);
    }

    public void deleteSticker(long messageId, int messageType) throws RetrofitException {
        messageManipulator.deleteSticker(messageId, messageType);
    }

    public boolean isFileType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.FileMessage;
    }

    public boolean isCommentType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.CommentMessage;
    }

    public boolean isStickerCommentType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.CommentStickerMessage;
    }

    public boolean isPublicTopic(int entityType) {
        return entityType == JandiConstants.TYPE_PUBLIC_TOPIC;
    }

    public boolean isDirectMessage(int entityType) {
        return entityType == JandiConstants.TYPE_DIRECT_MESSAGE;
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
        } catch (RetrofitException e) {
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
        } catch (RetrofitException e) {
            SendMessageRepository.getRepository().updateSendMessageStatus(
                    sendingMessage.getLocalId(), SendMessage.Status.FAIL);
            EventBus.getDefault().post(new SendFailEvent(sendingMessage.getLocalId()));

            int errorCode = e.getStatusCode();
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
        return TeamInfoLoader.getInstance().getMyId() == writerId;
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
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent())
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

    public void updateLastLinkId(long lastUpdateLinkId) throws RetrofitException {
        messageManipulator.setLastReadLinkId(lastUpdateLinkId);
    }

    public void saveTempMessage(long roomId, String sendEditText) {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(roomId);
        readyMessage.setText(sendEditText);
        ReadyMessageRepository.getRepository().upsertReadyMessage(readyMessage);
    }

    public void deleteTopic(long entityId, int entityType) throws RetrofitException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entityClientManager.deleteChannel(entityId);
        } else {
            entityClientManager.deletePrivateGroup(entityId);
        }
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

        long chatIdFromPush = JandiPreference.getChatIdFromPush(activity);
        if (chatIdFromPush == entityId) {
            NotificationManager notificationManager =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }

    }

    public boolean isEnabledIfUser(long entityId) {

        if (TeamInfoLoader.getInstance().isUser(entityId)) {
            User user = TeamInfoLoader.getInstance().getUser(entityId);
            return user.isEnabled();
        } else {
            return true;
        }

    }

    public ResMessages getBeforeMarkerMessage(long linkId) throws RetrofitException {

        return messageManipulator.getBeforeMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
    }

    public ResMessages getAfterMarkerMessage(long linkId, int count) throws RetrofitException {
        return messageManipulator.getAfterMarkerMessage(linkId, count);
    }

    public void upsertMyMarker(long roomId, long lastLinkId) {
        long myId = TeamInfoLoader.getInstance().getMyId();
        RoomMarkerRepository.getInstance().upsertRoomMarker(roomId, myId, lastLinkId);
    }

    public long sendStickerMessage(long teamId, long entityId, StickerInfo stickerInfo, long localId) {

        String type = null;

        ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), teamId, entityId, type, "", new ArrayList<>());

        try {
            ResCommon resCommon = stickerApi.get().sendSticker(reqSendSticker);

            SendMessageRepository.getRepository()
                    .updateSendMessageStatus(localId, resCommon.id, SendMessage.Status.COMPLETE);

            trackMessagePostSuccess();
            EventBus.getDefault().post(new SendCompleteEvent(localId, resCommon.id));

            return resCommon.id;
        } catch (RetrofitException e) {
            e.printStackTrace();

            SendMessageRepository.getRepository()
                    .updateSendMessageStatus(localId, SendMessage.Status.FAIL);
            EventBus.getDefault().post(new SendFailEvent(localId));

            int errorCode = e.getStatusCode();
            trackMessagePostFail(errorCode);
            return -1;
        }

    }

    private void trackMessagePostSuccess() {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.MessagePost)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .build());
        AnalyticsUtil.flushSprinkler();

    }

    private void trackMessagePostFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.MessagePost)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
        AnalyticsUtil.flushSprinkler();
    }

    public void trackMessageDeleteSuccess(long messageId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.MessageDelete)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.MessageId, messageId)
                .build());

    }

    public void trackMessageDeleteFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.MessageDelete)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public void registStarredMessage(long teamId, long messageId) throws RetrofitException {
        try {
            messageApi.get().registStarredMessage(teamId, messageId, new ReqNull());
            MessageRepository.getRepository().updateStarred(messageId, true);
        } catch (RetrofitException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void unregistStarredMessage(long teamId, long messageId) throws RetrofitException {
        try {
            messageApi.get().unregistStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);
        } catch (RetrofitException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean isUser(long entityId) {
        return TeamInfoLoader.getInstance().isUser(entityId);
    }

    public String getReadyMessage(long roomId) {
        return ReadyMessageRepository.getRepository().getReadyMessage(roomId).getText();
    }

    public long getLastReadLinkId(long roomId) {
        long myId = TeamInfoLoader.getInstance().getMyId();
        Room room = TeamInfoLoader.getInstance().getRoom(roomId);
        if (room == null || room.getMarkers() == null || room.getMarkers().isEmpty()) {
            return -1;
        }
        return Observable.from(room.getMarkers())
                .filter(messageMarker -> messageMarker.getMemberId() == myId)
                .map(Marker::getReadLinkId)
                .firstOrDefault(-1L)
                .toBlocking()
                .first();
    }

    public void sortByTime(List<ResMessages.Link> records) {
        Collections.sort(records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
    }

    public long getMyId() {
        return TeamInfoLoader.getInstance().getMyId();
    }

    public boolean isTeamOwner() {
        return TeamInfoLoader.getInstance().getUser(getMyId()).isTeamOwner();
    }

    public boolean isCurrentTeam(long teamId) {
        return AccountRepository.getRepository().getSelectedTeamId() == teamId;
    }

    public void upsertMessages(long roomId, List<ResMessages.Link> messages) {
        Observable.from(messages)
                .doOnNext(link -> link.roomId = roomId)
                .doOnNext(link -> {
                    // 삭제된 파일/코멘트/메세지만 처리
                    if (TextUtils.equals(link.status, "archived")) {
                        if (!(link.message instanceof ResMessages.FileMessage)) {
                            MessageRepository.getRepository().deleteMessageOfMessageId(link.messageId);
                        } else {
                            MessageRepository.getRepository()
                                    .upsertFileMessage((ResMessages.FileMessage) link.message);
                        }
                    }
                })
                .filter(link -> {
                    // 이벤트와 삭제된 메세지는 처리 됐으므로..
                    return !TextUtils.equals(link.status, "archived");
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
        return TeamInfoLoader.getInstance().isUser(entityId)
                && TeamInfoLoader.getInstance().getUser(entityId).isInactive();
    }

    public ResMessages.Link getDummyMessage(long localId) {
        SendMessage sendMessage = SendMessageRepository.getRepository().getSendMessageOfLocal(localId);
        long id = TeamInfoLoader.getInstance().getMyId();
        return getDummyMessageLink(id, sendMessage);
    }

    public void presetTextContent(List<ResMessages.Link> records) {
        Observable.from(records)
                .filter(link -> link.message instanceof ResMessages.TextMessage
                        || link.message instanceof ResMessages.CommentMessage)
                .subscribe(link -> {
                    if (link.message instanceof ResMessages.TextMessage) {
                        presetTextMessage(link);
                    } else {
                        presetCommentMessage(link);
                    }
                }, throwable -> {
                });
    }

    private void presetCommentMessage(ResMessages.Link link) {
        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
        long myId = TeamInfoLoader.getInstance().getMyId();
        if (commentMessage.content.contentBuilder == null) {
            FormatParam formatMessage = commentMessage.formatMessage;
            if (formatMessage != null && formatMessage instanceof PollFinished) {

                commentMessage.content.contentBuilder =
                        PollUtil.buildFormatMessage(JandiApplication.getContext(), (PollFinished) formatMessage,
                                commentMessage, myId,
                                UiUtils.getPixelFromSp(11f));
            } else {
                SpannableStringBuilder messageBuilder = new SpannableStringBuilder();
                messageBuilder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
                messageBuilder.append(" ");

                MentionAnalysisInfo mentionAnalysisInfo =
                        MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                                .textSize(UiUtils.getPixelFromSp(11f))
                                .clickable(true)
                                .build();

                SpannableLookUp.text(messageBuilder)
                        .hyperLink(false)
                        .markdown(false)
                        .webLink(false)
                        .emailLink(false)
                        .telLink(false)
                        .mention(mentionAnalysisInfo, false)
                        .lookUp(JandiApplication.getContext());

                commentMessage.content.contentBuilder = messageBuilder;
            }
        }
    }

    private void presetTextMessage(ResMessages.Link link) {
        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
        if (textMessage.content.contentBuilder == null) {

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            if (!TextUtils.isEmpty(textMessage.content.body)) {
                messageStringBuilder.append(textMessage.content.body);
                long myId = TeamInfoLoader.getInstance().getMyId();
                MentionAnalysisInfo mentionInfo = MentionAnalysisInfo.newBuilder(myId, textMessage.mentions)
                        .textSize(UiUtils.getPixelFromSp(14f))
                        .clickable(true)
                        .build();

                SpannableLookUp.text(messageStringBuilder)
                        .hyperLink(false)
                        .markdown(false)
                        .webLink(false)
                        .telLink(false)
                        .emailLink(false)
                        .mention(mentionInfo, false)
                        .lookUp(JandiApplication.getContext());

            } else {
                messageStringBuilder.append("");
            }
            textMessage.content.contentBuilder = messageStringBuilder;
        }

    }

    public void deletePollMessage(long teamId, long pollId) throws RetrofitException {
        pollApi.get().deletePoll(teamId, pollId);
    }
}
