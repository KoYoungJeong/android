package com.tosslab.jandi.app.utils.mimetype.filter;

import android.text.TextUtils;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class GoogleDocumentIconFilter implements IconFilter {

    private static final String INCLUDE_FILTER = "gdocument";

    @Override
    public boolean isIconFilter(String iconType) {

        if (TextUtils.isEmpty(iconType)) {
            return false;
        }

        return IconFilterUtil.isFilter(iconType, INCLUDE_FILTER);
    }
}
