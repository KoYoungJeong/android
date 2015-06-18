package com.tosslab.jandi.app.ui.photo;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.photo.presenter.PhotoViewPresenter;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgress;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
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
public class PhotoViewActivity extends AppCompatActivity implements PhotoViewPresenter.View {

    public static final String TASK_ID_ACTIONBAR_HIDE = "actionbar_hide";

    @Extra
    String imageUrl;

    @Extra
    String imageType;

    @Extra
    String imageName;

    @ViewById(R.id.pv_photoview)
    PhotoView photoView;

    @ViewById(R.id.progress_photoview)
    CircleProgress progress;

    @ViewById(R.id.tv_photoview_percentage)
    TextView tvPercentage;

    @ViewById(R.id.vg_photoview_progress)
    LinearLayout vgProgress;

    @Bean
    PhotoViewPresenter presenter;

    @AfterViews
    void initView() {
        Glide.get(this).clearMemory();
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);

        setupActionBar();

        setupProgress();

        presenter.setView(this);
        presenter.loadImage(imageUrl, imageType,
                (downloaded, total) -> updateProgress(total, downloaded));

        photoView.setOnPhotoTapListener((view, x, y) -> toggleActionbar());

        autoHideActionBar();
    }

    private void setupProgress() {
        int progressWidth = (int) (getResources().getDisplayMetrics().density * 4);
        progress.setBgStrokeWidth(progressWidth);
        progress.setProgressStrokeWidth(progressWidth);
        progress.setBgColor(getResources().getColor(R.color.jandi_primary_color));
        progress.setProgressColor(getResources().getColor(R.color.jandi_accent_color));
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

    /**
     * After 3sec, hide actionbar
     */
    @Background(id = TASK_ID_ACTIONBAR_HIDE, delay = 3000)
    public void autoHideActionBar() {
        if (getSupportActionBar().isShowing()) {
            // if Actionbar show, then hide actionbar
            toggleActionbar();
        }
    }

    @UiThread
    public void toggleActionbar() {
        ActionBar actionBar = getSupportActionBar();
        boolean showing = actionBar.isShowing();

        int newUiOptions;
        if (showing) {
            newUiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;

            actionBar.hide();

        } else {
            newUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;

            actionBar.show();
            BackgroundExecutor.cancelAll(TASK_ID_ACTIONBAR_HIDE, false);
            autoHideActionBar();
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    // Gif 일 때는 Ion 이용.(다운로드 불필요)
    @UiThread
    @Override
    public void loadImageGif() {
        Ion.with(getApplicationContext())
                .load(imageUrl)
                .progress((downloaded, total) -> updateProgress(total, downloaded))
                .intoImageView(photoView)
                .setCallback((e, result) -> {
                    hideProgress();
                    if (e != null) {
                        e.printStackTrace();
                        if (result != null) {
                            result.setImageResource(R.drawable.jandi_fl_icon_deleted);
                        }
                    }
                });
    }

    private boolean isForeground = true;

    @Override
    public boolean isForeground() {
        return isForeground;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    protected void onStop() {
        isForeground = false;
        super.onStop();
    }

    @UiThread
    @Override
    public void updateProgress(long total, long downloaded) {
        progress.setMax((int) total);
        progress.setProgress((int) downloaded);
        int percentage = (int) ((downloaded / (float) total) * 100);
        tvPercentage.setText(percentage + "%");
    }

    @UiThread
    @Override
    public void hideProgress() {
        vgProgress.animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new SimpleEndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (vgProgress != null) {
                            vgProgress.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @UiThread
    @Override
    public <T> void loadImage(T target) {
        Glide.with(this).load(target).asBitmap()
                .fitCenter()
                .error(R.drawable.jandi_fl_icon_deleted)
                .listener(new RequestListener<T, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, T model, Target<Bitmap> target,
                                               boolean isFirstResource) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                        hideProgress();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, T model,
                                                   Target<Bitmap> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        hideProgress();
                        return false;
                    }
                })
                .into(photoView);
    }

}
