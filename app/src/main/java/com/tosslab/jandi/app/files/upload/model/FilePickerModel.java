package com.tosslab.jandi.app.files.upload.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.file.FileUploadApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.profile.defaultimage.ProfileImageSelectorActivity;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

public class FilePickerModel {

    public static final long MAX_FILE_SIZE = 1024 * 1024 * 300;

    public String getFilePath(Context context, int requestCode, Intent intent, File filePath) {
        String realFilePath;
        switch (requestCode) {
            case FileUploadController.TYPE_UPLOAD_IMAGE_GALLERY:
                if (intent == null) {
                    return "";
                }
                Uri data = intent.getData();

                if (data != null) {
                    realFilePath = ImageFilePath.getPath(context, data);

                    return realFilePath;
                } else {
                    return "";
                }

            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
            case FileUploadController.TYPE_UPLOAD_TAKE_VIDEO:
                if (filePath == null) {
                    LogUtil.e("filePath object is null...");
                    return "";
                }
                if (!filePath.exists()) {
                    LogUtil.e("filePath is not exists");
                    return "";
                }
                return filePath.getAbsolutePath();
            case FileUploadController.TYPE_UPLOAD_EXPLORER:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    realFilePath = intent.getStringExtra("GetPath") +
                            File.separator +
                            intent.getStringExtra("GetFileName");
                    return realFilePath;
                } else {
                    Uri uri = intent.getData();
                    String realPath = ImageFilePath.getPath(context, uri);
                    return realPath;
                }
            default:
                return "";
        }
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getActivity(), FileExplorerActivity.class);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    public void openExplorerForActivityResult(Activity activity) {
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        activity.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    public void openCameraImageForActivityResult(Fragment fragment, File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.createOptimizedFileUri(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openCameraVideoForActivityResult(Fragment fragment, File file) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.createOptimizedFileUri(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_VIDEO);
    }

    public void openContactActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_CONTACT);
    }

    public void openCameraImageForActivityResult(Activity activity, File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.createOptimizedFileUri(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openCameraVideoForActivityResult(Activity activity, File file) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtil.createOptimizedFileUri(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_VIDEO);
    }

    public void openCharacterActivityForActivityResult(Activity activity, Uri fileUri) {
        Intent intent = new Intent(activity, ProfileImageSelectorActivity.class);
        intent.putExtra("profile_image_file_uri", fileUri);
        activity.startActivityForResult(intent, ModifyProfileActivity.REQUEST_CHARACTER);
    }

    public void openCharacterActivityForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(fragment.getContext(), ProfileImageSelectorActivity.class);
        intent.putExtra("profile_image_file_uri", fileUri);
        fragment.startActivityForResult(intent, ModifyProfileActivity.REQUEST_CHARACTER);
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_IMAGE_GALLERY);
    }

    public void openAlbumForActivityResult(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public boolean isOverSize(String... realFilePath) {
        File uploadFile;
        int totalSize = 0;
        for (String filePath : realFilePath) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                totalSize += uploadFile.length();
                if (totalSize > MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isOverSize(List<String> realFilePath) {
        File uploadFile;
        int totalSize = 0;
        for (String filePath : realFilePath) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                totalSize += uploadFile.length();
                if (totalSize > MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isPublicEntity(long entityId) {
        return TeamInfoLoader.getInstance().isPublicTopic(entityId);
    }

    public ResUploadedFile uploadFile(ProgressDialog progressDialog, String realFilePath, String title, long entityId, String comment) throws IOException {
        File uploadFile = new File(realFilePath);

        return new FileUploadApi().uploadFile(title, entityId, TeamInfoLoader.getInstance().getTeamId(), comment, new ArrayList<>(), uploadFile, callback -> callback.distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.setMax(100);
                        progressDialog.setProgress(progress);
                    }
                }, t -> {
                }, () -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }))
                .execute().body();


    }

    public Human uploadProfilePhoto(File file, long memberId) throws RetrofitException {
        return new ProfileApi(InnerApiRetrofitBuilder.getInstance()).uploadProfilePhoto(TeamInfoLoader.getInstance().getTeamId(), memberId, file);
    }

    public ArrayList<String> getFilePathsFromInnerGallery(Intent intent) {
        return intent.getStringArrayListExtra(ImageAlbumActivity.EXTRA_DATAS);
    }

}
