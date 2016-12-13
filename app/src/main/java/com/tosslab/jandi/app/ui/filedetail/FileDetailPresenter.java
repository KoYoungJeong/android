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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class FileDetailPresenter {

    public static final String TAG = FileDetailActivity.TAG;

    FileDetailModel fileDetailModel;

    private View view;
    private PublishSubject<Pair<Long, Boolean>> initializePublishSubject;
    private PublishSubject<FileStarredInfo> starredStatePublishSubject;

    private retrofit2.Call<okhttp3.ResponseBody> currentDownloadingFile;

    @Inject
    public FileDetailPresenter(View view, FileDetailModel fileDetailModel) {
        this.fileDetailModel = fileDetailModel;
        this.view = view;
        initObjects();
    }

    void initObjects() {
        initializePublishSubject = PublishSubject.create();
        initializePublishSubject.throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

        Observable.fromCallable(() ->
                fileDetailModel.getFileDetailFromServer(fileId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                        if (e.getResponseCode() == 40300) {
                            view.showNotAccessedFile();
                        } else {
                            view.showUnexpectedErrorToast();
                        }
                    }
                    view.finish();
                })
                .doOnNext(fileDetail -> {
                    if (fileDetail == null || fileDetail.messageCount <= 0) {
                        LogUtil.e(TAG, "fileDetail == null || fileDetail.messageCount <= 0");
                        view.showUnexpectedErrorToast();
                        view.finish();
                    }
                })
                .filter(fileDetail -> fileDetail != null && fileDetail.messageCount > 0)
                .map(fileDetail -> fileDetailModel.getEnableMessages(fileDetail.messageDetails))
                .doOnNext(messages -> {
                    if (messages == null || messages.size() <= 0
                            || !(messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)) {
                        LogUtil.e(TAG, "messages == null || messages.size() <= 0\n" +
                                "|| !(messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)");
                        view.showUnexpectedErrorToast();
                        view.finish();
                    }

                    view.clearFileDetailAndComments();
                })
                .filter(messages -> messages != null &&
                        messages.size() > 0 &&
                        messages.get(messages.size() - 1) instanceof ResMessages.FileMessage)
                .subscribe(messages -> {
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
                }, Throwable::printStackTrace);

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

    public void onSendCommentWithSticker(long fileId, long stickerGroupId,
                                         String stickerId, String comment,
                                         List<MentionObject> mentions) {

        Observable.fromCallable(() -> {
            return fileDetailModel.sendMessageCommentWithSticker(
                    fileId, stickerGroupId, stickerId, comment, mentions);
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(links -> {

                    StringBuilder stickerIdStringBuilder = new StringBuilder(String.valueOf(stickerGroupId));
                    stickerIdStringBuilder.append("-");
                    stickerIdStringBuilder.append(stickerId);

                    for (ResMessages.Link link : links) {
                        if (link.message instanceof ResMessages.CommentStickerMessage) {
                            SprinklrMessagePost.sendLogWithStickerFile(
                                    link.messageId, stickerIdStringBuilder.toString(), fileId
                            );
                        } else {
                            SprinklrMessagePost.sendLogWithFileComment(
                                    link.messageId, fileId,
                                    mentions.size(), fileDetailModel.hasAllMention(comment, mentions));
                        }
                    }


                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(links -> {
                    retrieveFileDetail(fileId, false);
                    view.scrollToLastComment();
                }, (t) -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    if (t instanceof RetrofitException) {
                        SprinklrMessagePost.trackFail(((RetrofitException) t).getResponseCode());
                    }
                });

    }

    public void onSendComment(long fileId, String message, List<MentionObject> mentions) {
        boolean hasAllMention = fileDetailModel.hasAllMention(message, mentions);

        Observable.fromCallable(() -> {
            return fileDetailModel.sendMessageComment(fileId, message, mentions);

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(messageId -> {
                    SprinklrMessagePost.sendLogWithFileComment(
                            messageId, fileId, mentions.size(), hasAllMention);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageId -> {
                    retrieveFileDetail(fileId, false);
                    view.scrollToLastComment();
                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));

                    if (t instanceof RetrofitException) {
                        SprinklrMessagePost.trackFail(((RetrofitException) t).getResponseCode());
                    }
                });

    }

    public void onChangeStarredState(long fileId, boolean starred) {
        starredStatePublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    private void changeStarredState(long fileId, boolean starred) {
        Completable.fromCallable(() -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (starred) {
                fileDetailModel.registStarredFile(teamId, fileId);
                view.showStarredSuccessToast();
            } else {
                fileDetailModel.unregistStarredFile(teamId, fileId);
                view.showUnstarredSuccessToast();
            }
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (starred) {
                        view.showStarredSuccessToast();
                    } else {
                        view.showUnstarredSuccessToast();
                    }
                    view.setFilesStarredState(starred);
                    view.notifyDataSetChanged();
                }, t -> {
                    Log.getStackTraceString(t);

                });
    }

    public void onChangeFileCommentStarredState(long messageId, boolean starred) {

        Completable.fromCallable(() -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (starred) {
                fileDetailModel.registStarredMessage(teamId, messageId);
            } else {
                fileDetailModel.unregistStarredMessage(teamId, messageId);
            }
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (starred) {
                        view.showCommentStarredSuccessToast();
                    } else {
                        view.showCommentUnStarredSuccessToast();
                    }

                    view.modifyCommentStarredState(messageId, starred);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());

                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                });
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

    public void joinAndMove(TopicRoom topicRoom) {
        view.showProgress();

        Completable.fromCallable(() -> {
            fileDetailModel.joinEntity(topicRoom);

            fileDetailModel.updateJoinedTopic(topicRoom.getId());

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgress();
                    view.moveToMessageListActivity(topicRoom.getId(), JandiConstants.TYPE_PUBLIC_TOPIC, topicRoom.getId(), false);
                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));

                    view.dismissProgress();
                });
    }

    public void onShareAction(long entityId, long fileId) {
        view.showProgress();
        Completable.fromCallable(() -> {
            fileDetailModel.shareMessage(fileId, entityId);

            SprinklrFileShare.sendLog(entityId, fileId);

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    retrieveFileDetail(fileId, false);

                    view.dismissProgress();

                    view.showMoveToSharedTopicDialog(entityId);
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        LogUtil.e(TAG, Log.getStackTraceString(t));

                        int errorCode = e.getStatusCode();
                        SprinklrFileShare.sendFailLog(errorCode);
                        view.dismissProgress();
                        view.showShareErrorToast();
                    } else {
                        LogUtil.e(TAG, Log.getStackTraceString(t));

                        SprinklrFileShare.sendFailLog(-1);
                        view.dismissProgress();
                        view.showShareErrorToast();

                    }

                });
    }

    public void onUnshareAction(long entityId, long fileId) {
        view.showProgress();
        Completable.fromCallable(() -> {
            fileDetailModel.unshareMessage(fileId, entityId);
            SprinklrFileUnshare.sendLog(entityId, fileId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    retrieveFileDetail(fileId, false);
                    view.dismissProgress();
                    view.showUnshareSuccessToast();

                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        LogUtil.e(TAG, Log.getStackTraceString(e));

                        int errorCode = e.getStatusCode();
                        SprinklrFileUnshare.sendFailLog(errorCode);
                        view.dismissProgress();
                        view.showUnshareErrorToast();

                    } else {
                        LogUtil.e(TAG, Log.getStackTraceString(t));

                        SprinklrFileUnshare.sendFailLog(-1);
                        view.dismissProgress();
                        view.showUnshareErrorToast();

                    }
                });
    }

    public void onDeleteFile(long fileId, long topicId) {
        view.showProgress();
        Completable.fromCallable(() -> {
            fileDetailModel.deleteFile(fileId);
            SprinklrFileDelete.sendLog(topicId, fileId);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgress();
                    view.showDeleteSuccessToast();
                    view.deliverResultToMessageList();

                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        LogUtil.e(TAG, Log.getStackTraceString(e));

                        int errorCode = e.getStatusCode();
                        SprinklrFileDelete.sendFailLog(errorCode);

                        view.dismissProgress();
                        view.showDeleteErrorToast();
                    } else {
                        LogUtil.e(TAG, Log.getStackTraceString(t));

                        SprinklrFileDelete.sendFailLog(-1);
                        view.dismissProgress();
                        view.showDeleteErrorToast();
                    }
                });
    }

    public void onDeleteComment(int messageType, long messageId, long feedbackId) {
        if (!fileDetailModel.isNetworkConneted()) {
            view.showCheckNetworkDialog(false);
            return;
        }

        // 추후 List 데이터 모델로 치환 하도록
        Pair<Integer, ResMessages.OriginalMessage>
                commentInfoFromAdapter = view.getCommentInfo(messageId);
        int adapterPosition = commentInfoFromAdapter.first;
        ResMessages.OriginalMessage comment = commentInfoFromAdapter.second;
        if (adapterPosition > 0) {
            view.removeComment(adapterPosition);
            view.notifyDataSetChanged();
        }

        Completable.fromCallable(() -> {
            if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                fileDetailModel.deleteStickerComment(feedbackId, messageId);
            } else {
                fileDetailModel.deleteComment(messageId, feedbackId);
            }
            SprinklrMessageDelete.sendLog(messageId);

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {}, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));

                    view.showCommentDeleteErrorToast();

                    if (adapterPosition > 0 && comment != null && comment.id > 0) {
                        view.addComment(adapterPosition, comment);
                        view.notifyDataSetChanged();
                    }
                    if (t instanceof RetrofitException) {
                        SprinklrMessageDelete.sendFailLog(((RetrofitException) t).getResponseCode());
                    }
                });
    }

    public void onEnableExternalLink(long fileId) {
        view.showProgress();


        Observable.fromCallable(() -> {
            long teamId = fileDetailModel.getTeamId();
            ResMessages.FileMessage fileMessage = fileDetailModel.enableExternalLink(teamId, fileId);
            SprinklrPublicLinkCreated.sendLog(fileId);
            return fileMessage;

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileMessage -> {
                    setFileDetailToView(fileMessage);
                    view.notifyDataSetChanged();
                    view.dismissProgress();
                    view.setExternalLinkToClipboard();

                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    view.dismissProgress();
                    view.showUnexpectedErrorToast();
                    if (t instanceof RetrofitException) {
                        SprinklrPublicLinkCreated.sendFailLog(((RetrofitException) t).getResponseCode());
                    }

                });

    }

    public void onDisableExternalLink(long fileId) {
        view.showProgress();

        Observable.fromCallable(() -> {
            long teamId = fileDetailModel.getTeamId();
            ResMessages.FileMessage fileMessage = fileDetailModel.disableExternalLink(teamId, fileId);
            SprinklrPublicLinkDeleted.sendLog(fileId);
            return fileMessage;

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileMessage -> {
                    setFileDetailToView(fileMessage);
                    view.notifyDataSetChanged();

                    view.dismissProgress();

                    view.showDisableExternalLinkSuccessToast();

                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    view.dismissProgress();
                    view.showUnexpectedErrorToast();
                    if (t instanceof RetrofitException) {
                        SprinklrPublicLinkDeleted.sendFailLog(((RetrofitException) t).getResponseCode());
                    }

                });
    }


    public void onTopicDeleted(long entityId,
                               Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities) {
        if (shareEntities == null || shareEntities.size() <= 0) {
            return;
        }

        Observable.from(shareEntities)
                .takeFirst(integerWrapper -> entityId == integerWrapper.getShareEntity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> view.finish());

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
