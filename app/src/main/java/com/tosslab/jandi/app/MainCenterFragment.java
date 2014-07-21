package com.tosslab.jandi.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.ConfirmModifyMessageEvent;
import com.tosslab.jandi.app.events.ReqModifyMessageEvent;
import com.tosslab.jandi.app.events.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.RequestMessageListEvent;
import com.tosslab.jandi.app.lists.MessageItem;
import com.tosslab.jandi.app.lists.MessageItemListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.MultipartUtility;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 11..
 */
@EFragment(R.layout.fragment_main)
public class MainCenterFragment extends BaseFragment  {
    private final Logger log = Logger.getLogger(MainCenterFragment.class);

    @RestService
    TossRestClient tossRestClient;

    @ViewById(R.id.list_messages)
    ListView listMessages;
    @Bean
    MessageItemListAdapter messageItemListAdapter;
    @ViewById(R.id.et_message)
    EditText etMessage;

    private Context mContext;
    private String mMyToken;
    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    // Update 관련
    private Timer mTimer;
    private int mLastUpdateLinkId = 0;

    int mFirstItemId = -1;
    boolean mIsFirstMessage = true;
    boolean mDoLoading = true;

    // 현재 선택한 것 : Channel, Direct Message or Private Group
    RequestMessageListEvent mCurrentEvent;

    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        mMyToken = JandiPreference.getMyToken(mContext);

        listMessages.setAdapter(messageItemListAdapter);
        // 스크롤의 맨 위으로 올라갔을 경우 (리스트 업데이트)
        listMessages.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // not using this
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mIsFirstMessage == false && mDoLoading == false
                        && firstVisibleItem == 0) {
                    mDoLoading = true;
                    absListView.setSelection(firstVisibleItem + visibleItemCount);
                    log.debug("Top of scrolled list. Try to get former message list.");
                    getMessages();
                }

            }
        });

        mFirstItemId = -1;
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        resumeTimer();
        super.onResume();
    }

    @Override
    public void onPause() {
        pauseTimer();
        super.onPause();
    }

    /************************************************************
     * Timer Task
     * 주기적으로 message update 내역을 polling
     ************************************************************/

    private void pauseTimer() {
        log.debug("pause polling");
        if (mTimer != null)
            mTimer.cancel();
    }

    private void resumeTimer() {
        log.debug("resume polling");
        TimerTask task = new UpdateTimerTask();
        mTimer = new Timer();
        mTimer.schedule(task, 1500, 3000);  // 1.5초뒤, 3초마다
    }

    /**
     * Polling task
     * Timer는 OnResume, OnPause 의 생명주기와 함께함
     */
    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            log.debug("update messages by polling");
            getUpdateMessages();
        }
    }

    /************************************************************
     * Message List 획득
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     ************************************************************/

    /**
     * Navigation Panel 에서 선택한 Channel, Member or PG 의 메시지 list 획득
     * @param event
     */
    public void onEvent(RequestMessageListEvent event) {
        log.debug("EVENT : from MainLeftFragment : SelectCdpItemEvent");

        mIsFirstMessage = true;
        mCurrentEvent = event;
        getMessagesAfterCleaning();
    }

    @UiThread
    public void getMessagesAfterCleaning() {
        mFirstItemId = -1;
        messageItemListAdapter.clearAdapter();
        getMessages();
    }

    @UiThread
    public void getMessages() {
        if (mCurrentEvent != null) {
            mProgressWheel.show();
            getMessagesInBackground(mCurrentEvent.type, mCurrentEvent.id);
        } else {
            // TODO : 시작 화면 보이기
        }

    }

    @Background
    public void getMessagesInBackground(int type, int id) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, type, id);
        try {
            ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);

            if (mFirstItemId == -1) {
                if (restResMessages.messageCount > 0) {
                    // 업데이트를 위해 가장 마지막 Link ID를 저장한다.
                    int currentLastLinkId = restResMessages.messages.get(0).id;
                    if (currentLastLinkId >= 0) {
                        mLastUpdateLinkId = currentLastLinkId;
                    }
                }
            }
            // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
            mIsFirstMessage = restResMessages.isFirst;
            // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
            mFirstItemId = restResMessages.firstIdOfReceviedList;

            messageItemListAdapter.insertMessageItem(restResMessages);
            log.debug("success to " + restResMessages.messageCount + " messages from " + mFirstItemId);
            getMessagesDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to get messages.", e);
            getMessagesDone(false, "메시지 획득에 실패했습니다");
        }
    }

    @UiThread
    public void getMessagesDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        if (isOk) {
            refreshListAdapter();
        } else {
            ColoredToast.showError(mContext, message);
        }

    }

    @UiThread
    void refreshListAdapter() {
        messageItemListAdapter.notifyDataSetChanged();
        mDoLoading = false;
    }

    /************************************************************
     * Message List 업데이트
     * Message 리스트의 업데이트 획득 (from 서버)
     ************************************************************/
    @UiThread
    void getUpdateMessages() {
        if (mCurrentEvent != null) {
            getUpdateMessagesInBackground(mCurrentEvent.type, mCurrentEvent.id);
        }
    }

    @Background
    public void getUpdateMessagesInBackground(int type, int id) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, type, id);
        try {
            if (mLastUpdateLinkId > 0) {
                ResMessages restResMessages = messageManipulator.updateMessages(mLastUpdateLinkId);
                int nMessages = restResMessages.messageCount;
                log.info("success to " + nMessages +
                        " messages updated at " + mLastUpdateLinkId);
                if (nMessages > 0) {
                    int currentLastLinkId = restResMessages.messages.get(nMessages - 1).id;
                    if (currentLastLinkId >= 0) {
                        mLastUpdateLinkId = currentLastLinkId;
                    }
                    // Update 된 메시지만 부분 삽입한다.
                    messageItemListAdapter.updatedMessageItem(restResMessages);
                }

                getUpdateMessagesDone();
            }
        } catch (RestClientException e) {
            log.error("fail to get updated messages", e);
        }

    }

    @UiThread
    public void getUpdateMessagesDone() {
        refreshListAdapter();
    }

    /************************************************************
     * Message 전송
     ************************************************************/

    @Click(R.id.btn_send_comment)
    void sendMessage() {
        String message = etMessage.getText().toString();
        hideSoftKeyboard();

        if (message.length() > 0) {
            sendMessageInBackground(message);
        }
    }

    @UiThread
    void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(etMessage.getWindowToken(),0);
        etMessage.setText("");
    }


    @Background
    public void sendMessageInBackground(String message) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, mCurrentEvent.type, mCurrentEvent.id);
        try {
            messageManipulator.sendMessage(message);
            log.debug("success to send message");
            sendMessageDone(true, "생성 성공");
        } catch (RestClientException e) {
            log.error("fail to send message", e);
            sendMessageDone(false, "Fail to send");
        }
    }

    @UiThread
    public void sendMessageDone(boolean isOk, String message) {
        if (isOk) {
            ColoredToast.show(mContext, message);
            getUpdateMessages();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * Message 수정
     ************************************************************/

    /**
     * Message Item의 Long Click 시, 수정/삭제 팝업 메뉴 활성화
     * @param item
     */
    @ItemLongClick
    void list_messagesItemLongClicked(MessageItem item) {
        checkPermissionForManipulateMessage(item);
    }

    void checkPermissionForManipulateMessage(MessageItem item) {
        if (item.getContentType()  == MessageItem.TYPE_IMAGE) {
            showWarningCheckPermission("파일 수정 기능은 차후에...");
        } else if (item.getContentType()  == MessageItem.TYPE_FILE) {
            showWarningCheckPermission("파일 수정 기능은 차후에...");
        } else if (((MainActivity)getActivity()).mCdpItemManager.mMe.id == item.getUserId()) {
            showDialog(item);
        } else {
            showWarningCheckPermission("권한이 없습니다.");
        }
    }

    @UiThread
    void showWarningCheckPermission(String message) {
        ColoredToast.showWarning(mContext, message);
    }

    void showDialog(MessageItem item) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstance(item);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // TODO : Serialize 객체로 이벤트 전달할 것
    // Message 수정 이벤트 획득
    public void onEvent(ReqModifyMessageEvent event) {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(event.messageType, event.messageId
                , event.currentMessage, event.feedbackId);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // Message 수정 서버 요청
    public void onEvent(ConfirmModifyMessageEvent event) {
        modifyMessage(event.messageType, event.messageId, event.inputMessage, event.feedbackId);
    }

    @UiThread
    void modifyMessage(int messageType, int messageId, String inputMessage, int feedbackId) {
        modifyMessageInBackground(messageType, messageId, inputMessage, feedbackId);
    }

    @Background
    void modifyMessageInBackground(int messageType, int messageId, String inputMessage, int feedbackId) {

        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, mCurrentEvent.type, mCurrentEvent.id);

        try {
            if (messageType == MessageItem.TYPE_STRING) {
                log.debug("Try to modify message");
                messageManipulator.modifyMessage(messageId, inputMessage);
            } else if (messageType == MessageItem.TYPE_COMMENT) {
                log.debug("Try to modify comment");
                messageManipulator.modifyMessageComment(messageId, inputMessage, feedbackId);
            }
            modifyMessageDone(true, "수정 성공");
        } catch (RestClientException e) {
            log.error("fail to modify message");
            modifyMessageDone(false, "수정 실패");
        }
    }

    @UiThread
    void modifyMessageDone(boolean isOk, String message) {
        if (isOk) {
            ColoredToast.show(mContext, message);
            getUpdateMessages();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * Message 삭제
     ************************************************************/

    // Message 삭제 이벤트 획득
    public void onEvent(ConfirmDeleteMessageEvent event) {
        deleteMessage(event.messageType, event.messageId, event.feedbackId);
    }

    @UiThread
    void deleteMessage(int messageType, int messageId, int feedbackId) {
        deleteMessageInBackground(messageType, messageId, feedbackId);
    }

    @Background
    void deleteMessageInBackground(int messageType, int messageId, int feedbackId) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, mCurrentEvent.type, mCurrentEvent.id);
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageManipulator.deleteMessage(messageId);
                log.debug("success to delete message");
            } else if (messageType == MessageItem.TYPE_COMMENT) {
                messageManipulator.deleteMessageComment(messageId, feedbackId);
            }
            deleteMessageDone(true, "Deleted !!");
        } catch (RestClientException e) {
            log.error("fail to delete message", e);
            deleteMessageDone(false, "Fail to delete");
        }
    }

    @UiThread
    void deleteMessageDone(boolean isOk, String message) {
        if (isOk) {
            ColoredToast.show(mContext, message);
            getUpdateMessages();
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * 파일 업로드
     ************************************************************/

    @Click(R.id.btn_upload_file)
    void uploadFile() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    public void onEvent(RequestFileUploadEvent event) {
        Intent intent = null;
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                log.info("upload file from gallery");
                // Gallery
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                intent = new Intent(mContext, FileExplorerActivity.class);
                startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
                break;
            default:
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        log.debug("onActivityResule : " + requestCode + " / " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            String realFilePath = null;
            switch (requestCode) {
                case JandiConstants.TYPE_UPLOAD_GALLERY:
                    Uri targetUri = data.getData();
//                    realFilePath = getFilePathFromUri(targetUri);
                    realFilePath = getRealPathFromUri(targetUri);
                    log.debug("Get Photo from URI : " + targetUri.toString() + ", FilePath : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                    break;
                case JandiConstants.TYPE_UPLOAD_EXPLORER:
                    String path = data.getStringExtra("GetPath");
                    realFilePath = path + File.separator + data.getStringExtra("GetFileName");
                    log.debug("Get File from Explorer : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                    break;
                default:
                    break;
            }
        }
    }

    // File Upload 대화상자 보여주기
    void showFileUploadDialog(String realFilePath) {
        DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, mCurrentEvent.id);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // File Upload 확인 이벤트 획득
    public void onEvent(ConfirmFileUploadEvent event) {
        pauseTimer();
//        mProgressWheel.show();
//        uploadFileInBackground(event.cdpId, event.realFilePath, event.comment);
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + event.realFilePath);
        progressDialog.show();

        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstants.SERVICE_ROOT_URL + "inner-api/file";
        Ion.with(mContext, requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int)(downloaded/total));
                    }
                })
                .setHeader("Authorization", mMyToken)
                .setMultipartParameter("title", uploadFile.getName())
                .setMultipartParameter("share", "" + event.cdpId)
                .setMultipartParameter("permission", "755")

                .setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        uploadFileDone(e, result);
                    }
                });
    }

    @Background
    void uploadFileInBackground(int cdpIdToBeShared, String fileUri, String comment) {

        String requestURL = JandiConstants.SERVICE_ROOT_URL + "inner-api/file";

        File uploadFile = new File(fileUri);
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, mMyToken);

            multipart.addFormField("title", uploadFile.getName());
            multipart.addFormField("share", "" + cdpIdToBeShared);
            multipart.addFormField("permission", "755");
            multipart.addFilePart("userFile", uploadFile);
            if (comment != null && comment.length() > 0) {
                multipart.addFormField("comment", comment);
            }

            log.debug("try to upload file, " + uploadFile.getName() + " with " + comment + ", to " + cdpIdToBeShared);

            List<String> response = multipart.finish();
            log.debug("SERVER REPLIED:");
            for (String line : response) {
                log.debug(line);
            }
            uploadFileDone(true, "File Uploaded !!");
        } catch (IOException ex) {
            log.error("fail to upload file.", ex);
            uploadFileDone(false, "Fail to upload file");

        }
    }

    @UiThread
    void uploadFileDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        resumeTimer();  // resume timer
        if (isOk) {
            ColoredToast.show(mContext, message);
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    @UiThread
    void uploadFileDone(Exception exception, JsonObject result) {
        if (exception == null) {
            log.debug(result);
            ColoredToast.show(mContext, "File uploaded");
        } else {
            log.error("Upload failed", exception);
            ColoredToast.showError(mContext, "Upload failed");
        }

        resumeTimer();  // resume timer
    }

    private String getRealPathFromUri(Uri contentUri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(contentUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    /************************************************************
     * 파일 상세
     ************************************************************/
    @ItemClick
    void list_messagesItemClicked(MessageItem item) {
        switch (item.getContentType()) {
            case MessageItem.TYPE_STRING:
                // DO NOTHING
                break;
            case MessageItem.TYPE_COMMENT:
                moveToFileDetailActivity(item.getFeedbackId());
                break;
            case MessageItem.TYPE_IMAGE:
            case MessageItem.TYPE_FILE:
                moveToFileDetailActivity(item.getId());
                break;

        }
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .start();
        EventBus.getDefault().postSticky(((MainActivity)mContext).mCdpItemManager);
    }
}
