package com.tosslab.toss.app;

import android.app.Activity;
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
import android.widget.TextView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.navigation.MessageItem;
import com.tosslab.toss.app.navigation.MessageItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.TossRestPgMessages;
import com.tosslab.toss.app.network.entities.TossRestResId;
import com.tosslab.toss.app.network.entities.TossRestSendingMessage;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.Charset;

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
    boolean mIsFirstMessage = false;
    boolean mDoLoading = true;

    // 현재 선택한 것 : Channel, Direct Message or Private Group
    int mCurrentNavType = ChooseNaviActionEvent.TYPE_PRIVATE_GROUP;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
    }

    @AfterViews
    void bindAdapter() {
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();

        listMessages.setAdapter(messageItemListAdapter);

        // 스크롤의 맨 끝으로 내려갔을 경우 (리스트 업데이트)
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
                    Log.e(TAG, "Loading");
                    getMessages(mCurrentNavType, 0, null);
                }

            }
        });

        // 초기에 기본으로 보여질 Message
        // TODO : 현재에는 0번 Private Group
        mFirstItemId = -1;
        getMessages(mCurrentNavType, 0, null);
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * Message Item의 Long Click 시, 팝업 메뉴 활성화
     * @param item
     */
    @ItemLongClick
    void list_messagesItemLongClicked(MessageItem item) {
        Log.e(TAG, "Long Clicked");
    }

    /**
     * Navigation Panel 에서 선택한 Channel, Member or PG 정보
     * @param event
     */
    public void onEvent(ChooseNaviActionEvent event) {
        mCurrentNavType = event.type;
        refreshAll(mCurrentNavType, event.id, event.userId);
    }

    @UiThread
    public void refreshAll(int type, int id, String userId) {
        mFirstItemId = -1;
        messageItemListAdapter.clearAdapter();
        getMessages(type, id, userId);
    }

    /**
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     * @param type
     * @param id
     * @param userId
     */
    @UiThread
    public void getMessages(int type, int id, String userId) {
        mDoLoading = true;
        mProgressWheel.show();
        getMessagesInBackground(type, id, userId);
    }

    @Background
    public void getMessagesInBackground(int type, int id, String userId) {
        if (type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            TossRestPgMessages restPgMessages = null;
            try {
                tossRestClient.setHeader("Authorization", myToken);
                restPgMessages = tossRestClient.getGroupMessages(id, mFirstItemId, 10);

                // 만일 지금 받은 메시지가 끝이라면 이를 저장함.
                mIsFirstMessage = restPgMessages.isFirst;
                // 지금 받은 리스트의 첫번째 entity의 ID를 저장한다.
                mFirstItemId = restPgMessages.firstIdOfReceviedList;

                messageItemListAdapter.retrievePgMessageItem(restPgMessages);
                getMessagesEnd();
            } catch (RestClientException e) {
                Log.e(TAG, "Get Fail", e);
            }
            Log.e(TAG, "Get Success");
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

    /**
     * Message 전송
     */
    @Click(R.id.btn_send_comment)
    void sendMessage() {
        String message = etMessage.getText().toString();
        hideSoftKeyboard();

        if (message.length() > 0) {
            sendMessage(mCurrentNavType, message);
        }
    }

    @UiThread
    void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(etMessage.getWindowToken(),0);
        etMessage.setText("");
    }

    @Background
    public void sendMessage(int type, String message) {
        if (type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            TossRestSendingMessage sendingMessage = new TossRestSendingMessage();
            sendingMessage.type = "string";
            sendingMessage.content = message;

            TossRestResId restResId = null;
            try {
                tossRestClient.setHeader("Authorization", myToken);
                restResId = tossRestClient.sendGroupMessage(sendingMessage, 0);
                refreshAll(mCurrentNavType, 0, null);
            } catch (RestClientException e) {
                Log.e(TAG, "Send Fail", e);
            }
            Log.e(TAG, "Send Success");
        }
    }

    /**
     * 파일 업로드
     * TODO : 현재는 Image Upload 만...
     */
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

        // Authorization Header 지정
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", myToken);

        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Add the Jackson and String message converters
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        HttpEntity< MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<MultiValueMap<String, Object>>(parts, requestHeaders);

        try {
            String response = restTemplate.postForObject("https://192.168.0.11:3000/inner-api/file",
                    requestEntity, String.class);
            Log.d(TAG, response);
        } catch (RestClientException e) {
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
