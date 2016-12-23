package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileUpload;
import com.tosslab.jandi.app.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFileUploadControllerImpl implements FileUploadController {

    FilePickerModel filePickerModel;

    private File filePath;

    public MainFileUploadControllerImpl() {
        filePickerModel = new FilePickerModel();
    }

    @Override
    public void selectFileSelector(int type, Fragment fragment, long entityId) {
        switch (type) {
            case TYPE_UPLOAD_GALLERY:
                fragment.startActivityForResult(Henson.with(fragment.getActivity())
                        .gotoImageAlbumActivity()
                        .entityId(entityId)
                        .build(), TYPE_UPLOAD_GALLERY);
                break;
            case TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    filePath = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(fragment, Uri.fromFile(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TYPE_UPLOAD_EXPLORER:
                filePickerModel.openExplorerForActivityResult(fragment);
                break;
            default:
                break;

        }
    }

    @Override
    public void selectFileSelector(int type, Activity activity) {
        switch (type) {
            case TYPE_UPLOAD_GALLERY:
                activity.startActivityForResult(Henson.with(activity)
                        .gotoImageAlbumActivity()
                        .build(), TYPE_UPLOAD_GALLERY);
                break;
            case TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    filePath = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(activity, Uri.fromFile(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TYPE_UPLOAD_EXPLORER:
                filePickerModel.openExplorerForActivityResult(activity);
                break;
            default:
                break;

        }
    }

    @Override
    public void selectFileSelector(int type, Fragment fragment) {
        switch (type) {
            case TYPE_UPLOAD_GALLERY:
                fragment.startActivityForResult(Henson.with(fragment.getActivity())
                        .gotoImageAlbumActivity()
                        .build(), TYPE_UPLOAD_GALLERY);
                break;
            case TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    filePath = File.createTempFile("camera", ".jpg", directory);
                    filePickerModel.openCameraForActivityResult(fragment, Uri.fromFile(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TYPE_UPLOAD_EXPLORER:
                filePickerModel.openExplorerForActivityResult(fragment);
                break;
            default:
                break;

        }
    }


    @Override
    public List<String> getFilePath(Context context, int requestCode, Intent intent) {
        ArrayList<String> filePaths = new ArrayList<>();
        switch (requestCode) {
            case TYPE_UPLOAD_GALLERY:
                filePaths.addAll(filePickerModel.getFilePathsFromInnerGallery(intent));
                break;
            case TYPE_UPLOAD_TAKE_PHOTO:
                if (filePath != null) {
                    filePaths.add(filePickerModel.getFilePath(context, requestCode, intent, filePath));
                }
                break;
            case TYPE_UPLOAD_EXPLORER:
                filePaths.add(filePickerModel.getFilePath(context, requestCode, intent, filePath));
                break;
        }
        return filePaths;
    }

    @Override
    public void startUpload(Activity activity, String title, long entityId, String realFilePath, String comment) {
        ProgressDialog uploadProgress = getUploadProgress(activity, realFilePath);

        uploadFile(activity.getApplicationContext(), title, entityId, realFilePath, comment, uploadProgress);
    }

    void uploadFile(Context context, String title, long entityId, String realFilePath, String comment, ProgressDialog uploadProgress) {

        Completable.fromCallable(() -> {
            ResUploadedFile result = filePickerModel.uploadFile(uploadProgress, realFilePath, title, entityId, comment);
            SprinklrFileUpload.sendLog(entityId, result.getMessageId());
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(() -> {
                    dismissProgressDialog(uploadProgress);
                })
                .subscribe(() -> {
                    showSuccessToast(context, context.getString(R.string.jandi_file_upload_succeed));
                }, t -> {
                    SprinklrFileUpload.sendFailLog(-1);
                    showFailToast(context, context.getString(R.string.err_file_upload_failed));
                });
    }

    void showFailToast(Context context, String message) {
        ColoredToast.showError(message);
    }

    void showSuccessToast(Context context, String message) {
        ColoredToast.show(message);
    }


    public ProgressDialog getUploadProgress(Activity activity, String realFilePath) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(activity.getString(R.string.jandi_file_uploading) + " " + realFilePath);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        return progressDialog;

    }

    void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    @Override
    public File getUploadedFile() {
        return filePath;
    }
}
