package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 20..
 */
public class FileUploadDialogFragment extends DialogFragment {
    static private int selectedEntityIdToBeShared;    // Share 할 chat-room
    private final Logger log = Logger.getLogger(FileUploadDialogFragment.class);
    private EntitySimpleListAdapter mEntityArrayAdapter;

    public static FileUploadDialogFragment newInstance(String realFilePath, int currentEntityId) {
        selectedEntityIdToBeShared = currentEntityId;
        FileUploadDialogFragment frag = new FileUploadDialogFragment();
        Bundle args = new Bundle();
        args.putString("realFilePath", realFilePath);
        args.putInt("currentEntityId", currentEntityId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // 회면 밖 터치시 다이얼로그 종료
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
        // 키보드 자동 올리기
        me.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        me.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String realFilePath = getArguments().getString("realFilePath", "");
        final int currentEntityId = getArguments().getInt("currentEntityId");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_upload_file, null);

        // 파일 이름
        final EditText editTextInputName = (EditText) mainView.findViewById(R.id.et_file_name_to_be_uploaded);
        if (realFilePath.length() > 0) {
            File f = new File(realFilePath);
            editTextInputName.setText(f.getName());
        } else {
            // TODO : ERROR 처리
        }

        // CDP
        final Spinner spinner = (Spinner) mainView.findViewById(R.id.spinner_cdps);
        spinner.setPrompt(getString(R.string.jandi_action_share));
        EntityManager entityManager = EntityManager.getInstance(getActivity());
        mEntityArrayAdapter = new EntitySimpleListAdapter(getActivity(), entityManager.retrieveExclusivedEntities(Arrays.asList(entityManager.getMe().getId())));
        spinner.setAdapter(mEntityArrayAdapter);

        int size = mEntityArrayAdapter.getCount();
        int currentIndex = 0;
        for (int idx = 0; idx < size; ++idx) {
            if (mEntityArrayAdapter.getItem(idx).getId() == currentEntityId) {
                currentIndex = idx;
                break;
            }
        }

        spinner.setSelection(currentIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEntityIdToBeShared
                        = ((FormattedEntity) adapterView.getItemAtPosition(i)).getEntity().id;
                log.debug("Change to cdp ID to be shared : " + selectedEntityIdToBeShared);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // File 코멘트
        final EditText editTextFileComment = (EditText) mainView.findViewById(R.id.et_comment_with_file_upload);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setTitle(R.string.title_file_upload)
                .setPositiveButton(R.string.jandi_upload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(
                                        new ConfirmFileUploadEvent(
                                                selectedEntityIdToBeShared,
                                                realFilePath,
                                                editTextFileComment.getText().toString()));
                                dismiss();
                            }
                        }
                )
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
