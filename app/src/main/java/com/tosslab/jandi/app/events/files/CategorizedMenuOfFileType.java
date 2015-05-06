package com.tosslab.jandi.app.events.files;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class CategorizedMenuOfFileType {
    public static final int[] stringTitleResourceList = {
            R.string.jandi_file_category_all,
            R.string.jandi_google_docs,
            R.string.jandi_file_category_document,
            R.string.jandi_file_category_presentation,
            R.string.jandi_file_category_spreadsheet,
            R.string.jandi_file_category_pdf,
            R.string.jandi_file_category_image,
            R.string.jandi_file_category_video,
            R.string.jandi_file_category_audio
    };
    public static final int[] drawableResourceList = {      // 위에랑 hash map으로 합칠까...
            R.drawable.jandi_fl_icon_etc,
            R.drawable.jandi_fl_icon_set_googledocs,
            R.drawable.jandi_fl_icon_txt,
            R.drawable.jandi_fl_icon_ppt,
            R.drawable.jandi_fl_icon_exel,
            R.drawable.jandi_fl_icon_pdf,
            R.drawable.jandi_fl_icon_img,
            R.drawable.jandi_fl_icon_video,
            R.drawable.jandi_fl_icon_audio
    };

    // 서버 통신용 쿼리
    public static final String[] stringQueryList = {
            "all",
            "googleDocs",
            "document",
            "presentation",
            "spreadsheet",
            "pdf",
            "image",
            "video",
            "audio"
    };

    public int type;

    public CategorizedMenuOfFileType(int type) {
        this.type = type;
    }

    public String getServerQuery() {
        return stringQueryList[type];
    }
}
