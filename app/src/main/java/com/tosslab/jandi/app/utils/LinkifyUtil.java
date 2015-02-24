package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;

import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
public class LinkifyUtil {

    public static final boolean addLinks(Context context, Spannable text, Pattern pattern) {

        Matcher m = pattern.matcher(text);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (allowed) {
                String url = m.group(0);

                URLSpan span = new URLSpan(url) {
                    @Override
                    public void onClick(View widget) {
                        InternalWebActivity_.intent(context)
                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .url(url)
                                .start();

                    }
                };

                text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return true;
    }

}
