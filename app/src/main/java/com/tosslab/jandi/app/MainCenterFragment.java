package com.tosslab.jandi.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.ChoicedCdpEvent;
import com.tosslab.jandi.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.ConfirmModifyMessageEvent;
import com.tosslab.jandi.app.events.HideSoftKeyboardForMessageInput;
import com.tosslab.jandi.app.events.ReqModifyMessageEvent;
import com.tosslab.jandi.app.events.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.RequestMessageListEvent;
import com.tosslab.jandi.app.lists.MessageItem;
import com.tosslab.jandi.app.lists.MessageItemListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.net.URLConnection;
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
    PullToRefreshListView mPullToRefreshListMessages;
    ListView mActualListView;
    @Bean
    MessageItemListAdapter messageItemListAdapter;
    @ViewById(R.id.et_message)
    EditText etMessage;

    private Context mContext;
    private String mMyToken;
    private ProgressWheel mProgressWheel;

    // Update 관련
    private Timer mTimer;
    private int mLastUpdateLinkId = 0;

    int mFirstItemId = -1;
    boolean mIsFirstMessage = false;

    // 현재 소프트웨어 키보드가 올라와 있는지 여부
    boolean mIsShownKeyboard = false;

    // 현재 선택한 것 : Channel, Direct Message or Private Group
    RequestMessageListEvent mCurrentEvent;

    @AfterInject
    void test() {
        log.debug("When??");
    }
    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(mContext);

        //
        // Set up of PullToRefresh
        mPullToRefreshListMessages.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
                String label = DateUtils.formatDateTime(mContext, System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                listViewPullToRefreshBase.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                new GetFutherMessagesTask().execute();
            }
        });
        mActualListView = mPullToRefreshListMessages.getRefreshableView();

        // Empty View를 가진 ListView 설정
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.view_message_list_empty, null);
        mActualListView.setEmptyView(emptyView);
        mActualListView.setAdapter(messageItemListAdapter);

        mActualListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                messagesItemLongClicked(messageItemListAdapter.getItem(i - 1));
                return true;
            }
        });
        mActualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                messagesItemClicked(messageItemListAdapter.getItem(i - 1));
            }
        });

        // 스크롤이 맨 아래 근처에 위치하면 업데이트 내역이 생기면 바로 최하단으로 이동하고
        // 스크롤이 중간에서 위로 존재하면 최하단으로 이동하지 않는다.
        mActualListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // not using this
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int thresholdOfStickingBottom = totalItemCount - firstVisibleItem - visibleItemCount;
                if (thresholdOfStickingBottom >= 5) {   // 이게 적절한 threshold 인지는 계속 하면서...
                    mActualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                } else {
                    mActualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                }

            }
        });

    }

    /**
     * 리스트 뷰의 최하단으로 이동한다.
     */
    private void goToBottomOfListView() {
        mActualListView.setSelection(messageItemListAdapter.getCount() - 1);
    }

    /**
     * 액션바의 왼쪽 오른쪽 패널이 열릴 경우 키보드를 강제로 내린다.
     * @param event
     */
    public void onEvent(HideSoftKeyboardForMessageInput event) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
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
        resumeUpdateTimer();
        super.onResume();
    }

    @Override
    public void onPause() {
        pauseUpdateTimer();
        super.onPause();
    }

    /************************************************************
     * Timer Task
     * 주기적으로 message update 내역을 polling
     ************************************************************/

    private void pauseUpdateTimer() {
        log.debug("pause polling");
        if (mTimer != null)
            mTimer.cancel();
    }

    private void resumeUpdateTimer() {
        log.debug("resume polling");
        TimerTask task = new UpdateTimerTask();
        mTimer = new Timer();
        mTimer.schedule(task, 3000, 3000);  // 3초뒤, 3초마다
    }

    /**
     * Polling task
     * Timer는 OnResume, OnPause 의 생명주기와 함께함
     */
    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            log.debug("update messages by polling");
            getUpdateMessages(false);
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
        log.debug("on RequestMessageListEvent");
        // 왼쪽 패널에서 선택한 CDP 의 이름을 하일라이트 표시
        EventBus.getDefault().post(new ChoicedCdpEvent(event.id));

        mIsFirstMessage = false;
        mPullToRefreshListMessages.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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
            pauseUpdateTimer();
            mProgressWheel.show();
            getMessagesInBackground(mCurrentEvent.type, mCurrentEvent.id);
        } else {
            log.warn("empty current event");
        }

    }

    @Background
    public void getMessagesInBackground(int type, int id) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, type, id);
        try {
            ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);
            messageItemListAdapter.insertMessageItem(restResMessages);

            if (mFirstItemId == -1) {
                if (restResMessages.messageCount > 0) {
                    // 업데이트를 위해 가장 최신의 Link ID를 저장한다.
                    int currentLastLinkId = messageItemListAdapter.getLastLinkId();
                    if (currentLastLinkId >= 0) {
                        mLastUpdateLinkId = currentLastLinkId;
                    }
                } else {
                    showWarningEmpty();
                    return;
                }
            }
            // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
            mIsFirstMessage = restResMessages.isFirst;
            // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
            mFirstItemId = restResMessages.firstIdOfReceviedList;


            log.debug("success to " + restResMessages.messageCount + " messages from " + mFirstItemId);
            getMessagesDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to get messages.", e);
            getMessagesDone(false, "메시지 획득에 실패했습니다");
        }
    }

    @UiThread
    public void showWarningEmpty() {
        mProgressWheel.dismiss();
        ColoredToast.showWarning(mContext, "등록된 메시지가 없습니다.");
        resumeUpdateTimer();
    }

    @UiThread
    public void getMessagesDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        resumeUpdateTimer();
        if (isOk) {
            if (mIsFirstMessage) {
                mPullToRefreshListMessages.setMode(PullToRefreshBase.Mode.DISABLED);
            }
            refreshListAdapter();
            goToBottomOfListView();
        } else {
            ColoredToast.showError(mContext, message);
        }

    }

    void refreshListAdapter() {
        messageItemListAdapter.notifyDataSetChanged();
    }

    /**
     * Full To Refresh 전용
     */
    private class GetFutherMessagesTask extends AsyncTask<Void, Void, String> {
        private int currentMessagesSize;

        @Override
        protected void onPreExecute() {
            currentMessagesSize = messageItemListAdapter.getCount();
            pauseUpdateTimer();
        }

        @Override
        protected String doInBackground(Void... voids) {
            MessageManipulator messageManipulator = new MessageManipulator(
                    tossRestClient, mMyToken, mCurrentEvent.type, mCurrentEvent.id);
            try {
                ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);
                messageItemListAdapter.insertMessageItem(restResMessages);
                // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
                mIsFirstMessage = restResMessages.isFirst;
                // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
                mFirstItemId = restResMessages.firstIdOfReceviedList;

                log.debug("success to " + restResMessages.messageCount + " messages from " + mFirstItemId);
                return null;
            } catch (RestClientException e) {
                log.error("fail to get messages.", e);
                return "메시지 획득에 실패했습니다";
            }

        }

        @Override
        protected void onPostExecute(String errMessage) {
            mPullToRefreshListMessages.onRefreshComplete();
            if (mIsFirstMessage) {
                ColoredToast.showWarning(mContext, "처음입니다.");
                mPullToRefreshListMessages.setMode(PullToRefreshBase.Mode.DISABLED);
            }

            if (errMessage == null) {
                // Success
                refreshListAdapter();
                // 리스트에 아이템이 추가되더라도 현재 위치를 고수하도록 이동한다.
                // +1 은 이전 메시지의 0번째 item은 실제 아이템이 아닌 날짜 경계선이기에 그 포지션을 뺀다.
                // +1 은 인덱스 0 - 사이즈는 1부터...
                int index = messageItemListAdapter.getCount() - currentMessagesSize + 2;
                log.debug(">>REFRESH<< @" + index);
                mActualListView.setSelectionFromTop(index, 0);
            } else {
                ColoredToast.showError(mContext, errMessage);
            }
            resumeUpdateTimer();
            super.onPostExecute(errMessage);
        }
    }

    /************************************************************
     * Message List 업데이트
     * Message 리스트의 업데이트 획득 (from 서버)
     ************************************************************/
//    @UiThread
    void getUpdateMessages(boolean doWithResumingUpdateTimer) {
        if (mCurrentEvent != null) {
            getUpdateMessagesInBackground(mCurrentEvent.type, mCurrentEvent.id, doWithResumingUpdateTimer);
        } else {
            log.warn("empty current event");
        }
    }

    @Background
    public void getUpdateMessagesInBackground(int type, int id, boolean doWithResumingUpdateTimer) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, type, id);
        try {
            if (mLastUpdateLinkId >= 0) {
                ResMessages restResMessages = messageManipulator.updateMessages(mLastUpdateLinkId);
                int nMessages = restResMessages.messageCount;
                boolean isEmpty = true;
                log.info("success to " + nMessages +
                        " messages updated at " + mLastUpdateLinkId);
                if (nMessages > 0) {
                    isEmpty = false;
                    // Update 된 메시지만 부분 삽입한다.
                    messageItemListAdapter.updatedMessageItem(restResMessages);
                    // 가장 최신의 LinkId를 업데이트한다.
                    int currentLastLinkId = messageItemListAdapter.getLastLinkId();
                    if (currentLastLinkId >= 0) {
                        mLastUpdateLinkId = currentLastLinkId;
                    }
                }

                getUpdateMessagesDone(isEmpty, doWithResumingUpdateTimer);
            }
        } catch (RestClientException e) {
            log.error("fail to get updated messages", e);
        }

    }

    @UiThread
    public void getUpdateMessagesDone(boolean isEmpty, boolean doWithResumingUpdateTimer) {
        if (isEmpty) {
            // DO NOTHING
            return;
        }
        refreshListAdapter();
        if (doWithResumingUpdateTimer) {
            resumeUpdateTimer();
        }
    }

    /************************************************************
     * Message 전송
     ************************************************************/

    @Click(R.id.btn_send_comment)
    void sendMessage() {
        pauseUpdateTimer();
        String message = etMessage.getText().toString();
        etMessage.setText("");
        if (message.length() > 0) {
            sendMessageInBackground(message);
        }
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
        } else {
            ColoredToast.showError(mContext, message);
        }
        getUpdateMessages(true);
    }

    /************************************************************
     * Message 수정
     ************************************************************/

    /**
     * Message Item의 Long Click 시, 수정/삭제 팝업 메뉴 활성화
     * @param item
     */
    void messagesItemLongClicked(MessageItem item) {
        if (!item.isDateDivider) {
            checkPermissionForManipulateMessage(item);
        }
    }

    void checkPermissionForManipulateMessage(MessageItem item) {
        if (item.getContentType()  == MessageItem.TYPE_IMAGE) {
            showWarningCheckPermission("파일 수정 기능은 차후에...");
        } else if (item.getContentType()  == MessageItem.TYPE_FILE) {
            showWarningCheckPermission("파일 수정 기능은 차후에...");
        } else if (((MainActivity)getActivity()).cdpItemManager.mMe.id == item.getUserId()) {
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
        pauseUpdateTimer();
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
            getUpdateMessages(true);
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
        pauseUpdateTimer();
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
            getUpdateMessages(true);
        } else {
            ColoredToast.showError(mContext, message);
        }
    }

    /************************************************************
     * 파일 업로드
     ************************************************************/

    public interface FinishUpdateCallback {
        public void doneUpdate();
    }

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
        log.debug("onActivityResult : " + requestCode + " / " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);


        String realFilePath = null;
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri targetUri = data.getData();
                    realFilePath = getRealPathFromUri(targetUri);
                    log.debug("Get Photo from URI : " + targetUri.toString() + ", FilePath : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra("GetPath");
                    realFilePath = path + File.separator + data.getStringExtra("GetFileName");
                    log.debug("Get File from Explorer : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_FILE_DETAIL_REFRESH:
                log.info("Return to MainCenterFragment from FileDetailActivity");
                // 파일 상세 Activity에서 넘어온 경우, 댓글이 달렸을 수도 있으니 바로 업데이트한다.
                getUpdateMessages(false);
                break;
            default:
                break;
        }
    }

    // File Upload 대화상자 보여주기
    void showFileUploadDialog(String realFilePath) {
        DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, mCurrentEvent.id);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // File Upload 확인 이벤트 획득
    public void onEvent(ConfirmFileUploadEvent event) {
        pauseUpdateTimer();

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Uploading " + event.realFilePath);
        progressDialog.show();

        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstants.SERVICE_ROOT_URL + "inner-api/file";

        Builders.Any.M ionBuilder
                = Ion
                .with(mContext, requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int)(downloaded/total));
                    }
                })
                .setHeader("Authorization", mMyToken)
                .setHeader("Accept", "application/vnd.tosslab.jandi-v1+json")
                .setMultipartParameter("title", uploadFile.getName())
                .setMultipartParameter("share", "" + event.cdpId)
                .setMultipartParameter("permission", "755");
        // Comment가 함께 등록될 경우 추가
        if (event.comment != null && !event.comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", event.comment);
        }
        ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        uploadFileDone(e, result);
                    }
                });
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

        getUpdateMessages(true);
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
    void messagesItemClicked(MessageItem item) {
        if (!item.isDateDivider) {
            switch (item.getContentType()) {
                case MessageItem.TYPE_STRING:
                    // DO NOTHING
                    break;
                case MessageItem.TYPE_COMMENT:
                    moveToFileDetailActivity(item.getFeedbackId());
                    break;
                case MessageItem.TYPE_IMAGE:
                case MessageItem.TYPE_FILE:
                    moveToFileDetailActivity(item.getMessageId());
                    break;

            }
        }
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        EventBus.getDefault().postSticky(((MainActivity) mContext).cdpItemManager);
    }

}
