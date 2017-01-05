package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfileFileUploadControllerImpl implements FileUploadController {

    FilePickerModel filePickerModel;

    private File file;
    private ProgressWheel progressWheel;

    public ProfileFileUploadControllerImpl() {
        filePickerModel = new FilePickerModel();
    }

    @Override
    public void selectFileSelector(int type, Fragment fragment, long entityId) {

    }

    @Override
    public void selectFileSelector(int requestCode, Activity activity) {
        switch (requestCode) {
            case ModifyProfileActivity.REQUEST_CROP:
                activity.startActivityForResult(Henson.with(activity)
                        .gotoImageAlbumActivity()
                        .mode(ImageAlbumActivity.EXTRA_MODE_CROP_PICK)
                        .build(), requestCode);
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    file = File.createTempFile("camera", ".jpg", directory);
                    new FilePickerModel().openCameraForActivityResult(activity, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ModifyProfileActivity.REQUEST_CHARACTER:
                try {
                    File directory = new File(FileUtil.getCacheDir("character"));
                    file = File.createTempFile("character", ".png", directory);
                    new FilePickerModel().openCharacterActivityForActivityResult(activity, Uri.fromFile(file));
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
                fragment.startActivityForResult(Henson.with(fragment.getActivity())
                        .gotoImageAlbumActivity()
                        .mode(ImageAlbumActivity.EXTRA_MODE_CROP_PICK)
                        .build(), requestCode);
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    File directory = new File(FileUtil.getDownloadPath());
                    file = File.createTempFile("camera", ".jpg", directory);
                    new FilePickerModel().openCameraForActivityResult(fragment, Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ModifyProfileActivity.REQUEST_CHARACTER:
                try {
                    File directory = new File(FileUtil.getCacheDir("character"));
                    file = File.createTempFile("character", ".png", directory);
                    new FilePickerModel().openCharacterActivityForActivityResult(fragment, Uri.fromFile(file));
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
    public void startUpload(Activity activity, String title, long memberId, String realFilePath, String comment) {
        if (GoogleImagePickerUtil.isUrl(realFilePath)) {
            String downloadDir = FileUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            ProgressDialog downloadProgress =
                    GoogleImagePickerUtil.getDownloadProgress(activity, downloadDir, downloadName);
            downloadImageAndShowFileUploadDialog(activity,
                    downloadProgress, realFilePath, downloadDir, downloadName, memberId);
        } else {
            uploadProfileImage(activity, new File(realFilePath), memberId);
        }
    }

    void downloadImageAndShowFileUploadDialog(Activity activity,
                                              ProgressDialog downloadProgress,
                                              String url, String downloadDir, String downloadName,
                                              long memberId) {

        Observable.fromCallable(() -> {
            return GoogleImagePickerUtil.downloadFile(downloadProgress,
                    url,
                    downloadDir,
                    downloadName);

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    dismissProgressDialog(downloadProgress);
                    uploadProfileImage(activity, file, memberId);
                }, Throwable::printStackTrace);
    }

    void uploadProfileImage(Activity activity, File profileFile, long memberId) {
        showProgressWheel(activity);

        Observable.fromCallable(() -> {

            File convertedProfileFile = ImageUtil.convertProfileFile(profileFile);
            try {
                long userId = memberId;
                if (userId <= 0) {
                    userId = TeamInfoLoader.getInstance().getMyId();
                }
                Human human = filePickerModel.uploadProfilePhoto(convertedProfileFile, userId);
                String photoUrl = human.getPhotoUrl();
                long myId = TeamInfoLoader.getInstance().getMyId();
                HumanRepository.getInstance().updatePhotoUrl(myId, photoUrl);
                return human;
            } finally {
                if (convertedProfileFile != null && convertedProfileFile.exists()) {
                    convertedProfileFile.delete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .doOnNext(it -> {
                    TeamInfoLoader.getInstance().refresh();
                    EventBus.getDefault().post(new ProfileChangeEvent(it));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    successPhotoUpload(activity.getApplicationContext());
                    dismissProgressWheel();
                }, t -> {
                    t.printStackTrace();
                    dismissProgressWheel();
                    LogUtil.e("uploadFileDone: FAILED", t);
                    failPhotoUpload(activity.getApplicationContext());
                });


    }

    void failPhotoUpload(Context context) {
        ColoredToast.show(context.getString(R.string.err_profile_photo_upload));
    }

    void successPhotoUpload(Context context) {
        ColoredToast.show(context.getString(R.string.jandi_profile_photo_upload_succeed));
    }

    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    void showProgressWheel(Activity activity) {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(activity);
            progressWheel.setCancelable(false);
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    @Override
    public File getUploadedFile() {
        return file;
    }
}
