package com.tosslab.jandi.app.utils.mimetype.filter;

import android.text.TextUtils;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class GoogleSpreadSheetIconFilter implements IconFilter {
    private static final String INCLUDE_FILTER = "gspreadsheet";

    @Override
    public boolean isIconFilter(String iconType) {

        if (TextUtils.isEmpty(iconType)) {
            return false;
        }

        return IconFilterUtil.isFilter(iconType, INCLUDE_FILTER);
    }
}
