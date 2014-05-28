package com.tosslab.toss.app.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tosslab.toss.app.R;
import com.tosslab.toss.app.events.ConfirmCreateCdpEvent;

import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
@EFragment
public class CreateCdpAlertDialogFragment extends DialogFragment {
    public static CreateCdpAlertDialogFragment newInstance(int title, int cdpType) {
        CreateCdpAlertDialogFragment frag = new CreateCdpAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("cdpType", cdpType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int title = getArguments().getInt("title");
        final int cdpType = getArguments().getInt("cdpType");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_input_text, null);

        final EditText inputName = (EditText)mainView.findViewById(R.id.et_dialog_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(title)
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(new ConfirmCreateCdpEvent(cdpType,
                                        inputName.getText().toString()));
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }
}
