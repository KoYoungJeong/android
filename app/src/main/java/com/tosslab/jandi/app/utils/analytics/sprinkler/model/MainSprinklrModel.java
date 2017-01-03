package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.domain.event.Event;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

/**
 * Created by tee on 2016. 9. 13..
 */

public class MainSprinklrModel {

    private FutureTrack.Builder builder;

    protected MainSprinklrModel(Event event,
                                boolean isSetAccountId,
                                boolean isSetMemberId) {

        builder = new FutureTrack.Builder();
        builder.event(event);

        if (isSetAccountId) {
            setAccountId();
        }
        if (isSetMemberId) {
            setMemberId();
        }

    }

    protected void sendFail(int errorCode) {
        builder.property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode).build();

    }

    protected void setEvent(Event event) {
        builder.event(event);
    }

    private void setAccountId() {
        builder.accountId(AccountUtil.getAccountUUID(JandiApplication.getContext()));
    }

    private void setMemberId() {
        builder.memberId(AccountUtil.getMemberId(JandiApplication.getContext()));
    }

    protected void setProperty(String propertyKey, Object value) {
        builder.property(propertyKey, value);
    }

    protected void sendSuccess() {
        builder.property(PropertyKey.ResponseSuccess, true);
        send();
    }

    protected void send() {
        AnalyticsUtil.trackSprinkler(builder.build());
    }

}
