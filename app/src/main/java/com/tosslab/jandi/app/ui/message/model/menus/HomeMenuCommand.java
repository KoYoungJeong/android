package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
class HomeMenuCommand implements MenuCommand {

    Activity activity;

    HomeMenuCommand(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void execute(MenuItem menuItem) {
        activity.finish();
    }
}
