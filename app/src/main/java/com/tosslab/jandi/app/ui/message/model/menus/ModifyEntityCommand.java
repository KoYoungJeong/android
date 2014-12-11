package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.app.DialogFragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
class ModifyEntityCommand implements MenuCommand {


    private Activity activity;
    private ChattingInfomations chattingInfomations;


    ModifyEntityCommand(Activity activity, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.chattingInfomations = chattingInfomations;
    }

    @Override
    public void execute(MenuItem menuItem) {
        modifyEntity();
    }

    private void modifyEntity() {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_TOPIC
                , chattingInfomations.entityType
                , chattingInfomations.entityId
                , chattingInfomations.entityName);
        newFragment.show(activity.getFragmentManager(), "dialog");
    }
}
