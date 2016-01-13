package com.tosslab.jandi.app.utils.mimetype.placeholder;

import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class PlaceholderUtil {

    private static Map<MimeTypeUtil.PlaceholderType, PlaceholderSource> placeholderMap;
    private static Map<MimeTypeUtil.PlaceholderType, PlaceholderFilter> filterMapper;

    static {
        initFilterMapper();
        intPlaceholderMapper();

    }

    private static void initFilterMapper() {
        filterMapper = new HashMap<MimeTypeUtil.PlaceholderType, PlaceholderFilter>();

        filterMapper.put(MimeTypeUtil.PlaceholderType.Audio, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "audio"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Image, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "image"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Video, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "video"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Pdf, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "pdf"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Hwp, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "hwp"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.ZIP, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "zip"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Document, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "document"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.SpreadSheet, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "spreadsheet"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Presentation, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "s3") && TextUtils.equals(iconType, "presentation"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Google, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "google"));
        filterMapper.put(MimeTypeUtil.PlaceholderType.Dropbox, (serverUrl, iconType) -> TextUtils.equals(serverUrl, "dropbox"));
    }

    private static void intPlaceholderMapper() {
        placeholderMap = new HashMap<MimeTypeUtil.PlaceholderType, PlaceholderSource>();

        placeholderMap.put(MimeTypeUtil.PlaceholderType.Audio, () -> R.drawable.jandi_down_placeholder_audio);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Image, () -> R.drawable.jandi_down_placeholder_img);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Video, () -> R.drawable.jandi_down_placeholder_video);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Pdf, () -> R.drawable.jandi_down_placeholder_pdf);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Hwp, () -> R.drawable.jandi_down_placeholder_hwp);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Document, () -> R.drawable.jandi_down_placeholder_text);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.SpreadSheet, () -> R.drawable.jandi_down_placeholder_excel);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Presentation, () -> R.drawable.jandi_down_placeholder_ppt);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Etc, () -> R.drawable.jandi_down_placeholder_etc);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Google, () -> R.drawable.jandi_down_placeholder_google);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.Dropbox, () -> R.drawable.jandi_down_placeholder_dropbox);
        placeholderMap.put(MimeTypeUtil.PlaceholderType.ZIP, () -> R.drawable.jandi_down_placeholder_zip);
    }

    public static MimeTypeUtil.PlaceholderType getPlaceholderType(String serverUrl, String iconType) {

        String tempServerUrl;
        if (!TextUtils.isEmpty(serverUrl)) {
            tempServerUrl = serverUrl;
        } else {
            tempServerUrl = "s3";
        }

        String tempIconType;
        if (!TextUtils.isEmpty(iconType)) {
            tempIconType = iconType;
        } else {
            tempIconType = "etc";
        }

        for (MimeTypeUtil.PlaceholderType placeholderType : filterMapper.keySet()) {
            if (filterMapper.get(placeholderType).isFilter(tempServerUrl, tempIconType)) {
                return placeholderType;
            }
        }

        return MimeTypeUtil.PlaceholderType.Etc;
    }

    public static int getPlaceholderImage(MimeTypeUtil.PlaceholderType placeholderType) {
        if (placeholderMap.containsKey(placeholderType)) {
            return placeholderMap.get(placeholderType).getPlaceholdereIcon();
        } else {
            return placeholderMap.get(MimeTypeUtil.PlaceholderType.Etc).getPlaceholdereIcon();
        }
    }
}
