package com.tosslab.jandi.app.ui.photo;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Steve SeongUg Jung on 14. 12. 9..
 */
@EActivity(R.layout.activity_photo_view)
public class PhotoViewActivity extends ActionBarActivity {

    @Extra
    String imageUrl;

    @Extra
    String imageType;

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
            loadImage();
        }

        autoHideActionBar();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void loadImage() {
        Ion.with(photoView)
                .crossfade(true)
                .load(imageUrl)
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadGif() {
        Ion.with(photoView)
                .load(imageUrl)
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private boolean isGif() {
        return TextUtils.equals(imageType, "image/gif");
    }

    /**
     * After 3sec, hide actionbar
     */
    @Background(delay = 3000)
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
            autoHideActionBar();
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

    }


}
