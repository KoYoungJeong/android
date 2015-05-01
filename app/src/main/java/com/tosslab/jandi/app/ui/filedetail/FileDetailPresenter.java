package com.tosslab.jandi.app.ui.filedetail;

import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.filedetail.fileinfo.FileHeadManager;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailPresenter {

    @RootContext
    AppCompatActivity activity;

    @SystemService
    InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    @SystemService
    ClipboardManager clipboardManager;

    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;

    @Bean
    FileHeadManager fileHeadManager;

    @ViewById(R.id.list_file_detail_comments)
    ListView listFileDetailComments;

    @ViewById(R.id.ly_file_detail_input_comment)
    LinearLayout inputCommentLayout;

    @ViewById(R.id.et_file_detail_comment)
    EditText editTextComment;
    @ViewById(R.id.btn_file_detail_send_comment)
    Button buttonSendComment;

    private ProgressWheel mProgressWheel;


    @AfterViews
    void initViews() {
        addFileDetailViewAsListviewHeader();
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

    }

    private void addFileDetailViewAsListviewHeader() {
        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = fileHeadManager.getHeaderView();

        listFileDetailComments.addHeaderView(header);
        listFileDetailComments.setAdapter(fileDetailCommentListAdapter);
    }


    public void setSendButtonSelected(boolean selected) {
        buttonSendComment.setSelected(selected);
    }

    @UiThread
    public void drawFileDetail(ResFileDetail resFileDetail, boolean isSendAction) {

        ResMessages.OriginalMessage fileDetail = getFileMessage(resFileDetail.messageDetails);

        final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;

        fileHeadManager.setFileInfo(fileMessage);

        if (TextUtils.equals(fileMessage.status, "archived")) {

            inputCommentLayout.setVisibility(View.GONE);

            activity.getSupportActionBar().setTitle(R.string.jandi_deleted_file);

        }

        fileDetailCommentListAdapter.clear();
        fileDetailCommentListAdapter.updateFileComments(resFileDetail);
        fileDetailCommentListAdapter.notifyDataSetChanged();

        if (isSendAction) {
            listFileDetailComments.setSelection(fileDetailCommentListAdapter.getCount());
        }
    }

    private ResMessages.OriginalMessage getFileMessage(List<ResMessages.OriginalMessage> messageDetails) {

        for (ResMessages.OriginalMessage messageDetail : messageDetails) {
            if (messageDetail instanceof ResMessages.FileMessage) {
                return messageDetail;
            }
        }

        return null;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void drawFileSharedEntities(ResMessages.FileMessage resFileDetail) {
        fileHeadManager.drawFileSharedEntities(resFileDetail);
    }


    public void clearAdapter() {
        fileDetailCommentListAdapter.clear();
    }

    @UiThread
    public void showProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }

        if (mProgressWheel != null) {
            mProgressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {

        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }

    }


    @UiThread
    public void unshareMessageSucceed(int entityIdToBeUnshared) {
        ColoredToast.show(activity, activity.getString(R.string.jandi_unshare_succeed, activity.getSupportActionBar().getTitle()));
        fileDetailCommentListAdapter.clear();
    }


    public void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
        editTextComment.setText("");
    }

    @UiThread
    public void downloadDone(File file, String fileType, ProgressDialog progressDialog) {

        progressDialog.dismiss();

        // Success
        Intent i = new Intent();
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.setDataAndType(Uri.fromFile(file), fileType);
        try {
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            String rawString = activity.getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(activity, formatString);
        }

    }

    @UiThread
    public void showUserInfoDialog(FormattedEntity user) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        UserInfoDialogFragment_.builder().entityId(user.getId()).build().show(activity.getSupportFragmentManager(), "dialog");

    }

    public String getCommentText() {
        return editTextComment.getText().toString();
    }

    public void copyToClipboard(String contentString) {
        ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void showDeleteFileDialog(int fileId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(activity.getString(R.string.jandi_file_delete_message))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_delete, (dialog, which) -> EventBus.getDefault().post(new ConfirmDeleteFileEvent(fileId)))
                .create().show();

    }

    @UiThread
    public void drawFileWriterState(boolean isEnabled) {
        fileHeadManager.drawFileWriterState(isEnabled);
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(activity, message);
    }
}
