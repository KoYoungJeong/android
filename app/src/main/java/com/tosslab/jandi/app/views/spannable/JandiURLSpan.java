package com.tosslab.jandi.app.views.spannable;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.conference_call.ConferenceCallApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.GooroomeeRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqGooroomeeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeeOtp;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
        ApplicationUtil.startWebBrowser(context, url);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.MsgURL);
    }

}