package com.tosslab.jandi.app.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class ImageUtil {
    public static final String TAG = ImageUtil.class.getSimpleName();

    public static String getImageFileUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith("http")) {
            return url.replaceAll(" ", "%20");
        } else {
            return JandiConstantsForFlavors.getServiceFileUrl() + url.replaceAll(" ", "%20");
        }
    }

    public static String getThumbnailUrl(ResMessages.FileContent fileContent) {
        if (fileContent == null || fileContent.extraInfo == null) {
            return null;
        }

        String targetUrl;

        if (!TextUtils.isEmpty(fileContent.extraInfo.thumbnailUrl)) {
            if (!TextUtils.isEmpty(fileContent.extraInfo.largeThumbnailUrl)) {
                targetUrl = fileContent.extraInfo.largeThumbnailUrl;
            } else if (!TextUtils.isEmpty(fileContent.extraInfo.mediumThumbnailUrl)) {
                targetUrl = fileContent.extraInfo.mediumThumbnailUrl;
            } else if (!TextUtils.isEmpty(fileContent.extraInfo.smallThumbnailUrl)) {
                targetUrl = fileContent.extraInfo.smallThumbnailUrl;
            } else {
                targetUrl = fileContent.extraInfo.thumbnailUrl;
            }
        } else {
            targetUrl = fileContent.fileUrl;
        }

        return getImageFileUrl(targetUrl);
    }

    public static String getOriginalUrl(ResMessages.FileContent content) {
        if (content == null) {
            return null;
        }

        String original = content.fileUrl;
        return getImageFileUrl(original);

    }

    public static String getLargeProfileUrl(ResSearch.File content) {
        if (!TextUtils.isEmpty(content.getThumbnailUrl())) {
            if (!TextUtils.isEmpty(content.getLargeThumbnailUrl())) {
                return content.getLargeThumbnailUrl();
            } else if (!TextUtils.isEmpty(content.getMediumThumbnailUrl())) {
                return content.getMediumThumbnailUrl();
            } else if (!TextUtils.isEmpty(content.getSmallThumbnailUrl())) {
                return content.getSmallThumbnailUrl();
            }
            return content.getThumbnailUrl();
        } else {
            return content.getFileUrl();
        }
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

    public static void loadProfileImage(ImageView imageView, String url, int placeHolder) {
        loadProfileImage(imageView, Uri.parse(url), placeHolder);
    }

    public static void loadProfileImage(ImageView imageView, Uri uri, int placeHolderResId) {
        ImageLoader.newInstance()
                .placeHolder(placeHolderResId, ImageView.ScaleType.FIT_CENTER)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .transformation(new JandiProfileTransform(imageView.getContext(),
                        TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                        TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                        Color.WHITE))
                .uri(uri)
                .into(imageView);
    }

    public static void loadProfileImage(ImageView imageView,
                                        String url, int placeHolderResId, int backgroundColor) {
        ImageLoader.newInstance()
                .placeHolder(placeHolderResId, ImageView.ScaleType.FIT_CENTER)
                .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .transformation(new JandiProfileTransform(imageView.getContext(),
                        TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                        TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                        backgroundColor))
                .uri(Uri.parse(url))
                .into(imageView);
    }

    public static Bitmap getBitmap(Context context, String url) throws Exception {
        return ImageLoader.newInstance().uri(Uri.parse(url)).getBitmapRect(context);
    }

    public static void setResourceIconOrLoadImage(final ImageView imageView,
                                                  final View vOutLine,
                                                  final String fileUrl,
                                                  final String thumbnailUrl,
                                                  final String serverUrl,
                                                  final String fileType) {

        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, fileType, SourceTypeUtil.TYPE_A);

        boolean hasImageUrl = !TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(thumbnailUrl);

        if (!TextUtils.equals(fileType, "image") || !hasImageUrl) {
            setImageViewLayout(imageView, vOutLine, false);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ImageLoader.loadFromResources(imageView, mimeTypeIconImage);
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            setImageViewLayout(imageView, vOutLine, false);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ImageLoader.loadFromResources(imageView, mimeTypeIconImage);
        } else {
            setImageViewLayout(imageView, vOutLine, true);
            if (TextUtils.isEmpty(thumbnailUrl)) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                ImageLoader.loadFromResources(imageView, R.drawable.comment_no_img);
                return;
            }

            ImageLoader loader = ImageLoader.newInstance()
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP);
            loader.placeHolder(
                    R.drawable.comment_img_preview, ImageView.ScaleType.FIT_XY);
            loader.error(R.drawable.comment_no_img, ImageView.ScaleType.FIT_XY);
            loader.uri(Uri.parse(thumbnailUrl)).into(imageView);
        }
    }

    private static void setImageViewLayout(ImageView imageView, View vOutLine, boolean isNeedOutLine) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        if (!isNeedOutLine) {
            if (vOutLine != null) {
                vOutLine.setVisibility(View.GONE);
            }
            layoutParams.width = (int) UiUtils.getPixelFromDp(64);
            layoutParams.height = (int) UiUtils.getPixelFromDp(64);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.setMargins(0, 0, 0, 0);
        } else {
            if (vOutLine != null) {
                vOutLine.setVisibility(View.VISIBLE);
            }
            layoutParams.width = (int) UiUtils.getPixelFromDp(63);
            layoutParams.height = (int) UiUtils.getPixelFromDp(63);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.setMargins(
                    (int) UiUtils.getPixelFromDp((float) 0.5), (int) UiUtils.getPixelFromDp((float) 0.5), 0, 0);
        }
    }

    public static void setResourceIconOrLoadImageForComment(final ImageView imageView,
                                                            final View vOutLine,
                                                            final String fileUrl,
                                                            final String thumbnailUrl,
                                                            final String serverUrl,
                                                            final String fileType) {

        if (vOutLine != null) {
            vOutLine.setVisibility(View.GONE);
        }

        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, fileType, SourceTypeUtil.TYPE_C);

        boolean hasImageUrl = !TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(thumbnailUrl);
        if (!TextUtils.equals(fileType, "image") || !hasImageUrl) {
            imageView.setBackgroundColor(Color.TRANSPARENT);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ImageLoader.loadFromResources(imageView, mimeTypeIconImage);
            return;
        }

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);

        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            imageView.setBackgroundColor(Color.TRANSPARENT);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ImageLoader.loadFromResources(imageView, mimeTypeIconImage);
        } else {
            if (TextUtils.isEmpty(thumbnailUrl)) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(imageView, R.drawable.comment_no_img);
                return;
            }

            if (vOutLine != null) {
                vOutLine.setVisibility(View.VISIBLE);
            }

            ImageLoader loader = ImageLoader.newInstance();
            loader.actualImageScaleType(ImageView.ScaleType.CENTER_CROP);
            loader.backgroundColor(imageView.getResources().getColor(R.color.jandi_messages_image_view_bg));
            loader.placeHolder(
                    R.drawable.comment_img_preview, ImageView.ScaleType.CENTER_INSIDE);
            loader.error(R.drawable.comment_no_img, ImageView.ScaleType.CENTER_INSIDE);
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

    public static File convertProfileFile(File file) {
        int desired_width = 640;
        int desired_height = 640;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, desired_width, desired_height);
        options.inJustDecodeBounds = false;

        Bitmap smallerBm = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        FileOutputStream fOut;

        File smallPictureFile = null;

        try {
            String newPath = file.getPath().replace(file.getName(), "converted-" + file.getName());
            smallPictureFile = new File(newPath);
            fOut = new FileOutputStream(smallPictureFile);
            smallerBm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            smallerBm.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return smallPictureFile;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public enum Thumbnails {
        SMALL, MEDIUM, LARGE, THUMB, ORIGINAL
    }

}
