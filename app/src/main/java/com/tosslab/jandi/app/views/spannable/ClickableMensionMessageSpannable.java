package com.tosslab.jandi.app.views.spannable;

import android.content.Context;

import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 20..
 */
public class ClickableMensionMessageSpannable extends MentionMessageSpannable implements ClickableSpannable{

    private int entityId;

    public ClickableMensionMessageSpannable(Context context, String name, int entityId, float pxSize,
                                            int textColor, int backgroundColor) {
        super(context, name, pxSize, textColor, backgroundColor);
        this.entityId = entityId;
    }

    public void onClick() {

        FormattedEntity entity = EntityManager.getInstance()
                .getEntityById(entityId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY && entity.isUser()) {
            EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.Mention));
        }

    }

}
