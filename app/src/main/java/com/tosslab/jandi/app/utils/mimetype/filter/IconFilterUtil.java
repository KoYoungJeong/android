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

    private static final IconFilter ETC_ICON_FILTER = new EtcIconFilter();
    private static Map<MimeTypeUtil.FilterType, IconFilter> mimeFilterMap;

    static {
        mimeFilterMap = new HashMap<MimeTypeUtil.FilterType, IconFilter>();

        mimeFilterMap.put(MimeTypeUtil.FilterType.Audio, new AudioIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Image, new ImageIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Video, new VideoIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Pdf, new PdfIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Hwp, new HwpIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Document, new DocumentIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.SpreadSheet, new SpreadSheetIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.Presentation, new PresentationIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.GoogleDocument, new GoogleDocumentIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.GoogleSpreadSheet, new GoogleSpreadSheetIconFilter());
        mimeFilterMap.put(MimeTypeUtil.FilterType.GooglePresentation, new GooglePresentationIconFilter());

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
