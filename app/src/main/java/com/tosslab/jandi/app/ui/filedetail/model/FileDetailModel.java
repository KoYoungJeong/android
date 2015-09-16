package com.tosslab.jandi.app.ui.filedetail.model;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.FileDetailRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.FileSizeUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailModel {

    @RootContext
    Context context;

    @Bean
    MessageManipulator messageManipulator;

    @Bean
    EntityClientManager entityClientManager;

    public void deleteFile(int fileId) throws RetrofitError {
        entityClientManager.deleteFile(fileId);
    }

    public ResFileDetail getFileDetailInfo(int fileId) throws RetrofitError {
        return entityClientManager.getFileDetail(fileId);
    }

    public void shareMessage(int fileId, int entityIdToBeShared) throws RetrofitError {
        entityClientManager.shareMessage(fileId, entityIdToBeShared);
    }

    public void unshareMessage(int fileId, int entityIdToBeUnshared) throws RetrofitError {
        entityClientManager.unshareMessage(fileId, entityIdToBeUnshared);
    }

    public void sendMessageComment(int fileId, String message, List<MentionObject> mentions) throws RetrofitError {
        entityClientManager.sendMessageComment(fileId, message, mentions);
    }

    public ResLeftSideMenu.User getUserProfile(int userEntityId) throws RetrofitError {
        return entityClientManager.getUserProfile(userEntityId);
    }

    public File download(String url, String fileName, String ext, ProgressDialog progressDialog) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        String downloadFileName = FileSizeUtil.getDownloadFileName(fileName, ext);

        return Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                .write(new File(dir, downloadFileName))
                .get();
    }

    public boolean isMyComment(int writerId) {
        EntityManager entityManager = EntityManager.getInstance();

        if (entityManager == null) {
            return false;
        }
        FormattedEntity me = entityManager.getMe();
        return me != null && me.getId() == writerId;
    }

    public void deleteComment(int messageId, int feedbackId) throws RetrofitError {
        entityClientManager.deleteMessageComment(messageId, feedbackId);

    }

    public void deleteStickerComment(int messageId, int messageType) throws RetrofitError {
        messageManipulator.deleteSticker(messageId, messageType);

    }

    public boolean isMediaFile(String fileType) {

        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    public android.net.Uri addGallery(File result, String fileType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, result.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, result.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, fileType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, result.getAbsolutePath());

        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public List<FormattedEntity> getUnsharedEntities(ResMessages.FileMessage fileMessage) {
        if (fileMessage == null) {
            return Collections.emptyList();
        }

        Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = fileMessage.shareEntities;

        EntityManager entityManager = EntityManager.getInstance();

        List<Integer> list = new ArrayList<>();

        boolean include = false;
        int myEntityId = entityManager.getMe().getId();
        Iterator<ResMessages.OriginalMessage.IntegerWrapper> iterator = shareEntities.iterator();
        while (iterator.hasNext()) {
            int shareEntity = iterator.next().getShareEntity();
            list.add(shareEntity);
            if (shareEntity == myEntityId) {
                include = true;
            }
        }

        if (!include) {
            list.add(myEntityId);
        }

        List<FormattedEntity> entities = entityManager.retrieveExclusivedEntities(list);

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        Observable.from(entities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                .toSortedList((formattedEntity, formattedEntity2) -> {
                    if (formattedEntity.isUser() && formattedEntity2.isUser()) {
                        return formattedEntity.getName()
                                .compareToIgnoreCase(formattedEntity2.getName());
                    } else if (!formattedEntity.isUser() && !formattedEntity2.isUser()) {
                        return formattedEntity.getName()
                                .compareToIgnoreCase(formattedEntity2.getName());
                    } else {
                        if (formattedEntity.isUser()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                })
                .subscribe(formattedEntities::addAll);

        return formattedEntities;
    }

    public ResCommon joinEntity(FormattedEntity entityId) throws RetrofitError {

        return entityClientManager.joinChannel(entityId.getChannel().id);

    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessageCommentWithSticker(int fileId, int stickerGroupId, String stickerId, String comment, List<MentionObject> mentions) throws RetrofitError {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerGroupId, stickerId, teamId, fileId, "", comment, mentions);
            RequestApiManager.getInstance().sendStickerCommentByStickerApi(reqSendSticker);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trackFileDownloadSuccess(int fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDownload)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileDownload.name(), "ResponseSuccess");

    }

    public void trackFileShareSuccess(int topicId, int fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileShare.name(), "ResponseSuccess");

    }

    public void trackFileShareFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileShare.name(), "ResponseFail");

    }

    public void trackFileUnShareSuccess(int topicId, int fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileUnShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileUnShare.name(), "ResponseSuccess");

    }

    public void trackFileUnShareFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileUnShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileUnShare.name(), "ResponseFail");

    }

    public void trackFileDeleteSuccess(int topicId, int fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileDelete.name(), "ResponseSuccess");

    }

    public void trackFileDeleteFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        AnalyticsUtil.sendEvent(Event.FileDelete.name(), "ResponseFail");

    }

    public void registStarredMessage(int teamId, int messageId) throws RetrofitError {
        try {
            RequestApiManager.getInstance()
                    .registStarredMessageByTeamApi(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public void unregistStarredMessage(int teamId, int messageId) throws RetrofitError {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
            LogUtil.e("teamId", teamId + "");
            LogUtil.e("messageId", messageId + "");
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public List<FileDetail> getFileDetail(int fileId) {
        return FileDetailRepository.getRepository().getFileDetail(fileId);
    }

    public void saveFileDetailInfo(ResFileDetail resFileDetail) {
        ResMessages.FileMessage fileMessage = extractFileMssage(resFileDetail.messageDetails);

        MessageRepository.getRepository().upsertFileMessage(fileMessage);

        Observable.from(resFileDetail.messageDetails)
                .filter(originalMessage -> !(originalMessage instanceof ResMessages.FileMessage))
                .map(originalMessage -> {
                    FileDetail fileDetail = new FileDetail();
                    fileDetail.setFile(fileMessage);

                    if (originalMessage instanceof ResMessages.CommentStickerMessage) {
                        fileDetail.setSticker(((ResMessages.CommentStickerMessage) originalMessage));
                    } else if (originalMessage instanceof ResMessages.CommentMessage) {
                        fileDetail.setComment(((ResMessages.CommentMessage) originalMessage));
                    } else {
                        return null;
                    }

                    return fileDetail;
                })
                .subscribe(fileDetail ->
                        FileDetailRepository.getRepository().upsertFileDetail(fileDetail));
    }

    public int getMyId() {
        return EntityManager.getInstance().getMe().getId();
    }

    public ResMessages.FileMessage extractFileMssage(List<ResMessages.OriginalMessage> messageList) {

        ResMessages.FileMessage defaultValue = new ResMessages.FileMessage();
        ResMessages.FileMessage fileMessage = Observable.from(messageList)
                .filter(originalMessage -> originalMessage instanceof ResMessages.FileMessage)
                .firstOrDefault(defaultValue)
                .map(originalMessage1 -> ((ResMessages.FileMessage) originalMessage1))
                .toBlocking()
                .first();

        if (fileMessage == defaultValue) {
            return null;
        }

        return fileMessage;

    }

    public List<ResMessages.OriginalMessage> extractCommentMessage(List<ResMessages.OriginalMessage> messageList) {

        List<ResMessages.OriginalMessage> sortedCommentMessages = new ArrayList<>();

        Observable.from(messageList)
                .filter(originalMessage -> !(originalMessage instanceof ResMessages.FileMessage))
                .toSortedList((lhs, rhs) -> lhs.createTime.compareTo(rhs.createTime))
                .subscribe(originalMessages -> sortedCommentMessages.addAll(originalMessages));

        return sortedCommentMessages;
    }

    public ResMessages.FileMessage getFileMessage(int fileId) {

        return MessageRepository.getRepository().getFileMessage(fileId);
    }
}