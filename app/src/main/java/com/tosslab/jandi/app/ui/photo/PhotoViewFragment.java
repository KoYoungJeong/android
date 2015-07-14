package com.tosslab.jandi.app.ui.photo;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.photo.presenter.PhotoViewPresenter;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgress;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Steve SeongUg Jung on 14. 12. 9..
 */
@EFragment(R.layout.fragment_photo_view)
public class PhotoViewFragment extends Fragment implements PhotoViewPresenter.View {

    public static final String TASK_ID_ACTIONBAR_HIDE = "actionbar_hide";

    @FragmentArg
    String imageUrl;

    @FragmentArg
    String imageType;

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
    private CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener;

    @AfterViews
    void initView() {
        Glide.get(getActivity().getApplicationContext()).clearMemory();
        Glide.get(getActivity().getApplicationContext()).setMemoryCategory(MemoryCategory.HIGH);

        setupProgress();

        presenter.setView(this);
        presenter.loadImage(imageUrl, imageType,
                (downloaded, total) -> updateProgress(total, downloaded));

        photoView.setOnPhotoTapListener((view, x, y) -> {
            if (carouselImageClickListener != null) {
                carouselImageClickListener.onCarouselImageClick();
            }
        });

    }

    private void setupProgress() {
        int progressWidth = (int) (getResources().getDisplayMetrics().density * 4);
        progress.setBgStrokeWidth(progressWidth);
        progress.setProgressStrokeWidth(progressWidth);
        progress.setBgColor(getResources().getColor(R.color.jandi_primary_color));
        progress.setProgressColor(getResources().getColor(R.color.jandi_accent_color));
    }

    // Gif 일 때는 Ion 이용.(다운로드 불필요)
    @UiThread
    @Override
    public void loadImageGif() {
        Ion.with(getActivity().getApplicationContext())
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
    public void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    public void onStop() {
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


    public void setOnCarouselImageClickListener(CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener) {

        this.carouselImageClickListener = carouselImageClickListener;
    }
}
