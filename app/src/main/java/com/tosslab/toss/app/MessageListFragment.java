package com.tosslab.toss.app;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.navigation.MessageItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.TossRestPgMessages;
import com.tosslab.toss.app.network.entities.TossRestResId;
import com.tosslab.toss.app.network.entities.TossRestSendingMessage;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.RestClientException;

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
     * Navigation Panel 에서 선택한 Channel, Member or PG 정보
     * @param event
     */
    public void onEvent(ChooseNaviActionEvent event) {
        mCurrentNavType = event.type;
        refresh(mCurrentNavType, event.id, event.userId);
    }

    @UiThread
    public void refresh(int type, int id, String userId) {
        mFirstItemId = -1;
        messageItemListAdapter.clearAdapter();
        messageItemListAdapter.notifyDataSetChanged();
        getMessages(type, id, userId);
    }

    /**
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     * @param type
     * @param id
     * @param userId
     */
    @Background
    public void getMessages(int type, int id, String userId) {
        mDoLoading = true;
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
                refreshListAdapter();
            } catch (RestClientException e) {
                Log.e(TAG, "Get Fail", e);
            }
            Log.e(TAG, "Get Success");
        }
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
                refresh(mCurrentNavType, 0, null);
            } catch (RestClientException e) {
                Log.e(TAG, "Send Fail", e);
            }
            Log.e(TAG, "Send Success");
        }
    }
}
