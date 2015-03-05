package com.tosslab.jandi.app.ui.message.v2;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
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
    ActionBarActivity activity;

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.layout_messages_preview_last_item)
    View previewLayout;

    @ViewById(R.id.img_message_preview_user_profile)
    ImageView previewProfileView;

    @ViewById(R.id.txt_message_preview_user_name)
    TextView previewNameView;

    @ViewById(R.id.txt_message_preview_content)
    TextView previewContent;

    @ViewById(R.id.ll_messages)
    View sendLayout;

    @ViewById(R.id.ll_messages_disable_alert)
    View disabledUser;

    @ViewById(R.id.layout_messages_empty)
    View emptyMessageView;

    private MessageListAdapter messageListAdapter;

    private ProgressWheel progressWheel;
    private String tempMessage;
    private boolean isDisabled;

    @AfterInject
    void initObject() {
        messageListAdapter = new MessageListAdapter(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @AfterViews
    void initViews() {

        messageListView.setAdapter(messageListAdapter);

        setSendEditText(tempMessage);

        if (isDisabled) {
            sendLayout.setVisibility(View.GONE);
            disabledUser.setVisibility(View.VISIBLE);
            setPreviewVisibleGone();
        }

        messageListView.setEmptyView(emptyMessageView);

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
        intent.setType("image/*");
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
        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
    }

    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(commentMessage, false);
        newFragment.show(activity.getSupportFragmentManager(), "dioalog");
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

    public List<ResMessages.Link> getLastItemsWithoutDummy() {
        int count = messageListAdapter.getCount();
        int lastIdx = Math.max(count - 20, 0);
        List<ResMessages.Link> lastItems = new ArrayList<ResMessages.Link>();
        for (int idx = count - 1; idx >= lastIdx; --idx) {
            if (messageListAdapter.getItem(idx) instanceof DummyMessageLink) {
                continue;
            }
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

    public void changeToArchive(int messageId) {
        int position = messageListAdapter.indexByMessageId(messageId);
        String archivedStatus = "archived";
        if (position > 0) {
            ResMessages.Link item = messageListAdapter.getItem(position);
            item.message.status = archivedStatus;
            item.message.updateTime = new Date();
        }

        List<Integer> commentIndexes = messageListAdapter.indexByFeedbackId(messageId);

        for (Integer commentIndex : commentIndexes) {
            ResMessages.Link item = messageListAdapter.getItem(commentIndex);
            item.feedback.status = archivedStatus;
            item.feedback.updateTime = new Date();
        }

        if (position > 0 || commentIndexes.size() > 0) {

            messageListAdapter.notifyDataSetChanged();
        }

    }

    public void updateMessageIdAtSendingMessage(long localId, int messageId) {
        if (!hasMessage(messageId)) {
            messageListAdapter.updateMessageId(localId, messageId);
        } else {
            deleteLocalMessage(localId);
        }
    }

    private void deleteLocalMessage(long localId) {
        deleteDummyMessageAtList(localId);
    }

    private boolean hasMessage(int messageId) {
        int idx = messageListAdapter.indexByMessageId(messageId);
        return idx > 0;
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

    @UiThread
    public void addDummyMessages(List<ResMessages.Link> dummyMessages) {
        for (ResMessages.Link dummyMessage : dummyMessages) {
            messageListAdapter.addDummyMessage(((DummyMessageLink) dummyMessage));
        }
    }

    public void showDummyMessageDialog(long localId) {
        DummyMessageDialog_.builder()
                .localId(localId)
                .build()
                .show(activity.getFragmentManager(), "dialog");
    }

    public DummyMessageLink getDummyMessage(long localId) {
        int position = messageListAdapter.getDummeMessagePositionByLocalId(localId);
        return ((DummyMessageLink) messageListAdapter.getItem(position));
    }

    public void deleteDummyMessageAtList(long localId) {
        int position = messageListAdapter.getDummeMessagePositionByLocalId(localId);
        messageListAdapter.remove(position);
        messageListAdapter.notifyDataSetChanged();
    }

    private boolean isVisibleLastItem() {
        return messageListView.getLastVisiblePosition() == messageListAdapter.getCount() - 1;
    }

    @UiThread
    public void showPreviewIfNotLastItem() {

        if (isVisibleLastItem()) {
            return;
        }

        ResMessages.Link item = messageListAdapter.getItem(messageListAdapter.getCount() - 1);

        previewNameView.setText(item.message.writer.name);
        String url = item.message.writer.u_photoThumbnailUrl != null && !(TextUtils.isEmpty(item.message.writer.u_photoThumbnailUrl.smallThumbnailUrl)) ? item.message.writer.u_photoThumbnailUrl.smallThumbnailUrl : item.message.writer.u_photoUrl;
        Ion.with(previewProfileView)
                .transform(new IonCircleTransform())
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + url);

        if (item.message instanceof ResMessages.FileMessage) {
            previewContent.setText(((ResMessages.FileMessage) item.message).content.title);
        } else if (item.message instanceof ResMessages.CommentMessage) {
            previewContent.setText(((ResMessages.CommentMessage) item.message).content.body);
        } else if (item.message instanceof ResMessages.TextMessage) {
            previewContent.setText(((ResMessages.TextMessage) item.message).content.body);
        }

        previewLayout.setVisibility(View.VISIBLE);
    }

    public void setPreviewVisibleGone() {
        if (previewLayout != null) {
            previewLayout.setVisibility(View.GONE);
        }
    }

    public void disableChat() {
        isDisabled = true;
    }
}
