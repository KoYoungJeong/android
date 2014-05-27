package com.tosslab.toss.app;

import android.util.Log;
import android.widget.ListView;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.events.RefreshCdpListEvent;
import com.tosslab.toss.app.navigation.CdpItem;
import com.tosslab.toss.app.navigation.CdpItemListAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_navigation_drawer)
public class NavigationDrawerFragment extends BaseFragment {

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
     * MainActivity 에서 리플레쉬 명령을 받으면 List 갱신을 수행
     * @param event
     */
    public void onEvent(RefreshCdpListEvent event) {
        channelListAdapter.retrieveCdpItemsFromChannels(event.mInfos.channels);
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
}
