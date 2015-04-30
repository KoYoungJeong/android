package com.tosslab.jandi.app.ui.photo;

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

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;

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

    @ViewById(R.id.photo_photo_view)
    PhotoView photoView;

    @ViewById(R.id.progress_photo_view)
    ProgressBar progressBar;

    @AfterViews
    void initView() {

        setupActionBar();

        photoView.setOnPhotoTapListener((view, v, v2) -> toggleActionbar());

        if (isGif()) {
            loadGif();
        } else {
            loadImageDeepZoom();
        }

        autoHideActionBar();
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

    private void loadImageDeepZoom() {
        Ion.with(PhotoViewActivity.this)
                .load(imageUrl)
                .setLogging("INFO", Log.INFO)
                .withBitmap()
                .crossfade(true)
                .fitCenter()
                .deepZoom()
                .intoImageView(photoView)
                .setCallback((e, result) -> {

                    if (e != null) {
                        loadImagePlain();
                    } else {

                        Drawable drawable = result.getDrawable();
                        int intrinsicWidth = drawable.getIntrinsicWidth();
                        int intrinsicHeight = drawable.getIntrinsicHeight();
                        if (intrinsicHeight <= 0 || intrinsicWidth <= 0) {
                            loadImagePlain();
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }


                    }
                });
    }

    @UiThread
    void loadImagePlain() {
        Ion.with(PhotoViewActivity.this)
                .load(imageUrl)
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

    private void loadGif() {
        Ion.with(photoView)
                .load(imageUrl)
                .setCallback((e, result) -> progressBar.setVisibility(View.GONE));
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
