package com.tosslab.jandi.app.ui.carousel.presenter;

import android.app.ProgressDialog;

import com.tosslab.jandi.app.permissions.Check;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;

import java.io.File;
import java.util.List;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
public interface CarouselViewerPresenter {

    void onInitImageFiles(long roomId, long startFileMessageId);

    void onInitImageSingleFile(CarouselFileInfo singleImageInfo);

    void onBeforeImageFiles(long roomId, long fileLinkId, int count);

    void onAfterImageFiles(long roomId, long fileLinkId, int count);

    void onFileDownload(CarouselFileInfo fileInfo);

    void onChangeStarredState(long fileId, boolean starred);

    void onExportFile(CarouselFileInfo carouselFileInfo, ProgressDialog progressDialog);

    void onDeleteFile(long fileMessageId);

    void onEnableExternalLink(CarouselFileInfo carouselFileInfo);

    void onDisableExternalLink(CarouselFileInfo carouselFileInfo);

    void onShareAction(long entityId, CarouselFileInfo carouselFileInfo);

    void onUnshareAction(long entityId, CarouselFileInfo carouselFileInfo);

    void joinAndMove(TopicRoom topic);

    void onSocketCommentEvent(CarouselFileInfo carouselFileInfo, boolean isCommentAdded);

    void onSocketCommentEvent(List<CarouselFileInfo> fileInfos, long fileMessageId, boolean isCommentAdded);

    void onImageFileDeleted();

    void onImageFileDeleted(List<CarouselFileInfo> fileInfos, long deletedFileMessageId);

    void setIsFirst(boolean isFirst);

    boolean isFirst();

    void setIsLast(boolean isLast);

    boolean isLast();

    void clearAllEventQueue();

    interface View {

        void initCarouselInfo(CarouselFileInfo fileInfo);

        void addFileInfos(List<CarouselFileInfo> fileInfoList);

        void showFailToast(String message);

        void setFileTitle(String fileName);

        void setFileWriterName(String fileWriterName);

        void setFileCreateTime(String fileCreateTime);

        void addFileInfos(int position, List<CarouselFileInfo> imageFiles);

        void setInitFail();

        void movePosition(int startLinkPosition);

        void setFileInfo(String size, String ext);

        void setFileComments(int fileCommentCount);

        void showStarredSuccessToast();

        void showUnstarredSuccessToast();

        void dismissDialog(ProgressDialog progressDialog);

        void showUnexpectedErrorToast();

        void exportLink(String fileOriginalUrl);

        void showDialog(ProgressDialog progressDialog);

        void startExportedFileViewerActivity(File result, String mimeType);

        void startDownloadedFileViewerActivity(File result, String mimeType);

        void requestPermission(int requestCode, String... permissions);

        void checkPermission(String persmissionString, Check.HasPermission hasPermission, Check.NoPermission noPermission);

        void showProgress();

        void dismissProgress();

        void showDeleteSuccessToast();

        void showDeleteErrorToast();

        void setExternalLinkToClipboard();

        void showDisableExternalLinkSuccessToast();

        void showShareErrorToast();

        void showMoveToSharedTopicDialog(long entityId);

        void showUnshareErrorToast();

        void moveToMessageListActivity(long entityId, int entityType, long roomId, boolean isStarred);

        void remove(CarouselFileInfo fileInfo);

        void finish();

        void deletedFinish(long fileId);

        void notifyDataSetChanged();

        void setVisibilitySwipeToLeftButton(boolean show);

        void setVisibilitySwipeToRightButton(boolean show);
    }

}
