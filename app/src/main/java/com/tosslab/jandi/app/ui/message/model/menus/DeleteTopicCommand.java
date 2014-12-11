package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.app.DialogFragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.dialogs.DeleteTopicDialogFragment;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
class DeleteTopicCommand implements MenuCommand {

    private Activity activity;

    DeleteTopicCommand(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void execute(MenuItem menuItem) {
        requestToDeleteTopic();
    }

    void requestToDeleteTopic() {
        DialogFragment newFragment = DeleteTopicDialogFragment.newInstance();
        newFragment.show(activity.getFragmentManager(), "dialog");
    }
}
