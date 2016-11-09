package com.tosslab.jandi.app.utils.mimetype.filter;

import android.text.TextUtils;

import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class IconFilterUtil {

    private static Map<MimeTypeUtil.FilterType, IconFilter> mimeFilterMap;

    static {
        mimeFilterMap = new HashMap<MimeTypeUtil.FilterType, IconFilter>();

        mimeFilterMap.put(MimeTypeUtil.FilterType.Audio, iconType -> IconFilterUtil.isFilter(iconType, "audio"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Image, iconType -> IconFilterUtil.isFilter(iconType, "image"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Video, iconType -> IconFilterUtil.isFilter(iconType, "video"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Pdf, iconType -> IconFilterUtil.isFilter(iconType, "pdf"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Hwp, iconType -> IconFilterUtil.isFilter(iconType, "hwp"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Document, iconType -> IconFilterUtil.isFilter(iconType, "document"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.SpreadSheet, iconType -> IconFilterUtil.isFilter(iconType, "spreadsheet"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Presentation, iconType -> IconFilterUtil.isFilter(iconType, "presentation"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.GoogleDocument, iconType -> IconFilterUtil.isFilter(iconType, "gdocument"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.GoogleSpreadSheet, iconType -> IconFilterUtil.isFilter(iconType, "gspreadsheet"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.GooglePresentation, iconType -> IconFilterUtil.isFilter(iconType, "gpresentation"));
        mimeFilterMap.put(MimeTypeUtil.FilterType.Zip, iconType -> IconFilterUtil.isFilter(iconType, "zip"));
    }

    public static boolean isFilter(String iconType, List<String> filters) {
        for (String filter : filters) {
            if (TextUtils.equals(iconType, filter)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFilter(String iconType, String filter) {
        return TextUtils.equals(iconType, filter);
    }

    public static MimeTypeUtil.FilterType getMimeType(String filterTypeValue) {
        IconFilter iconFilter;
        MimeTypeUtil.FilterType filterType = null;
        for (MimeTypeUtil.FilterType type : mimeFilterMap.keySet()) {
            iconFilter = mimeFilterMap.get(type);

            if (iconFilter.isIconFilter(filterTypeValue)) {
                filterType = type;
                break;
            }
        }
        return filterType != null ? filterType : MimeTypeUtil.FilterType.Etc;
    }

}
