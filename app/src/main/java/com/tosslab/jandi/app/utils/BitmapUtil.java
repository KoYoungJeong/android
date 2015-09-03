package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class BitmapUtil {
    public static Bitmap getCircularBitmapImage(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
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

        Glide.get(JandiApplication.getContext()).clearMemory();
        Ion.getDefault(JandiApplication.getContext()).dump();

        while (true) {
            try {
                return BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            }
        }
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

    public static void loadImageByGlideOrIonWhenGif(ImageView imageView,
                                                    String url, int placeHolder, int error) {
        if (url.toLowerCase().endsWith("gif")) {
            Ion.with(imageView)
                    .fitCenter()
                    .placeholder(placeHolder)
                    .error(error)
                    .crossfade(true)
                    .load(url);
            return;
        }

        Glide.with(JandiApplication.getContext())
                .load(url)
                .placeholder(placeHolder)
                .error(error)
                .animate(view -> {
                    view.setAlpha(0.0f);
                    view.animate()
                            .alpha(1.0f)
                            .setDuration(300);
                })  // Avoid doesn't working 'fitCenter with crossfade'
                .fitCenter()
                .into(imageView);
    }

    public enum Thumbnails {
        SMALL, MEDIUM, LARGE, ORIGINAL
    }

}
