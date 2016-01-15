package com.tosslab.jandi.app.utils.file;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 15. 2. 10..
 */
public class GoogleImagePickerUtil {
    public static boolean isUrl(String realFilePath) {
        return realFilePath.toLowerCase().startsWith("http://") || realFilePath.toLowerCase().startsWith("https://");
    }

    /**
     * 구글,피카사 등 웹의 이미지 다운로드한다
     *
     * @param context
     * @param downloadProgress
     * @param url
     * @param downloadDir
     * @param downloadName
     * @return
     * @throws Exception
     */
    public static File downloadFile(Context context, ProgressDialog downloadProgress, String url, String downloadDir, String downloadName) throws Exception {

        File dir = new File(downloadDir);
        dir.mkdirs();

        if (downloadProgress != null) {

            return Ion.with(context)
                    .load(url)
                    .progressDialog(downloadProgress)
                    .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                    .write(new File(dir, downloadName))
                    .get();
        } else {
            return Ion.with(context)
                    .load(url)
                    .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                    .write(new File(dir, downloadName))
                    .get();
        }

    }

    public static String getWebImageName() {
        return String.format("%s_%s.jpg", String.valueOf(System.currentTimeMillis()), "web_image");
    }

    public static String getWebImageNameOnly() {
        return String.format("%s_%s", String.valueOf(System.currentTimeMillis()), "web_image");
    }

    /**
     * @return /sdcard/DOWNLOAD/Jandi
     */
    public static String getDownloadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Jandi";
    }

    public static ProgressDialog getDownloadProgress(Context context, String downloadDir, String downloadPath) {

        final ProgressDialog progressDialog = new ProgressDialog(context);

        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return progressDialog;
            }
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_action_download) + " " + downloadDir + "/" + downloadPath);
        progressDialog.show();

        return progressDialog;
    }


}
