package com.tosslab.toss.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.TossRestPgMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
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

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
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
        getMessages(event);
    }

    /**
     * 선택한 Channel, Member or PG 에 대한 Message 리스트 획득 (from 서버)
     * @param event
     */
    @Background
    public void getMessages(ChooseNaviActionEvent event) {
        if (event.type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            TossRestPgMessages restPgMessages = null;
            try {
                tossRestClient.setHeader("Authorization", myToken);
                restPgMessages = tossRestClient.getGroupMessages(0, -1, 10);
            } catch (RestClientException e) {
                Log.e("HI", "Get Fail", e);
            }
            Log.e("HI", "Success");
        }
    }
}
