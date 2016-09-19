package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.Check;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.filedetail.domain.FileStarredInfo;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileDelete;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileShare;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileUnshare;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrMessageDelete;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrMessagePost;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPublicLinkCreated;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPublicLinkDeleted;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@EBean
public class FileDetailPresenter {

    public static final String TAG = FileDetailActivity.TAG;

    @Bean
    FileDetailModel fileDetailModel;

    private View view;
    private PublishSubject<Pair<Long, Boolean>> initializePublishSubject;
    private PublishSubject<FileStarredInfo> starredStatePublishSubject;

    private retrofit2.Call<okhttp3.ResponseBody> currentDownloadingFile;

    public void setView(View view) {
        this.view = view;
    }

    @AfterInject
    void initObjects() {
        initializePublishSubject = PublishSubject.create();
        initializePublishSubject.throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .subscribe(pair -> {
                    boolean isNetworkConnected = fileDetailModel.isNetworkConneted();
                    if (!isNetworkConnected) {
                        view.showCheckNetworkDialog(true);
                        return;
                    }

                    retrieveFileDetail(pair.first, pair.second);
                });

        starredStatePublishSubject = PublishSubject.create();
        starredStatePublishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .subscribe(fileStarredInfo -> {
                    changeStarredState(fileStarredInfo.getFileId(), fileStarredInfo.isStarred());
                }, Throwable::printStackTrace);
    }

    public void onInitializeFileDetail(long fileId, boolean withProgress) {
        initializePublishSubject.onNext(Pair.create(fileId, withProgress));
    }

    // 화면 진입시, 코멘트(스티커 포함) 보낼 때 사용되어 짐
    private void retrieveFileDetail(long fileId, boolean withProgress) {
        if (withProgress) {
            view.showProgress();
        }

        ResFileDetail fileDetail = null;
        try {
            fileDetail = fileDetailModel.getFileDetailFromServer(fileId);
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            if (e.getResponseCode() == 40300) {
                view.showNotAccessedFile();
            } else {
                view.showUnexpectedErrorToast();
            }
            view.finish();
            return;
        }

        if (fileDetail == null || fileDetail.messageCount <= 0) {
            LogUtil.e(TAG, "fileDetail == null || fileDetail.messageCount <= 0");
            view.showUnexpectedErrorToast();
            view.finish();
            return;
        }

        List<ResMessages.OriginalMessage> messages =
                fileDetailModel.getEnableMessages(fileDetail.messageDetails);
        if (messages == null || messages.size() <= 0
                || !(messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)) {
            LogUtil.e(TAG, "messages == null || messages.size() <= 0\n" +
                    "|| !(messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)");
            view.showUnexpectedErrorToast();
            view.finish();
            return;
        }

        view.clearFileDetailAndComments();

        // 파일의 상세정보는 API 로 부터 받아온 리스트의 마지막에 있다.
        int fileDetailPosition = messages.size() - 1;
        ResMessages.FileMessage fileMessage =
                (ResMessages.FileMessage) messages.get(fileDetailPosition);
        if (fileMessage.content != null) {
            setFileDetailToView(fileMessage);

            messages.remove(fileDetailPosition);
        }

        if (messages.size() > 0) {
            fileDetailModel.sortByDate(messages);
            view.setComments(messages);
        }

        view.notifyDataSetChanged();

        if (withProgress) {
            view.dismissProgress();
        }
    }

    private void setFileDetailToView(ResMessages.FileMessage fileMessage) {
        boolean isImageFile = fileDetailModel.isImageFile(fileMessage.content);
        boolean isDeletedFile = fileDetailModel.isDeletedFile(fileMessage.status);
        boolean isMyFile = fileDetailModel.isMyFile(fileMessage.writerId);
        boolean isExternalSharedFile = fileMessage.content.externalShared;
        List<Long> sharedTopicIds = fileDetailModel.getSharedTopicIds(fileMessage);
        view.setFileDetail(fileMessage, sharedTopicIds,
                isMyFile, isDeletedFile, isImageFile, isExternalSharedFile);
    }

    @Background(serial = "file_detail_background")
    public void onSendCommentWithSticker(long fileId, long stickerGroupId,
                                         String stickerId, String comment,
                                         List<MentionObject> mentions) {
        try {
            long messageId = fileDetailModel.sendMessageCommentWithSticker(
                    fileId, stickerGroupId, stickerId, comment, mentions);

            retrieveFileDetail(fileId, false);

            view.scrollToLastComment();

            StringBuilder stickerIdStringBuilder = new StringBuilder(String.valueOf(stickerGroupId));
            stickerIdStringBuilder.append("-");
            stickerIdStringBuilder.append(stickerId);

            SprinklrMessagePost.sendLogWithStickerFile(
                    messageId, stickerIdStringBuilder.toString(), fileId,
                    mentions.size(), fileDetailModel.hasAllMention(comment, mentions));

        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            if (e instanceof RetrofitException) {
                SprinklrMessagePost.trackFail(((RetrofitException) e).getResponseCode());
            }
        }
    }

    @Background(serial = "file_detail_background")
    public void onSendComment(long fileId, String message, List<MentionObject> mentions) {
        boolean hasAllMention = fileDetailModel.hasAllMention(message, mentions);

        try {
            long messageId = fileDetailModel.sendMessageComment(fileId, message, mentions);

            retrieveFileDetail(fileId, false);

            view.scrollToLastComment();

            SprinklrMessagePost.sendLogWithFileComment(
                    messageId, fileId, mentions.size(), hasAllMention);
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            if (e instanceof RetrofitException) {
                SprinklrMessagePost.trackFail(((RetrofitException) e).getResponseCode());
            }
        }
    }

    public void onChangeStarredState(long fileId, boolean starred) {
        starredStatePublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    private void changeStarredState(long fileId, boolean starred) {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        try {
            if (starred) {
                fileDetailModel.registStarredFile(teamId, fileId);
                view.showStarredSuccessToast();
            } else {
                fileDetailModel.unregistStarredFile(teamId, fileId);
                view.showUnstarredSuccessToast();
            }
            view.setFilesStarredState(starred);
            view.notifyDataSetChanged();
        } catch (RetrofitException e) {
            Log.getStackTraceString(e);
        }
    }

    @Background
    public void onChangeFileCommentStarredState(long messageId, boolean starred) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (starred) {
                fileDetailModel.registStarredMessage(teamId, messageId);
                view.showCommentStarredSuccessToast();
            } else {
                fileDetailModel.unregistStarredMessage(teamId, messageId);
                view.showCommentUnStarredSuccessToast();
            }
            view.modifyCommentStarredState(messageId, starred);

            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void onDownloadAction(final long fileId, final ResMessages.FileContent fileContent) {
        if (fileContent == null) {
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            String fileUrl = ImageUtil.getImageFileUrl(fileContent.fileUrl);
            view.startGoogleOrDropboxFileActivity(fileUrl);
        } else {
            view.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    () -> DownloadService.start(fileId,
                            ImageUtil.getImageFileUrl(fileContent.fileUrl),
                            fileContent.title,
                            fileContent.ext,
                            fileContent.type),
                    () -> view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }


    }

    public void onExportFile(final ResMessages.FileMessage fileMessage,
                             final ProgressDialog progressDialog) {
        if (fileDetailModel.isFileFromGoogleOrDropbox(fileMessage.content)) {
            view.dismissDialog(progressDialog);

            if (TextUtils.isEmpty(fileMessage.content.fileUrl)) {
                view.showUnexpectedErrorToast();
            } else {
                view.exportLink(fileMessage.content.fileUrl);
            }

            return;
        }
        view.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                () -> {
                    view.showDialog(progressDialog);
                    downloadFileAndManage(FileManageType.EXPORT, fileMessage, progressDialog);
                },
                () -> {
                    view.dismissDialog(progressDialog);
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION_EXPORT,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                });
    }

    public void onOpenFile(final ResMessages.FileMessage fileMessage,
                           final ProgressDialog progressDialog) {
        view.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                () -> {
                    view.showDialog(progressDialog);
                    downloadFileAndManage(FileManageType.OPEN, fileMessage, progressDialog);
                },
                () -> {
                    view.dismissDialog(progressDialog);
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION_OPEN,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                });
    }

    void downloadFileAndManage(final FileManageType type,
                               ResMessages.FileMessage fileMessage, ProgressDialog progressDialog) {
        if (fileMessage == null || fileMessage.content == null) {
            view.dismissDialog(progressDialog);
            return;
        }

        String downloadFilePath = fileDetailModel.getDownloadFilePath(fileMessage.content.title);
        String downloadUrl = fileDetailModel.getDownloadUrl(fileMessage.content.fileUrl);
        final String mimeType = fileMessage.content.type;

        currentDownloadingFile = fileDetailModel.downloadFile(downloadUrl, downloadFilePath,
                callback -> callback.distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            progressDialog.setMax(100);
                            progressDialog.setProgress(it);
                        }, t -> {
                            t.printStackTrace();
                            view.dismissDialog(progressDialog);
                            if (currentDownloadingFile == null || currentDownloadingFile.isCanceled()) {
                                currentDownloadingFile = null;
                                return;
                            }
                            view.showUnexpectedErrorToast();
                        }, () -> {
                            view.dismissDialog(progressDialog);
                            if (currentDownloadingFile == null || currentDownloadingFile.isCanceled()) {
                                currentDownloadingFile = null;
                                return;
                            }
                            if (type == FileDetailPresenter.FileManageType.EXPORT) {
                                view.startExportedFileViewerActivity(new File(fileDetailModel.getDownloadFilePath(fileMessage.content.title)), mimeType);
                            } else if (type == FileDetailPresenter.FileManageType.OPEN) {
                                view.startDownloadedFileViewerActivity(new File(fileDetailModel.getDownloadFilePath(fileMessage.content.title)), mimeType);
                            }
                        }));
    }

    public void cancelCurrentDownloading() {
        if (currentDownloadingFile != null && currentDownloadingFile.isExecuted()) {
            currentDownloadingFile.cancel();
        }
    }

    @Background
    public void joinAndMove(TopicRoom topicRoom) {
        view.showProgress();

        try {
            fileDetailModel.joinEntity(topicRoom);

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.updateJoinedTopic(topicRoom.getId());

            view.dismissProgress();

            view.moveToMessageListActivity(topicRoom.getId(), entityType, topicRoom.getId(), false);
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            view.dismissProgress();
        }
    }

    @Background
    public void onShareAction(long entityId, long fileId) {
        view.showProgress();
        try {
            fileDetailModel.shareMessage(fileId, entityId);

            // FIXME "왜 share/unshare 후 entity 갱신이 있지"
//            fileDetailModel.refreshEntity();

            SprinklrFileShare.sendLog(entityId, fileId);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.showMoveToSharedTopicDialog(entityId);
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getStatusCode();
            SprinklrFileShare.sendFailLog(errorCode);
            view.dismissProgress();
            view.showShareErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            SprinklrFileShare.sendFailLog(-1);
            view.dismissProgress();
            view.showShareErrorToast();
        }
    }

    @Background
    public void onUnshareAction(long entityId, long fileId) {
        view.showProgress();
        try {
            fileDetailModel.unshareMessage(fileId, entityId);

//            fileDetailModel.refreshEntity();

            SprinklrFileUnshare.sendLog(entityId, fileId);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.showUnshareSuccessToast();
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getStatusCode();
            SprinklrFileUnshare.sendFailLog(errorCode);
            view.dismissProgress();
            view.showUnshareErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            SprinklrFileUnshare.sendFailLog(-1);
            view.dismissProgress();
            view.showUnshareErrorToast();
        }
    }

    @Background
    public void onDeleteFile(long fileId, long topicId) {
        view.showProgress();
        try {
            fileDetailModel.deleteFile(fileId);

            SprinklrFileDelete.sendLog(topicId, fileId);

            view.dismissProgress();

            view.showDeleteSuccessToast();

            view.deliverResultToMessageList();
        } catch (RetrofitException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getStatusCode();
            SprinklrFileDelete.sendFailLog(errorCode);

            view.dismissProgress();
            view.showDeleteErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            SprinklrFileDelete.sendFailLog(-1);
            view.dismissProgress();
            view.showDeleteErrorToast();
        }
    }

    @Background
    public void onDeleteComment(int messageType, long messageId, long feedbackId) {
        if (!fileDetailModel.isNetworkConneted()) {
            view.showCheckNetworkDialog(false);
            return;
        }

        // 추후 List 데이터 모델로 치환 하도록
        Pair<Integer, ResMessages.OriginalMessage>
                commentInfoFromAdapter = view.getCommentInfo(messageId);
        Integer adapterPosition = commentInfoFromAdapter.first;
        ResMessages.OriginalMessage comment = commentInfoFromAdapter.second;
        if (adapterPosition > 0) {
            view.removeComment(adapterPosition);
            view.notifyDataSetChanged();
        }

        try {
            if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                fileDetailModel.deleteStickerComment(messageId, MessageItem.TYPE_STICKER_COMMNET);
            } else {
                fileDetailModel.deleteComment(messageId, feedbackId);
            }
            SprinklrMessageDelete.sendLog(messageId);
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            view.showCommentDeleteErrorToast();

            if (adapterPosition > 0 && comment != null && comment.id > 0) {
                view.addComment(adapterPosition, comment);
                view.notifyDataSetChanged();
            }
            if (e instanceof RetrofitException) {
                SprinklrMessageDelete.sendFailLog(((RetrofitException) e).getResponseCode());
            }
        }
    }

    @Background
    public void onEnableExternalLink(long fileId) {
        view.showProgress();

        long teamId = fileDetailModel.getTeamId();

        try {
            ResMessages.FileMessage fileMessage = fileDetailModel.enableExternalLink(teamId, fileId);

            setFileDetailToView(fileMessage);

            view.notifyDataSetChanged();

            view.dismissProgress();

            view.setExternalLinkToClipboard();

            SprinklrPublicLinkCreated.sendLog(fileId);

        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
            view.showUnexpectedErrorToast();
            if (e instanceof RetrofitException) {
                SprinklrPublicLinkCreated.sendFailLog(((RetrofitException) e).getResponseCode());
            }
        }
    }

    @Background
    public void onDisableExternalLink(long fileId) {
        view.showProgress();

        long teamId = fileDetailModel.getTeamId();

        try {
            ResMessages.FileMessage fileMessage = fileDetailModel.disableExternalLink(teamId, fileId);
            setFileDetailToView(fileMessage);
            view.notifyDataSetChanged();

            view.dismissProgress();

            view.showDisableExternalLinkSuccessToast();

            SprinklrPublicLinkDeleted.sendLog(fileId);

        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
            view.showUnexpectedErrorToast();
            if (e instanceof RetrofitException) {
                SprinklrPublicLinkDeleted.sendFailLog(((RetrofitException) e).getResponseCode());
            }
        }
    }


    public void onTopicDeleted(long entityId,
                               Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities) {
        if (shareEntities == null || shareEntities.size() <= 0) {
            return;
        }

        Iterator<ResMessages.OriginalMessage.IntegerWrapper> iterator = shareEntities.iterator();
        while (iterator.hasNext()) {
            ResMessages.OriginalMessage.IntegerWrapper integerWrapper = iterator.next();

            if (entityId == integerWrapper.getShareEntity()) {
                view.finish();
                return;
            }
        }
    }

    public enum FileManageType {
        EXPORT, OPEN
    }

    public interface View {
        void showProgress();

        void dismissProgress();

        void showDialog(Dialog dialog);

        void dismissDialog(Dialog dialog);

        void showUnexpectedErrorToast();

        void showStarredSuccessToast();

        void showUnstarredSuccessToast();

        void showCommentStarredSuccessToast();

        void showCommentUnStarredSuccessToast();

        void modifyCommentStarredState(long messageId, boolean starred);

        void showShareErrorToast();

        void showUnshareSuccessToast();

        void showUnshareErrorToast();

        void showDeleteSuccessToast();

        void showDeleteErrorToast();

        void showEnableExternalLinkSuccessToast();

        void showDisableExternalLinkSuccessToast();

        void showMoveToSharedTopicDialog(long entityId);

        void showCheckNetworkDialog(boolean shouldFinishWhenConfirm);

        void showKeyboard();

        void hideKeyboard();

        void exportLink(String link);

        void copyToClipboard(String text);

        void setExternalLinkToClipboard();

        void clearFileDetailAndComments();

        void setFileDetail(ResMessages.FileMessage fileMessage, List<Long> sharedTopicIds,
                           boolean isMyFile, boolean isDeletedFile,
                           boolean isImageFile, boolean isExternalShared);

        void setComments(List<ResMessages.OriginalMessage> fileComments);

        void notifyDataSetChanged();

        void scrollToLastComment();

        void setFilesStarredState(boolean starred);

        void startGoogleOrDropboxFileActivity(String fileUrl);

        void requestPermission(int requestCode, String... permissions);

        void startExportedFileViewerActivity(File file, String mimeType);

        void startDownloadedFileViewerActivity(File file, String mimeType);

        void moveToMessageListActivity(long entityId, int entityType, long roomId, boolean isStarred);

        void deliverResultToMessageList();

        void finish();

        void checkPermission(String persmissionString, Check.HasPermission hasPermission, Check.NoPermission noPermission);

        Pair<Integer, ResMessages.OriginalMessage> getCommentInfo(long messageId);

        void removeComment(int position);

        void showCommentDeleteErrorToast();

        void addComment(int adapterPosition, ResMessages.OriginalMessage comment);

        void showNotAccessedFile();
    }

}
