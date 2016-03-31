package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel_;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
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

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EBean
public class ProfileFileUploadControllerImpl implements FileUploadController {

    @Bean
    FilePickerModel filePickerModel;

    private File file;
    private ProgressWheel progressWheel;

    @Override
    public void selectFileSelector(int type, Fragment fragment, long entityId) {

    }

    @Override
    public void selectFileSelector(int requestCode, Activity activity) {
        switch (requestCode) {
            case ModifyProfileActivity.REQUEST_CROP:
                ImageAlbumActivity_.intent(activity)
                        .mode(ImageAlbumActivity.EXTRA_MODE_CROP_PICK)
                        .startForResult(requestCode);
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    file = File.createTempFile("camera", ".jpg", directory);
                    FilePickerModel_.getInstance_(JandiApplication.getContext())
                            .openCameraForActivityResult(activity, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ModifyProfileActivity.REQUEST_CHARACTER:
                try {
                    File directory = new File(FileUtil.getCacheDir("character"));
                    file = File.createTempFile("character", ".png", directory);
                    FilePickerModel_.getInstance_(JandiApplication.getContext())
                            .openCharacterActivityForActivityResult(activity, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    @Override
    public void selectFileSelector(int requestCode, Fragment fragment) {
        switch (requestCode) {
            case ModifyProfileActivity.REQUEST_CROP:
                ImageAlbumActivity_.intent(fragment)
                        .mode(ImageAlbumActivity.EXTRA_MODE_CROP_PICK)
                        .startForResult(requestCode);
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    file = File.createTempFile("camera", ".jpg", directory);
                    FilePickerModel_.getInstance_(JandiApplication.getContext())
                            .openCameraForActivityResult(fragment, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ModifyProfileActivity.REQUEST_CHARACTER:
                try {
                    File directory = new File(FileUtil.getCacheDir("character"));
                    file = File.createTempFile("character", ".png", directory);
                    FilePickerModel_.getInstance_(JandiApplication.getContext())
                            .openCharacterActivityForActivityResult(fragment, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public List<String> getFilePath(Context context, int requestCode, Intent intent) {
        return Arrays.asList(filePickerModel.getFilePath(context, requestCode, intent, file));
    }

    @Override
    public void startUpload(Activity activity, String title, long entityId, String realFilePath, String comment) {
        if (GoogleImagePickerUtil.isUrl(realFilePath)) {
            String downloadDir = FileUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            ProgressDialog downloadProgress =
                    GoogleImagePickerUtil.getDownloadProgress(activity, downloadDir, downloadName);
            downloadImageAndShowFileUploadDialog(activity,
                    downloadProgress, realFilePath, downloadDir, downloadName);
        } else {
            uploadProfileImage(activity, new File(realFilePath));
        }
    }

    @Background
    void downloadImageAndShowFileUploadDialog(Activity activity,
                                              ProgressDialog downloadProgress,
                                              String url, String downloadDir, String downloadName) {
        try {
            File file = GoogleImagePickerUtil.downloadFile(
                    activity.getApplicationContext(), downloadProgress, url, downloadDir, downloadName);
            dismissProgressDialog(downloadProgress);
            uploadProfileImage(activity, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Background
    void uploadProfileImage(Activity activity, File profileFile) {
        showProgressWheel(activity);
        try {
            filePickerModel.uploadProfilePhoto(activity.getApplicationContext(), profileFile);
            successPhotoUpload(activity.getApplicationContext());

            if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                // 소켓이 연동되어 있지 않는 상태인 경우..
                ResLeftSideMenu leftSideMenu = EntityClientManager_.getInstance_(activity).getTotalEntitiesInfo();
                LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
                EntityManager.getInstance().refreshEntity();
                ResLeftSideMenu.User me = EntityManager.getInstance().getMe().getUser();
                EventBus.getDefault().post(new ProfileChangeEvent(me));
            }
            dismissProgressWheel();
        } catch (RetrofitException | ExecutionException | InterruptedException e) {
            dismissProgressWheel();
            LogUtil.e("uploadFileDone: FAILED", e);
            failPhotoUpload(activity.getApplicationContext());
        }
    }

    @UiThread
    void failPhotoUpload(Context context) {
        ColoredToast.show(context.getString(R.string.err_profile_photo_upload));
    }

    @UiThread
    void successPhotoUpload(Context context) {
        ColoredToast.show(context.getString(R.string.jandi_profile_photo_upload_succeed));
    }

    @UiThread(propagation = UiThread.Propagation.ENQUEUE)
    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.ENQUEUE)
    void showProgressWheel(Activity activity) {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(activity);
            progressWheel.setCancelable(false);
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
    public void showFileUploadDialog(Context context, FragmentManager fragmentManager, String realFilePath, long entityId) {

    }

    @Override
    public void moveInsertFileCommnetActivity(Context context, List<String> realFilePath, int entityId) {

    }

    @Override
    public File getUploadedFile() {
        return file;
    }
}