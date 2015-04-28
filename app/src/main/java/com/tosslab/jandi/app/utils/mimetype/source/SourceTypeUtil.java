package com.tosslab.jandi.app.utils.mimetype.source;

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
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_audio);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_audio_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_audio_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Audio, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_img);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_img_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_img_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Image, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_video);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_video_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_video_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Video, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_pdf);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_pdf_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_pdf_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Pdf, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_hwp);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_hwp_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_hwp_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Hwp, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_txt);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_txt_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_txt_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Document, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_exel);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_exel_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_exel_google);
        resourceMapper.put(MimeTypeUtil.FilterType.SpreadSheet, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_ppt);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_ppt_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_ppt_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Presentation, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.S3, () -> R.drawable.jandi_fl_icon_etc);
        value.put(MimeTypeUtil.SourceType.Dropbox, () -> R.drawable.jandi_fl_icon_etc_dropbox);
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_etc_google);
        resourceMapper.put(MimeTypeUtil.FilterType.Etc, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_set_docs);
        resourceMapper.put(MimeTypeUtil.FilterType.GoogleDocument, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_set_pr);
        resourceMapper.put(MimeTypeUtil.FilterType.GoogleSpreadSheet, value);

        value = new HashMap<MimeTypeUtil.SourceType, Source>();
        value.put(MimeTypeUtil.SourceType.Google, () -> R.drawable.jandi_fl_icon_set_ss);
        resourceMapper.put(MimeTypeUtil.FilterType.GoogleSpreadSheet, value);

    }

    public static MimeTypeUtil.SourceType getSourceType(String sourceUrl) {

        MimeTypeUtil.SourceType sourceType = MimeTypeUtil.SourceType.valueOf(sourceUrl);

        return sourceType != null ? sourceType : MimeTypeUtil.SourceType.S3;

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
