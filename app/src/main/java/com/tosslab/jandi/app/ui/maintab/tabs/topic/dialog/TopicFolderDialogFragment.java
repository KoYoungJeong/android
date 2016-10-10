package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;


/**
 * Created by tee on 15. 9. 8..
 */
@EFragment
public class TopicFolderDialogFragment extends DialogFragment {

    @FragmentArg
    long folderId;
    @FragmentArg
    String folderName;
    @FragmentArg
    int seq;

    TextView tvFolderTitle;

    @Bean
    TopicFolderSettingModel topicFolderDialogModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder_popup, null);
        tvFolderTitle = (TextView) view.findViewById(R.id.tv_popup_title);

        view.findViewById(R.id.tv_folder_rename).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Rename);
            clickFolderRename();
            dismiss();
        });
        view.findViewById(R.id.tv_folder_delete).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Delete);
            clickFolderDelete();
            dismiss();
        });

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
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

    private void showRenameFolderDialog(long folderId, String name, int seq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.jandi_folder_rename);

        input.setText(name);
        input.setHint(R.string.jandi_entity_create_entity_name);
        input.setSelection(name.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    renameFolder(folderId, input.getText().toString().trim(), seq);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), null)
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() <= 0) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    private void showDeleteFolderDialog(long folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    deleteTopicFolder(folderId);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), null)
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        builder.show();
    }

    @Background
    public void deleteTopicFolder(long folderId) {
        try {
            topicFolderDialogModel.deleteTopicFolder(folderId);
            FolderRepository.getInstance().deleteFolder(folderId);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            showDeleteFolderToast();
            dismiss();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void renameFolder(long folderId, String name, int seq) {
        try {
            topicFolderDialogModel.renameFolder(folderId, name, seq);
            FolderRepository.getInstance().updateFolderName(folderId, name);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            showRenameFolderToast();
        } catch (RetrofitException e) {
            e.printStackTrace();
            if (e.getResponseCode() == 40008) {
                showFailedRenameFolderToast();
            }
        }
    }

    @UiThread
    public void showRenameFolderToast() {
        ColoredToast.show(JandiApplication.getContext().getString(R.string.jandi_folder_renamed));
    }

    @UiThread
    public void showFailedRenameFolderToast() {
        ColoredToast.showWarning(JandiApplication.getContext().getString(R.string.jandi_folder_alread_has_name));
    }

    @UiThread
    public void showDeleteFolderToast() {
        ColoredToast.show(JandiApplication.getContext().getString(R.string.jandi_folder_removed));
    }

}
