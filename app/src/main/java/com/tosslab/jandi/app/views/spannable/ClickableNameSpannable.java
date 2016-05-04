package com.tosslab.jandi.app.views.spannable;

import com.tosslab.jandi.app.events.profile.ShowProfileEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class ClickableNameSpannable extends NameSpannable implements ClickableSpannable {
    private final long entityId;

    public ClickableNameSpannable(long entityId, int textSize, int textColor) {
        super(textSize, textColor);
        this.entityId = entityId;
    }

    @Override
    public void onClick() {
        EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.SystemMessage));
    }
}
