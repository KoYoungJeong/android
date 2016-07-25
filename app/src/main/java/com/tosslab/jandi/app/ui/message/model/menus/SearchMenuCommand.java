package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.Context;
import android.view.MenuItem;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.search.main.view.FileSearchActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
public class SearchMenuCommand implements MenuCommand {

    private final Context context;
    private final long entityId;

    public SearchMenuCommand(Context context, long entityId) {
        this.context = context;
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        FileSearchActivity.start(context, entityId);

        if (TeamInfoLoader.getInstance().isUser(entityId)) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_Search);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_Search);
        }
    }
}
