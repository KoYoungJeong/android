package com.tosslab.jandi.app.ui.filedetail.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.FileDetailRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.sticker.StickerApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.Lazy;
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

    @Inject
    Lazy<StickerApi> stickerApi;
    @Inject
    Lazy<MessageApi> messageApi;

    @Inject
    Lazy<FileApi> fileApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public boolean isNetworkConneted() {
        return NetworkCheckUtil.isConnected();
    }

    public void deleteFile(long fileId) throws RetrofitException {
        entityClientManager.deleteFile(fileId);
    }

    public ResFileDetail getFileDetailFromServer(long fileId) throws RetrofitException {
        return entityClientManager.getFileDetail(fileId);
    }

    public void shareMessage(long fileId, long entityIdToBeShared) throws RetrofitException {
        entityClientManager.shareMessage(fileId, entityIdToBeShared);
    }

    public void unshareMessage(long fileId, long entityIdToBeUnshared) throws RetrofitException {
        entityClientManager.unshareMessage(fileId, entityIdToBeUnshared);
    }

    public void sendMessageComment(long fileId, String message, List<MentionObject> mentions) throws RetrofitException {
        entityClientManager.sendMessageComment(fileId, message, mentions);
    }

    public void deleteComment(long messageId, long feedbackId) throws RetrofitException {
        entityClientManager.deleteMessageComment(messageId, feedbackId);
    }

    public void deleteStickerComment(long messageId, int messageType) throws RetrofitException {
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

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();


        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
        Observable.from(fileMessage.shareEntities)
                .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .filter(shareEntity -> TeamInfoLoader.getInstance().isTopic(shareEntity))
                .collect(() -> sharedTopicIds, List::add)
                .subscribe();

        return sharedTopicIds;
    }

    public ResCommon joinEntity(TopicRoom entity) throws RetrofitException {

        return entityClientManager.joinChannel(entity.getId());

    }

    public boolean refreshEntity() {
        try {
//            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
//            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
//            EntityManager.getInstance().refreshEntity();
            return true;
//        } catch (RetrofitException e) {
//            e.printStackTrace();
//            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessageCommentWithSticker(long fileId, long stickerGroupId, String stickerId, String comment, List<MentionObject> mentions) throws RetrofitException {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            ReqSendSticker reqSendSticker = ReqSendSticker.create(stickerGroupId, stickerId, teamId, fileId, "", comment, mentions);
            stickerApi.get().sendStickerComment(reqSendSticker);
        } catch (RetrofitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trackFileShareSuccess(long topicId, long fileId) {

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileShare)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .property(PropertyKey.FileId, fileId)
                .build());

    }

    public void trackFileShareFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileShare)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

    public void trackFileUnShareSuccess(long topicId, long fileId) {

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileUnShare)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .property(PropertyKey.FileId, fileId)
                .build());

    }

    public void trackFileUnShareFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileUnShare)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

    public void trackFileDeleteSuccess(long topicId, long fileId) {

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileDelete)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, topicId)
                .property(PropertyKey.FileId, fileId)
                .build());

    }

    public void trackFileDeleteFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileDelete)
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
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void unregistStarredMessage(long teamId, long messageId) throws RetrofitException {
        try {
            messageApi.get().unregistStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void sortByDate(List<ResMessages.OriginalMessage> messages) {
        Collections.sort(messages, (lhs, rhs) -> lhs.createTime.before(rhs.createTime) ? -1 : 1);
    }

    public long getMyId() {
        return TeamInfoLoader.getInstance().getMyId();
    }

    public ResMessages.FileMessage getFileMessage(long fileId) {

        return MessageRepository.getRepository().getFileMessage(fileId);
    }

    public boolean isTeamOwner() {
        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).isTeamOwner();
    }


    public long getTeamId() {
        return TeamInfoLoader.getInstance().getTeamId();
    }

    public ResMessages.FileMessage enableExternalLink(long teamId, long fileId) throws RetrofitException {
        return fileApi.get().enableFileExternalLink(teamId, fileId, new ReqNull());
    }

    public ResMessages.FileMessage disableExternalLink(long teamId, long fileId) throws RetrofitException {
        return fileApi.get().disableFileExternalLink(teamId, fileId);
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
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent())
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
        return writerId == TeamInfoLoader.getInstance().getMyId()
                || TeamInfoLoader.getInstance().getUser(writerId).isTeamOwner();
    }

    public boolean isDeletedFile(String status) {
        return status.equals("archived");
    }

    public boolean isFileFromGoogleOrDropbox(ResMessages.FileContent fileContent) {
        if (fileContent == null) {
            return false;
        }
        return TextUtils.equals(fileContent.serverUrl, "google")
                || TextUtils.equals(fileContent.serverUrl, "dropbox");
    }

    public List<TopicRoom> getTopicRooms() {
        List<TopicRoom> topicList = TeamInfoLoader.getInstance().getTopicList();

        return Observable.from(topicList)
                .filter(TopicRoom::isJoined)
                .toSortedList((formattedEntity, formattedEntity2) -> {
                    return StringCompareUtil.compare(formattedEntity.getName(), formattedEntity2.getName());
                })
                .toBlocking().first();
    }

    public List<Member> getMembers() {
        List<Member> members = new ArrayList<>();
        List<User> first = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(user -> TeamInfoLoader.getInstance().getMyId() != user.getId())
                .toSortedList((formattedEntity, formattedEntity2) -> {
                    return StringCompareUtil.compare(formattedEntity.getName(), formattedEntity2.getName());
                })
                .toBlocking().first();

        members.add(TeamInfoLoader.getInstance().getJandiBot());
        members.addAll(first);
        return members;
    }

    public void updateJoinedTopic(long id) {
        TopicRepository.getInstance().updateTopicJoin(id, true);
        TeamInfoLoader.getInstance().refresh();
    }
}