package com.tosslab.jandi.app.utils.mimetype.source;

import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class SourceTypeUtil {

    public static Map<MimeTypeUtil.FilterType, Map<MimeTypeUtil.SourceType, Source>> resourceMapper;

    static {
        resourceMapper = new HashMap<MimeTypeUtil.FilterType, Map<MimeTypeUtil.SourceType, Source>>();

        HashMap<MimeTypeUtil.SourceType, Source> value;
        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_audio_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_audio_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_audio_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Audio, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_img_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_img_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_img_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Image, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_video_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_video_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_video_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Video, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_pdf_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_pdf_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_pdf_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Pdf, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_hwp_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_hwp_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_hwp_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Hwp, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_text_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_text_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_text_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Document, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_excel_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_excel_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_excel_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.SpreadSheet, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_ppt_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_ppt_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_ppt_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Presentation, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_zip_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_zip_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_zip_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Zip, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.file_icon_etc_192);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.file_icon_etc_dropbox_192);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_etc_google_192);
        resourceMapper.put(MimeTypeUtil.FilterType.Etc, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_google_docs_192);
        resourceMapper.put(MimeTypeUtil.FilterType.GoogleDocument, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_google_ppt_192);
        resourceMapper.put(MimeTypeUtil.FilterType.GooglePresentation, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.file_icon_google_spreadsheet_192);
        resourceMapper.put(MimeTypeUtil.FilterType.GoogleSpreadSheet, value);

    }

    public static MimeTypeUtil.SourceType getSourceType(String sourceUrl) {
        if (TextUtils.isEmpty(sourceUrl)) {
            return MimeTypeUtil.SourceType.S3;
        }

        for (MimeTypeUtil.SourceType sourceType : MimeTypeUtil.SourceType.values()) {
            if (!TextUtils.isEmpty(sourceUrl)
                    && TextUtils.equals(sourceType.name().toLowerCase(), sourceUrl.toLowerCase())) {
                return sourceType;
            }
        }

        return MimeTypeUtil.SourceType.S3;

    }


    public static int getFileIcon(MimeTypeUtil.FilterType mimeType, MimeTypeUtil.SourceType sourceType) {

        if (resourceMapper.containsKey(mimeType)) {
            Map<MimeTypeUtil.SourceType, Source> sourceTypeSourceMap = resourceMapper.get(mimeType);
            if (sourceTypeSourceMap.containsKey(sourceType)) {
                return sourceTypeSourceMap.get(sourceType).getSourceIcon();
            } else {
                return sourceTypeSourceMap.get(MimeTypeUtil.SourceType.S3).getSourceIcon();
            }
        } else {
            Map<MimeTypeUtil.SourceType, Source> sourceTypeSourceMap = resourceMapper.get(MimeTypeUtil.FilterType.Etc);
            if (sourceTypeSourceMap.containsKey(sourceType)) {
                return sourceTypeSourceMap.get(sourceType).getSourceIcon();
            } else {
                return sourceTypeSourceMap.get(MimeTypeUtil.SourceType.S3).getSourceIcon();
            }
        }
    }
}
