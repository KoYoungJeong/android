package com.tosslab.jandi.app.ui.carousel.presenter;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.utils.file.FileUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
@EBean
public class CarouselViewerPresenterImpl implements CarouselViewerPresenter {

    @Bean
    CarouselViewerModel carouselViewerModel;
    private View view;
    private long fileId;
    private long roomId;

    private boolean isFirst;
    private boolean isLast;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    @Background
    @Override
    public void onInitImageFiles() {
        List<CarouselFileInfo> imageFiles;
        try {

            long teamId = carouselViewerModel.getTeamId();

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel.searchInitFileList(teamId, roomId, fileId);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitException e) {
            e.printStackTrace();
            view.setInitFail();
            return;
        }

        setImageFiles(imageFiles);
    }

    private void setImageFiles(List<CarouselFileInfo> imageFiles) {
        if (imageFiles.size() > 0) {
            view.addFileInfos(imageFiles);

            int startLinkPosition = carouselViewerModel.findLinkPosition(imageFiles, fileId);
            if (startLinkPosition >= 0) {
                CarouselFileInfo carouselFirstFileInfo = imageFiles.get(startLinkPosition);

                view.movePosition(startLinkPosition);

                view.setActionbarTitle(carouselFirstFileInfo.getFileName(), FileUtil.fileSizeCalculation(
                        carouselFirstFileInfo.getSize()), carouselFirstFileInfo.getExt());
                view.setFileWriterName(carouselFirstFileInfo.getFileWriter());
                view.setFileCreateTime(carouselFirstFileInfo.getFileCreateTime());
            }
        }
    }

    @Override
    public void onInitImageSingleFile(String imageExt, String imageOriginUrl, String imageThumbUrl, String imageType, String imageName, long imageSize) {
        List<CarouselFileInfo> imageFiles = new ArrayList<>();
        CarouselFileInfo.Builder builder = new CarouselFileInfo.Builder();
        CarouselFileInfo carouselFileInfo = builder.ext(imageExt)
                .fileOriginalUrl(imageOriginUrl)
                .fileThumbUrl(imageThumbUrl)
                .fileType(imageType)
                .fileLinkUrl(imageOriginUrl)
                .fileName(imageName)
                .size(imageSize)
                .create();
        imageFiles.add(carouselFileInfo);
        setImageFiles(imageFiles);
    }

    @Background
    @Override
    public void onBeforeImageFiles(long fileLinkId, int count) {

        if (isFirst) {
            return;
        }

        List<CarouselFileInfo> imageFiles;
        try {

            long teamId = carouselViewerModel.getTeamId();

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel.searchBeforeFileList
                    (teamId, roomId, fileLinkId, count);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitException e) {
            e.printStackTrace();
            imageFiles = new ArrayList<>();
        }

        if (imageFiles.size() < count) {
            isFirst = true;
        }

        if (imageFiles.size() > 0) {
            view.addFileInfos(0, imageFiles);
        }

    }

    @Background
    @Override
    public void onAfterImageFiles(long fileLinkId, int count) {

        if (isLast) {
            return;
        }

        List<CarouselFileInfo> imageFiles;
        try {

            long teamId = carouselViewerModel.getTeamId();

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel
                    .searchAfterFileList(teamId, roomId, fileLinkId, count);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitException e) {
            e.printStackTrace();
            imageFiles = new ArrayList<>();
        }

        if (imageFiles.size() < count) {
            isLast = true;
        }

        if (imageFiles.size() > 0) {
            view.addFileInfos(imageFiles);
        }

    }

    @Override
    public void onFileDownload(CarouselFileInfo fileInfo) {
        DownloadService.start(fileId,
                fileInfo.getFileLinkUrl(),
                fileInfo.getFileName(),
                fileInfo.getExt(),
                fileInfo.getFileType());
    }

    @Override
    public void onFileDatail() {
        view.moveToFileDatail();
    }

}
