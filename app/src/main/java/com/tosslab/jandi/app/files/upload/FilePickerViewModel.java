package com.tosslab.jandi.app.files.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
public interface FilePickerViewModel {
    void showFileUploadTypeDialog(FragmentManager fragmentManager);

    void selectFileSelector(int type, Fragment fragment, int entityId);

    void selectFileSelector(int type, Activity activity);

    List<String> getFilePath(Context context, int requestCode, Intent intent);

    void startUpload(Context context, String title, int entityId, String realFilePath, String comment);


    void showFileUploadDialog(Context context, FragmentManager fragmentManager, String realFilePath, int entityId);

    void moveInsertFileCommnetActivity(Context context, List<String> realFilePath, int entityId);
}