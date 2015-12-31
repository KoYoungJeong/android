package com.tosslab.jandi.app.ui.photo;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatableDrawable;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgress;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.image.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;
import com.tosslab.jandi.app.utils.image.ClosableAttachStateChangeListener;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Steve SeongUg Jung on 14. 12. 9..
 */
@EFragment(R.layout.fragment_photo_view)
public class PhotoViewFragment extends Fragment {
    public static final String TAG = PhotoViewFragment.class.getSimpleName();

    @FragmentArg
    boolean fromCarousel = false;

    @FragmentArg
    String imageUrl;

    @FragmentArg
    String imageType;

    @ViewById(R.id.pv_photoview)
    PhotoView photoView;

    @ViewById(R.id.progress_photoview)
    CircleProgress progressBar;

    @ViewById(R.id.tv_photoview_percentage)
    TextView tvPercentage;

    @ViewById(R.id.vg_photoview_progress)
    LinearLayout vgProgress;

    private CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener;

    private OnSwipeExitListener onSwipeExitListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSwipeExitListener) {
            onSwipeExitListener = (OnSwipeExitListener) activity;
        }
    }

    @AfterViews
    void initView() {
        setupProgress();

        photoView.setOnPhotoTapListener((view, x, y) -> {
            if (carouselImageClickListener != null) {
                carouselImageClickListener.onCarouselImageClick();
            }
        });

        photoView.setOnSingleFlingListener((e1, e2, velocityX, velocityY) -> {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                return false;
            }

            if (onSwipeExitListener == null) {
                return false;
            }

            onSwipeExitListener.onSwipeExit(velocityY > 0
                    ? OnSwipeExitListener.DIRECTION_TO_BOTTOM : OnSwipeExitListener.DIRECTION_TO_TOP);
            return true;
        });

        loadImage(Uri.parse(imageUrl));
    }

    private void setupProgress() {
        int progressWidth = (int) (getResources().getDisplayMetrics().density * 4);
        progressBar.setBgStrokeWidth(progressWidth);
        progressBar.setProgressStrokeWidth(progressWidth);
        progressBar.setBgColor(getResources().getColor(R.color.jandi_primary_color));
        progressBar.setProgressColor(getResources().getColor(R.color.jandi_accent_color));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void updateProgress(float progress) {
        progressBar.setMax(100);
        int percentage = (int) (progress * 100);
        progressBar.setProgress(percentage);
        tvPercentage.setText(percentage + "%");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void loadImage(Uri uri) {
        int width = ApplicationUtil.getDisplaySize(false);
        int height = ApplicationUtil.getDisplaySize(true);

        ResizeOptions resizeOptions = fromCarousel
                ? new ResizeOptions(width, height)
                : new ResizeOptions(ImageUtil.STANDARD_IMAGE_SIZE, ImageUtil.STANDARD_IMAGE_SIZE);

        ImageUtil.loadDrawable(uri, resizeOptions, new BaseOnResourceReadyCallback() {
            @Override
            public void onReady(Drawable drawable, CloseableReference reference) {
                hideProgress();

                setImageResource(drawable, reference);
            }

            @Override
            public void onFail(Throwable cause) {
                LogUtil.e(TAG, Log.getStackTraceString(cause));
                hideProgress();
            }

            @Override
            public void onProgressUpdate(float progress) {
                LogUtil.i(TAG, "progressBar = " + progress);
                updateProgress(progress);
            }
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setImageResource(Drawable drawable, CloseableReference reference) {
        photoView.setImageDrawable(drawable);

        if (drawable instanceof AnimatableDrawable) {
            ((AnimatableDrawable) drawable).start();
        }

        photoView.addOnAttachStateChangeListener(new ClosableAttachStateChangeListener(reference));
    }

    public void setOnCarouselImageClickListener(
            CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener) {
        this.carouselImageClickListener = carouselImageClickListener;
    }
}
