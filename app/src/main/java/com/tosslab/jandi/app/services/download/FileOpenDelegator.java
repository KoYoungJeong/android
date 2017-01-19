package com.tosslab.jandi.app.services.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.io.Serializable;

/**
 * Created by tonyjs on 15. 11. 17..
 */
public class FileOpenDelegator extends BroadcastReceiver {
    public static final String TAG = FileOpenDelegator.class.getSimpleName();

    private static final String KEY_FILE_TYPE = "file_type";
    private static final String KEY_FILE = "file";

    public static Intent getIntent(File file, String fileType) {
        Intent intent = new Intent("com.tosslab.jandi.app.FileOpen");
        intent.putExtra(KEY_FILE, file);
        intent.putExtra(KEY_FILE_TYPE, fileType);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Serializable serializable = intent.getSerializableExtra(KEY_FILE);
        if (serializable == null || !(serializable instanceof File)) {
            LogUtil.e(TAG, "file is empty");
            return;
        }

        String fileType = intent.getStringExtra(KEY_FILE_TYPE);
        if (TextUtils.isEmpty(fileType)) {
            LogUtil.e(TAG, "fileType is empty.");
            return;
        }

        File file = (File) serializable;
        String mimeType = getFileType(file, fileType);


        try {
            Intent fileViewerOpenIntent = FileUtil.createFileIntent(file, mimeType);
            fileViewerOpenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fileViewerOpenIntent);
        } catch (Exception e) {
            String error = context.getResources().getString(R.string.err_unsupported_file_type);
            String formatString = String.format(error, file);
            ColoredToast.show(formatString);
        }
    }

    String getFileType(File file, String fileType) {
        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(
                    fileName.substring(idx + 1, fileName.length()).toLowerCase());
        } else {
            return mimeTypeMap.getExtensionFromMimeType(fileType.toLowerCase());
        }
    }
}
