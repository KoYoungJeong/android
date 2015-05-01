package com.tosslab.jandi.app.ui.message.v2.model.file;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.DeleteAction_;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.DownloadAction;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.FileAction;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.ShareAction;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.UnshareAction;

/**
 * Created by Steve SeongUg Jung on 15. 4. 20..
 */
public class FileActor {

    public void showFileActionDialog(Context context, ResMessages.Link link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.file_message_actions, (dialog, which) -> getFileAction(context, which).action(link))
                .setNegativeButton(R.string.jandi_cancel, null)
                .show();

    }

    private FileAction getFileAction(Context context, int which) {
        ActionType actionType = ActionType.values()[which];
        FileAction fileAction;
        switch (actionType) {
            default:
            case Download:
                fileAction = new DownloadAction(context);
                break;
            case Share:
                fileAction = new ShareAction();
                break;
            case Unshare:
                fileAction = new UnshareAction();
                break;
            case Delete:
                fileAction = DeleteAction_.getInstance_(context);
                break;
        }
        return fileAction;
    }

    private static enum ActionType {
        Download, Share, Unshare, Delete
    }
}
