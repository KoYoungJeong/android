package com.tosslab.jandi.app.ui.filedetail.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
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
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import retrofit.RetrofitError;
import rx.Observable;

@EBean
public class FileDetailModel {
    public static final String TAG = FileDetailModel.class.getSimpleName();

    @RootContext
    Context context;

    @Bean
    MessageManipulator messageManipulator;

    @Bean
    EntityClientManager entityClientManager;

    public boolean isNetworkConneted() {
        return NetworkCheckUtil.isConnected();
    }

    public void deleteFile(long fileId) throws RetrofitError {
        entityClientManager.deleteFile(fileId);
    }

    public ResFileDetail getFileDetailFromServer(long fileId) throws RetrofitError {
        return entityClientManager.getFileDetail(fileId);
    }

    public void shareMessage(long fileId, long entityIdToBeShared) throws RetrofitError {
        entityClientManager.shareMessage(fileId, entityIdToBeShared);
    }

    public void unshareMessage(long fileId, long entityIdToBeUnshared) throws RetrofitError {
        entityClientManager.unshareMessage(fileId, entityIdToBeUnshared);
    }

    public void sendMessageComment(long fileId, String message, List<MentionObject> mentions) throws RetrofitError {
        entityClientManager.sendMessageComment(fileId, message, mentions);
    }

    public ResLeftSideMenu.User getUserProfile(long userEntityId) throws RetrofitError {
        return entityClientManager.getUserProfile(userEntityId);
    }

    public boolean isMyComment(long writerId) {
        EntityManager entityManager = EntityManager.getInstance();

        if (entityManager == null) {
            return false;
        }
        FormattedEntity me = entityManager.getMe();
        return me != null && me.getId() == writerId;
    }

    public void deleteComment(long messageId, long feedbackId) throws RetrofitError {
        entityClientManager.deleteMessageComment(messageId, feedbackId);
    }

    public void deleteStickerComment(long messageId, int messageType) throws RetrofitError {
        messageManipulator.deleteSticker(messageId, messageType);
    }

    public List<ResMessages.OriginalMessage> getEnableMessages(
            List<ResMessages.OriginalMessage> messages) {
        List<ResMessages.OriginalMessage> filteredMessages = new ArrayList<>();
        if (messages == null || messages.size() <= 0) {
            return filteredMessages;
        }

        Observable.from(messages)
                .filter(message ->
                        (message instanceof ResMessages.FileMessage)
                                || !("archived".equals(message.status)))
                .collect(() -> filteredMessages, List::add)
                .subscribe();

        return filteredMessages;
    }

    public List<Long> getSharedTopicIds(ResMessages.OriginalMessage fileDetail) {
        List<Long> sharedTopicIds = new ArrayList<>();

        EntityManager entityManager = EntityManager.getInstance();


        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
        Observable.from(fileMessage.shareEntities)
                .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .filter(shareEntity -> {
                    FormattedEntity entity = entityManager.getEntityById(shareEntity);
                    return entity != EntityManager.UNKNOWN_USER_ENTITY && !entity.isUser();
                })
                .collect(() -> sharedTopicIds, List::add)
                .subscribe();

        return sharedTopicIds;
    }

    public List<FormattedEntity> getUnsharedEntities() {

        // 모든 대상이 공유 대상이 되도록 함
        EntityManager entityManager = EntityManager.getInstance();
        List<FormattedEntity> entities = entityManager.retrieveAccessableEntities();

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        Observable.from(entities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                .filter(formattedEntity -> formattedEntity.getId() != entityManager.getMe().getId())
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

        if (EntityManager.getInstance().hasJandiBot()) {
            formattedEntities.add(0, EntityManager.getInstance().getJandiBot());
        }

        return formattedEntities;
    }

    public ResCommon joinEntity(FormattedEntity entity) throws RetrofitError {

        return entityClientManager.joinChannel(entity.getChannel().id);

    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.upsertBadgeCount(totalEntitiesInfo.team.id, totalUnreadCount);
            BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());
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

    public void sendMessageCommentWithSticker(long fileId, long stickerGroupId, String stickerId, String comment, List<MentionObject> mentions) throws RetrofitError {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerGroupId, stickerId, teamId, fileId, "", comment, mentions);
            RequestApiManager.getInstance().sendStickerCommentByStickerApi(reqSendSticker);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trackFileShareSuccess(long topicId, long fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

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

    }

    public void trackFileUnShareSuccess(long topicId, long fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileUnShare)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

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

    }

    public void trackFileDeleteSuccess(long topicId, long fileId) {

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDelete)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, topicId)
                        .property(PropertyKey.FileId, fileId)
                        .build());

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

    }

    public void registStarredMessage(long teamId, long messageId) throws RetrofitError {
        try {
            RequestApiManager.getInstance()
                    .registStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, true);
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void unregistStarredMessage(long teamId, long messageId) throws RetrofitError {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void sortByDate(List<ResMessages.OriginalMessage> messages) {
        Collections.sort(messages, (lhs, rhs) -> lhs.createTime.before(rhs.createTime) ? -1 : 1);
    }

    public long getMyId() {
        return EntityManager.getInstance().getMe().getId();
    }

    public ResMessages.FileMessage getFileMessage(long fileId) {

        return MessageRepository.getRepository().getFileMessage(fileId);
    }

    public boolean isTeamOwner() {
        return EntityManager.getInstance().getMe().isTeamOwner();
    }


    public long getTeamId() {
        return EntityManager.getInstance().getTeamId();
    }

    public ResMessages.FileMessage enableExternalLink(long teamId, long fileId) {
        return RequestApiManager.getInstance().enableFileExternalLink(teamId, fileId);
    }

    public ResMessages.FileMessage disableExternalLink(long teamId, long fileId) {
        return RequestApiManager.getInstance().disableFileExternalLink(teamId, fileId);
    }

    public void updateExternalLink(String fileUrl, boolean externalShared, String externalUrl, String externalCode) {
        FileDetailRepository.getRepository().updateFileExternalLink(fileUrl,
                externalShared,
                externalUrl,
                externalCode);
    }

    public Future<File> downloadFile(String downloadUrl, String downloadPath, ProgressCallback progressCallback, FutureCallback<File> callback) {
        return Ion.with(JandiApplication.getContext())
                .load(downloadUrl)
                .progressHandler(progressCallback)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .write(new File(downloadPath)).setCallback(callback);
    }

    public String getDownloadFilePath(String title) {
        String downloadPath = FileUtil.getDownloadPath();
        File file = new File(downloadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return new StringBuffer(downloadPath).append("/").append(title).toString();
    }

    public String getDownloadUrl(String fileUrl) {
        if (!fileUrl.endsWith("/download")) {
            return new StringBuffer(fileUrl).append("/download").toString();
        }
        return fileUrl;
    }

    /**
     * @param cursorPosition
     * @param message
     * @return
     * @see com.tosslab.jandi.app.ui.message.v2.model.MessageListModel#needSpace(int, String)
     */
    public boolean needSpace(int cursorPosition, String message) {
        int selectionStart = cursorPosition;
        if (selectionStart > 0) {
            CharSequence charSequence = message.substring(selectionStart - 1, selectionStart);
            return !TextUtils.isEmpty(charSequence.toString().trim());
        }
        return false;
    }

    public boolean isImageFile(ResMessages.FileContent fileContent) {
        if (fileContent == null) {
            return false;
        }
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);
        boolean fileFromGoogleOrDropbox = MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType);
        return !fileFromGoogleOrDropbox && fileContent.type.startsWith("image");
    }

    public boolean isMyFile(long writerId) {
        return writerId == EntityManager.getInstance().getMe().getId()
                || isTeamOwner();
    }

    public boolean isDeletedFile(String status) {
        return status.equals("archived");
    }

    public String getFileType(File file, String fileType) {
        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(
                    fileName.substring(idx + 1, fileName.length()).toLowerCase());
        } else {
            return mimeTypeMap.getExtensionFromMimeType(fileType.toLowerCase());
        }
    }

    public boolean isFileFromGoogleOrDropbox(ResMessages.FileContent fileContent) {
        if (fileContent == null) {
            return false;
        }
        return TextUtils.equals(fileContent.serverUrl, "google")
                || TextUtils.equals(fileContent.serverUrl, "dropbox");
    }
}