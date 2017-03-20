package com.tosslab.jandi.app.ui.message.v2;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tee on 2017. 3. 17..
 */


public class DownloadSAFFile extends AsyncTask<Void, Void, File> {
    private Context context;
    private Uri uri;

    public DownloadSAFFile(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    protected File doInBackground(Void... params) {
        File cacheFile = new File(context.getExternalCacheDir(), "image_cache");
        try {
            InputStream is = null;
            is = context.getContentResolver().openInputStream(uri);
            OutputStream os = new FileOutputStream(cacheFile);
            byte[] buffer = new byte[1024];
            int len = is.read(buffer);
            while (len != -1) {
                os.write(buffer, 0, len);
                len = is.read(buffer);
            }
            return cacheFile;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(File file) {
        if (file != null) {
        } else {
        }
    }
}