package com.tosslab.jandi.app.views.spannable;

import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 20..
 */
public class ClickableMentionMessageSpannable extends MentionMessageSpannable implements ClickableSpannable {

    private long entityId;

    public ClickableMentionMessageSpannable(String entityName, long entityId, float textSize,
                                            int textColor, int backgroundColor) {
        super(entityName, textSize, textColor, backgroundColor);
        this.entityId = entityId;
    }

    @Override
    public void onClick() {

        boolean user = TeamInfoLoader.getInstance().isUser(entityId);

        if ((user)) {
            EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.Mention));
        }

    }

}
