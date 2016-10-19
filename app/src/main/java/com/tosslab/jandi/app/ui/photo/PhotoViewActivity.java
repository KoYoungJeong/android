package com.tosslab.jandi.app.ui.photo;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by Steve SeongUg Jung on 15. 7. 14..
 */
@EActivity(R.layout.activity_photo_view)
public class PhotoViewActivity extends BaseAppCompatActivity implements OnSwipeExitListener {

    @Extra
    String thumbUrl;

    @Extra
    String originalUrl;

    @Extra
    String imageType;

    @Extra
    String imageName;

    @Extra
    String extensions;

    private boolean isFullScreen;

    @AfterViews
    void initViews() {
        PhotoViewFragment fragment = PhotoViewFragment_.builder()
                .imageType(imageType)
                .originalUrl(thumbUrl)
                .thumbUrl(originalUrl)
                .extensions(extensions)
                .mode(PhotoViewFragment.EXTRA_MODE_NORMAL)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vg_photo_view, fragment)
                .commit();

        setUpActionbar();

        fragment.setOnCarouselImageClickListener(() -> {
            isFullScreen = !isFullScreen;
            setUpFullScreen(isFullScreen);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpFullScreen(isFullScreen);
    }

    private void setUpFullScreen(boolean isFullScreen) {
        int systemUiOptions;
        ActionBar actionBar = getSupportActionBar();

        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // close nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // close status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                // Do Nothing
                systemUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            // 액션바 하단 툴바 숨기기
            if (actionBar != null) {
                actionBar.hide();
            }
        } else {
            //  상태바, 액션바, 하단 툴바 노출
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

            } else {
                systemUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            if (actionBar != null) {
                actionBar.show();
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(systemUiOptions);
    }

    private void setUpActionbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_photo_view);
        toolbar.setTitle(imageName);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    @Override
    public void onSwipeExit(int direction) {
        finish();

        int anim = R.anim.slide_out_to_bottom;
        if (direction == OnSwipeExitListener.DIRECTION_TO_TOP) {
            anim = R.anim.slide_out_to_top;
        }

        overridePendingTransition(0, anim);
    }
}
