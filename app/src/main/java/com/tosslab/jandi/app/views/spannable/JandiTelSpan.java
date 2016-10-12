package com.tosslab.jandi.app.views.spannable;

import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

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

        try {
            JandiApplication.getContext().startActivity(callIntent);
        } catch (Exception e) {
            ColoredToast.show(R.string.jandi_err_unexpected);
            e.printStackTrace();
        }

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.MsgCellPhone);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
