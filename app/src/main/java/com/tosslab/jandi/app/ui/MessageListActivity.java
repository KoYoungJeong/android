package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.tosslab.jandi.app.FileExplorerActivity;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.ConfirmModifyCdpEvent;
import com.tosslab.jandi.app.events.ConfirmModifyMessageEvent;
import com.tosslab.jandi.app.events.ReqModifyMessageEvent;
import com.tosslab.jandi.app.events.RequestFileUploadEvent;
import com.tosslab.jandi.app.lists.MessageItem;
import com.tosslab.jandi.app.lists.MessageItemConverter;
import com.tosslab.jandi.app.lists.MessageItemListAdapter;
import com.tosslab.jandi.app.network.MessageManipulator;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.events.StickyEntityManager;
import com.tosslab.jandi.app.ui.lists.EntityManager;
import com.tosslab.jandi.app.ui.lists.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.ui.models.FormattedEntity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
@EActivity(R.layout.fragment_main)
public class MessageListActivity extends BaseActivity {
    private final Logger log = Logger.getLogger(MessageListActivity.class);
    private final String DIALOG_TAG = "dialog";

    @Extra
    boolean isMyEntity;
    @Extra
    int entityType;
    @Extra
    int entityId;
    @Extra
    String entityName;

    @RestService
    TossRestClient tossRestClient;

    @ViewById(R.id.list_messages)
    PullToRefreshListView pullToRefreshListViewMessages;
    ListView actualListView;
    @Bean
    MessageItemListAdapter messageItemListAdapter;
    @ViewById(R.id.et_message)
    EditText etMessage;

    private Context mContext;
    private String mMyToken;
    private ProgressWheel mProgressWheel;

    // Update 관련
    private Timer mTimer;
    private int mLastUpdateLinkId = -1;

    private int mFirstItemId = -1;
    private boolean mIsFirstMessage = false;

    private MessageItemConverter mMessageItemConverter;

    private EntityManager mEntityManager;

    @AfterViews
    void bindAdapter() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(entityName);

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(mContext);
        mMessageItemConverter = new MessageItemConverter();

        //
        // Set up of PullToRefresh
        pullToRefreshListViewMessages.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> listViewPullToRefreshBase) {
                String label = DateUtils.formatDateTime(mContext, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // Update the LastUpdatedLabel
                listViewPullToRefreshBase.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                new GetFutherMessagesTask().execute();
            }
        });
        actualListView = pullToRefreshListViewMessages.getRefreshableView();

        // Empty View를 가진 ListView 설정
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.view_message_list_empty, null);
        actualListView.setEmptyView(emptyView);
        actualListView.setAdapter(messageItemListAdapter);

//        actualListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                messagesItemLongClicked(messageItemListAdapter.getItem(i - 1));
//                return true;
//            }
//        });
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                messagesItemClicked(messageItemListAdapter.getItem(i - 1));
            }
        });

        // 스크롤이 맨 아래 근처에 위치하면 업데이트 내역이 생기면 바로 최하단으로 이동하고
        // 스크롤이 중간에서 위로 존재하면 최하단으로 이동하지 않는다.
        actualListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // not using this
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int thresholdOfStickingBottom = totalItemCount - firstVisibleItem - visibleItemCount;
                if (thresholdOfStickingBottom >= 5) {   // 이게 적절한 threshold 인지는 계속 하면서...
                    actualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                } else {
                    actualListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                }

            }
        });

        getMessages();
    }

    /**
     * 리스트 뷰의 최하단으로 이동한다.
     */
    private void goToBottomOfListView() {
        actualListView.setSelection(messageItemListAdapter.getCount() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        resumeUpdateTimer();
    }

    @Override
    public void onPause() {
        pauseUpdateTimer();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            // DON'T SHOW OPTION MENU
            return true;
        }
        if (isMyEntity) {
            getMenuInflater().inflate(R.menu.manipulate_my_entity_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.manipulate_entity_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_entity_invite:
            case R.id.action_my_entity_invite:
                inviteMembersToEntity();
                return true;
            case R.id.action_my_entity_rename:
                modifyEntity();
                return true;
            case R.id.action_my_entity_delete:
                deleteEntityInBackground();
                return true;
            case R.id.action_entity_leave:
            case R.id.action_my_entity_leave:
                leaveEntityInBackground();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEvent(StickyEntityManager event) {
        log.debug("onEvent : StickyEntityManager");
        mEntityManager = event.entityManager;
    }

    /************************************************************
     * Timer Task
     * 주기적으로 message update 내역을 polling
     ************************************************************/

    private void pauseUpdateTimer() {
        log.info("pauseUpdateTimer");
        if (mTimer != null)
            mTimer.cancel();
    }

    private void resumeUpdateTimer() {
        log.info("resumeUpdateTimer");
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
            getUpdateMessagesWithoutResumingUpdateTimer();
        }
    }


    /************************************************************
     * Message List 획득
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     ************************************************************/

    @UiThread
    public void getMessages() {
        mIsFirstMessage = false;
        pullToRefreshListViewMessages.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        mFirstItemId = -1;
        mMessageItemConverter.clear();
        messageItemListAdapter.clearAdapter();

        pauseUpdateTimer();
        mProgressWheel.show();
        getMessagesInBackground(entityType, entityId);
    }

    @Background
    public void getMessagesInBackground(int type, int id) {
        MessageManipulator messageManipulator = new MessageManipulator(
                tossRestClient, mMyToken, type, id);
        try {
            ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);
            mMessageItemConverter.insertMessageItem(restResMessages);
            messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());

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

            log.debug("getMessagesInBackground : " + restResMessages.messageCount
                    + " messages from " + mFirstItemId);
            getMessagesDone(true, null);
        } catch (RestClientException e) {
            log.error("getMessagesInBackground : FAILED", e);
            getMessagesDone(false, getString(R.string.err_get_messages_failed));
        }
    }

    @UiThread
    public void showWarningEmpty() {
        mProgressWheel.dismiss();
        mLastUpdateLinkId = 0;
        ColoredToast.showWarning(mContext, getString(R.string.warn_empty_messages));
        resumeUpdateTimer();
    }

    @UiThread
    public void getMessagesDone(boolean isOk, String message) {
        mProgressWheel.dismiss();
        resumeUpdateTimer();
        if (isOk) {
            if (mIsFirstMessage) {
                // 현재 entity에서 더 이상 가져올 메시지가 없다면 pull to refresh를 끈다.
                pullToRefreshListViewMessages.setMode(PullToRefreshBase.Mode.DISABLED);
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
                    tossRestClient, mMyToken, entityType, entityId);
            try {
                ResMessages restResMessages = messageManipulator.getMessages(mFirstItemId);
                mMessageItemConverter.insertMessageItem(restResMessages);
                messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
                // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
                mIsFirstMessage = restResMessages.isFirst;
                // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
                mFirstItemId = restResMessages.firstIdOfReceviedList;

                log.debug("GetFutherMessagesTask : " + restResMessages.messageCount
                        + " messages from " + mFirstItemId);
                return null;
            } catch (RestClientException e) {
                log.error("GetFutherMessagesTask : FAILED", e);
                return getString(R.string.err_get_messages_failed);
            }

        }

        @Override
        protected void onPostExecute(String errMessage) {
            pullToRefreshListViewMessages.onRefreshComplete();
            if (mIsFirstMessage) {
                ColoredToast.showWarning(mContext, getString(R.string.warn_no_more_messages));
                pullToRefreshListViewMessages.setMode(PullToRefreshBase.Mode.DISABLED);
            }

            if (errMessage == null) {
                // Success
                refreshListAdapter();
                // 리스트에 아이템이 추가되더라도 현재 위치를 고수하도록 이동한다.
                // +1 은 이전 메시지의 0번째 item은 실제 아이템이 아닌 날짜 경계선이기에 그 포지션을 뺀다.
                // +1 은 인덱스 0 - 사이즈는 1부터...
                int index = messageItemListAdapter.getCount() - currentMessagesSize + 2;
                log.debug("GetFutherMessagesTask : REFRESH at " + index);
                actualListView.setSelectionFromTop(index, 0);
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
    void getUpdateMessagesAndResumeUpdateTimer() {
        getUpdateMessages(true);
    }

    void getUpdateMessagesWithoutResumingUpdateTimer() {
        getUpdateMessages(false);
    }

    void getUpdateMessages(boolean doWithResumingUpdateTimer) {
        getUpdateMessagesInBackground(entityType, entityId, doWithResumingUpdateTimer);
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
                log.info("getUpdateMessagesInBackground : " + nMessages
                        + " messages updated at ID, " + mLastUpdateLinkId);
                if (nMessages > 0) {
                    isEmpty = false;
                    // Update 된 메시지만 부분 삽입한다.
                    mMessageItemConverter.updatedMessageItem(restResMessages);
                    messageItemListAdapter.replaceMessageItem(mMessageItemConverter.reformatMessages());
                    // 가장 최신의 LinkId를 업데이트한다.
                    int currentLastLinkId = messageItemListAdapter.getLastLinkId();
                    if (currentLastLinkId >= 0) {
                        mLastUpdateLinkId = currentLastLinkId;
                    }
                }
                getUpdateMessagesDone(isEmpty, doWithResumingUpdateTimer);
            } else {
                log.warn("getUpdateMessagesInBackground : LastUpdateLinkId = " + mLastUpdateLinkId);
            }
        } catch (RestClientException e) {
            log.error("fail to get updated messages", e);
        }

    }

    @UiThread
    public void getUpdateMessagesDone(boolean isEmpty, boolean doWithResumingUpdateTimer) {
        log.info("getUpdateMessagesDone : and resumeTimer? " + doWithResumingUpdateTimer);
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
                tossRestClient, mMyToken, entityType, entityId);
        try {
            messageManipulator.sendMessage(message);
            log.debug("sendMessageInBackground : succeed");
            sendMessageDone(true, null);
        } catch (RestClientException e) {
            log.error("sendMessageInBackground : FAILED", e);
            sendMessageDone(false, getString(R.string.err_send_messages_failed));
        }
    }

    @UiThread
    public void sendMessageDone(boolean isOk, String message) {
        if (!isOk) {
            ColoredToast.showError(mContext, message);
        }
        getUpdateMessagesAndResumeUpdateTimer();
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
//        if (item.getContentType()  == MessageItem.TYPE_IMAGE) {
//            showWarningCheckPermission("파일 수정 기능은 차후에...");
//        } else if (item.getContentType()  == MessageItem.TYPE_FILE) {
//            showWarningCheckPermission("파일 수정 기능은 차후에...");
//        } else if (((MainActivity)getActivity()).cdpItemManager.mMe.id == item.getUserId()) {
//            showDialog(item);
//        } else {
//            showWarningCheckPermission(getString(R.string.warn_no_permission));
//        }
    }

    @UiThread
    void showWarningCheckPermission(String message) {
        ColoredToast.showWarning(mContext, message);
    }

    void showDialog(MessageItem item) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstance(item);
        newFragment.show(getFragmentManager(), DIALOG_TAG);
    }

    // TODO : Serialize 객체로 이벤트 전달할 것
    // Message 수정 이벤트 획득
    public void onEvent(ReqModifyMessageEvent event) {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(event.messageType, event.messageId
                , event.currentMessage, event.feedbackId);
        newFragment.show(getFragmentManager(), DIALOG_TAG);
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
                tossRestClient, mMyToken, entityType, entityId);

        try {
            if (messageType == MessageItem.TYPE_STRING) {
                log.debug("modifyMessageInBackground : Try for message");
                messageManipulator.modifyMessage(messageId, inputMessage);
            } else if (messageType == MessageItem.TYPE_COMMENT) {
                log.debug("modifyMessageInBackground : Try for comment");
                messageManipulator.modifyMessageComment(messageId, inputMessage, feedbackId);
            }
            modifyMessageDone(true, getString(R.string.modify_messages_succeed));
        } catch (RestClientException e) {
            log.error("modifyMessageInBackground : FAILED");
            modifyMessageDone(false, getString(R.string.err_modify_messages_failed));
        }
    }

    @UiThread
    void modifyMessageDone(boolean isOk, String message) {
        if (isOk) {
            ColoredToast.show(mContext, message);
            getUpdateMessagesAndResumeUpdateTimer();
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
                tossRestClient, mMyToken, entityType, entityId);
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageManipulator.deleteMessage(messageId);
                log.debug("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_COMMENT) {
                messageManipulator.deleteMessageComment(messageId, feedbackId);
            }
            deleteMessageDone(true, null);
        } catch (RestClientException e) {
            log.error("deleteMessageInBackground : FAILED", e);
            deleteMessageDone(false, getString(R.string.err_delete_messages_failed));
        }
    }

    @UiThread
    void deleteMessageDone(boolean isOk, String message) {
        if (isOk) {
            getUpdateMessagesAndResumeUpdateTimer();
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
        fileUploadTypeDialog.show(getFragmentManager(), DIALOG_TAG);
    }

    public void onEvent(RequestFileUploadEvent event) {
        Intent intent = null;
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                log.info("RequestFileUploadEvent : from gallery");
                // Gallery
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                log.info("RequestFileUploadEvent : from explorer");
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
                    log.debug("onActivityResult : Photo URI : " + targetUri.toString()
                            + ", FilePath : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra("GetPath");
                    realFilePath = path + File.separator + data.getStringExtra("GetFileName");
                    log.debug("onActivityResult : from Explorer : " + realFilePath);
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_FILE_DETAIL_REFRESH:
                log.info("onActivityResult : Come from FileDetailActivity");
                // 파일 상세 Activity에서 넘어온 경우, 댓글이 달렸을 수도 있으니 바로 업데이트한다.
                getUpdateMessagesWithoutResumingUpdateTimer();
                break;
            default:
                break;
        }
    }

    // File Upload 대화상자 보여주기
    void showFileUploadDialog(String realFilePath) {
        DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, entityId);
        newFragment.show(getFragmentManager(), DIALOG_TAG);
    }

    // File Upload 확인 이벤트 획득
    public void onEvent(ConfirmFileUploadEvent event) {
        pauseUpdateTimer();

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.file_uploading)+ " " + event.realFilePath);
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
            ColoredToast.show(mContext, getString(R.string.upload_file_succeed));
        } else {
            log.error("uploadFileDone: FAILED", exception);
            ColoredToast.showError(mContext, getString(R.string.err_upload_file_failed));
        }
        getUpdateMessagesAndResumeUpdateTimer();
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
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        EventBus.getDefault().postSticky(new StickyEntityManager(mEntityManager));
    }

    /************************************************************
     * Channel, PrivateGroup Leave
     ************************************************************/

    @Background
    public void leaveEntityInBackground() {
        try {
            tossRestClient.setHeader("Authorization", mMyToken);
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                tossRestClient.leaveChannel(entityId);
            } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
                tossRestClient.leaveGroup(entityId);
            }
            leaveEntitySucceed();
        } catch (RestClientException e) {
            log.error("fail to leave cdp");
            leaveEntityFailed("탈퇴에 실패했습니다.");
        } catch (Exception e) {
            log.error("fail to leave cdp");
            leaveEntityFailed("탈퇴에 실패했습니다.");
        }
    }

    @UiThread
    public void leaveEntitySucceed() {
        finish();
    }

    @UiThread
    public void leaveEntityFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }

    /************************************************************
     * Channel, PrivateGroup 수정
     ************************************************************/
    private void modifyEntity() {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_CDP
                , entityType
                , entityId
                , entityName);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * 수정 이벤트 획득 from EditTextDialogFragment
     */
    public void onEvent(ConfirmModifyCdpEvent event) {
        modifyEntity(event);
    }

    @UiThread
    void modifyEntity(ConfirmModifyCdpEvent event) {
        modifyEntityInBackground(event);
    }

    @Background
    void modifyEntityInBackground(ConfirmModifyCdpEvent event) {
        ReqCreateCdp channel = new ReqCreateCdp();
        channel.name = event.inputName;

        try {
            tossRestClient.setHeader("Authorization", mMyToken);
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                tossRestClient.modifyChannel(channel, entityId);
            } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
                tossRestClient.modifyGroup(channel, entityId);
            }
            modifyEntitySucceed(event.inputName);
        } catch (RestClientException e) {
            log.error("modify failed", e);
            modifyEntityFailed("수정 실패");
        }
    }

    @UiThread
    void modifyEntitySucceed(String changedEntityName) {
        entityName = changedEntityName;
        getActionBar().setTitle(changedEntityName);
    }

    @UiThread
    void modifyEntityFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
    }

    /************************************************************
     * Channel, PrivateGroup 삭제
     ************************************************************/

    @Background
    void deleteEntityInBackground() {
        try {
            tossRestClient.setHeader("Authorization", mMyToken);
            if (entityType == JandiConstants.TYPE_CHANNEL) {
                tossRestClient.deleteChannel(entityId);
            } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
                tossRestClient.deleteGroup(entityId);
            }
            log.debug("delete success");
            deleteEntitySucceed();
        } catch (RestClientException e) {
            log.error("delete failed");
            deleteEntityFailed("삭제에 실패했습니다.");
        }
    }

    @UiThread
    public void deleteEntitySucceed() {
        finish();
    }

    @UiThread
    public void deleteEntityFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }

    /************************************************************
     * Channel, PrivateGroup Invite
     ************************************************************/
    public void inviteMembersToEntity() {
        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers
                = mEntityManager.getUnjoinedMembersOfEntity(entityId, entityType);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(mContext, "이미 모든 사용자가 가입되어 있습니다.");
            return;
        }

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(this, unjoinedMembers);
        lv.setAdapter(adapter);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setIcon(android.R.drawable.ic_menu_agenda);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_cdp_invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Integer> selectedCdp = adapter.getSelectedUserIds();
                for (int item : selectedCdp) {
                    log.debug("CDP ID, " + item + " is Selected");
                }
                inviteInBackground(entityType, entityId, selectedCdp);
            }
        });
        dialog.show();

    }

    @Background
    public void inviteInBackground(int cdpType, int cdpId, List<Integer> invitedUsers) {
        ResCommon res = null;
        try {
            tossRestClient.setHeader("Authorization", mMyToken);
            ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
            if (cdpType == JandiConstants.TYPE_CHANNEL) {
                res = tossRestClient.inviteChannel(cdpId, reqInviteUsers);
            } else if (cdpType == JandiConstants.TYPE_PRIVATE_GROUP) {
                res = tossRestClient.inviteGroup(cdpId, reqInviteUsers);
            }
            inviteSucceed(invitedUsers.size() + "명의 사용자를 초대했습니다.");
        } catch (RestClientException e) {
            log.error("fail to invite cdp");
            inviteFailed("초대에 실패했습니다.");
        } catch (Exception e) {
            log.error("fail to invite cdp");
            inviteFailed("초대에 실패했습니다.");
        }
    }

    @UiThread
    public void inviteSucceed(String message) {
        ColoredToast.show(mContext, message);
    }

    @UiThread
    public void inviteFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }
}
