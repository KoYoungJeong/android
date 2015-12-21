package com.tosslab.jandi.app.utils.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class ImageUtil {
    public static final String TAG = ImageUtil.class.getSimpleName();

    public static Bitmap getBlurBitmap(Bitmap bitmap, int radius) {
        Bitmap result;
        if (bitmap.getConfig() == null) {
            return bitmap;
        } else {
            result = bitmap.copy(bitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = result.getWidth();
        int h = result.getHeight();

        int[] pix = new int[w * h];
        result.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        result.setPixels(pix, 0, w, 0, 0, w, h);

        return result;
    }

    public static Bitmap getCircularBitmap(Bitmap source,
                                           float lineWidth, int lineColor, int bgColor) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float r = size / 2f;

        // Background
        Paint bgPaint = new Paint();
        bgPaint.setFlags(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(r, r, r - 1, bgPaint);

        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(
                squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setFlags(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        canvas.drawCircle(r, r, r, paint);

        Paint linePaint = new Paint();
        linePaint.setFlags(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(r, r, r, linePaint);

        squaredBitmap.recycle();
        return bitmap;
    }

    public static String getFileUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith("http")) {
            return url.replaceAll(" ", "%20");
        } else {
            return JandiConstantsForFlavors.SERVICE_FILE_URL + url.replaceAll(" ", "%20");
        }
    }

    public static Bitmap getOptimizedBitmap(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        DisplayMetrics displayMetrics = JandiApplication.getContext()
                .getResources().getDisplayMetrics();

        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;

        float widthRatio = bitmapWidth / deviceWidth;
        float heightRatio = bitmapHeight / deviceHeight;

        int maxRatio = (int) Math.max(widthRatio, heightRatio);

        int samplingSize;

        if (maxRatio <= 1) {
            samplingSize = 1;
        } else {
            boolean find = false;
            samplingSize = 1;
            while (!find) {
                samplingSize *= 2;

                if (samplingSize >= maxRatio) {
                    find = true;
                }
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = samplingSize;

//        Glide.get(JandiApplication.getContext()).clearMemory();
//        Ion.getDefault(JandiApplication.getContext()).dump();

        while (true) {
            try {
                return BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            }
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

        return getFileUrl(targetUrl);
    }

    public static String getThumbnailUrlOrOriginal(ResMessages.FileContent content,
                                                   Thumbnails thumbnails) {
        if (content == null) {
            return null;
        }

        String original = content.fileUrl;
        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        if (extraInfo == null) {
            return getFileUrl(original);
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
            return getFileUrl(original);
        }

        return getFileUrl(targetUrl);
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
            return !TextUtils.isEmpty(original) ? getFileUrl(original) : getFileUrl(extraImageUrl);
        }

        return getFileUrl(extraImageUrl);
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

    public static void loadCircleImageByFresco(SimpleDraweeView draweeView, Uri uri,
                                               int placeHolderResId) {
        GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
        RoundingParams roundingParams = hierarchy.getRoundingParams() == null
                ? new RoundingParams() : hierarchy.getRoundingParams();
        roundingParams.setRoundAsCircle(true);
        roundingParams.setBorder(
                TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);
        hierarchy.setRoundingParams(roundingParams);

        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        Resources resources = draweeView.getResources();
        Drawable placeHolder = resources.getDrawable(placeHolderResId);
        hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.CENTER_CROP);

        draweeView.setHierarchy(hierarchy);

        ImageDecodeOptions imageDecodeOptions = ImageDecodeOptions.newBuilder()
                .setDecodePreviewFrame(true)
                .setBackgroundColor(Color.BLACK)
                .build();

        ViewGroup.LayoutParams layoutParams = draweeView.getLayoutParams();
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setImageDecodeOptions(imageDecodeOptions)
                .setResizeOptions(new ResizeOptions(layoutParams.width, layoutParams.height))
                .setAutoRotateEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(false)
                .setImageRequest(imageRequest)
                .setOldController(draweeView.getController())
                .build();

        draweeView.setController(controller);
    }

    public static void loadCircleImageByFresco(SimpleDraweeView draweeView, String url,
                                               int placeHolder) {
        loadCircleImageByFresco(draweeView, Uri.parse(url), placeHolder);
    }

    public static void loadDrawable(Uri uri, final OnResourceReadyCallback onResourceReadyCallback) {
        loadDrawable(uri, null, false, onResourceReadyCallback);
    }

    public static void loadDrawable(Uri uri, boolean executeIntoCallerThread,
                                    final OnResourceReadyCallback onResourceReadyCallback) {
        loadDrawable(uri, null, executeIntoCallerThread, onResourceReadyCallback);
    }

    public static void loadDrawable(Uri uri,
                                    ResizeOptions resizeOptions,
                                    final OnResourceReadyCallback onResourceReadyCallback) {
        loadDrawable(uri, resizeOptions, false, onResourceReadyCallback);
    }

    public static void loadDrawable(Uri uri,
                                    ResizeOptions resizeOptions,
                                    boolean executeIntoCallerThread,
                                    final OnResourceReadyCallback onResourceReadyCallback) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                .setResizeOptions(resizeOptions)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, JandiApplication.getContext());

        ExecutorService executorService = executeIntoCallerThread
                ? CallerThreadExecutor.getInstance()
                : UiThreadExecutorService.getInstance();

        dataSource.subscribe(new BitmapDataSubscriber(onResourceReadyCallback), executorService);
    }

    public static String getLocalFilePath(int messageId) {
        String localPath = UploadedFileInfoRepository.getRepository()
                .getUploadedFileInfo(messageId).getLocalPath();
        return new File(localPath).exists() ? localPath : "";
    }

    public static int getMaximumBitmapSize() {
        int minimum = 512;

        DisplayMetrics displayMetrics =
                JandiApplication.getContext().getResources().getDisplayMetrics();

        return (int) (minimum * Math.pow(2, displayMetrics.density));
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

    public static class BitmapDataSubscriber
            extends BaseDataSubscriber<CloseableReference<CloseableImage>> {

        private OnResourceReadyCallback onResourceReadyCallback;

        public BitmapDataSubscriber(OnResourceReadyCallback onResourceReadyCallback) {
            this.onResourceReadyCallback = onResourceReadyCallback;
        }

        @Override
        public void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            if (!dataSource.isFinished() || onResourceReadyCallback == null) {
                return;
            }
            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null) {
                CloseableImage closeableImage = imageReference.get();
                Drawable drawable = getDrawable(closeableImage);
                if (drawable != null) {
                    onResourceReadyCallback.onReady(drawable, imageReference);
                } else {
                    onResourceReadyCallback.onFail(new NullPointerException("Drawable is empty."));
                    CloseableReference.closeSafely(imageReference);
                }
            } else {
                onResourceReadyCallback.onFail(new NullPointerException("ImageReference is empty."));
            }
        }

        private Drawable getDrawable(CloseableImage closeableImage) {
            if (closeableImage instanceof CloseableBitmap) {
                return getBitmapDrawable((CloseableBitmap) closeableImage);
            } else if (closeableImage instanceof CloseableAnimatedImage) {
                return getAnimatedDrawable((CloseableAnimatedImage) closeableImage);
            }
            return null;
        }

        private Drawable getAnimatedDrawable(CloseableAnimatedImage animatedImage) {
            LogUtil.i(TAG, "animatableImage loaded");
            AnimatedDrawableFactory animatedDrawableFactory =
                    Fresco.getImagePipelineFactory().getAnimatedDrawableFactory();
            return animatedDrawableFactory.create(animatedImage.getImageResult());
        }

        private Drawable getBitmapDrawable(CloseableBitmap closeableBitmap) {
            Bitmap underlyingBitmap = closeableBitmap.getUnderlyingBitmap();
            if (isValidateBitmap(underlyingBitmap)) {
                return new BitmapDrawable(
                        JandiApplication.getContext().getResources(), underlyingBitmap);
            }
            return null;
        }

        private boolean isValidateBitmap(Bitmap bitmap) {
            return bitmap != null && !bitmap.isRecycled();
        }

        @Override
        public void onFailureImpl(DataSource dataSource) {
            // handle failure
            if (onResourceReadyCallback != null) {
                onResourceReadyCallback.onFail(dataSource.getFailureCause());
            }
        }

        @Override
        public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
            if (!dataSource.isFinished() && onResourceReadyCallback != null) {
                onResourceReadyCallback.onProgressUpdate(dataSource.getProgress());
            }
        }
    }

}