package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.ui.filedetail.domain.FileStarredInfo;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
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

    private Future<File> currentDownloadingFile;

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
                        view.showCheckNetworkDialog();
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
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.showUnexpectedErrorToast();
            view.finish();
            return;
        }

        if (fileDetail == null || fileDetail.messageCount <= 0) {
            view.showUnexpectedErrorToast();
            view.finish();
            return;
        }

        List<ResMessages.OriginalMessage> messages =
                fileDetailModel.getEnableMessages(fileDetail.messageDetails);
        if (messages == null || messages.size() <= 0
                || !(messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)) {
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
        view.showProgress();
        try {
            fileDetailModel.sendMessageCommentWithSticker(
                    fileId, stickerGroupId, stickerId, comment, mentions);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.scrollToLastComment();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
        }
    }

    @Background(serial = "file_detail_background")
    public void onSendComment(long fileId, String message, List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageComment(fileId, message, mentions);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.scrollToLastComment();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
        }
    }

    public void onChangeStarredState(long fileId, boolean starred) {
        starredStatePublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    private void changeStarredState(long fileId, boolean starred) {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        try {
            if (starred) {
                fileDetailModel.registStarredMessage(teamId, fileId);
                view.showStarredSuccessToast();
            } else {
                fileDetailModel.unregistStarredMessage(teamId, fileId);
                view.showUnstarredSuccessToast();
            }
            view.setFilesStarredState(starred);
            view.notifyDataSetChanged();
        } catch (RetrofitError e) {
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
        } catch (RetrofitError e) {
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
            return;
        }

        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() -> {
                    DownloadService.start(fileId,
                            ImageUtil.getImageFileUrl(fileContent.fileUrl),
                            fileContent.title,
                            fileContent.ext,
                            fileContent.type);
                })
                .noPermission(() -> {
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }).check();
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
        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() ->
                        downloadFileAndManage(FileManageType.EXPORT, fileMessage, progressDialog))
                .noPermission(() -> {
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION_EXPORT,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }).check();
    }

    public void onOpenFile(final ResMessages.FileMessage fileMessage,
                           final ProgressDialog progressDialog) {
        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(() -> {
                    downloadFileAndManage(FileManageType.OPEN, fileMessage, progressDialog);
                })
                .noPermission(() -> {
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }).check();
    }

    void downloadFileAndManage(final FileManageType type,
                               ResMessages.FileMessage fileMessage, ProgressDialog progressDialog) {
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        String downloadFilePath = fileDetailModel.getDownloadFilePath(fileMessage.content.title);
        String downloadUrl = fileDetailModel.getDownloadUrl(fileMessage.content.fileUrl);
        final String mimeType = fileMessage.content.type;

        currentDownloadingFile =
                fileDetailModel.downloadFile(downloadUrl, downloadFilePath,
                        (downloaded, total) -> progressDialog.setProgress((int) (downloaded * 100 / total)),
                        (e, result) -> {
                            progressDialog.dismiss();
                            if (currentDownloadingFile.isCancelled()) {
                                currentDownloadingFile = null;
                                return;
                            }
                            currentDownloadingFile = null;
                            if (e == null && result != null) {
                                if (type == FileManageType.EXPORT) {
                                    view.startExportedFileViewerActivity(result, mimeType);
                                } else if (type == FileManageType.OPEN) {
                                    view.startDownloadedFileViewerActivity(result, mimeType);
                                }
                            } else {
                                view.showUnexpectedErrorToast();
                            }
                        });
    }

    public void cancelCurrentDownloading() {
        if (currentDownloadingFile != null) {
            currentDownloadingFile.cancel(true);
        }
    }

    @Background
    public void joinAndMove(FormattedEntity entity) {
        view.showProgress();

        try {
            fileDetailModel.joinEntity(entity);

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.refreshEntity();

            view.dismissProgress();

            view.moveToMessageListActivity(entity.getId(), entityType, entity.getId(), false);
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

            fileDetailModel.refreshEntity();

            fileDetailModel.trackFileShareSuccess(entityId, fileId);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.showMoveToSharedTopicDialog(entityId);
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileShareFail(errorCode);
            view.dismissProgress();
            view.showShareErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            fileDetailModel.trackFileShareFail(-1);
            view.dismissProgress();
            view.showShareErrorToast();
        }
    }

    @Background
    public void onUnshareAction(long entityId, long fileId) {
        view.showProgress();
        try {
            fileDetailModel.unshareMessage(fileId, entityId);

            fileDetailModel.refreshEntity();

            fileDetailModel.trackFileUnShareSuccess(entityId, fileId);

            retrieveFileDetail(fileId, false);

            view.dismissProgress();

            view.showUnshareSuccessToast();
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileUnShareFail(errorCode);
            view.dismissProgress();
            view.showUnshareErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            fileDetailModel.trackFileUnShareFail(-1);
            view.dismissProgress();
            view.showUnshareErrorToast();
        }
    }

    @Background
    public void onDeleteFile(long fileId, long topicId) {
        view.showProgress();
        try {
            fileDetailModel.deleteFile(fileId);

            fileDetailModel.trackFileDeleteSuccess(topicId, fileId);

            view.dismissProgress();

            view.showDeleteSuccessToast();

            view.deliverResultToMessageList();
        } catch (RetrofitError e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileDeleteFail(errorCode);

            view.dismissProgress();
            view.showDeleteErrorToast();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            fileDetailModel.trackFileDeleteFail(-1);
            view.dismissProgress();
            view.showDeleteErrorToast();
        }
    }

    @Background
    public void onDeleteComment(int messageType, long messageId, long feedbackId) {
        view.showProgress();
        try {
            if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                fileDetailModel.deleteStickerComment(messageId, MessageItem.TYPE_STICKER_COMMNET);
            } else {
                fileDetailModel.deleteComment(messageId, feedbackId);
            }

        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
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
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
            view.showUnexpectedErrorToast();
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
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
            view.showUnexpectedErrorToast();
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

    enum FileManageType {
        EXPORT, OPEN
    }

    public interface View {
        void showProgress();

        void dismissProgress();

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

        void showCheckNetworkDialog();

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
    }

}
