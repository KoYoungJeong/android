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

    void getImageFiles(int entityId, int startLinkId, Context context);

    void onFileDownload(CarouselFileInfo fileInfo, ProgressDialog progressDialog);

    interface View {

        void addFileInfos(List<CarouselFileInfo> fileInfoList);

        void setActionbarTitle(String fileName, String size, String ext);

        void setFileWriterName(String fileWriterName);

        void setFileCreateTime(String fileCreateTime);

        void moveFileDatail();

        void showFailToast(String message);

        void downloadDone(File file, String fileType, ProgressDialog progressDialog);

    }

}
