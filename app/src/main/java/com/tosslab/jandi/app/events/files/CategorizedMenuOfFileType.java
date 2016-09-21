package com.tosslab.jandi.app.events.files;

import android.text.TextUtils;

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
            R.string.jandi_file_category_audio,
            R.string.jandi_file_category_zip
    };
    public static final int[] drawableResourceList = {      // 위에랑 hash map으로 합칠까...
            R.drawable.icon_search_all_files,
            R.drawable.file_icon_set_googledocs,
            R.drawable.file_icon_txt,
            R.drawable.file_icon_ppt,
            R.drawable.file_icon_exel,
            R.drawable.file_icon_pdf,
            R.drawable.file_icon_img,
            R.drawable.file_icon_video,
            R.drawable.file_icon_audio,
            R.drawable.file_icon_zip
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
            "audio",
            "archive"
    };

    public int type;

    public CategorizedMenuOfFileType(int type) {
        this.type = type;
    }

    public static int findTitleResIdFromQuery(String query) {
        for (int idx = 0, size = stringQueryList.length; idx < size; idx++) {
            if (TextUtils.equals(query, stringQueryList[idx])) {
                return stringTitleResourceList[idx];
            }
        }
        return -1;
    }

    public String getServerQuery() {
        return stringQueryList[type];
    }
}
