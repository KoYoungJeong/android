package com.tosslab.jandi.app.views.spannable;

import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.JandiApplication;

public class JandiEmailSpan extends UnderlineSpan implements ClickableSpannable {

    private String email;
    private final int color;

    public JandiEmailSpan(String email, int color) {
        super();
        this.email = email;
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }


    public void onClick() {
        Intent callIntent = new Intent(Intent.ACTION_SENDTO);
        callIntent.setData(Uri.parse("mailto:" + email));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        JandiApplication.getContext().startActivity(callIntent);
    }

    public String getEmail() {
        return email;
    }
}
