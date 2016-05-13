package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.regex.Regex;
import com.tosslab.jandi.app.utils.regex.TelRegex;
import com.tosslab.jandi.app.views.spannable.ClickableMentionMessageSpannable;
import com.tosslab.jandi.app.views.spannable.ClickableSpannable;
import com.tosslab.jandi.app.views.spannable.JandiEmailSpan;
import com.tosslab.jandi.app.views.spannable.JandiTelSpan;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
public class LinkifyUtil {

    static final String REG_EX_EMAIL = "[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,4})";

    private static final Class[] clickableSpannables = {
            JandiURLSpan.class,
            JandiTelSpan.class,
            JandiEmailSpan.class,
            ClickableMentionMessageSpannable.class
    };

    private LinkifyUtil() {
    }

    public static boolean addLinks(Context context, Spannable text) {

        boolean hasWebLink = addWebLinks(context, text);
        boolean hasPhoneLink = addPhoneLinks(context, text);
        boolean hasEmailLink = addEmailLinks(context, text);

        return hasWebLink || hasPhoneLink || hasEmailLink;
    }

    public static boolean addWebLinks(Context context, Spannable text) {
        boolean hasLink = false;

        Matcher matcher = Regex.VALID_URL_PATTERN.matcher(text);
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
            String url;
            int start = matcher.start(Regex.VALID_URL_GROUP_URL);
            int end;
            Matcher spaceMatcher = Pattern.compile("( |\n)").matcher(text);
            if (spaceMatcher.find(start + 1)) {
                end = spaceMatcher.start();
            } else {
                end = text.length();
            }

            url = text.subSequence(start, end).toString();

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

        Matcher matcher = TelRegex.VALID_TEL_PATTERN.matcher(text);

        int color = context.getResources().getColor(R.color.jandi_accent_color);

        while (matcher.find()) {
            hasLink = true;

            String phoneNumber = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            JandiTelSpan span = new JandiTelSpan(phoneNumber, color);
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return hasLink;
    }

    public static boolean addEmailLinks(Context context, Spannable spannable) {
        Matcher matcher = Pattern.compile(REG_EX_EMAIL).matcher(spannable);

        boolean hasLink = false;

        int color = context.getResources().getColor(R.color.jandi_accent_color);

        while (matcher.find()) {
            hasLink = true;

            String email = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            JandiEmailSpan jandiEmailSpan = new JandiEmailSpan(email, color);
            spannable.setSpan(jandiEmailSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return hasLink;
    }

    public static void setOnLinkClick(TextView textView) {
        textView.setOnTouchListener(new View.OnTouchListener() {

            private boolean isInLongClickProcess = false;
            private ClickableSpannable clickableSpannable;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView widget = (TextView) v;
                CharSequence text = widget.getText();

                if (!(text instanceof Spanned)) {
                    return false;
                }

                Spanned buffer = ((Spanned) text);
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);

                    // Text 영역을 벗어난 곳을 터치
                    if (off >= buffer.length()) {
                        return false;
                    }

                    ClickableSpannable[] clickableSpan =
                            buffer.getSpans(off, off, ClickableSpannable.class);

                    if (clickableSpan != null && clickableSpan.length > 0) {
                        for (int spanIdx = clickableSpan.length - 1; spanIdx >= 0; spanIdx--) {
                            clickableSpannable = clickableSpan[spanIdx];
                            if (clickableSpan[spanIdx] instanceof ClickableMentionMessageSpannable) {
                                return true;
                            }
                        }
                        return true;
                    }
                    return false;
                }

                if (action == MotionEvent.ACTION_MOVE) {
                    long onTouchTime = event.getEventTime() - event.getDownTime();
                    if (onTouchTime >= ViewConfiguration.getLongPressTimeout()) {
                        if (isInLongClickProcess) {
                            return false;
                        }
                        isInLongClickProcess = true;
                        findParentAndPerformLongClickIfNeed(textView.getParent());
                        return false;
                    }
                }

                if (action == MotionEvent.ACTION_UP) {
                    if (isInLongClickProcess) {
                        isInLongClickProcess = false;
                        if (clickableSpannable != null) {
                            clickableSpannable = null;
                        }
                        return false;
                    }
                    if (clickableSpannable != null) {
                        clickableSpannable.onClick();
                        clickableSpannable = null;
                        return true;
                    }
                }

                if (action == MotionEvent.ACTION_CANCEL) {
                    if (isInLongClickProcess) {
                        isInLongClickProcess = false;
                        if (clickableSpannable != null) {
                            clickableSpannable = null;
                        }
                        return false;
                    }
                    if (clickableSpannable != null) {
                        clickableSpannable = null;
                    }
                }

                return false;
            }

            private void findParentAndPerformLongClickIfNeed(ViewParent parent) {
                if (parent == null) {
                    return;
                }

                if (!(parent instanceof ViewGroup)) {
                    return;
                }

                ViewGroup viewGroup = (ViewGroup) parent;
                if (viewGroup.isLongClickable()) {
                    viewGroup.setPressed(true);
                    viewGroup.performLongClick();
                    viewGroup.setPressed(false);
                    return;
                }

                findParentAndPerformLongClickIfNeed(viewGroup.getParent());
            }
        });

    }

}
