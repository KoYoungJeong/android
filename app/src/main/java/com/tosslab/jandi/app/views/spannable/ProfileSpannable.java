package com.tosslab.jandi.app.views.spannable;

import android.text.style.ClickableSpan;
import android.view.View;

import com.tosslab.jandi.app.events.profile.ShowProfileEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 4. 23..
 */
public class ProfileSpannable extends ClickableSpan {

    private final long entityId;

    public ProfileSpannable(long entityId) {
        this.entityId = entityId;
    }


    @Override
    public void onClick(View widget) {
        EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.SystemMessage));
    }
}
