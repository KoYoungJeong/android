package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.GifReadyEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

public class ImageFileViewHolder extends FileViewHolder {

    public static final int SIZE_1MB = 1024 * 1024;
    @Bind(R.id.iv_file_detail_thumb)
    ImageView ivFileThumb;

    @Bind(R.id.vg_file_detail_tap_to_view)
    View btnTapToView;

    @Bind(R.id.vg_file_detail_no_image)
    View vUnavailableIndicator;
    @Bind(R.id.vg_photoview_play)
    ViewGroup vGifPlay;
    @Bind(R.id.btn_photoview_play)
    View btnGifPlay;
    @Bind(R.id.tv_photoview_play_size)
    TextView tvGifPlay;
    @Bind(R.id.progress_file_detail)
    CircleProgressBar progressBar;
    @Bind(R.id.tv_file_detail_percentage)
    TextView tvProgress;
    @Bind(R.id.vg_file_detail_progress)
    ViewGroup vgProgress;
    private OnImageFileClickListener onImageFileClickListener;
    private ImageLoader.ProgressStarted progressStarted;
    private ImageLoader.ProgressDownloading progressDownloading;
    private ImageLoader.ProgressPresent progressPresent;

    private ImageFileViewHolder(View itemView, OnImageFileClickListener onImageFileClickListener) {
        super(itemView);
        this.onImageFileClickListener = onImageFileClickListener;
    }

    public static ImageFileViewHolder newInstance(ViewGroup parent, OnImageFileClickListener onImageFileClickListener) {
        return new ImageFileViewHolder(FileViewHolder.getItemView(parent), onImageFileClickListener);
    }

    @Override
    public void addContentView(ViewGroup parent) {
        LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_file_detail_image_content, parent, true);
    }

    @Override
    protected void initView() {
        int progressWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, progressBar.getResources().getDisplayMetrics());
        progressBar.setBgStrokeWidth(progressWidth);
        progressBar.setProgressStrokeWidth(progressWidth);
        progressBar.setBgColor(progressBar.getResources().getColor(R.color.jandi_primary_color));
        progressBar.setProgressColor(progressBar.getResources().getColor(R.color.jandi_accent_color));
    }

    @Override
    public void bindFileContent(ResMessages.FileMessage fileMessage) {
        vgProgress.setVisibility(View.GONE);
        final ResMessages.FileContent content = fileMessage.content;

        boolean hasImageUrl = ImageUtil.hasImageUrl(content);
        final String originalUrl = ImageUtil.getOriginalUrl(content);

        if (!hasImageUrl || !FileExtensionsUtil.shouldSupportImageExtensions(content.type)) {
            ivFileThumb.setVisibility(View.GONE);
            ivFileThumb.setOnClickListener(null);
            vUnavailableIndicator.setVisibility(View.VISIBLE);
            return;
        }

        final long fileMessageId = fileMessage.id;

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);
        ivFileThumb.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, fileMessage, false));

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);

        if (isDeleted(fileMessage.status)) {

            ivFileThumb.setImageResource(R.drawable.file_icon_delete_198);
            return;
        }

        progressStarted = () -> {
            Completable.fromAction(() -> {
                if (vgProgress.getVisibility() != View.VISIBLE) {
                    vgProgress.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(0);
                progressBar.setMax(100);
                tvProgress.setText("0 %");
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        };
        progressDownloading = progress -> {
            Completable.fromAction(() -> {
                if (vgProgress.getVisibility() != View.VISIBLE) {
                    vgProgress.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(progress);
                tvProgress.setText(progress + " %");
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        };
        progressPresent = () -> {
            Completable.fromAction(() -> {
                vgProgress.setVisibility(View.GONE);
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
        };
        if (!TextUtils.isEmpty(localFilePath)) {
            ImageLoader.newInstance()
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                    .uri(UriUtil.getFileUri(localFilePath))
                    .into(ivFileThumb);
            return;
        }

        if (content.type.contains("gif") && content.size > 0) {
            if (content.size < SIZE_1MB) {

                // 캐시로드 할 때는 그냥 호출
                ImageLoader.newInstance()
                        .blockNetworking(true)
                        .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                        .listener(new SimpleRequestListener<Uri, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, Uri model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFirstResource) {

                                // 캐시 없으면 프로그래스 노출 로딩
                                ImageLoader.newInstance()
                                        .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                                        .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                                        .uri(Uri.parse(ImageUtil.getOriginalUrl(content)))
                                        .intoWithProgress(ivFileThumb, progressStarted, progressDownloading, null, progressPresent);
                                return true;
                            }
                        })
                        .uri(Uri.parse(ImageUtil.getOriginalUrl(content)))
                        .into(ivFileThumb);

            } else {
                // progress 처리
                Uri originalUri = Uri.parse(originalUrl);

                // 캐시로드.
                ImageLoader.newInstance()
                        .blockNetworking(true)
                        .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                        .listener(new SimpleRequestListener<Uri, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, Uri model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFirstResource) {
                                // 썸네일 노출 + Play 버튼 노출
                                setUpGifPlay(content);
                                loadingThumb(content);

                                return true;
                            }
                        })
                        .uri(originalUri)
                        .into(ivFileThumb);
            }
        } else if (hasThumbnailUrl) {
            // 캐시 로드
            ImageLoader.newInstance()
                    .blockNetworking(true)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .uri(Uri.parse(ImageUtil.getThumbnailUrl(content)))
                    .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            // 캐시 없으면 프로그래스 노출 로딩
                            loadingThumb(content);
                            return true;
                        }
                    })
                    .into(ivFileThumb);
        } else {
            // 캐시 로드
            ImageLoader.newInstance()
                    .blockNetworking(true)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable,
                                                       Uri model, Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource) {
                            btnTapToView.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onException(Exception e, Uri model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFirstResource) {
                            // cache 가 되어 있지 않음
                            showTapToViewLayout(fileMessageId, fileMessage);
                            return true;
                        }
                    })
                    .uri(Uri.parse(ImageUtil.getOriginalUrl(fileMessage.content)))
                    .into(ivFileThumb);
        }

    }

    private void setUpGifPlay(ResMessages.FileContent content) {
        vGifPlay.setVisibility(View.VISIBLE);
        tvGifPlay.setText(String.format("GIF, %s", FileUtil.formatFileSize(content.size)));
        btnGifPlay.setOnClickListener(v -> {
            ImageLoader.newInstance()
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                    .placeHolder(ivFileThumb.getDrawable())
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onResourceReady(GlideDrawable glideDrawable, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            EventBus.getDefault().post(new GifReadyEvent(ImageUtil.getOriginalUrl(content)));
                            return super.onResourceReady(glideDrawable, model, target, isFromMemoryCache, isFirstResource);
                        }
                    })
                    .uri(Uri.parse(ImageUtil.getOriginalUrl(content)))
                    .intoWithProgress(ivFileThumb, progressStarted, progressDownloading, null, progressPresent);
            vGifPlay.setVisibility(View.GONE);
        });
    }

    protected void loadingThumb(ResMessages.FileContent content) {
        if (ImageUtil.getThumbnailUrl(content) != null) {
            ImageLoader.newInstance()
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                    .uri(Uri.parse(ImageUtil.getThumbnailUrl(content)))
                    .intoWithProgress(ivFileThumb, progressStarted, progressDownloading, null, progressPresent);
        }
    }

    private void showTapToViewLayout(long fileMessageId, ResMessages.FileMessage fileMessage) {

        btnTapToView.setVisibility(View.VISIBLE);

        ivFileThumb.setOnClickListener(null);

        btnTapToView.setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewOriginalImage);

            btnTapToView.setVisibility(View.GONE);

            moveToPhotoViewer(fileMessageId, fileMessage, true);
        });
    }

    private void moveToPhotoViewer(long fileMessageId, ResMessages.FileMessage fileMessage,
                                   boolean shouldOpenImmediately) {
        if (onImageFileClickListener != null) {
            onImageFileClickListener.onImageFileClick(fileMessageId, fileMessage, shouldOpenImmediately);
        }
    }

    public void setOnImageFileClickListener(OnImageFileClickListener onImageFileClickListener) {
        this.onImageFileClickListener = onImageFileClickListener;
    }

    public interface OnImageFileClickListener {
        void onImageFileClick(long fileMessageId, ResMessages.FileMessage fileMessage,
                              boolean shouldOpenImmediately);
    }
}
