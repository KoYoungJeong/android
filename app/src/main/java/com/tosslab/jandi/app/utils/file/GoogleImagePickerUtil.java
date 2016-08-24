package com.tosslab.jandi.app.utils.file;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.file.FileDownloadApi;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;

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
     * @param downloadProgress
     * @param url
     * @param downloadDir
     * @param downloadName
     * @return
     * @throws Exception
     */
    public static File downloadFile(ProgressDialog downloadProgress,
                                    String url,
                                    String downloadDir,
                                    String downloadName) throws Exception {

        File dir = new File(downloadDir);
        dir.mkdirs();

        if (downloadProgress != null) {
            return new FileDownloadApi().downloadImmediatly(url, dir + "/" + downloadName, callback -> callback
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(it -> {
                        downloadProgress.setMax(100);
                        downloadProgress.setProgress(it);
                    }, t -> {
                        if (downloadProgress != null && downloadProgress.isShowing()) {
                            downloadProgress.dismiss();
                        }
                    }, () -> {
                        if (downloadProgress != null && downloadProgress.isShowing()) {
                            downloadProgress.dismiss();
                        }
                    })
            );

        } else {
            return new FileDownloadApi().downloadImmediatly(url, dir + "/" + downloadName, null);
        }

    }

    public static String getWebImageName() {
        return String.format("%s_%s.jpg", String.valueOf(System.currentTimeMillis()), "web_image");
    }

    public static String getWebImageNameOnly() {
        return String.format("%s_%s", String.valueOf(System.currentTimeMillis()), "web_image");
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
