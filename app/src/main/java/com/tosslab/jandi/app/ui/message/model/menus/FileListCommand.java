package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.search.file.view.FileSearchActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
class FileListCommand implements MenuCommand {

    private final Activity activity;
    private final long entityId;

    FileListCommand(Activity activity, long entityId) {
        this.activity = activity;
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        FileSearchActivity.start(activity, entityId);

        if (TeamInfoLoader.getInstance().isUser(entityId)) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_File);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_File);
        }
    }
}
