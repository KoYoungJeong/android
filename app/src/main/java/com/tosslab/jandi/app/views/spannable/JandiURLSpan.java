package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

/**
 * Created by Steve SeongUg Jung on 15. 4. 21..
 */
public class JandiURLSpan extends UnderlineSpan implements ClickableSpannable {
    private final Context context;
    private final String url;
    private final int color;
    public JandiURLSpan(Context context, String url, int color) {
        this.context = context;
        this.url = url;
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }

    @Override
    public void onClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getAvailableUrl(url)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            InternalWebActivity_.intent(context)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .url(url)
                    .start();
        }
    }

    String getAvailableUrl(String url) {

        int protocolIndex = url.indexOf("://");
        if (protocolIndex <= 0) {
            return "http://" + url;
        } else {
            String protocol = url.substring(0, protocolIndex);
            String uri = url.substring(protocolIndex);

            StringBuilder builder = new StringBuilder();
            builder.append(protocol.toLowerCase())
                    .append(uri);

            return builder.toString();
        }

    }

}
