package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.regex.Regex;
import com.tosslab.jandi.app.views.spannable.ClickableMensionMessageSpannable;

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

        boolean hasWebLink = addWebLinks(context, text);
        boolean hasPhoneLink = addPhoneLinks(context, text);

        return hasWebLink || hasPhoneLink;
    }

    public static boolean addWebLinks(Context context, Spannable text) {
        boolean hasLink = false;

        Matcher matcher = Regex.VALID_URL.matcher(text);
        int color = context.getResources().getColor(R.color.jandi_accent_color);
        while (matcher.find()) {

            if (matcher.group(Regex.VALID_URL_GROUP_PROTOCOL) == null) {
                // skip if protocol is not present and 'extractURLWithoutProtocol' is false
                // or URL is preceded by invalid character.
                if (Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN
                        .matcher(matcher.group(Regex.VALID_URL_GROUP_BEFORE)).matches()) {
                    continue;
                }
            }
            String url = matcher.group(Regex.VALID_URL_GROUP_URL);
            int start = matcher.start(Regex.VALID_URL_GROUP_URL);
            int end = matcher.end(Regex.VALID_URL_GROUP_URL);
            Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
            if (tco_matcher.find()) {
                // In the case of t.co URLs, don't allow additional path characters.
                url = tco_matcher.group();
                end = start + url.length();
            }

            hasLink = true;

            JandiURLSpan span = new JandiURLSpan(context, url, color);

            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return hasLink;
    }

    public static boolean addPhoneLinks(Context context, Spannable text) {
        boolean hasLink = false;

        Matcher matcher = Regex.VALID_PHONE_NUMBER.matcher(text);

        int color = context.getResources().getColor(R.color.jandi_accent_color);

        while (matcher.find()) {
            hasLink = true;

            String phoneNumber = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            System.out.println("phoneNumber(" + phoneNumber + ") start - " + start + " end - " + end);

            JandiTelSpan span = new JandiTelSpan(phoneNumber, color);
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return hasLink;
    }

    public static final void setOnLinkClick(TextView textView) {
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView widget = (TextView) v;
                CharSequence text = widget.getText();

                if (!(text instanceof Spanned)) {
                    return false;
                }

                Spanned buffer = ((Spanned) text);
                int action = event.getAction();

                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);

                    JandiURLSpan[] link = buffer.getSpans(off, off, JandiURLSpan.class);

                    if (link.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            link[0].onClick();
                        }

                        return true;
                    }

                    ClickableMensionMessageSpannable[] mention = buffer.getSpans(off, off,
                            ClickableMensionMessageSpannable.class);

                    if (mention.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            mention[0].onClick();
                        }

                        return true;
                    }

                    JandiTelSpan[] telSpans = buffer.getSpans(off, off, JandiTelSpan.class);
                    if (telSpans.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            telSpans[0].onClick();
                        }

                        return true;
                    }
                }

                return false;

            }
        });

    }
}
