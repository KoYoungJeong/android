package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
public interface FilePickerViewModel {
    int TYPE_UPLOAD_GALLERY = 0x00;
    int TYPE_UPLOAD_TAKE_PHOTO = 0x01;
    int TYPE_UPLOAD_EXPLORER = 0x02;

    void selectFileSelector(int type, Fragment fragment, long entityId);

    void selectFileSelector(int type, Activity activity);

    List<String> getFilePath(Context context, int requestCode, Intent intent);

    void startUpload(Activity activity, String title, long entityId, String realFilePath, String comment);

    void showFileUploadDialog(Context context, FragmentManager fragmentManager, String realFilePath, long entityId);

    void moveInsertFileCommnetActivity(Context context, List<String> realFilePath, int entityId);

    File getUploadedFile();
}
