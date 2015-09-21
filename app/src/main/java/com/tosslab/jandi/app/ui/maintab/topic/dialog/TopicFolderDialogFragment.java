package com.tosslab.jandi.app.ui.maintab.topic.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.model.TopicFolderDialogModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 9. 8..
 */
@EFragment
public class TopicFolderDialogFragment extends DialogFragment {

    @FragmentArg
    int folderId;
    @FragmentArg
    String folderName;
    @FragmentArg
    int seq;

    TextView tvFolderTitle;

    @Bean
    TopicFolderDialogModel topicFolderDialogModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder_popup, null);
        tvFolderTitle = (TextView) view.findViewById(R.id.tv_popup_title);

        view.findViewById(R.id.tv_folder_rename).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Rename);
            clickFolderRename();
        });
        view.findViewById(R.id.tv_folder_delete).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Delete);
            clickFolderDelete();
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    void initView() {
        tvFolderTitle.setText(folderName);
    }

    void clickFolderRename() {
        showRenameFolderDialog(folderId, folderName, seq);
    }

    void clickFolderDelete() {
        showDeleteFolderDialog(folderId);
    }

    private void showRenameFolderDialog(int folderId, String name, int seq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout vgInputEditText = (LinearLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.input_edit_text_view, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_input);

        input.setText(name);
        input.setSelection(name.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    renameFolder(folderId, input.getText().toString(), seq);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                    TopicFolderDialogFragment.this.dismiss();
                })
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        builder.show();
    }

    private void showDeleteFolderDialog(int folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    deleteTopicFolder(folderId);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), (dialog, which) -> {
                    dialog.cancel();
                    TopicFolderDialogFragment.this.dismiss();
                })
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        builder.show();
    }

    @Background
    public void deleteTopicFolder(int folderId) {
        try {
            topicFolderDialogModel.deleteTopicFolder(folderId);
            showDeleteFolderToast();
            dismiss();
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void renameFolder(int folderId, String name, int seq) {
        try {
            topicFolderDialogModel.renameFolder(folderId, name, seq);
            showRenameFolderToast();
            dismiss();
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @UiThread
    public void showRenameFolderToast() {
        ColoredToast.show(JandiApplication.getContext(),
                JandiApplication.getContext().getString(R.string.jandi_folder_renamed));
    }

    @UiThread
    public void showDeleteFolderToast() {
        ColoredToast.show(JandiApplication.getContext(),
                JandiApplication.getContext().getString(R.string.jandi_folder_removed));
    }

}
