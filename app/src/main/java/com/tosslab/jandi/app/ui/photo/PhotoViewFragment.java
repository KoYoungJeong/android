package com.tosslab.jandi.app.ui.photo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.GifReadyEvent;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Steve SeongUg Jung on 14. 12. 9..
 */
public class PhotoViewFragment extends Fragment {
    public static final String TAG = PhotoViewFragment.class.getSimpleName();

    public static final int EXTRA_MODE_SINGLE = 0x01;
    public static final int EXTRA_MODE_CAROUSEL = 0x02;

    private static final long MB_1 = 1024 * 1024;

    @Nullable
    @InjectExtra
    String thumbUrl;
    @Nullable
    @InjectExtra
    String originalUrl;
    @Nullable
    @InjectExtra
    String imageType;
    @Nullable
    @InjectExtra
    String extensions;
    @Nullable
    @InjectExtra
    long size = -1;
    @Nullable
    @InjectExtra
    int mode; // only for use Sprinklr

    @Bind(R.id.iv_photoView)
    SubsamplingScaleImageView ivPhotoView;

    @Bind(R.id.vg_no_preview)
    ViewGroup vgNoPreview;

    @Bind(R.id.pv_photoview)
    PhotoView pvPhotoView;

    @Bind(R.id.progress_photoview)
    CircleProgressBar progressBar;
    @Bind(R.id.tv_photoview_percentage)
    TextView tvPercentage;
    @Bind(R.id.vg_photoview_progress)
    RelativeLayout vgProgress;
    @Bind(R.id.vg_photoview_tap_to_view)
    View btnTapToViewOriginal;

    @Bind(R.id.tv_photoview_play_size)
    TextView tvPlayGif;

    @Bind(R.id.vg_photoview_play)
    View vgPlayGif;


    private CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener;
    private OnSwipeExitListener onSwipeExitListener;

    public static PhotoViewFragment create(String thumbUrl,
                                           String originalUrl,
                                           String imageType,
                                           String extensions,
                                           long size,
                                           int mode) {

        Bundle bundle = new Bundle();
        bundle.putString("thumbUrl", thumbUrl);
        bundle.putString("originalUrl", originalUrl);
        bundle.putString("imageType", imageType);
        bundle.putString("extensions", extensions);
        bundle.putLong("size", size);
        bundle.putInt("mode", mode);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSwipeExitListener) {
            onSwipeExitListener = (OnSwipeExitListener) activity;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }
        initView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(GifReadyEvent event) {
        if (imageType.toLowerCase().contains("gif")
                && size > MB_1
                && TextUtils.equals(event.getOriginalUrl(), originalUrl)) {
            Completable.fromAction(() -> {
                vgPlayGif.setVisibility(View.GONE);
                loadImageForGif(Uri.parse(originalUrl));
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        }
    }

    void initView() {
        setupProgress();

        boolean shouldSupportImageExtensions = FileExtensionsUtil.shouldSupportImageExtensions(imageType);
        if (!shouldSupportImageExtensions
                || (TextUtils.isEmpty(thumbUrl) && TextUtils.isEmpty(originalUrl))) {
            LogUtil.e(TAG, "Url is empty.");
            vgProgress.setVisibility(View.GONE);
            showError();
            ivPhotoView.setZoomEnabled(false);
            pvPhotoView.setZoomable(false);
            return;
        }

        if (!TextUtils.isEmpty(imageType)
                && imageType.toLowerCase().contains("gif")
                && size > 0) {

            pvPhotoView.setVisibility(View.VISIBLE);
            ivPhotoView.setVisibility(View.GONE);

            // gif 인 경우 1MB 이상은 플레이 버튼 누르도록 함
            if (size < MB_1) {
                loadImage(Uri.parse(originalUrl));
            } else {
                hideProgress();
                ImageLoader.newInstance()
                        // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                        .blockNetworking(true)
                        .listener(new SimpleRequestListener<Uri, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, Uri model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFirstResource) {
                                // cache 가 되어 있지 않음
                                vgPlayGif.setVisibility(View.VISIBLE);
                                tvPlayGif.setText(getExt(imageType).toUpperCase() + ", " + FileUtil.formatFileSize(size));
                                loadImageForGif(Uri.parse(thumbUrl));
                                return true;
                            }
                        })
                        .fragment(this)
                        .uri(Uri.parse(originalUrl))
                        .into(pvPhotoView);
            }

        } else if (!TextUtils.isEmpty(thumbUrl)) {
            pvPhotoView.setVisibility(View.GONE);
            ivPhotoView.setVisibility(View.VISIBLE);
            loadImage(Uri.parse(thumbUrl));
        } else {
            pvPhotoView.setVisibility(View.VISIBLE);
            ivPhotoView.setVisibility(View.GONE);

            final Uri originalUri = Uri.parse(originalUrl);

            ImageLoader.newInstance()
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                    .blockNetworking(true)
                    .listener(new SimpleRequestListener<Uri, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, Uri model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource) {
                            // cache 가 되어 있지 않음
                            showTapToView(originalUri);
                            return true;
                        }
                    })
                    .fragment(this)
                    .uri(originalUri)
                    .intoWithProgress(ivPhotoView,
                            () -> Observable.just(0)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(it -> {
                                        progressBar.setMax(100);
                                        progressBar.setProgress(it);
                                        tvPercentage.setText("0 %");
                                    }),
                            progress -> Observable.just(progress)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(it -> {
                                        progressBar.setMax(100);
                                        progressBar.setProgress(it);
                                        tvPercentage.setText(String.format("%d %%", progress));
                                    }),
                            null,
                            this::hideProgress);
        }

        if (ivPhotoView.getVisibility() == View.VISIBLE) {

            ivPhotoView.setOnClickListener(v -> {
                if (carouselImageClickListener != null) {
                    carouselImageClickListener.onCarouselImageClick();
                }
            });

            final GestureDetector gestureDetector = new GestureDetector(getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                                return false;
                            }

                            if (onSwipeExitListener == null) {
                                return false;
                            }

                            onSwipeExitListener.onSwipeExit(velocityY > 0
                                    ? OnSwipeExitListener.DIRECTION_TO_BOTTOM : OnSwipeExitListener.DIRECTION_TO_TOP);
                            return true;
                        }
                    });

            ivPhotoView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        } else {
            pvPhotoView.setOnViewTapListener((view, x, y) -> {
                if (carouselImageClickListener != null) {
                    carouselImageClickListener.onCarouselImageClick();
                }
            });

            pvPhotoView.setOnSingleFlingListener((e1, e2, velocityX, velocityY) -> {
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
        }
    }

    public void loadImageForGif(Uri uri) {
        ImageLoader.newInstance()
                .uri(uri)
                .placeHolder(pvPhotoView.getDrawable())
                .fragment(this)
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        showError();
                        return true;
                    }
                })
                .intoWithProgress(pvPhotoView, () -> Observable.just(0)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(it -> {
                                    if (vgProgress.getVisibility() != View.VISIBLE) {
                                        vgProgress.setVisibility(View.VISIBLE);
                                    }
                                    progressBar.setMax(100);
                                    progressBar.setProgress(it);
                                    tvPercentage.setText("0 %");
                                }),
                        progress -> Observable.just(progress)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(it -> {
                                    progressBar.setMax(100);
                                    progressBar.setProgress(it);
                                    tvPercentage.setText(String.format("%d %%", progress));
                                }),
                        null,
                        this::hideProgress);
    }

    @NonNull
    private String getExt(String imageType) {
        if (!TextUtils.isEmpty(imageType)) {
            int index = imageType.indexOf("/");
            if (index >= 0 && index < imageType.length()) {
                return imageType.substring(index + 1);
            }
        }
        return "";
    }

    @OnClick(R.id.btn_photoview_play)
    void onPlayGifClick() {
        loadImage(Uri.parse(originalUrl));
        vgPlayGif.setVisibility(View.GONE);

    }

    private void showTapToView(Uri originalUri) {
        // PhotoView 그려진 이미지(Drawable)이 없으면 ViewTapListener 가 동작하지 않는다.
        ivPhotoView.setBackgroundColor(Color.TRANSPARENT);
        vgProgress.setVisibility(View.GONE);
        btnTapToViewOriginal.setVisibility(View.VISIBLE);
        btnTapToViewOriginal.setOnClickListener(v -> {
            btnTapToViewOriginal.setVisibility(View.GONE);
            vgProgress.setVisibility(View.VISIBLE);
            if (mode == EXTRA_MODE_SINGLE) {
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.ImageFullScreen,
                        AnalyticsValue.Action.ViewOriginalImage);
            } else {
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.Carousel,
                        AnalyticsValue.Action.ViewOriginalImage);
            }

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

    public void hideProgress() {
        Completable.fromAction(() -> {

            if (vgProgress.getVisibility() == View.VISIBLE) {
                progressBar.setProgress(100);
                tvPercentage.setText("100 %");
            }
            vgProgress.setVisibility(View.GONE);
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();

    }

    public void loadImage(Uri uri) {
        Bitmap cachedBitmap = ivPhotoView.getDrawingCache();
        Drawable placeholder = null;
        if (cachedBitmap != null) {
            placeholder = new BitmapDrawable(cachedBitmap);
        }
        ImageLoader.newInstance()
                .uri(uri)
                .placeHolder(placeholder)
                .fragment(this)
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        showError();
                        return true;
                    }
                })
                .intoWithProgress(ivPhotoView, () -> Observable.just(0)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(it -> {
                                    if (vgProgress.getVisibility() != View.VISIBLE) {
                                        vgProgress.setVisibility(View.VISIBLE);
                                    }
                                    progressBar.setMax(100);
                                    progressBar.setProgress(it);
                                    tvPercentage.setText("0 %");
                                }),
                        progress -> Observable.just(progress)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(it -> {
                                    progressBar.setMax(100);
                                    progressBar.setProgress(it);
                                    tvPercentage.setText(String.format("%d %%", progress));
                                }),
                        null,
                        this::hideProgress);
    }

    void showError() {
        Completable.fromAction(() -> {
            vgNoPreview.setVisibility(View.VISIBLE);
            pvPhotoView.setVisibility(View.GONE);
            ivPhotoView.setVisibility(View.GONE);
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    public void setOnCarouselImageClickListener(

            CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener) {
        this.carouselImageClickListener = carouselImageClickListener;
    }

}
