package com.tosslab.jandi.app.ui.photo;

import android.app.ActionBar;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
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
public class PhotoViewActivity extends FragmentActivity {

    @Extra
    String imageUrl;

    @Extra
    String imageType;

    @ViewById(R.id.photo_photo_view)
    PhotoView photoView;

    @AfterViews
    void initView() {

        photoView.setOnPhotoTapListener((view, v, v2) -> toggleActionbar());

        if (isGif()) {
            loadGif();
        } else {
            loadImage();
        }

        autoHideActionBar();
    }

    private void loadImage() {
        Glide.with(this)
                .load(imageUrl)
                .crossFade()
                .into(photoView);
    }

    private void loadGif() {
        Ion.with(this)
                .load(imageUrl)
                .intoImageView(photoView);
    }

    private boolean isGif() {
        return TextUtils.equals(imageType, "image/gif");
    }

    /**
     * After 3sec, hide actionbar
     */
    @Background(delay = 3000)
    void autoHideActionBar() {
        if (getActionBar().isShowing()) {
            // if Actionbar show, then hide actionbar
            toggleActionbar();
        }
    }

    @UiThread
    void toggleActionbar() {
        ActionBar actionBar = getActionBar();
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
