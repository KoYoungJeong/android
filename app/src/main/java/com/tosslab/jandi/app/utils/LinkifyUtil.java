package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
public class LinkifyUtil {

    private static final Pattern WEB_URL = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "(?:" + Patterns.DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + Patterns.GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~\\|"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); // and finally, a word boundary or end of


    public static final boolean addLinks(Context context, Spannable text) {

        Matcher m = Patterns.WEB_URL.matcher(text);

        boolean hasLink = false;

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            hasLink = true;
            String url = m.group(0);

            URLSpan span = new URLSpan(url) {
                @Override
                public void onClick(View widget) {
                    InternalWebActivity_.intent(context)
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .url(url)
                            .start();

                    if (context instanceof Activity) {
                        Activity activity = ((Activity) context);
                        activity.overridePendingTransition(R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
                    }

                }
            };

            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return hasLink;
    }

}
