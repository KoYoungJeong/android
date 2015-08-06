package com.tosslab.jandi.app.views.spannable;

import android.content.Context;

import com.tosslab.jandi.app.events.RequestUserInfoEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 20..
 */
public class ClickableMensionMessageSpannable extends MentionMessageSpannable {

    public ClickableMensionMessageSpannable(Context context, String name, int entityId, float pxSize) {
        super(context, name, entityId, pxSize);
    }

    public ClickableMensionMessageSpannable(Context context, String name, int entityId, float pxSize,
                                            int textColor, int backgroundColor) {
        super(context, name, entityId, pxSize, textColor, backgroundColor);
    }

    public void onClick() {

        EventBus.getDefault().post(new RequestUserInfoEvent(entityId));

    }

}
