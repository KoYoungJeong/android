package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class CategorizingAsFileType {
    public static final int TYPE_ALL    = 0;
    public static final int TYPE_IMAGES = 1;
    public static final int TYPE_PDFS   = 2;

    public static final String[] stringTitleList = {
            "All",
            "Images",
            "PDFs"
    };
    public static final int[] resourceList = {      // 위에랑 hash map으로 합칠까...
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

    public CategorizingAsFileType(int type) {
        this.type = type;
    }

    public String getServerQuery() {
        return stringQueryList[type];
    }
}
