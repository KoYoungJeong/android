package com.tosslab.toss.app;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.toss.app.events.SelectCdpItemEvent;
import com.tosslab.toss.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.toss.app.events.ConfirmModifyMessageEvent;
import com.tosslab.toss.app.events.ReqModifyMessageEvent;
import com.tosslab.toss.app.navigation.MessageItem;
import com.tosslab.toss.app.navigation.MessageItemListAdapter;
import com.tosslab.toss.app.network.MessageManipulator;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.network.models.RestFileUploadResponse;
import com.tosslab.toss.app.utils.EditTextAlertDialogFragment;
import com.tosslab.toss.app.utils.ManipulateMessageAlertDialog;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EFragment(R.layout.fragment_main)
public class MessageListFragment extends BaseFragment {
    private static final String TAG = "MessageListFragment";
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
                    Log.e(TAG, "Loading");
                    getMessages();
                }

            }
        });

        // 초기에 기본으로 보여질 Message
        // TODO : 현재에는 0번 Private Group
        mFirstItemId = -1;
        getMessages();
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
//        mTimer.cancel();
        super.onDestroy();
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
        refreshAll();
    }

    @UiThread
    public void refreshAll() {
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

            messageItemListAdapter.retrieveMessageItem(restResMessages);
            Log.e(TAG, "Get Success");
            getMessagesEnd();
        } catch (RestClientException e) {
            Log.e(TAG, "Get Fail", e);
        }
    }

    @UiThread
    public void getMessagesEnd() {
        mProgressWheel.dismiss();
        refreshListAdapter();
    }

    @UiThread
    void refreshListAdapter() {
        messageItemListAdapter.notifyDataSetChanged();
        mDoLoading = false;
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
            Log.e(TAG, "Send Success");
        } catch (RestClientException e) {
            Log.e(TAG, "Send Fail", e);
        }

        sendMessageDone();
    }

    @UiThread
    public void sendMessageDone() {
        // TODO : Update 호출
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
        Log.e(TAG, "Long Clicked");
        showDialog(item);
    }

    void showDialog(MessageItem item) {
        DialogFragment newFragment = ManipulateMessageAlertDialog.newInstance(item);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // Message 수정 이벤트 획득
    public void onEvent(ReqModifyMessageEvent event) {
        Log.e(TAG, "Edit Message : " + event.messageId);
        DialogFragment newFragment = EditTextAlertDialogFragment.newInstance(event.messageId
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
            Log.e(TAG, "Modify Success");
        } catch (RestClientException e) {
            Log.e(TAG, "Modify Fail", e);
        }

        modifyMessageDone();
    }

    @UiThread
    void modifyMessageDone() {
        mProgressWheel.dismiss();
        refreshAll();
    }

    /************************************************************
     * Message 삭제
     ************************************************************/

    // Message 삭제 이벤트 획득
    public void onEvent(ConfirmDeleteMessageEvent event) {
        Log.e(TAG, "Delete message :" + event.messageId);
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
        } catch (RestClientException e) {
            Log.e(TAG, "Delete Fail", e);
        }
        deleteMessageDone();
    }

    @UiThread
    void deleteMessageDone() {
        refreshAll();
    }

    /************************************************************
     * 파일 업로드
     * TODO : 현재는 Image Upload 만...
     ************************************************************/

    @Click(R.id.btn_upload_file)
    void uploadFile() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            Log.e(TAG, "Get Photo from URI : " + targetUri.toString());
            String realFilePath = getRealPathFromUri(targetUri);
            uploadFileInBackground(realFilePath);
        }
    }

    @Background
    void uploadFileInBackground(String fileUri) {
        // Upload 대상 파일 지정
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("userFile", new FileSystemResource(new File(fileUri)));

//        // Authorization Header 지정
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.set("Authorization", myToken);
//        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        // Create a new RestTemplate instance
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Add the Jackson and String message converters
//        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
//        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
//        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
//
//        HttpEntity< MultiValueMap<String, Object>> requestEntity
//                = new HttpEntity<MultiValueMap<String, Object>>(parts, requestHeaders);

        try {
//            RestFileUploadResponse response = restTemplate.postForObject("https://192.168.0.11:3000/inner-api/file",
//                    requestEntity, RestFileUploadResponse.class);
            RestFileUploadResponse response = tossRestClient.uploadFile(parts);
            Log.d(TAG, "Returned" + response.id);
        } catch (RestClientException e) {
            Log.e(TAG, "Error : " + e);
        } catch (ClassCastException e) {
            Log.e(TAG, "Error : " + e);
        }


    }

    // TODO : Poor Implementation
    private String getRealPathFromUri(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
