package com.tosslab.jandi.app.ui.photo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.controller.AutoProgressUpdateController;

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
    String thumbUrl;
    @FragmentArg
    String originalUrl;
    @FragmentArg
    String imageType;
    @FragmentArg
    String extensions;

    @ViewById(R.id.pv_photoview)
    PhotoView photoView;
    @ViewById(R.id.progress_photoview)
    CircleProgressBar progressBar;
    @ViewById(R.id.tv_photoview_percentage)
    TextView tvPercentage;
    @ViewById(R.id.vg_photoview_progress)
    LinearLayout vgProgress;
    @ViewById(R.id.vg_photoview_tap_to_view)
    View btnTapToViewOriginal;
    private CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener;
    private OnSwipeExitListener onSwipeExitListener;
    private AutoProgressUpdateController autoProgressUpdateController;

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

        photoView.setOnViewTapListener((view, x, y) -> {
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

        boolean shouldSupportImageExtensions = FileExtensionsUtil.shouldSupportImageExtensions(extensions);
        if (!shouldSupportImageExtensions
                || (TextUtils.isEmpty(thumbUrl) && TextUtils.isEmpty(originalUrl))) {
            LogUtil.e(TAG, "Url is empty.");
            vgProgress.setVisibility(View.GONE);
            showError();
            photoView.setZoomable(false);
            return;
        }

        autoProgressUpdateController = new AutoProgressUpdateController();
        autoProgressUpdateController.setPercentageTextView(tvPercentage);
        autoProgressUpdateController.setProgressBar(progressBar);
//        autoProgressUpdateController.start();

        if (!TextUtils.isEmpty(thumbUrl)) {

            loadImage(Uri.parse(thumbUrl));

        } else {

            final Uri originalUri = Uri.parse(originalUrl);

            ImageLoader.newInstance()
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
//                    .blockNetworking(true)
                    .listener(new SimpleRequestListener<Uri, GlideDrawable>() {

                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable,
                                                       Uri model, Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource) {
                            hideProgress();
                            return false;
                        }

                        @Override
                        public boolean onException(Exception e, Uri model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource) {
                            hideProgress();

                            // cache 가 되어 있지 않음
                            showTapToView(originalUri);
                            return true;
                        }
                    })
                    .uri(originalUri)
                    .intoWithProgress(photoView);
        }
    }

    private void showTapToView(Uri originalUri) {
        // PhotoView 그려진 이미지(Drawable)이 없으면 ViewTapListener 가 동작하지 않는다.
        photoView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        vgProgress.setVisibility(View.GONE);
        btnTapToViewOriginal.setVisibility(View.VISIBLE);
        btnTapToViewOriginal.setOnClickListener(v -> {
            btnTapToViewOriginal.setVisibility(View.GONE);

            vgProgress.setVisibility(View.VISIBLE);

            autoProgressUpdateController = new AutoProgressUpdateController();
            autoProgressUpdateController.setPercentageTextView(tvPercentage);
            autoProgressUpdateController.setProgressBar(progressBar);

            autoProgressUpdateController.start();

            loadImage(originalUri);
        });
    }

    private void setupProgress() {
        int progressWidth = (int) (getResources().getDisplayMetrics().density * 4);
        progressBar.setBgStrokeWidth(progressWidth);
        progressBar.setProgressStrokeWidth(progressWidth);
        progressBar.setBgColor(getResources().getColor(R.color.jandi_primary_color));
        progressBar.setProgressColor(getResources().getColor(R.color.jandi_accent_color));
        progressBar.setMax(100);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void updateProgress(float progress) {
        if (progressBar.getProgress() == 100) {
            return;
        }

        int percentage = (int) (progress * 100);
        percentage = Math.min(percentage, 99);

        progressBar.setProgress(percentage);
        tvPercentage.setText(percentage + "%");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void hideProgress() {
        if (vgProgress.getVisibility() == View.VISIBLE) {
            progressBar.setProgress(100);
            tvPercentage.setText(100 + "%");
        }
        vgProgress.setVisibility(View.GONE);

        if (autoProgressUpdateController != null) {
            autoProgressUpdateController.cancel();
            autoProgressUpdateController = null;
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void loadImage(Uri uri) {
        ImageLoader.newInstance()
                .uri(uri)
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                        hideProgress();

                        showError();
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   Uri model, Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        hideProgress();
                        return false;
                    }
                })
                .intoWithProgress(photoView);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showError() {
        ImageLoader.loadFromResources(photoView, R.drawable.file_noimage);
    }

    @Override
    public void onDestroyView() {
        if (autoProgressUpdateController != null) {
            autoProgressUpdateController.cancel();
        }
        super.onDestroyView();
    }

    public void setOnCarouselImageClickListener(

            CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener) {
        this.carouselImageClickListener = carouselImageClickListener;
    }

}
