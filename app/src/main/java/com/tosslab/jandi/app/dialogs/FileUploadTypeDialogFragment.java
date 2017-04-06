package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 21..
 */
public class FileUploadTypeDialogFragment extends DialogFragment {

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // 회면 밖 터치시 다이얼로그 종료
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.title_file_upload)
                .setItems(R.array.types_file_upload, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int eventType;
                        switch (which) {
                            case 0:     // from gallery
                                eventType = FileUploadController.TYPE_UPLOAD_IMAGE_GALLERY;
                                break;
                            case 1:     // from camera
                                eventType = FileUploadController.TYPE_UPLOAD_TAKE_PHOTO;
                                break;
                            case 2:     // from Explorer
                            default:
                                eventType = FileUploadController.TYPE_UPLOAD_EXPLORER;
                                break;

                        }
                        EventBus.getDefault().post(new RequestFileUploadEvent(eventType));
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.jandi_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }
}
