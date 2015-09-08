package com.tosslab.jandi.app.ui.maintab.topic.dialog;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.model.TopicFolderDialogModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 9. 8..
 */
@EFragment(R.layout.fragment_folder_popup)
public class TopicFolderDialogFragment extends DialogFragment {

    @FragmentArg
    int folderId;
    @FragmentArg
    String folderName;
    @FragmentArg
    int seq;

    @ViewById(R.id.tv_popup_title)
    TextView tvFolderTitle;

    @Bean
    TopicFolderDialogModel topicFolderDialogModel;

    @AfterViews
    void initView() {
        tvFolderTitle.setText(folderName);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Click(R.id.tv_folder_rename)
    void clickFolderRename() {
        showRenameFolderDialog(folderId, folderName, seq);
    }

    @Click(R.id.tv_folder_delete)
    void clickFolderDelete() {
        showDeleteFolderDialog(folderId);
    }

    public void showRenameFolderDialog(int folderId, String name, int seq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout vgInputEditText = (LinearLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.input_edit_text_view, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_input);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, displayMetrics);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, displayMetrics);
        vgInputEditText.setMinimumWidth(minWidth);
        vgInputEditText.setPadding(padding, input.getPaddingTop(), padding, input.getPaddingBottom());

        input.setText(name);
        input.setMaxLines(1);
        input.setCursorVisible(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSelection(name.length());

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText(R.string.jandi_folder_insert_name);
        tvTitle.setTextColor(getResources().getColor(R.color.black));
        tvTitle.setTextSize(20);
        int paddingTopLeftRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, displayMetrics);
        tvTitle.setPadding(paddingTopLeftRight, paddingTopLeftRight, paddingTopLeftRight, 0);

        builder.setView(vgInputEditText)
                .setCustomTitle(tvTitle)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        renameFolder(folderId, input.getText().toString(), seq);
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        TopicFolderDialogFragment.this.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        TopicFolderDialogFragment.this.dismiss();
                    }
                });
        builder.show();
    }

    public void showDeleteFolderDialog(int folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTopicFolder(folderId);
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        TopicFolderDialogFragment.this.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        TopicFolderDialogFragment.this.dismiss();
                    }
                });
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
