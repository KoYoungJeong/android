package com.tosslab.jandi.app.ui.carousel;

import android.content.Context;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.DateTransformator;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
@EBean
public class CarouselViewerModel {

    public List<ResMessages.FileMessage> searchInitFileList(int teamId, int roomId, int messageId)
            throws RetrofitError {
        return RequestApiManager.getInstance().searchInitImageFileByFileApi(teamId, roomId,
                messageId, 20);
    }

    public List<ResMessages.FileMessage> searchBeforeFileList(int teamId, int roomId, int fileLinkId, int count) {
        return RequestApiManager.getInstance().searchOldImageFileByFileApi(teamId, roomId,
                fileLinkId, count);
    }

    public List<ResMessages.FileMessage> searchAfterFileList(int teamId, int roomId, int fileLinkId, int count) {
        return RequestApiManager.getInstance().searchNewImageFileByFileApi(teamId, roomId,
                fileLinkId, count);
    }

    public List<CarouselFileInfo> getImageFileConvert(final int entityId,
                                                      List<ResMessages.FileMessage> fileMessages) {
        List<CarouselFileInfo> fileInfos = new ArrayList<CarouselFileInfo>();

        Observable.from(fileMessages)
                .map(fileMessage -> new CarouselFileInfo.Builder()
                        .entityId(entityId)
                        .fileLinkId(fileMessage.id)
                        .fileName(fileMessage.content.name)
                        .fileType(fileMessage.content.type)
                        .fileLinkUrl(ImageUtil.getFileUrl(fileMessage.content.fileUrl))
                        .fileThumbUrl(ImageUtil.getOptimizedImageUrl(fileMessage.content))
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

    public int getTeamId(Context context) {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

    public int findLinkPosition(List<CarouselFileInfo> imageFiles, int fileId) {

        CarouselFileInfo defaultValue = new CarouselFileInfo.Builder().create();
        CarouselFileInfo startFile = Observable.from(imageFiles)
                .filter(carouselFileInfo -> carouselFileInfo.getFileLinkId() == fileId)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return imageFiles.indexOf(startFile);

    }
}
