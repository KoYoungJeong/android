package com.tosslab.jandi.app.ui.carousel;

import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.FileSizeUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
@EBean
public class CarouselViewerPresenterImpl implements CarouselViewerPresenter {

    private View view;

    @Bean
    CarouselViewerModel carouselViewerModel;

    @RootContext
    Context context;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void getImageFiles(int entityId, int startLinkId, Context context) {
        List<CarouselFileInfo> imageFiles = null;
        try {
            ReqSearchFile reqSearchFile = carouselViewerModel.getReqSearchFile(entityId, startLinkId, context);

            ResSearchFile resSearchFile = carouselViewerModel.searchFileList(reqSearchFile, context);

            if (resSearchFile.fileCount < reqSearchFile.listCount) {
                // TODO 더이상 갱신 요청할 것 없음
            }

            imageFiles = carouselViewerModel.getImageFileConvert(entityId, context, resSearchFile);

        } catch (Exception e) {
            e.printStackTrace();
            imageFiles = new ArrayList<CarouselFileInfo>();
        }

        if (imageFiles.size() > 0) {
            CarouselFileInfo carouselFirstFileInfo = imageFiles.get(0);

            view.addFileInfos(imageFiles);
            view.setActionbarTitle(carouselFirstFileInfo.getFileName(), FileSizeUtil.fileSizeCalculation(
                    carouselFirstFileInfo.getSize()), carouselFirstFileInfo.getExt());
            view.setFileWriterName(carouselFirstFileInfo.getFileWriter());
            view.setFileCreateTime(carouselFirstFileInfo.getFileCreateTime());
        }
    }

    @Background
    @Override
    public void onFileDownload(CarouselFileInfo fileInfo, ProgressDialog progressDialog) {
        try {
            File downloadFile = carouselViewerModel.download(fileInfo.getFileLinkUrl(),
                    fileInfo.getFileName(), fileInfo.getFileType(),
                    progressDialog, context.getApplicationContext());

            carouselViewerModel.trackDownloadingFile(EntityManager.getInstance(context),
                    fileInfo, context);

            if (carouselViewerModel.isMediaFile(fileInfo.getFileType())) {
                carouselViewerModel.addGallery(downloadFile, fileInfo.getFileType(), context);
            }

            view.downloadDone(downloadFile, fileInfo.getFileType(), progressDialog);

        } catch (ExecutionException e) {
            e.printStackTrace();
            view.showFailToast(context.getApplicationContext().getString(R.string.err_download));
        } catch (InterruptedException e) {
            e.printStackTrace();
            view.showFailToast(context.getApplicationContext().getString(R.string.err_download));
        }
    }

    @Override
    public void onFileDatail() {
        view.moveToFileDatail();
    }

}
