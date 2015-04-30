package com.tosslab.jandi.app.utils.mimetype.filter;

import android.text.TextUtils;

import java.util.Arrays;

/**
 * Created by Steve SeongUg Jung on 15. 4. 28..
 */
public class PresentationIconFilter implements IconFilter {

    private static final String[] INCLUDE_FILTERS = {
            "presentation"
    };

    @Override
    public boolean isIconFilter(String iconType) {

        if (TextUtils.isEmpty(iconType)) {
            return false;
        }

        return IconFilterUtil.isFilter(iconType, Arrays.asList(INCLUDE_FILTERS));

    }

}
