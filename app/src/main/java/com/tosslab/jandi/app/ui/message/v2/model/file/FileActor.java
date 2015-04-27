package com.tosslab.jandi.app.ui.message.v2.model.file;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.model.file.action.DeleteAction;
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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.items(R.array.file_message_actions)
                .negativeText(R.string.jandi_cancel)
                .itemsCallback((dialog, itemView, which, text) -> getFileAction(context, which).action(link))
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
