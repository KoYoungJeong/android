package com.tosslab.jandi.app.views.spannable;

import android.text.style.ClickableSpan;
import android.view.View;

import com.tosslab.jandi.app.events.RequestUserInfoEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 4. 23..
 */
public class ProfileSpannable extends ClickableSpan {

    private final int entityId;

    public ProfileSpannable(int entityId) {
        this.entityId = entityId;
    }


    @Override
    public void onClick(View widget) {
        EventBus.getDefault().post(new RequestUserInfoEvent(entityId, RequestUserInfoEvent.From.SystemMessage));
    }
}
