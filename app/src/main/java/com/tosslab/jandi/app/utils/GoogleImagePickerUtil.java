package com.tosslab.jandi.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;

import java.io.File;
import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 2. 10..
 */
public class GoogleImagePickerUtil {
    public static boolean isUrl(String realFilePath) {
        return realFilePath.toLowerCase().startsWith("http://") || realFilePath.toLowerCase().startsWith("https://");
    }

    public static File downloadFile(Context context, ProgressDialog downloadProgress, String url, String downloadDir, String downloadName) throws Exception {

        File dir = new File(downloadDir);
        dir.mkdirs();

        return Ion.with(context)
                .load(url)
                .progressDialog(downloadProgress)
                .write(new File(dir, downloadName))
                .get();

    }

    public static String getWebImageName() {
        return String.format("%s_%s.jpg", String.valueOf(System.currentTimeMillis()), "web_image");
    }

    public static String getDownloadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Jandi";
    }

    public static ProgressDialog getDownloadProgress(Context context, String downloadDir, String downloadPath) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_action_download) + " " + downloadDir + "/" + downloadPath);
        progressDialog.show();

        return progressDialog;
    }
}
