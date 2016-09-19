package com.tosslab.jandi.app.ui.carousel.presenter;

import android.Manifest;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity;
import com.tosslab.jandi.app.ui.filedetail.FileDetailPresenter;
import com.tosslab.jandi.app.ui.filedetail.domain.FileStarredInfo;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileDelete;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileShare;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileUnshare;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPublicLinkCreated;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrPublicLinkDeleted;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
public class CarouselViewerPresenterImpl implements CarouselViewerPresenter {

    private final CarouselViewerModel carouselViewerModel;
    private final FileDetailModel fileDetailModel;
    private final View view;
    private boolean isFirst;
    private boolean isLast;

    private PublishSubject<FileStarredInfo> starredStatePublishSubject;
    private Subscription starredStatePublishSubjectSubs;
    private Call<ResponseBody> currentDownloadingFile;

    @Inject
    public CarouselViewerPresenterImpl(CarouselViewerModel carouselViewerModel, FileDetailModel fileDetailModel, View view) {
        this.carouselViewerModel = carouselViewerModel;
        this.fileDetailModel = fileDetailModel;
        this.view = view;

        initStarQueue();
    }

    void initStarQueue() {
        starredStatePublishSubject = PublishSubject.create();
        starredStatePublishSubjectSubs =
                starredStatePublishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .concatMap(fileStarredInfo -> Observable.defer(() -> {
                            boolean starred = fileStarredInfo.isStarred();
                            long fileId = fileStarredInfo.getFileId();
                            long teamId = AccountRepository.getRepository().getSelectedTeamId();
                            try {
                                if (starred) {
                                    fileDetailModel.registStarredFile(teamId, fileId);
                                } else {
                                    fileDetailModel.unregistStarredFile(teamId, fileId);
                                }
                                return Observable.just(fileStarredInfo);
                            } catch (Exception e) {
                                return Observable.error(e);
                            }
                        }))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fileStarredInfo -> {
                            if (fileStarredInfo.isStarred()) {
                                view.showStarredSuccessToast();
                            } else {
                                view.showUnstarredSuccessToast();
                            }
                        }, Throwable::printStackTrace);
    }

    @Override
    public void onInitImageFiles(long roomId, long startFileMessageId) {
        long teamId = carouselViewerModel.getTeamId();
        carouselViewerModel.getImageFileListObservable(teamId, roomId, startFileMessageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileMessages -> {
                    List<CarouselFileInfo> imageFileList =
                            carouselViewerModel.getImageFileConvert(roomId, fileMessages);
                    setImageFiles(imageFileList, startFileMessageId);
                }, e -> {
                    e.printStackTrace();
                    view.setInitFail();
                });
    }

    @Override
    public void onInitImageSingleFile(CarouselFileInfo singleImageInfo) {
        if (singleImageInfo == null) {
            return;
        }

        List<CarouselFileInfo> imageFiles = Arrays.asList(singleImageInfo);
        setImageFiles(imageFiles, singleImageInfo.getFileMessageId());
    }

    private void setImageFiles(List<CarouselFileInfo> imageFiles, long currentFileMessageId) {
        if (imageFiles.size() > 0) {
            view.addFileInfos(imageFiles);
            if (imageFiles.size() == 1) {
                view.setVisibilitySwipeToLeftButton(false);
                view.setVisibilitySwipeToRightButton(false);
            }
            int startLinkPosition =
                    carouselViewerModel.findLinkPosition(imageFiles, currentFileMessageId);
            if (startLinkPosition >= 0) {
                CarouselFileInfo carouselFirstFileInfo = imageFiles.get(startLinkPosition);

                view.movePosition(startLinkPosition);

                view.initCarouselInfo(carouselFirstFileInfo);
            }
        }
    }

    @Override
    public void onBeforeImageFiles(long roomId, long fileLinkId, int count) {

        if (isFirst()) {
            return;
        }

        long teamId = carouselViewerModel.getTeamId();
        carouselViewerModel.getBeforeImageFileListObservable(teamId, roomId, fileLinkId, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    return new ArrayList<>();
                })
                .subscribe(fileMessages -> {
                    List<CarouselFileInfo> imageFiles =
                            carouselViewerModel.getImageFileConvert(roomId, fileMessages);

                    setIsFirst(imageFiles.size() < count);
                    if (imageFiles.size() > 0) {
                        view.addFileInfos(0, imageFiles);
                    }
                }, Throwable::printStackTrace);
    }

    @Override
    public void onAfterImageFiles(long roomId, long fileLinkId, int count) {

        if (isLast()) {
            return;
        }

        long teamId = carouselViewerModel.getTeamId();
        carouselViewerModel.getAfterImageFileListObservable(teamId, roomId, fileLinkId, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    LogUtil.e(Log.getStackTraceString(throwable));
                    return new ArrayList<>();
                })
                .subscribe(fileMessages -> {
                    List<CarouselFileInfo> imageFiles =
                            carouselViewerModel.getImageFileConvert(roomId, fileMessages);
                    setIsLast(imageFiles.size() < count);
                    if (imageFiles.size() > 0) {
                        view.addFileInfos(imageFiles);
                    }
                });
    }

    @Override
    public void onFileDownload(CarouselFileInfo fileInfo) {
        DownloadService.start(fileInfo.getFileMessageId(),
                fileInfo.getFileOriginalUrl(),
                fileInfo.getFileName(),
                fileInfo.getExt(),
                fileInfo.getFileType());
    }

    @Override
    public void onChangeStarredState(long fileId, boolean starred) {
        starredStatePublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    @Override
    public void onExportFile(CarouselFileInfo carouselFileInfo, ProgressDialog progressDialog) {
        if (carouselViewerModel.isFileFromGoogleOrDropbox(carouselFileInfo.getServerUrl())) {
            view.dismissDialog(progressDialog);

            if (TextUtils.isEmpty(carouselFileInfo.getFileOriginalUrl())) {
                view.showUnexpectedErrorToast();
            } else {
                view.exportLink(carouselFileInfo.getFileOriginalUrl());
            }

            return;
        }
        view.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                () -> {
                    view.showDialog(progressDialog);
                    downloadFileAndManage(FileDetailPresenter.FileManageType.EXPORT,
                            carouselFileInfo, progressDialog);
                },
                () -> {
                    view.dismissDialog(progressDialog);
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION_EXPORT,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                });
    }

    @Override
    public void onDeleteFile(long fileMessageId) {
        view.showProgress();
        Observable.create(subscriber -> {
            try {
                fileDetailModel.deleteFile(fileMessageId);
                SprinklrFileDelete.sendLog(-1, fileMessageId);
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.showDeleteSuccessToast();
                    view.finish();
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));

                    if (e instanceof RetrofitException) {
                        int errorCode = ((RetrofitException) e).getStatusCode();
                        SprinklrFileDelete.sendFailLog(errorCode);
                    } else {
                        SprinklrFileDelete.sendFailLog(-1);
                    }

                    view.dismissProgress();
                    view.showDeleteErrorToast();
                });
    }

    @Override
    public void onEnableExternalLink(final CarouselFileInfo carouselFileInfo) {
        view.showProgress();

        long teamId = fileDetailModel.getTeamId();
        long fileMessageId = carouselFileInfo.getFileMessageId();

        Observable.<ResMessages.FileMessage>create(subscriber -> {
            try {
                ResMessages.FileMessage fileMessage =
                        fileDetailModel.enableExternalLink(teamId, fileMessageId);
                SprinklrPublicLinkCreated.sendLog(fileMessageId);
                subscriber.onNext(fileMessage);
            } catch (RetrofitException e) {
                SprinklrPublicLinkCreated.sendFailLog(e.getResponseCode());
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileMessage -> {
                    carouselFileInfo.setExternalCode(fileMessage.content.externalCode);
                    carouselFileInfo.setIsExternalFileShared(fileMessage.content.externalShared);

                    view.dismissProgress();
                    view.initCarouselInfo(carouselFileInfo);
                    view.setExternalLinkToClipboard();
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));
                    view.dismissProgress();
                    view.showUnexpectedErrorToast();
                });

    }

    @Override
    public void onDisableExternalLink(CarouselFileInfo carouselFileInfo) {
        view.showProgress();

        long teamId = fileDetailModel.getTeamId();
        long fileMessageId = carouselFileInfo.getFileMessageId();

        Observable.<ResMessages.FileMessage>create(subscriber -> {
            try {
                ResMessages.FileMessage fileMessage =
                        fileDetailModel.disableExternalLink(teamId, fileMessageId);
                SprinklrPublicLinkDeleted.sendLog(fileMessageId);
// fileDetailModel.trackDisablePublicLinkSuccess(fileMessageId);
                subscriber.onNext(fileMessage);
            } catch (RetrofitException e) {
                SprinklrPublicLinkDeleted.sendFailLog(e.getResponseCode());
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileMessage -> {
                    carouselFileInfo.setExternalCode(fileMessage.content.externalCode);
                    carouselFileInfo.setIsExternalFileShared(fileMessage.content.externalShared);

                    view.dismissProgress();
                    view.initCarouselInfo(carouselFileInfo);
                    view.showDisableExternalLinkSuccessToast();
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));
                    view.dismissProgress();
                    view.showUnexpectedErrorToast();
                });

    }

    @Override
    public void onShareAction(final long entityId, final CarouselFileInfo carouselFileInfo) {
        view.showProgress();

        final long fileMessageId = carouselFileInfo.getFileMessageId();
        Observable.create(subscriber -> {

            try {
                fileDetailModel.shareMessage(fileMessageId, entityId);
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    SprinklrFileShare.sendLog(entityId, fileMessageId);

                    List<Long> sharedEntities = carouselFileInfo.getSharedEntities();
                    sharedEntities.add(entityId);
                    carouselFileInfo.setSharedEntities(sharedEntities);

                    view.dismissProgress();

                    view.initCarouselInfo(carouselFileInfo);

                    view.showMoveToSharedTopicDialog(entityId);
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));

                    if (e instanceof RetrofitException) {
                        SprinklrFileShare.sendFailLog(((RetrofitException) e).getResponseCode());
                    } else {
                        SprinklrFileShare.sendFailLog(-1);
                    }

                    view.dismissProgress();
                    view.showShareErrorToast();
                });

    }

    @Override
    public void onUnshareAction(long entityId, CarouselFileInfo carouselFileInfo) {
        view.showProgress();

        final long fileMessageId = carouselFileInfo.getFileMessageId();
        Observable.create(subscriber -> {

            try {
                fileDetailModel.unshareMessage(fileMessageId, entityId);
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    SprinklrFileUnshare.sendLog(entityId, fileMessageId);

                    List<Long> sharedEntities = carouselFileInfo.getSharedEntities();
                    Long sharedEntityId = Observable.from(sharedEntities)
                            .takeFirst(id -> id == entityId)
                            .toBlocking()
                            .firstOrDefault(-1L);

                    if (sharedEntityId > 0) {
                        sharedEntities.remove(sharedEntityId);
                        carouselFileInfo.setSharedEntities(sharedEntities);
                        view.initCarouselInfo(carouselFileInfo);
                    }

                    view.dismissProgress();

                    view.showUnshareErrorToast();
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));

                    if (e instanceof RetrofitException) {
                        int errorCode = ((RetrofitException) e).getStatusCode();
                        SprinklrFileUnshare.sendFailLog(errorCode);
                    } else {
                        SprinklrFileUnshare.sendFailLog(-1);
                    }

                    view.dismissProgress();
                    view.showUnshareErrorToast();
                });

    }

    @Override
    public void joinAndMove(final TopicRoom topicRoom) {
        view.showProgress();

        Observable.create(subscriber -> {
            try {
                fileDetailModel.joinEntity(topicRoom);
                fileDetailModel.updateJoinedTopic(topicRoom.getId());
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

                    view.dismissProgress();

                    view.moveToMessageListActivity(topicRoom.getId(), entityType, topicRoom.getId(), false);
                }, e -> {
                    LogUtil.e(Log.getStackTraceString(e));

                    view.dismissProgress();
                });

    }

    @Override
    public void onSocketCommentEvent(CarouselFileInfo carouselFileInfo, boolean isCommentAdded) {
        Observable.just(carouselFileInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(carouselFileInfo1 -> {
                    int fileCommentCount = carouselFileInfo1.getFileCommentCount();
                    int newCommentCount = isCommentAdded ? fileCommentCount + 1 : fileCommentCount - 1;
                    carouselFileInfo1.setFileCommentCount(newCommentCount);
                    view.setFileComments(newCommentCount);
                });
    }

    @Override
    public void onSocketCommentEvent(List<CarouselFileInfo> fileInfos, long fileMessageId, boolean isCommentAdded) {
        Observable.from(fileInfos)
                .takeFirst(fileInfo ->
                        fileInfo.getFileMessageId() == fileMessageId)
                .firstOrDefault(new CarouselFileInfo.Builder().create())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileInfo -> {
                    int fileCommentCount = fileInfo.getFileCommentCount();
                    int newCommentCount = isCommentAdded ? fileCommentCount + 1 : fileCommentCount - 1;
                    fileInfo.setFileCommentCount(newCommentCount);
                    view.notifyDataSetChanged();
                });
    }

    @Override
    public void onImageFileDeleted() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.showDeleteSuccessToast();
                    view.finish();
                });
    }

    @Override
    public void onImageFileDeleted(List<CarouselFileInfo> fileInfos, long deletedFileMessageId) {
        Observable.from(fileInfos)
                .takeFirst(fileInfo ->
                        fileInfo.getFileMessageId() == deletedFileMessageId)
                .firstOrDefault(new CarouselFileInfo.Builder().create())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileInfo -> {
                    view.remove(fileInfo);
                    view.notifyDataSetChanged();
                });
    }

    @Override
    public void setIsFirst(boolean isFirst) {
        this.isFirst = isFirst;
        view.setVisibilitySwipeToLeftButton(!isFirst);
    }

    @Override
    public boolean isFirst() {
        return isFirst;
    }

    @Override
    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
        view.setVisibilitySwipeToRightButton(!isLast);
    }

    @Override
    public boolean isLast() {
        return isLast;
    }

    @Override
    public void clearAllEventQueue() {
        if (starredStatePublishSubjectSubs != null
                && !starredStatePublishSubjectSubs.isUnsubscribed()) {
            starredStatePublishSubjectSubs.unsubscribe();
        }
    }

    void downloadFileAndManage(final FileDetailPresenter.FileManageType type,
                               CarouselFileInfo carouselFileInfo, ProgressDialog progressDialog) {
        if (carouselFileInfo == null) {
            view.dismissDialog(progressDialog);
            return;
        }

        String downloadFilePath = fileDetailModel.getDownloadFilePath(carouselFileInfo.getFileName());
        String downloadUrl = fileDetailModel.getDownloadUrl(carouselFileInfo.getFileOriginalUrl());
        final String mimeType = carouselFileInfo.getFileType();

        currentDownloadingFile = fileDetailModel.downloadFile(downloadUrl, downloadFilePath,
                callback -> callback.distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {
                            progressDialog.setMax(100);
                            progressDialog.setProgress(it);
                        }, t -> {
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
                                view.startExportedFileViewerActivity(new File(fileDetailModel.getDownloadFilePath(carouselFileInfo.getFileName())), mimeType);
                            } else if (type == FileDetailPresenter.FileManageType.OPEN) {
                                view.startDownloadedFileViewerActivity(new File(fileDetailModel.getDownloadFilePath(carouselFileInfo.getFileName())), mimeType);
                            }
                        }));
    }


}
