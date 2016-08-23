package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.Context;
import android.view.MenuItem;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
public class SearchMenuCommand implements MenuCommand {

    private final Context context;
    private final long entityId;
    private final long memberId;

    public SearchMenuCommand(Context context, long entityId, long memberId) {
        this.context = context;
        this.entityId = entityId;
        this.memberId = memberId;
    }

    @Override
    public void execute(MenuItem menuItem) {

        context.startActivity(Henson.with(context)
                .gotoSearchActivity()
                .selectedRoomId(entityId)
                .selectedOneToOneRoomMemberId(memberId)
                .build());

        if (TeamInfoLoader.getInstance().isUser(memberId)) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Message, AnalyticsValue.Action.Message_Search);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.TopicChat_Search);
        }
    }
}
