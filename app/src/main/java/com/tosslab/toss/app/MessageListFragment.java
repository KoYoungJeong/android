package com.tosslab.toss.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.toss.app.dialogs.EditTextDialogFragment;
import com.tosslab.toss.app.dialogs.FileUploadDialogFragment;
import com.tosslab.toss.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.toss.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.toss.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.toss.app.events.ConfirmFileUploadEvent;
import com.tosslab.toss.app.events.ConfirmModifyMessageEvent;
import com.tosslab.toss.app.events.ReqModifyMessageEvent;
import com.tosslab.toss.app.events.RequestFileUploadEvent;
import com.tosslab.toss.app.events.SelectCdpItemEvent;
import com.tosslab.toss.app.lists.MessageItem;
import com.tosslab.toss.app.lists.MessageItemListAdapter;
import com.tosslab.toss.app.network.MessageManipulator;
import com.tosslab.toss.app.network.MultipartUtility;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EFragment(R.layout.fragment_main)
public class MessageListFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MessageListFragment.class);
    @RestService
    TossRestClient tossRestClient;
    @FragmentArg
    String myToken;

    @ViewById(R.id.list_messages)
    ListView listMessages;
    @Bean
    MessageItemListAdapter messageItemListAdapter;
    @ViewById(R.id.et_message)
    EditText etMessage;

    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
    // Update 관련
    private Timer mTimer;
    private Date mLastUpdateTime;

    int mFirstItemId = -1;
    boolean mIsFirstMessage = true;
    boolean mDoLoading = true;

    // 현재 선택한 것 : Channel, Direct Message or Private Group
    SelectCdpItemEvent mCurrentEvent;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
    }

    @AfterViews
    void bindAdapter() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();

        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

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

        mLastUpdateTime = new Date();

        mFirstItemId = -1;
        getMessages();
    }

    private void pauseTimer() {
        log.debug("pause polling");
        mTimer.cancel();
    }

    private void resumeTimer() {
        log.debug("resume polling");
        TimerTask task = new UpdateTimerTask();
        mTimer = new Timer();
        mTimer.schedule(task, 3000, 3000);  // 3초뒤, 3초마다
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
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
    public void onEvent(SelectCdpItemEvent event) {
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
                tossRestClient, mCurrentEvent, myToken);
        try {
            ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);

            // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
            mIsFirstMessage = restResMessages.isFirst;
            // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
            mFirstItemId = restResMessages.firstIdOfReceviedList;

            messageItemListAdapter.insertMessageItem(restResMessages);
            log.debug("success to " + restResMessages.messageCount + " messages from " + mFirstItemId);
            getMessagesDone();
        } catch (RestClientException e) {
            log.error("fail to get messages.", e);
        }
    }

    @UiThread
    public void getMessagesDone() {
        mProgressWheel.dismiss();
        refreshListAdapter();
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
                tossRestClient, mCurrentEvent, myToken);
        try {
            ResMessages restResMessages = messageManipulator.updateMessages(mLastUpdateTime);
            log.info("success to " + restResMessages.messageCount +
                    " messages updated at " + mLastUpdateTime.getTime());
            Date responseTime = restResMessages.responseTime;
            if (responseTime != null && responseTime.getTime() > 0) {
                mLastUpdateTime = responseTime;
            }

            // Update 된 메시지만 부분 삽입한다.
            messageItemListAdapter.updatedMessageItem(restResMessages);

            getUpdateMessagesDone();
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
                tossRestClient, mCurrentEvent, myToken);
        try {
            messageManipulator.sendMessage(message);
            log.debug("success to send message");
        } catch (RestClientException e) {
            log.error("fail to send message", e);
        }

        sendMessageDone();
    }

    @UiThread
    public void sendMessageDone() {
        getUpdateMessages();
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
        showDialog(item);
    }

    void showDialog(MessageItem item) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstance(item);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // Message 수정 이벤트 획득
    public void onEvent(ReqModifyMessageEvent event) {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(event.messageId
                , event.currentMessage);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // Message 수정 서버 요청
    public void onEvent(ConfirmModifyMessageEvent event) {
        modifyMessage(event.messageId, event.inputMessage);
    }

    @UiThread
    void modifyMessage(int messageId, String inputMessage) {
        mProgressWheel.show();
        modifyMessageInBackground(messageId, inputMessage);
    }

    @Background
    void modifyMessageInBackground(int messageId, String inputMessage) {
        MessageManipulator messageManipulator
                = new MessageManipulator(tossRestClient, mCurrentEvent, myToken);

        try {
            messageManipulator.modifyMessage(messageId, inputMessage);
            log.debug("success to modify message");
        } catch (RestClientException e) {
            log.error("fail to modify message");
        }

        modifyMessageDone();
    }

    @UiThread
    void modifyMessageDone() {
        mProgressWheel.dismiss();
        getUpdateMessages();
    }

    /************************************************************
     * Message 삭제
     ************************************************************/

    // Message 삭제 이벤트 획득
    public void onEvent(ConfirmDeleteMessageEvent event) {
        deleteMessage(event.messageId);
    }

    @UiThread
    void deleteMessage(int messageId) {
        deleteMessageInBackground(messageId);
    }

    @Background
    void deleteMessageInBackground(int messageId) {
        MessageManipulator messageManipulator
                = new MessageManipulator(tossRestClient, mCurrentEvent, myToken);
        try {
            messageManipulator.deleteMessage(messageId);
            log.debug("success to delete message");
        } catch (RestClientException e) {
            log.error("fail to delete message", e);
        }
        deleteMessageDone();
    }

    @UiThread
    void deleteMessageDone() {
        getUpdateMessages();
    }

    /************************************************************
     * 파일 업로드
     * TODO : 현재는 Image Upload 만...
     ************************************************************/

    @Click(R.id.btn_upload_file)
    void uploadFile() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    public void onEvent(RequestFileUploadEvent event) {
        Intent intent = null;
        switch (event.type) {
            case TossConstants.TYPE_UPLOAD_GALLERY:
                log.info("upload file from gallery");
                // Gallery
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, TossConstants.TYPE_UPLOAD_GALLERY);
                break;
            case TossConstants.TYPE_UPLOAD_EXPLORER:
                intent = new Intent(getActivity(), FileExplorerActivity.class);
                startActivityForResult(intent, TossConstants.TYPE_UPLOAD_EXPLORER);
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
                case TossConstants.TYPE_UPLOAD_GALLERY:
                    Uri targetUri = data.getData();
                    realFilePath = getRealPathFromUri(targetUri);
                    log.debug("Get Photo from URI : " + targetUri.toString() + ", FilePath : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                    break;
                case TossConstants.TYPE_UPLOAD_EXPLORER:
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
        DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // File Upload 확인 이벤트 획득
    public void onEvent(ConfirmFileUploadEvent event) {
        pauseTimer();
        uploadFileInBackground(event.realFilePath);
    }

    @Background
    void uploadFileInBackground(String fileUri) {

        String requestURL = TossConstants.SERVICE_ROOT_URL + "inner-api/file";

        File uploadFile = new File(fileUri);
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, myToken);

            multipart.addFormField("title", uploadFile.getName());
            multipart.addFormField("share", "" + mCurrentEvent.id);
            multipart.addFormField("permission", "755");
            multipart.addFilePart("userFile", uploadFile);

            log.debug("try to upload file, " + uploadFile.getName() + ", Authorization : " + myToken);

            List<String> response = multipart.finish();
            log.debug("SERVER REPLIED:");
            for (String line : response) {
                log.debug(line);
            }
            uploadFileDone();
        } catch (IOException ex) {
            log.error("fail to upload file.", ex);
        }
    }

    @UiThread
    void uploadFileDone() {
        // resume timer
        resumeTimer();
    }

    // TODO : Poor Implementation
    private String getRealPathFromUri(Uri contentUri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(contentUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;

//        String[] proj = { MediaStore.Images.Media.DATA };
//        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
//        Cursor cursor = loader.loadInBackground();
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
    }


    /************************************************************
     * 파일 상세
     ************************************************************/
    @ItemClick
    void list_messagesItemClicked(MessageItem item) {
        if (item.getContentType() != MessageItem.TYPE_STRING) {
            moveToFileDetailActivity(item.getId());
        }
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .myToken(myToken)
                .fileId(fileId)
                .start();
    }
}
