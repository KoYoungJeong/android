package com.tosslab.jandi.app.utils.mimetype;

import com.tosslab.jandi.app.utils.mimetype.filter.IconFilterUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class MimeTypeUtil {

    public static int getMimeTypeImage(String serverUrl, String iconType) {

        FilterType mimeType = IconFilterUtil.getMimeType(iconType);
        SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);

        return SourceTypeUtil.getFileIcon(mimeType, sourceType);

    }

    public static int getMimeTypePlaceholderImage(String serverUrl, String iconType) {
        PlaceholderType placeholderType = PlaceholderUtil.getPlaceholderType(serverUrl, iconType);

        return PlaceholderUtil.getPlaceholderImage(placeholderType);
    }


    public enum FilterType {
        Audio, Image, Video, Pdf, Document, SpreadSheet, Presentation, Hwp, GoogleDocument, GoogleSpreadSheet, GooglePresentation, Etc
    }

    public enum SourceType {
        S3, Google, Dropbox
    }

    public enum PlaceholderType {
        Audio, Image, Video, Pdf, Document, SpreadSheet, Presentation, Hwp, Etc, Google, Dropbox
    }
}
