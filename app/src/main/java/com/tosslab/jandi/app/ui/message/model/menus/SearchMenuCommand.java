package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.Context;
import android.view.MenuItem;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
@EBean
public class SearchMenuCommand implements MenuCommand {

    @RootContext
    Context context;
    private int entityId;

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        SearchActivity_.intent(context)
                .entityId(entityId)
                .start();

        if (EntityManager.getInstance().getEntityById(entityId).isUser()) {
            GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_Search);
        } else {
            GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_Search);
        }
    }
}
