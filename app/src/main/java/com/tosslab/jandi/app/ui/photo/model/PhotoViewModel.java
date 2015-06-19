package com.tosslab.jandi.app.ui.photo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created by tonyjs on 15. 5. 28..
 */
@EBean
public class PhotoViewModel {

    public static final String JANDI_FILE_DIRECTORY = "/JANDI";
    @RootContext
    Context context;

    public boolean isGif(String imageType) {
        return TextUtils.equals("image/gif", imageType);
    }

    public File getFile(String url) {
        if (TextUtils.isEmpty(url)) {
            // d
            return null;
        }
        File directory = new File(context.getFilesDir(), JANDI_FILE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }

        Uri uri = Uri.parse(url);
        File file = new File(directory, uri.getLastPathSegment());
        return file;
    }

    public File downloadFile(String url, File file, ProgressCallback callback)
            throws IOException, ExecutionException, InterruptedException {
        file.createNewFile();

        return Ion.with(context)
                .load(url)
                .progress(callback)
                .write(file)
                .get();
    }
}
