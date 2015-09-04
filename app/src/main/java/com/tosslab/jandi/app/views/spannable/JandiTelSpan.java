package com.tosslab.jandi.app.views.spannable;

import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tonyjs on 15. 8. 26..
 */
public class JandiTelSpan extends UnderlineSpan implements ClickableSpannable {

    private String phoneNumber;
    private final int color;

    public JandiTelSpan(String phoneNumber, int color) {
        super();
        this.phoneNumber = phoneNumber;
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
    }

    public void onClick() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        JandiApplication.getContext().startActivity(callIntent);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}