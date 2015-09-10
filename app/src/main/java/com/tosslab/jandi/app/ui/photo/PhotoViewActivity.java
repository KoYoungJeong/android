package com.tosslab.jandi.app.ui.photo;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by Steve SeongUg Jung on 15. 7. 14..
 */
@EActivity(R.layout.activity_photo_view)
public class PhotoViewActivity extends AppCompatActivity {

    @Extra
    String imageUrl;

    @Extra
    String imageType;

    @Extra
    String imageName;
    private boolean isFullScreen;

    @AfterViews
    void initViews() {

        Glide.get(getApplicationContext()).clearMemory();
        Glide.get(getApplicationContext()).setMemoryCategory(MemoryCategory.HIGH);


        PhotoViewFragment fragment = PhotoViewFragment_.builder()
                .imageType(imageType)
                .imageUrl(imageUrl)
                .build();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vg_photo_view, fragment)
                .commit();

        setUpActionbar();

        fragment.setOnCarouselImageClickListener(new CarouselViewerActivity.OnCarouselImageClickListener() {
            @Override
            public void onCarouselImageClick() {
                isFullScreen = !isFullScreen;
                setUpFullScreen(isFullScreen);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpFullScreen(isFullScreen);
        ActivityHelper.setOrientation(this);
    }

    private void setUpFullScreen(boolean isFullScreen) {
        int systemUiOptions;
        ActionBar actionBar = getSupportActionBar();

        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
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
}
