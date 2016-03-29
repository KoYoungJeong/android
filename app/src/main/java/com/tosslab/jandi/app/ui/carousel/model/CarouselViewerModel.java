package com.tosslab.jandi.app.ui.carousel.model;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
@EBean
public class CarouselViewerModel {

    @Inject
    Lazy<FileApi> fileApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public List<ResMessages.FileMessage> searchInitFileList(long teamId, long roomId, long messageId)
            throws RetrofitException {
        return fileApi.get().searchInitImageFile(teamId, roomId,
                messageId, 20);
    }

    public List<ResMessages.FileMessage> searchBeforeFileList(long teamId, long roomId, long fileLinkId, int count) throws RetrofitException {
        return fileApi.get().searchOldImageFile(teamId, roomId,
                fileLinkId, count);
    }

    public List<ResMessages.FileMessage> searchAfterFileList(long teamId, long roomId, long fileLinkId, int count) throws RetrofitException {
        return fileApi.get().searchNewImageFile(teamId, roomId,
                fileLinkId, count);
    }

    public List<CarouselFileInfo> getImageFileConvert(final long entityId,
                                                      List<ResMessages.FileMessage> fileMessages) {
        List<CarouselFileInfo> fileInfos = new ArrayList<CarouselFileInfo>();

        Observable.from(fileMessages)
                .map(fileMessage -> new CarouselFileInfo.Builder()
                        .entityId(entityId)
                        .fileLinkId(fileMessage.id)
                        .fileName(fileMessage.content.name)
                        .fileType(fileMessage.content.type)
                        .fileLinkUrl(ImageUtil.getImageFileUrl(fileMessage.content.fileUrl))
                        .fileThumbUrl(ImageUtil.getThumbnailUrl(fileMessage.content.extraInfo, ImageUtil.Thumbnails.THUMB))
                        .fileOriginalUrl(ImageUtil.getImageFileUrl(fileMessage.content.fileUrl))
                        .ext(fileMessage.content.ext)
                        .size(fileMessage.content.size)
                        .fileCreateTime(
                                DateTransformator.getTimeString(fileMessage.createTime))
                        .fileWriter(EntityManager.getInstance()
                                .getEntityNameById(fileMessage.writerId))
                        .create()).collect(() -> fileInfos, List::add)
                .subscribe();

        return fileInfos;
    }

    public long getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

    public int findLinkPosition(List<CarouselFileInfo> imageFiles, long fileId) {

        CarouselFileInfo defaultValue = new CarouselFileInfo.Builder().create();
        CarouselFileInfo startFile = Observable.from(imageFiles)
                .filter(carouselFileInfo -> carouselFileInfo.getFileLinkId() == fileId)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return imageFiles.indexOf(startFile);

    }
}
