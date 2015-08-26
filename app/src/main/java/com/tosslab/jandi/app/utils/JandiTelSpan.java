package com.tosslab.jandi.app.utils;

import android.content.Intent;
import android.net.Uri;
import android.text.style.ForegroundColorSpan;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tonyjs on 15. 8. 26..
 */
public class JandiTelSpan extends ForegroundColorSpan {

    private String phoneNumber;

    public JandiTelSpan(String phoneNumber, int color) {
        super(color);
        this.phoneNumber = phoneNumber;
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
