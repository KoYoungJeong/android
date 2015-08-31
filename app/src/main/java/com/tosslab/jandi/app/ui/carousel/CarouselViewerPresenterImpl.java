package com.tosslab.jandi.app.ui.carousel;

import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.utils.FileSizeUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
@EBean
public class CarouselViewerPresenterImpl implements CarouselViewerPresenter {

    @Bean
    CarouselViewerModel carouselViewerModel;
    private View view;
    private int fileId;
    private int roomId;

    private boolean isFirst;
    private boolean isLast;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    @Override
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Background
    @Override
    public void onInitImageFiles(Context context) {
        List<CarouselFileInfo> imageFiles;
        try {

            int teamId = carouselViewerModel.getTeamId(context);

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel.searchInitFileList(teamId, roomId, fileId);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitError e) {
            e.printStackTrace();
            view.setInitFail();
            return;
        }


        if (imageFiles.size() > 0) {

            view.addFileInfos(imageFiles);

            int startLinkPosition = carouselViewerModel.findLinkPosition(imageFiles, fileId);
            if (startLinkPosition >= 0) {
                CarouselFileInfo carouselFirstFileInfo = imageFiles.get(startLinkPosition);

                view.movePosition(startLinkPosition);

                view.setActionbarTitle(carouselFirstFileInfo.getFileName(), FileSizeUtil.fileSizeCalculation(
                        carouselFirstFileInfo.getSize()), carouselFirstFileInfo.getExt());
                view.setFileWriterName(carouselFirstFileInfo.getFileWriter());
                view.setFileCreateTime(carouselFirstFileInfo.getFileCreateTime());
            }
        }
    }

    @Background
    @Override
    public void onBeforeImageFiles(Context context, int fileLinkId, int count) {

        if (isFirst) {
            return;
        }

        List<CarouselFileInfo> imageFiles;
        try {

            int teamId = carouselViewerModel.getTeamId(context);

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel.searchBeforeFileList
                    (teamId, roomId, fileLinkId, count);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitError e) {
            e.printStackTrace();
            imageFiles = new ArrayList<CarouselFileInfo>();
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
    public void onAfterImageFiles(Context context, int fileLinkId, int count) {

        if (isLast) {
            return;
        }

        List<CarouselFileInfo> imageFiles;
        try {

            int teamId = carouselViewerModel.getTeamId(context);

            List<ResMessages.FileMessage> fileMessages = carouselViewerModel
                    .searchAfterFileList(teamId, roomId, fileLinkId, count);
            imageFiles = carouselViewerModel.getImageFileConvert(roomId, fileMessages);

        } catch (RetrofitError e) {
            e.printStackTrace();
            imageFiles = new ArrayList<CarouselFileInfo>();
        }

        if (imageFiles.size() < count) {
            isLast = true;
        }

        if (imageFiles.size() > 0) {
            view.addFileInfos(imageFiles);
        }

    }

    @Background
    @Override
    public void onFileDownload(Context context, CarouselFileInfo fileInfo, ProgressDialog progressDialog) {
        try {
            File downloadFile = carouselViewerModel.download(fileInfo.getFileLinkUrl(),
                    fileInfo.getFileName(), fileInfo.getExt(),
                    progressDialog, context.getApplicationContext());

            carouselViewerModel.trackDownloadingFile(EntityManager.getInstance(),
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
