package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public class TopicDetailCommand implements MenuCommand {

    private final Fragment fragment;
    private final long teamId;
    private final long entityId;

    public TopicDetailCommand(Fragment fragment, long teamId, long entityId) {
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

        if (TeamInfoLoader.getInstance().isUser(entityId)) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_Decription);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_Decription);
        }
    }
}
