package com.tosslab.jandi.app.ui.message.v2.model.file.action;

import android.app.AlertDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel_;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 15. 4. 20..
 */
@EBean
public class DeleteAction implements FileAction {

    @RootContext
    Context context;
    private ProgressWheel progressWheel;

    @Override
    public void action(ResMessages.Link link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(context.getString(R.string.jandi_file_delete_message))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_delete, (dialog, which) -> deleteFile(link.messageId))
                .create().show();

    }

    @Background
    void deleteFile(int fileId) {
        showProgressWheel();
        try {
            FileDetailModel_.getInstance_(context).deleteFile(fileId);
//            deleteFileDone(true);
        } catch (JandiNetworkException e) {
//            deleteFileDone(false);
        } catch (Exception e) {
//            deleteFileDone(false);
        } finally {
            dismissProgressWheel();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(context);
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }


    }
}
