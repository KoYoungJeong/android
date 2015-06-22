package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.ui.album.ImageAlbumActivity_;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EBean
public class EntityFileUploadViewModelImpl implements FilePickerViewModel {

    @Bean
    FilePickerModel filePickerModel;

    private File filePath;

    @Override
    public void showFileUploadTypeDialog(FragmentManager fragmentManager) {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(fragmentManager, "dialog");
    }


    @Override
    public void selectFileSelector(int type, Fragment fragment, int entityId) {
        switch (type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                ImageAlbumActivity_
                        .intent(fragment)
                        .entityId(entityId)
                        .startForResult(JandiConstants.TYPE_UPLOAD_GALLERY);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(GoogleImagePickerUtil.getDownloadPath());
                    filePath = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(fragment, Uri.fromFile(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                filePickerModel.openExplorerForActivityResult(fragment);
                break;
            default:
                break;

        }
    }

    @Override
    public void selectFileSelector(int type, Activity activity) {
        switch (type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                ImageAlbumActivity_.intent(activity).startForResult(JandiConstants.TYPE_UPLOAD_GALLERY);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(GoogleImagePickerUtil.getDownloadPath());
                    filePath = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(activity, Uri.fromFile(filePath));
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
        ArrayList<String> filePaths = new ArrayList<>();
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                filePaths.addAll(filePickerModel.getFilePathsFromInnerGallery(intent));
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                filePaths.add(filePickerModel.getFilePath(context, requestCode, intent, filePath));
                break;
        }
        return filePaths;
    }

    @Override
    public void startUpload(Context context, String title, int entityId, String realFilePath, String comment) {
        ProgressDialog uploadProgress = getUploadProgress(context, realFilePath);

        uploadFile(context, title, entityId, realFilePath, comment, uploadProgress);
    }

    @Background
    void uploadFile(Context context, String title, int entityId, String realFilePath, String comment, ProgressDialog uploadProgress) {
        boolean isPublicTopic = filePickerModel.isPublicEntity(context, entityId);
        try {
            JsonObject result = filePickerModel.uploadFile(context, uploadProgress, realFilePath, isPublicTopic, title, entityId, comment);
            if (result.get("code") == null) {

                LogUtil.e("Upload Success : " + result);
                showSuccessToast(context, context.getString(R.string.jandi_file_upload_succeed));
                filePickerModel.trackUploadingFile(context, entityId, result);
            } else {
                LogUtil.e("Upload Fail : Result : " + result);
                showFailToast(context, context.getString(R.string.err_file_upload_failed));
            }
        } catch (ExecutionException e) {
            showFailToast(context, context.getString(R.string.jandi_canceled));
        } catch (Exception e) {
            LogUtil.e("Upload Error : ", e);
            showFailToast(context, context.getString(R.string.err_file_upload_failed));
        } finally {
            dismissProgressDialog(uploadProgress);
        }

    }

    @UiThread
    void showFailToast(Context context, String message) {
        ColoredToast.showError(context, message);
    }

    @UiThread
    void showSuccessToast(Context context, String message) {
        ColoredToast.show(context, message);
    }


    public ProgressDialog getUploadProgress(Context context, String realFilePath) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_file_uploading) + " " + realFilePath);
        progressDialog.show();

        return progressDialog;

    }

    @Background
    void downloadImageAndShowFileUploadDialog(Context context, FragmentManager fragmentManager, int entityId, ProgressDialog downloadProgress, String url, String downloadDir, String downloadName) {

        try {
            File file = GoogleImagePickerUtil.downloadFile(context, downloadProgress, url, downloadDir, downloadName);
            dismissProgressDialog(downloadProgress);
            showFileUploadDialog(context, fragmentManager, file.getAbsolutePath(), entityId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    // File Upload 대화상자 보여주기
    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showFileUploadDialog(Context context, FragmentManager fragmentManager, String realFilePath, int entityId) {

        if (GoogleImagePickerUtil.isUrl(realFilePath)) {

            String downloadDir = GoogleImagePickerUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(context, downloadDir, downloadName);
            downloadImageAndShowFileUploadDialog(context, fragmentManager, entityId, downloadProgress, realFilePath, downloadDir, downloadName);
        } else {

            // 업로드 파일 용량 체크
            if (filePickerModel.isOverSize(realFilePath)) {
                exceedMaxFileSizeError(context);
            } else {
                DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, entityId);
                newFragment.show(fragmentManager, "dialog");
            }
        }

    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveInsertFileCommnetActivity(Context context, List<String> realFilePath, int entityId) {// 업로드 파일 용량 체크
        if (filePickerModel.isOverSize(realFilePath)) {
            exceedMaxFileSizeError(context);
        } else {
            FileUploadPreviewActivity_.intent(context)
                    .realFilePathList(new ArrayList<String>(realFilePath))
                    .selectedEntityIdToBeShared(entityId)
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
        }

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void exceedMaxFileSizeError(Context context) {

        ColoredToast.showError(context, context.getString(R.string.err_file_upload_failed));
    }

}
