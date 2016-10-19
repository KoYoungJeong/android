package com.tosslab.jandi.app.utils.mimetype.placeholder;

import android.text.TextUtils;

import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class PlaceholderUtil {

    private static Map<MimeTypeUtil.PlaceholderType, PlaceholderFilter> filterMapper;

    static {
        initFilterMapper();
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

}
