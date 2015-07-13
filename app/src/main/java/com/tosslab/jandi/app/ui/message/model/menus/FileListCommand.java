package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
class FileListCommand implements MenuCommand {

    private Activity activity;
    private ChattingInfomations chattingInfomations;

    FileListCommand(Activity activity, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.chattingInfomations = chattingInfomations;
    }

    @Override
    public void execute(MenuItem menuItem) {
        SearchActivity_.intent(activity)
                .isFromFiles(true)
                .entityId(chattingInfomations.entityId)
                .start();
    }
}
