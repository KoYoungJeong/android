package com.tosslab.jandi.app.utils.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
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

    public static void loadProfileImage(ImageView imageView, String url, int placeHolder) {
        loadProfileImage(imageView, Uri.parse(url), placeHolder);
    }

    public static void loadProfileImage(ImageView imageView, Uri uri, int placeHolderResId) {
        ImageLoader.newInstance()
                .placeHolder(placeHolderResId, ImageView.ScaleType.FIT_CENTER)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .transformation(new JandiProfileTransform(imageView.getContext()))
                .uri(uri)
                .into(imageView);
    }

    public static void loadProfileImage(ImageView imageView,
                                        Uri uri, int placeHolderResId, int backgroundColor) {
        ImageLoader.newInstance()
                .placeHolder(placeHolderResId, ImageView.ScaleType.FIT_CENTER)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .transformation(new JandiProfileTransform(imageView.getContext(),
                        TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                        TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                        backgroundColor))
                .uri(uri)
                .into(imageView);
    }

    public static void setResourceIconOrLoadImage(final ImageView imageView,
                                                  final View vOutLine,
                                                  final String fileUrl,
                                                  final String thumbnailUrl,
                                                  final String serverUrl,
                                                  final String fileType) {
        if (vOutLine != null) {
            vOutLine.setVisibility(View.GONE);
        }

        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, fileType);

        boolean hasImageUrl = !TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(thumbnailUrl);
        if (!TextUtils.equals(fileType, "image") || !hasImageUrl) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(mimeTypeIconImage);
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(mimeTypeIconImage);
        } else {
            if (TextUtils.isEmpty(thumbnailUrl)) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageResource(R.drawable.file_icon_img);
                return;
            }

            ImageLoader loader = ImageLoader.newInstance()
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER);

            if (vOutLine != null) {
                vOutLine.setVisibility(View.VISIBLE);
            }

            loader.actualImageScaleType(ImageView.ScaleType.CENTER_CROP);
            loader.placeHolder(
                    R.drawable.comment_image_preview_download, ImageView.ScaleType.FIT_XY);
            loader.error(R.drawable.file_icon_img, ImageView.ScaleType.FIT_CENTER);
            loader.listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Uri model,
                                           Target<GlideDrawable> target,
                                           boolean isFirstResource) {
                    if (vOutLine != null) {
                        vOutLine.setVisibility(View.GONE);
                    }
                    return false;
                }
            });

            loader.uri(Uri.parse(thumbnailUrl)).into(imageView);
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
