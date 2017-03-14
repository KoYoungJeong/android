package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
public interface FileUploadController {
    int TYPE_UPLOAD_IMAGE_GALLERY = 0;
    int TYPE_UPLOAD_TAKE_PHOTO = 1;
    int TYPE_UPLOAD_TAKE_VIDEO = 2;
    int TYPE_UPLOAD_EXPLORER = 3;
    int TYPE_UPLOAD_CONTACT = 4;
    int TYPE_UPLOAD_VIDEO_GALARY = 5;
    int TYPE_UPLOAD_IMAGE_VIDEO = 6;

    void selectFileSelector(int type, Fragment fragment, long entityId);

    void selectFileSelector(int type, Activity activity);

    void selectFileSelector(int requestCode, Fragment fragment);

    List<String> getFilePath(Context context, int requestCode, Intent intent);

    void startUpload(Activity activity, String title, long entityId, String realFilePath, String comment);

    File getUploadedFile();
}
