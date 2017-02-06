package com.tosslab.jandi.app.ui.carousel.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

public class CarouselViewerModel {

    private final Lazy<FileApi> fileApi;

    @Inject
    public CarouselViewerModel(Lazy<FileApi> fileApi) {
        this.fileApi = fileApi;
    }

    public static CarouselFileInfo getCarouselInfoFromFileMessage(long entityId,
                                                                  ResMessages.FileMessage fileMessage) {
        return new CarouselFileInfo.Builder()
                .entityId(entityId)
                .fileMessageId(fileMessage.id)
                .fileName(fileMessage.content.title)
                .fileType(fileMessage.content.type)
                .fileLinkUrl(ImageUtil.getImageFileUrl(fileMessage.content.fileUrl))
                .fileThumbUrl(ImageUtil.getOnlyLargestThumbnail(fileMessage.content))
                .fileOriginalUrl(ImageUtil.getImageFileUrl(fileMessage.content.fileUrl))
                .ext(fileMessage.content.ext)
                .size(fileMessage.content.size)
                .fileCreateTime(DateTransformator.getTimeString(fileMessage.createTime))
                .fileWriterId(fileMessage.writerId)
                .fileWriterName(TeamInfoLoader.getInstance().getMemberName(fileMessage.writerId))
                .fileCommentCount(fileMessage.commentCount)
                .isStarred(fileMessage.isStarred)
                .isExternalShared(fileMessage.content.externalShared)
                .externalCode(fileMessage.content.externalCode)
                .sharedEntities(fileMessage.shareEntities)
                .create();
    }

    public Observable<List<ResMessages.FileMessage>> getImageFileListObservable(
            long teamId, long roomId, long messageId) {
        return Observable.create(subscriber -> {
            try {
                List<ResMessages.FileMessage> fileMessages = fileApi.get()
                        .searchInitImageFile(teamId, roomId, messageId, 20);
                subscriber.onNext(fileMessages);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public Observable<List<ResMessages.FileMessage>> getBeforeImageFileListObservable(
            long teamId, long roomId, long fileLinkId, int count) {
        return Observable.create(subscriber -> {
            try {
                List<ResMessages.FileMessage> fileMessages = fileApi.get()
                        .searchOldImageFile(teamId, roomId, fileLinkId, count);
                subscriber.onNext(fileMessages);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public Observable<List<ResMessages.FileMessage>> getAfterImageFileListObservable(
            long teamId, long roomId, long fileLinkId, int count) {
        return Observable.create(subscriber -> {
            try {
                List<ResMessages.FileMessage> fileMessages = fileApi.get()
                        .searchNewImageFile(teamId, roomId, fileLinkId, count);
                subscriber.onNext(fileMessages);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public List<CarouselFileInfo> getImageFileConvert(final long entityId,
                                                      List<ResMessages.FileMessage> fileMessages) {
        List<CarouselFileInfo> fileInfos = new ArrayList<CarouselFileInfo>();
        if (fileMessages == null || fileMessages.size() <= 0) {
            return fileInfos;
        }

        Observable.from(fileMessages)
                .map(fileMessage ->
                        getCarouselInfoFromFileMessage(entityId, fileMessage))
                .collect(() -> fileInfos, List::add)
                .subscribe();

        return fileInfos;
    }

    public long getTeamId() {
        return TeamInfoLoader.getInstance().getTeamId();
    }

    public int findLinkPosition(List<CarouselFileInfo> imageFiles, long fileId) {

        CarouselFileInfo defaultValue = new CarouselFileInfo.Builder().create();
        CarouselFileInfo startFile = Observable.from(imageFiles)
                .filter(carouselFileInfo -> carouselFileInfo.getFileMessageId() == fileId)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return imageFiles.indexOf(startFile);

    }

    public boolean isFileFromGoogleOrDropbox(String serverUrl) {
        return TextUtils.equals(serverUrl, "google")
                || TextUtils.equals(serverUrl, "dropbox");
    }
}
