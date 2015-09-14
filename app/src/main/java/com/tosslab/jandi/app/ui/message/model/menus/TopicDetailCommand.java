package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public class TopicDetailCommand implements MenuCommand {

    private final Fragment fragment;
    private final int teamId;
    private final int entityId;

    public TopicDetailCommand(Fragment fragment, int teamId, int entityId) {
        this.fragment = fragment;
        this.teamId = teamId;
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        TopicDetailActivity_.intent(fragment)
                .entityId(entityId)
                .teamId(teamId)
                .startForResult(TopicDetailActivity.REQUEST_DETAIL);

        if (EntityManager.getInstance().getEntityById(entityId).isUser()) {
            GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_Decription);
        } else {
            GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_Decription);
        }
    }
}
