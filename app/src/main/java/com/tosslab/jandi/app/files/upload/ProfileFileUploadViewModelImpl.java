package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EBean
public class ProfileFileUploadViewModelImpl implements FilePickerViewModel {

    @Bean
    FilePickerModel filePickerModel;

    private File file;
    private ProgressWheel progressWheel;

    @Override
    public void showFileUploadTypeDialog(FragmentManager fragmentManager) {

    }

    @Override
    public void selectFileSelector(int type, Fragment fragment) {

    }

    @Override
    public void selectFileSelector(int type, Activity activity) {
        switch (type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                filePickerModel.openAlbumForActivityResult(activity);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(GoogleImagePickerUtil.getDownloadPath());
                    file = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(activity, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                filePickerModel.openExplorerForActivityResult(activity);
                break;
            default:
                break;

        }

    }

    @Override
    public List<String> getFilePath(Context context, int requestCode, Intent intent) {
        return Arrays.asList(filePickerModel.getFilePath(context, requestCode, intent, file));
    }

    @Override
    public void startUpload(Context context, String title, int entityId, String realFilePath, String comment) {

        if (GoogleImagePickerUtil.isUrl(realFilePath)) {

            String downloadDir = GoogleImagePickerUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(context, downloadDir, downloadName);
            downloadImageAndShowFileUploadDialog(context, downloadProgress, realFilePath, downloadDir, downloadName);
        } else {
            uploadProfileImage(context, new File(realFilePath));
        }
    }

    @Background
    void downloadImageAndShowFileUploadDialog(Context context, ProgressDialog downloadProgress, String url, String downloadDir, String downloadName) {

        try {
            File file = GoogleImagePickerUtil.downloadFile(context, downloadProgress, url, downloadDir, downloadName);
            dismissProgressDialog(downloadProgress);
            uploadProfileImage(context, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Background
    void uploadProfileImage(Context context, File profileFile) {
        showProgressWheel(context);
        try {
            filePickerModel.uploadProfilePhoto(context, profileFile);
            successPhotoUpload(context);
            dismissProgressWheel();

        } catch (ExecutionException e) {
            dismissProgressWheel();
            LogUtil.e("uploadFileDone: FAILED", e);
            failPhotoUpload(context);
        } catch (InterruptedException e) {
            dismissProgressWheel();
            LogUtil.e("uploadFileDone: FAILED", e);
            failPhotoUpload(context);
        }


    }

    @UiThread
    void failPhotoUpload(Context context) {
        ColoredToast.showError(context, context.getString(R.string.err_profile_photo_upload));
    }

    @UiThread
    void successPhotoUpload(Context context) {
        ColoredToast.show(context, context.getString(R.string.jandi_profile_photo_upload_succeed));

    }

    @UiThread(propagation = UiThread.Propagation.ENQUEUE)
    void dismissProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.ENQUEUE)
    void showProgressWheel(Context context) {

        if (progressWheel == null) {
            progressWheel = new ProgressWheel(context);
            progressWheel.init();
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }

    }


    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }


    @Override
    public void showFileUploadDialog(Context context, FragmentManager fragmentManager, String realFilePath, int entityId) {

    }

    @Override
    public void moveInsertFileCommnetActivity(Context context, List<String> realFilePath, int entityId) {

    }
}
