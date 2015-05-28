package com.tosslab.jandi.app.ui.photo;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimationFactory;
import com.bumptech.glide.request.target.Target;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.photo.model.PhotoViewModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Steve SeongUg Jung on 14. 12. 9..
 */
@EActivity(R.layout.activity_photo_view)
public class PhotoViewActivity extends AppCompatActivity {

    public static final String TASK_ID_ACTIONBAR_HIDE = "actionbar_hide";
    @Extra
    String imageUrl;

    @Extra
    String imageType;

    @Extra
    String imageName;

    @Bean
    PhotoViewModel model;

    @ViewById(R.id.photo_photo_view)
    PhotoView photoView;

    @ViewById(R.id.progress_photo_view)
    ProgressBar progressBar;

    @AfterViews
    void initView() {
        Log.i("JANDI", "imageUrl - " + imageUrl);
        setupActionBar();

        photoView.setOnPhotoTapListener((view, v, v2) -> toggleActionbar());

        if (isGif()) {
            loadGif();
        } else {
            downloadImageFile();
        }

        autoHideActionBar();
    }

    @Override
    protected void onDestroy() {
        model.deleteImageFile();
        super.onDestroy();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(imageName);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionMenuClick() {
        finish();
    }

    @UiThread
    void downloadImageFile() {
        // deep zoom 시 exif 정보를 토대로 회전 시켜보여주지 않음....
//        Ion.with(this)
//                .load(imageUrl)
//                .withBitmap()
//                .crossfade(true)
//                .fitCenter()
//                .error(R.drawable.jandi_fl_icon_deleted)
//                .intoImageView(photoView)
//                .setCallback((e, result) -> {
//                    progressBar.setVisibility(View.GONE);
//                });

        Glide.with(this).load(imageUrl)
                .asBitmap()
                .fitCenter()
                .error(R.drawable.jandi_fl_icon_deleted)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e,
                                               String model, Target<Bitmap> target,
                                               boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource,
                                                   String model, Target<Bitmap> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(photoView);
//        String directoryPath = getFilesDir() + File.separator + "/images";
//        File directory = new File(directoryPath);
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//
//        File tempFile = null;
//        try {
//            tempFile = File.createTempFile("image", ".jpg", directory);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        LogUtil.i(tempFile.getAbsolutePath());
//
//        File file = null;
//        try {
//            file = Ion.with(this).load(imageUrl).noCache().write(tempFile).get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return;
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            return;
//        }
//        if (file == null || !file.exists()) {
//            LogUtil.e("file is not exists");
//            return;
//        }
//
//        rotateBitmapAndShowImage(file);
    }

    @Background
    void rotateBitmapAndShowImage(File file) {
        String downloadedFilePath = file.getAbsolutePath();

//        int degree = model.getExifOrientationDegree(downloadedFilePath);
//        LogUtil.e("degree !! = " + degree);
//        Bitmap bitmap = null;
//        try {
//            bitmap = Ion.with(this).load(file).noCache().asBitmap().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return;
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        if (bitmap == null) {
//            LogUtil.e("bitmap is null");
//            return;
//        }
//
//        LogUtil.e("original width = " + bitmap.getWidth());
//        file.delete();
////        Bitmap bitmap = model.getBitmapFromFileAvoidOOM(downloadedFilePath);
//        Bitmap rotateBitmap = model.getRotateBitmap(bitmap, degree);
//
//        LogUtil.e("rotate width = " + rotateBitmap.getWidth());
//
//        file = model.getFileFromBitmap(rotateBitmap, downloadedFilePath);

        showImage(file);
    }

    @UiThread
    void showImage(File file) {
        model.setImageFile(file);

        // Deep Zoom 시 메모리 누수로 인한 앱 종료를 막기 위함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadImagePlain(file);
            return;
        }

        loadImageDeepZoom(file);
    }

    private void loadGif() {
        Ion.with(photoView)
                .load(imageUrl)
                .setCallback((e, result) -> progressBar.setVisibility(View.GONE));
    }

    void loadImagePlain(File file) {
        Ion.with(PhotoViewActivity.this)
                .load(file)
                .setLogging("INFO", Log.INFO)
                .withBitmap()
                .crossfade(true)
                .fitCenter()
                .error(R.drawable.jandi_fl_icon_deleted)
                .intoImageView(photoView)
                .setCallback((e, result) -> {
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void loadImageDeepZoom(File file) {
        Ion.with(PhotoViewActivity.this)
                .load(file)
                .setLogging("INFO", Log.INFO)
                .withBitmap()
                .crossfade(true)
                .fitCenter()
                .deepZoom()
                .intoImageView(photoView)
                .setCallback((e, result) -> {
                    if (e != null) {
                        loadImagePlain(file);
                    } else {
                        Drawable drawable = result.getDrawable();
                        int intrinsicWidth = drawable.getIntrinsicWidth();
                        int intrinsicHeight = drawable.getIntrinsicHeight();
                        if (intrinsicHeight <= 0 || intrinsicWidth <= 0) {
                            loadImagePlain(file);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private boolean isGif() {
        return TextUtils.equals(imageType, "image/gif");
    }

    /**
     * After 3sec, hide actionbar
     */
    @Background(id = TASK_ID_ACTIONBAR_HIDE, delay = 3000)
    void autoHideActionBar() {
        if (getSupportActionBar().isShowing()) {
            // if Actionbar show, then hide actionbar
            toggleActionbar();
        }
    }

    @UiThread
    void toggleActionbar() {
        ActionBar actionBar = getSupportActionBar();
        boolean showing = actionBar.isShowing();

        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();

        if (showing) {
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }

            actionBar.hide();

        } else {
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            actionBar.show();
            BackgroundExecutor.cancelAll(TASK_ID_ACTIONBAR_HIDE, false);
            autoHideActionBar();
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
}
