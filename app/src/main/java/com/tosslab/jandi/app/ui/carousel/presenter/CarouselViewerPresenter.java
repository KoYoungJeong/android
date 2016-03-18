package com.tosslab.jandi.app.ui.carousel.presenter;

import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;

import java.util.List;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
public interface CarouselViewerPresenter {

    void setView(View view);

    void onInitImageFiles();

    void onBeforeImageFiles(long fileLinkId, int count);

    void onAfterImageFiles(long fileLinkId, int count);

    void onFileDownload(CarouselFileInfo fileInfo);

    void onFileDatail();

    void setFileId(long startLinkId);

    void setRoomId(long roomId);

    void onInitImageSingleFile(String imageExt, String imageOriginUrl, String imageThumbUrl, String imageType);

    void onInitImageSingleFile(String imageExt, String imageOriginUrl, String imageThumbUrl, String imageType, String imageName, long imageSize);

    interface View {

        void addFileInfos(List<CarouselFileInfo> fileInfoList);

        void showFailToast(String message);

        void setActionbarTitle(String fileName, String size, String ext);

        void setFileWriterName(String fileWriterName);

        void setFileCreateTime(String fileCreateTime);

        void moveToFileDatail();

        void addFileInfos(int position, List<CarouselFileInfo> imageFiles);

        void setInitFail();

        void movePosition(int startLinkPosition);
    }

}
