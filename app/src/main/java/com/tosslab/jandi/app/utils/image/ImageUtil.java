package com.tosslab.jandi.app.utils.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;

import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class ImageUtil {
    public static final String TAG = ImageUtil.class.getSimpleName();

    public static final int STANDARD_IMAGE_SIZE = 2048;

    public static boolean hasCache(Uri uri) {
        final boolean isInMemoryCache = Fresco.getImagePipeline().isInBitmapMemoryCache(uri);
        LogUtil.i(TAG, "isInMemoryCache - " + isInMemoryCache);
        return isInMemoryCache || isInDiskCache(uri);
    }

    public static boolean isInDiskCache(Uri uri) {
        DataSource<Boolean> dataSource = Fresco.getImagePipeline().isInDiskCache(uri);
        boolean isInDiskCache = dataSource.getResult() != null && dataSource.getResult();
        LogUtil.d(TAG, "isInDiskCache - " + isInDiskCache);
        return isInDiskCache;
    }

    public static String getImageFileUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith("http")) {
            return url.replaceAll(" ", "%20");
        } else {
            return JandiConstantsForFlavors.SERVICE_FILE_URL + url.replaceAll(" ", "%20");
        }
    }

    public static String getThumbnailUrl(ResMessages.ThumbnailUrls extraInfo,
                                         Thumbnails thumbnails) {
        if (extraInfo == null) {
            return null;
        }

        String targetUrl = null;

        switch (thumbnails) {
            case SMALL:
                targetUrl = extraInfo.smallThumbnailUrl;
                break;
            case MEDIUM:
                targetUrl = extraInfo.mediumThumbnailUrl;
                break;
            case LARGE:
                targetUrl = extraInfo.largeThumbnailUrl;
                break;
            case THUMB:
            default:
                targetUrl = extraInfo.thumbnailUrl;
                break;
        }

        return getImageFileUrl(targetUrl);
    }

    public static String getThumbnailUrlOrOriginal(ResMessages.FileContent content,
                                                   Thumbnails thumbnails) {
        if (content == null) {
            return null;
        }

        String original = content.fileUrl;
        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        if (extraInfo == null) {
            return getImageFileUrl(original);
        }

        String targetUrl = null;
        switch (thumbnails) {
            case SMALL:
                targetUrl = extraInfo.smallThumbnailUrl;
                break;
            case MEDIUM:
                targetUrl = extraInfo.mediumThumbnailUrl;
                break;
            case LARGE:
                targetUrl = extraInfo.largeThumbnailUrl;
                break;
            case THUMB:
                targetUrl = extraInfo.thumbnailUrl;
                break;
            case ORIGINAL:
                targetUrl = original;
                break;
        }

        if (TextUtils.isEmpty(targetUrl)) {
            return getImageFileUrl(original);
        }

        return getImageFileUrl(targetUrl);
    }

    public static boolean hasImageUrl(ResMessages.FileContent fileContent) {
        boolean hasExtraSizeImageUrl = false;
        ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
        if (extraInfo != null) {
            hasExtraSizeImageUrl =
                    !isEmpty(extraInfo.smallThumbnailUrl)
                            || !isEmpty(extraInfo.mediumThumbnailUrl)
                            || !isEmpty(extraInfo.largeThumbnailUrl);
        }
        return !isEmpty(fileContent.fileUrl) || hasExtraSizeImageUrl;
    }

    public static boolean isEmpty(String url) {
        return TextUtils.isEmpty(url) || TextUtils.getTrimmedLength(url) <= 0;
    }

    public static String getOptimizedImageUrl(ResMessages.FileContent content) {
        String original = content.fileUrl;

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        String small = extraInfo != null ? extraInfo.smallThumbnailUrl : null;
        String medium = extraInfo != null ? extraInfo.mediumThumbnailUrl : null;
        String large = extraInfo != null ? extraInfo.largeThumbnailUrl : null;
        String extraImageUrl = getImageUrl(small, medium, large, original);
        Resources resources = JandiApplication.getContext().getResources();
        int dpi = resources.getDisplayMetrics().densityDpi;
        // XXHDPI 이상인 기기에서만 오리지널 파일을 로드
        if (dpi > DisplayMetrics.DENSITY_XHIGH) {
            return !TextUtils.isEmpty(original) ? getImageFileUrl(original) : getImageFileUrl(extraImageUrl);
        }

        return getImageFileUrl(extraImageUrl);
    }

    private static String getImageUrl(String small, String medium, String large, String original) {
        // 라지 사이즈부터 조회(640 x ~)
        if (!TextUtils.isEmpty(large)) {
            return large;
        }

        // 중간 사이즈 (360 x ~)
        if (!TextUtils.isEmpty(medium)) {
            return medium;
        }

        // 원본 파일
        if (!TextUtils.isEmpty(original)) {
            return original;
        }

        // 80x80 정사각형 이미지
        return small;
    }

    public static void loadProfileImage(SimpleDraweeView draweeView, String url, int placeHolder) {
        loadProfileImage(draweeView, Uri.parse(url), placeHolder);
    }

    public static void loadProfileImage(SimpleDraweeView draweeView, Uri uri, int placeHolderResId) {
        RoundingParams circleRoundingParams = getCircleRoundingParams(
                TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);
        ImageLoader.newBuilder()
                .placeHolder(placeHolderResId, ScalingUtils.ScaleType.FIT_CENTER)
                .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .roundingParams(circleRoundingParams)
                .backgroundColor(Color.BLACK)
                .load(uri)
                .into(draweeView);
    }

    public static void loadProfileImage(SimpleDraweeView draweeView, Uri uri, int placeHolderResId, int backgroundColor) {
        RoundingParams circleRoundingParams = getCircleRoundingParams(
                TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);
        ImageLoader.newBuilder()
                .placeHolder(placeHolderResId, ScalingUtils.ScaleType.CENTER_CROP)
                .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .roundingParams(circleRoundingParams)
                .backgroundColor(backgroundColor)
                .load(uri)
                .into(draweeView);
    }


    public static void loadProfileImageWithoutRounding(SimpleDraweeView draweeView, Uri uri, int placeHolderResId) {
        ImageLoader.newBuilder()
                .placeHolder(placeHolderResId, ScalingUtils.ScaleType.CENTER_CROP)
                .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .load(uri)
                .into(draweeView);
    }

    public static RoundingParams getCircleRoundingParams(int color, int width) {
        RoundingParams roundingParams = RoundingParams.asCircle();
        roundingParams.setBorder(color, width);
        return roundingParams;
    }

    public static void setResourceIconOrLoadImageForComment(final SimpleDraweeView draweeView,
                                                  final View vOutLine,
                                                  final String fileUrl,
                                                  final String thumbnailUrl,
                                                  final String serverUrl,
                                                  final String fileType) {

        if (vOutLine != null) {
            vOutLine.setVisibility(View.VISIBLE);
        }

        ImageLoader.Builder builder = ImageLoader.newBuilder()
                .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, fileType);

        boolean hasImageUrl = !TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(thumbnailUrl);
        if (!TextUtils.equals(fileType, "image") || !hasImageUrl) {
            if (vOutLine != null) {
                vOutLine.setVisibility(View.GONE);
            }

            builder.backgroundColor(Color.TRANSPARENT)
                    .load(mimeTypeIconImage).into(draweeView);
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            if (vOutLine != null) {
                vOutLine.setVisibility(View.GONE);
            }

            builder.backgroundColor(Color.TRANSPARENT)
                    .load(mimeTypeIconImage).into(draweeView);
        } else {
            if (TextUtils.isEmpty(thumbnailUrl)) {
                builder.actualScaleType(ScalingUtils.ScaleType.FIT_XY);
                builder.load(R.drawable.comment_no_img).into(draweeView);
                return;
            }

            builder.actualScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            builder.backgroundColor(draweeView.getResources().getColor(R.color.jandi_messages_image_view_bg));
            builder.placeHolder(
                    R.drawable.comment_img_preview, ScalingUtils.ScaleType.FIT_XY);
            builder.error(R.drawable.comment_no_img, ScalingUtils.ScaleType.FIT_XY);
            builder.load(Uri.parse(thumbnailUrl)).into(draweeView);
        }
    }

    public static void setResourceIconOrLoadImage(final SimpleDraweeView draweeView,
                                                  final View vOutLine,
                                                  final String fileUrl,
                                                  final String thumbnailUrl,
                                                  final String serverUrl,
                                                  final String fileType) {
        if (vOutLine != null) {
            vOutLine.setVisibility(View.GONE);
        }

        ImageLoader.Builder builder = ImageLoader.newBuilder()
                .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, fileType);

        boolean hasImageUrl = !TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(thumbnailUrl);
        if (!TextUtils.equals(fileType, "image") || !hasImageUrl) {
            builder.load(mimeTypeIconImage).into(draweeView);
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            builder.load(mimeTypeIconImage).into(draweeView);
        } else {
            if (TextUtils.isEmpty(thumbnailUrl)) {
                builder.actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                builder.load(R.drawable.file_icon_img).into(draweeView);
                return;
            }

            if (vOutLine != null) {
                vOutLine.setVisibility(View.VISIBLE);
            }

            builder.actualScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            builder.placeHolder(
                    R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.FIT_XY);
            builder.error(R.drawable.file_icon_img, ScalingUtils.ScaleType.FIT_CENTER);
            builder.controllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFailure(String id, Throwable throwable) {
                    if (vOutLine != null) {
                        vOutLine.setVisibility(View.GONE);
                    }
                }
            });

            builder.load(Uri.parse(thumbnailUrl)).into(draweeView);
        }
    }

    public static String getLocalFilePath(long messageId) {
        String localPath = UploadedFileInfoRepository.getRepository()
                .getUploadedFileInfo(messageId).getLocalPath();
        return new File(localPath).exists() ? localPath : "";
    }

    public static boolean isVerticalPhoto(int orientation) {
        return orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270;
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public enum Thumbnails {
        SMALL, MEDIUM, LARGE, THUMB, ORIGINAL
    }

}
