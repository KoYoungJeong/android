package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.file.FileListActivity_;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;

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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FileListActivity_.intent(activity)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .entityId(chattingInfomations.entityId)
                        .entityName(chattingInfomations.entityName)
                        .start();
                activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        }, 250);
    }
}
