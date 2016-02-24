package com.tosslab.jandi.app.views.spannable;

import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

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

    public void onClick() {

        FormattedEntity entity = EntityManager.getInstance()
                .getEntityById(entityId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY
                && (entity.isUser() || EntityManager.getInstance().isJandiBot(entityId))) {
            EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.Mention));
        }

    }

}
