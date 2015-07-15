package com.tosslab.jandi.app.ui.carousel;

import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;

import java.io.File;
import java.util.List;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
public interface CarouselViewerPresenter {

    void setView(View view);

    void onInitImageFiles(Context context);

    void onBeforeImageFiles(Context context, int fileLinkId, int count);

    void onAfterImageFiles(Context context, int fileLinkId, int count);

    void onFileDownload(Context context, CarouselFileInfo fileInfo, ProgressDialog progressDialog);

    void onFileDatail();

    void setFileId(int startLinkId);

    void setRoomId(int roomId);

    interface View {

        void addFileInfos(List<CarouselFileInfo> fileInfoList);

        void showFailToast(String message);

        void setActionbarTitle(String fileName, String size, String ext);

        void setFileWriterName(String fileWriterName);

        void setFileCreateTime(String fileCreateTime);

        void moveToFileDatail();

        void downloadDone(File file, String fileType, ProgressDialog progressDialog);

        void addFileInfos(int position, List<CarouselFileInfo> imageFiles);

        void setInitFail();

        void movePosition(int startLinkPosition);
    }

}
