package com.tosslab.jandi.app.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 6. 20..
 */
public class FileUploadDialogFragment extends DialogFragment {
    static private int selectedEntityIdToBeShared;    // Share 할 chat-room
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String realFilePath = getArguments().getString("realFilePath", "");
        final int currentEntityId = getArguments().getInt("currentEntityId");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_upload_file, null);

        // 파일 이름
        final EditText editTextInputName = (EditText) mainView.findViewById(R.id.et_file_name_to_be_uploaded);
        File uploadFile = new File(realFilePath);
        String fileName = uploadFile.getName();
        if (realFilePath.length() > 0) {
            editTextInputName.setText(fileName);
        }

        ImageView imageView = (ImageView) mainView.findViewById(R.id.img_upload_image);
        if (fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("png")) {
            Glide.with(getActivity())
                    .load(uploadFile)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }


        // CDP
        final Spinner spinner = (Spinner) mainView.findViewById(R.id.spinner_cdps);
        spinner.setPrompt(getString(R.string.jandi_action_share));
        EntityManager entityManager = EntityManager.getInstance();
        List<FormattedEntity> unsharedEntities = entityManager.retrieveExclusivedEntities(Arrays.asList(entityManager.getMe().getId()));

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        Observable.from(unsharedEntities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                .toSortedList((formattedEntity, formattedEntity2) -> {
                    if (formattedEntity.isUser() && formattedEntity2.isUser()) {
                        return formattedEntity.getName()
                                .compareToIgnoreCase(formattedEntity2.getName());
                    } else if (!formattedEntity.isUser() && !formattedEntity2.isUser()) {
                        return formattedEntity.getName()
                                .compareToIgnoreCase(formattedEntity2.getName());
                    } else {
                        if (formattedEntity.isUser()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                })
                .subscribe(formattedEntities::addAll);


        mEntityArrayAdapter = new EntitySimpleListAdapter(getActivity(), formattedEntities);
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
                LogUtil.d("Change to cdp ID to be shared : " + selectedEntityIdToBeShared);
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
                .setCancelable(true)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_upload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(editTextInputName.getWindowToken(), 0);

                                EventBus.getDefault().post(
                                        new ConfirmFileUploadEvent(
                                                editTextInputName.getText().toString().trim(),
                                                selectedEntityIdToBeShared,
                                                realFilePath,
                                                editTextFileComment.getText().toString().trim()));
                                dismiss();
                            }
                        }
                );

        return builder.create();
    }
}
