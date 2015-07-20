package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.Context;
import android.view.MenuItem;

import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity_;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public class TopicDetailCommand implements MenuCommand {

    private final Context context;
    private final int entityId;

    public TopicDetailCommand(Context context, int entityId) {
        this.context = context;
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        TopicDetailActivity_.intent(context)
                .entityId(entityId)
                .startForResult(TopicDetailActivity.REQUEST_DETAIL);
    }
}
