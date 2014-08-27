package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class CategorizedMenuOfFileType {
    public static final int[] stringTitleResourceList = {
            R.string.jandi_file_category_all,
            R.string.jandi_file_category_image,
            R.string.jandi_file_category_pdf
    };
    public static final int[] drawableResourceList = {      // 위에랑 hash map으로 합칠까...
            R.drawable.jandi_fl_icon_etc,
            R.drawable.jandi_fl_icon_img,
            R.drawable.jandi_fl_icon_pdf
    };

    // 서버 통신용 쿼리
    public static final String[] stringQueryList = {
            "all",
            "image",
            "pdf"
    };

    public int type;

    public CategorizedMenuOfFileType(int type) {
        this.type = type;
    }

    public String getServerQuery() {
        return stringQueryList[type];
    }
}
