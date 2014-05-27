package com.tosslab.toss.app;

import android.util.Log;
import android.widget.ListView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.navigation.MessageItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.TossRestPgMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
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
    @RestService
    TossRestClient tossRestClient;
    @FragmentArg
    String myToken;

    @ViewById(R.id.list_messages)
    ListView listMessages;
    @Bean
    MessageItemListAdapter messageItemListAdapter;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
    }

    @AfterViews
    void bindAdapter() {
        listMessages.setAdapter(messageItemListAdapter);
        // 초기에 기본으로 보여질 Message
        // TODO : 현재에는 0번 Private Group
        getMessages(ChooseNaviActionEvent.TYPE_PRIVATE_GROUP, 0, null);
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
        getMessages(event.type, event.id, event.userId);
    }

    /**
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     * @param type
     * @param id
     * @param userId
     */
    @Background
    public void getMessages(int type, int id, String userId) {
        if (type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            TossRestPgMessages restPgMessages = null;
            try {
                tossRestClient.setHeader("Authorization", myToken);
                restPgMessages = tossRestClient.getGroupMessages(id, -1, 15);
                messageItemListAdapter.retrievePgMessageItem(restPgMessages);
                refreshListAdapter();
            } catch (RestClientException e) {
                Log.e("HI", "Get Fail", e);
            }
            Log.e("HI", "Success");
        }
    }

    @UiThread
    void refreshListAdapter() {
        messageItemListAdapter.notifyDataSetChanged();
    }
}
