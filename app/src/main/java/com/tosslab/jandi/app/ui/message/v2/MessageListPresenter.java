package com.tosslab.jandi.app.ui.message.v2;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListPresenter {

    @ViewById(R.id.list_messages)
    StickyListHeadersListView messageListView;

    @ViewById(R.id.btn_send_message)
    Button sendButton;

    @ViewById(R.id.et_message)
    EditText messageEditText;

    @RootContext
    Activity activity;

    @SystemService
    ClipboardManager clipboardManager;

    private MessageListAdapter messageListAdapter;

    private ProgressWheel progressWheel;
    private String tempMessage;

    @AfterInject
    void initObject() {
        messageListAdapter = new MessageListAdapter(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @AfterViews
    void initViews() {
//        messageListView.setAreHeadersSticky(false);
        messageListView.setAdapter(messageListAdapter);

        setSendEditText(tempMessage);

    }

    @UiThread
    public void addAll(int position, List<ResMessages.Link> messages) {
        messageListAdapter.addAll(position, messages);
        messageListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void setEnableSendButton(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    public int getLastItemPosition() {
        return messageListAdapter.getCount();
    }

    @UiThread
    public void moveLastPage() {
        if (messageListView != null) {
            messageListView.setSelection(messageListAdapter.getCount() - 1);
        }
    }

    public void setLoadingComplete() {
        messageListAdapter.setLoadingComplete();
    }

    public void setNoMoreLoading() {
        messageListAdapter.setNoMoreLoading();
    }

    @UiThread
    public void moveToMessage(int linkId, int firstVisibleItemTop) {
        int itemPosition = messageListAdapter.getItemPositionByLinkId(linkId);
        messageListView.setSelectionFromTop(itemPosition, firstVisibleItemTop);
    }

    public int getFirstVisibleItemLinkId() {
        return messageListAdapter.getItem(messageListView.getFirstVisiblePosition()).messageId;
    }

    public int getFirstVisibleItemTop() {
        return messageListView.getChildAt(0).getTop();
    }

    @UiThread
    public void showNoMoreMessage() {
        ColoredToast.showWarning(activity, activity.getString(R.string.warn_no_more_messages));
    }

    public void moveFileDetailActivity(Fragment fragment, int messageId) {
        FileDetailActivity_
                .intent(fragment)
                .fileId(messageId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    public ResMessages.Link getItem(int position) {
        return messageListAdapter.getItem(position);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
    }

    public void openCameraForActivityResult(Fragment fragment) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
    }

    public void exceedMaxFileSizeError() {

        ColoredToast.showError(activity, activity.getString(R.string.err_file_upload_failed));
    }

    public String getSendEditText() {
        return messageEditText.getText().toString();
    }


    public void setSendEditText(String text) {
        tempMessage = text;
        if (messageEditText != null) {
            messageEditText.setText(tempMessage);
        }
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(activity, message);
    }

    public void showMessageMenuDialog(boolean myMessage, ResMessages.TextMessage textMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage(textMessage, myMessage);
        newFragment.show(activity.getFragmentManager(), "dioalog");
    }

    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(commentMessage, false);
        newFragment.show(activity.getFragmentManager(), "dioalog");
    }

    public ProgressDialog getUploadProgress(ConfirmFileUploadEvent event) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(activity.getString(R.string.jandi_file_uploading) + " " + event.realFilePath);
        progressDialog.show();

        return progressDialog;

    }

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(activity, message);
    }

    @UiThread
    public void dismissProgressDialog(ProgressDialog uploadProgressDialog) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    @UiThread
    public void clearMessages() {
        messageListAdapter.clear();

    }

    public List<ResMessages.Link> getLastItems() {
        int count = messageListAdapter.getCount();
        int lastIdx = Math.max(count - 20, 0);
        List<ResMessages.Link> lastItems = new ArrayList<ResMessages.Link>();
        for (int idx = count - 1; idx >= lastIdx; --idx) {
            lastItems.add(messageListAdapter.getItem(idx));
        }

        return lastItems;
    }

    @UiThread
    public void finish() {
        activity.finish();
    }

    public void copyToClipboard(String contentString) {
        final ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void updateMessageIdAtSendingMessage(long localId, int id) {
        messageListAdapter.updateMessageId(localId, id);
    }

    public void insertSendingMessage(long localId, String message, String name, String userLargeProfileUrl) {
        DummyMessageLink dummyMessageLink = new DummyMessageLink(localId, message, SendingState.Sending);

        dummyMessageLink.message.writer.name = name;
        dummyMessageLink.message.writer.u_photoUrl = userLargeProfileUrl;


        messageListAdapter.addDummyMessage(dummyMessageLink);
        messageListAdapter.notifyDataSetChanged();
    }

    public void updateDummyMessageState(long localId, SendingState state) {
        messageListAdapter.updateDummyMessageState(localId, state);
        messageListAdapter.notifyDataSetChanged();
    }
}
