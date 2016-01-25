package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;

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

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }

    public void onClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            ColoredToast.show(R.string.jandi_err_unexpected);
        }
    }

    public String getUrl() {
        return url;
    }
}
