package com.tosslab.jandi.app.network;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.JandiConstants;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by justinygchoi on 2014. 8. 22..
 */
public class AnalyticsClient {
    private MixpanelAPI mixpanel;

    public AnalyticsClient(Context context) {
        mixpanel = MixpanelAPI.getInstance(context, JandiConstants.MIXPANEL_TOKEN);
    }

    public void trackForSingIn(String distictId) {
        mixpanel.identify(distictId);
        mixpanel.track("Sign In", null);
    }

    public void flush() {
        mixpanel.flush();
    }
}
