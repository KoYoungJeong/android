package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity_;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public class TopicDetailCommand implements MenuCommand {

    private final Fragment fragment;
    private final int entityId;

    public TopicDetailCommand(Fragment fragment, int entityId) {
        this.fragment = fragment;
        this.entityId = entityId;
    }

    @Override
    public void execute(MenuItem menuItem) {
        TopicDetailActivity_.intent(fragment)
                .entityId(entityId)
                .startForResult(TopicDetailActivity.REQUEST_DETAIL);
    }
}
