package com.tosslab.jandi.app.utils.file;

import android.app.ProgressDialog;
import android.os.Environment;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class JandiDownloadUtil {

    public static File download(String url, String fileName, String ext, ProgressDialog
            progressDialog) throws ExecutionException, InterruptedException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        String downloadUrl;
        if (url.lastIndexOf("/download") > 0) {
            downloadUrl = url;
        } else {
            downloadUrl = url + "/download";
        }

        return Ion.with(JandiApplication.getContext())
                .load(downloadUrl)
                .progressHandler((downloaded, total) -> {
                    progressDialog.setMax(100);
                    progressDialog.setProgress((int) (downloaded * 100 / total));

                    if (downloaded >= total) {
                        progressDialog.dismiss();
                    }
                })
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .write(new File(dir, FileSizeUtil.getDownloadFileName(fileName, ext)))
                .get();
    }
}
