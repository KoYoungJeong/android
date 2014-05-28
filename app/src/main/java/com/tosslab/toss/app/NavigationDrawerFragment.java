package com.tosslab.toss.app;

import android.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.events.ConfirmCreateCdpEvent;
import com.tosslab.toss.app.events.RefreshCdpListEvent;
import com.tosslab.toss.app.events.RequestCdpListEvent;
import com.tosslab.toss.app.navigation.CdpItem;
import com.tosslab.toss.app.navigation.CdpItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.entities.RestCreatePrivateGroup;
import com.tosslab.toss.app.network.entities.TossRestResId;
import com.tosslab.toss.app.utils.CreateCdpAlertDialogFragment;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_navigation_drawer)
public class NavigationDrawerFragment extends BaseFragment {
    private static final String TAG = "NavigationDrawerFragment";

    @ViewById(R.id.list_nav_channels)
    ListView listChannels;
    @ViewById(R.id.list_nav_members)
    ListView listMembers;
    @ViewById(R.id.list_nav_private_groups)
    ListView listPrivateGroups;

    @Bean
    CdpItemListAdapter channelListAdapter;
    @Bean
    CdpItemListAdapter memberListAdapter;
    @Bean
    CdpItemListAdapter privateGroupListAdapter;

    @RestService
    TossRestClient tossRestClient;

    private ProgressWheel mProgressWheel;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
    }

    @AfterViews
    void bindAdapter() {
        listChannels.setAdapter(channelListAdapter);
        listMembers.setAdapter(memberListAdapter);
        listPrivateGroups.setAdapter(privateGroupListAdapter);
    }

    @ItemClick
    void list_nav_channelsItemClicked(CdpItem cdp) {
        Log.e("HI", cdp.name + " Clicked. type is " + cdp.id);
        ChooseNaviActionEvent event = new ChooseNaviActionEvent(ChooseNaviActionEvent.TYPE_CHENNEL, cdp.id);
        EventBus.getDefault().post(event);
    }

    @ItemClick
    void list_nav_membersItemClicked(CdpItem cdp) {
        Log.e("HI", cdp.name + " Clicked. type is " + cdp.userId);
        ChooseNaviActionEvent event = new ChooseNaviActionEvent(ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE, cdp.userId);
        EventBus.getDefault().post(event);
    }

    @ItemClick
    void list_nav_private_groupsItemClicked(CdpItem cdp) {
        Log.e("HI", cdp.name + " Clicked. type is " + cdp.id);
        ChooseNaviActionEvent event = new ChooseNaviActionEvent(ChooseNaviActionEvent.TYPE_PRIVATE_GROUP, cdp.id);
        EventBus.getDefault().post(event);
    }

    /**
     * 모든 CDP 리스트를 초기화하고 서버로 다시 리스트를 요청한다.
     */
    @UiThread
    void refreshAll() {
        channelListAdapter.clearAdapter();
        memberListAdapter.clearAdapter();
        privateGroupListAdapter.clearAdapter();

        RequestCdpListEvent event = new RequestCdpListEvent();
        EventBus.getDefault().post(event);
    }

    @Click(R.id.btn_action_add_channel)
    void createChannel() {
        showDialog(0);
    }

    @Click(R.id.btn_add_private_group)
    void createPrivateGroup() {
        showDialog(2);
    }

    /**
     * MainActivity 에서 리플레쉬 명령을 받으면 List 갱신을 수행
     * @param event
     */
    public void onEvent(RefreshCdpListEvent event) {
        channelListAdapter.retrieveCdpItemsFromChannels(event.mInfos.joinChannels);
        memberListAdapter.retrieveCdpItemsFromMembers(event.mInfos.members);
        privateGroupListAdapter.retrieveCdpItemsFromPravateGroups(event.mInfos.privateGroups);
        refreshListAdapter();
    }

    @UiThread
    void refreshListAdapter() {
        channelListAdapter.notifyDataSetChanged();
        memberListAdapter.notifyDataSetChanged();
        privateGroupListAdapter.notifyDataSetChanged();
    }

    /**
     * Private Group 생성
     */
    @Background
    void requestCreatePrivateGroup(String pgName) {
        // TODO : Error 처리
        if (pgName.length() <= 0) {
            return;
        }
        RestCreatePrivateGroup restCreatePrivateGroup = new RestCreatePrivateGroup();
        restCreatePrivateGroup.name = pgName;

        TossRestResId restResId = null;
        try {
            tossRestClient.setHeader("Authorization", ((MainActivity)getActivity()).myToken);
            restResId = tossRestClient.createPrivateGroup(restCreatePrivateGroup);
            refreshAll();
            Log.e(TAG, "Create Success");
        } catch (RestClientException e) {
            Log.e(TAG, "Create Fail", e);
        }
    }

    /**
     * Event Listener로 등록
     */
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
     * Alert Dialog 관련
     */
    void showDialog(int cdpType) {
        int titleStringId = R.string.create_channel;
        // TODO : poor implemantaion
        if (cdpType == 2) {
            titleStringId = R.string.create_private_group;
        }
        DialogFragment newFragment = CreateCdpAlertDialogFragment.newInstance(titleStringId, cdpType);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(ConfirmCreateCdpEvent event) {
        if (event.cdpType == 2) {
            requestCreatePrivateGroup(event.inputName);
        }
    }
}
