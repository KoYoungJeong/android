package com.tosslab.toss.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.tosslab.toss.app.MainActivity;
import com.tosslab.toss.app.R;
import com.tosslab.toss.app.events.ConfirmFileUploadEvent;
import com.tosslab.toss.app.lists.CdpArrayAdapter;

import org.androidannotations.annotations.EFragment;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 20..
 */
public class FileUploadDialogFragment extends DialogFragment {
    private CdpArrayAdapter cdpArrayAdapter;
    public static FileUploadDialogFragment newInstance(String realFilePath) {

        FileUploadDialogFragment frag = new FileUploadDialogFragment();
        Bundle args = new Bundle();
        args.putString("realFilePath", realFilePath);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String realFilePath = getArguments().getString("realFilePath", "");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_upload_file, null);

        final EditText editTextInputName = (EditText)mainView.findViewById(R.id.et_file_name_to_be_uploaded);
        if (realFilePath.length() > 0) {
            File f = new File(realFilePath);
            editTextInputName.setText(f.getName());
        } else {
            // TODO : ERROR 처리
        }

        final Spinner spinner = (Spinner)mainView.findViewById(R.id.spinner_cdps);
        spinner.setPrompt("공유");
        cdpArrayAdapter = new CdpArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                ((MainActivity)getActivity()).cdpItemManager.retrieve());
        spinner.setAdapter(cdpArrayAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setTitle(R.string.title_file_upload)
                .setPositiveButton(R.string.upload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(new ConfirmFileUploadEvent(realFilePath));
                                dismiss();
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
