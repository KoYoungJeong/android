package com.tosslab.toss.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.tosslab.toss.app.MainActivity;
import com.tosslab.toss.app.R;
import com.tosslab.toss.app.events.ConfirmFileUploadEvent;
import com.tosslab.toss.app.lists.CdpArrayAdapter;
import com.tosslab.toss.app.lists.CdpItem;

import org.apache.log4j.Logger;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 20..
 */
public class FileUploadDialogFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(FileUploadDialogFragment.class);
    private CdpArrayAdapter cdpArrayAdapter;

    static private int selectedCdpIdToBeShared;    // Share 할 CDP

    public static FileUploadDialogFragment newInstance(String realFilePath, int currentCdpId) {
        selectedCdpIdToBeShared = currentCdpId;
        FileUploadDialogFragment frag = new FileUploadDialogFragment();
        Bundle args = new Bundle();
        args.putString("realFilePath", realFilePath);
        args.putInt("currentCdpId", currentCdpId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String realFilePath = getArguments().getString("realFilePath", "");
        final int currentCdpId = getArguments().getInt("currentCdpId");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_upload_file, null);

        // 파일 이름
        final EditText editTextInputName = (EditText)mainView.findViewById(R.id.et_file_name_to_be_uploaded);
        if (realFilePath.length() > 0) {
            File f = new File(realFilePath);
            editTextInputName.setText(f.getName());
        } else {
            // TODO : ERROR 처리
        }

        // CDP
        final Spinner spinner = (Spinner)mainView.findViewById(R.id.spinner_cdps);
        spinner.setPrompt("공유");
        cdpArrayAdapter = new CdpArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                ((MainActivity)getActivity()).cdpItemManager.retrieveWithoutTitle());
        spinner.setAdapter(cdpArrayAdapter);
        spinner.setSelection(cdpArrayAdapter.getPosition(currentCdpId));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCdpIdToBeShared = ((CdpItem)adapterView.getItemAtPosition(i)).id;
                log.debug("Change to cdp ID to be shared : " + selectedCdpIdToBeShared);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // File 코멘트
        final EditText editTextFileComment = (EditText)mainView.findViewById(R.id.et_comment_with_file_upload);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setTitle(R.string.title_file_upload)
                .setPositiveButton(R.string.upload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(
                                        new ConfirmFileUploadEvent(
                                                selectedCdpIdToBeShared,
                                                realFilePath,
                                                editTextFileComment.getText().toString()));
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
