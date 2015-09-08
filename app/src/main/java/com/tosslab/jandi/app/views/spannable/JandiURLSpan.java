package com.tosslab.jandi.app.views.spannable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

/**
 * Created by Steve SeongUg Jung on 15. 4. 21..
 */
public class JandiURLSpan extends UnderlineSpan implements ClickableSpannable{
    private final Context context;
    private final String url;
    private final int color;

    public JandiURLSpan(Context context, String url, int color) {
        this.context = context;
        this.url = url;
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }

    public void onClick() {
        InternalWebActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .url(url)
                .start();

        if (context instanceof Activity) {
            Activity activity = ((Activity) context);
            activity.overridePendingTransition(R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
        }
    }

    public String getUrl() {
        return url;
    }
}
