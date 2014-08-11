package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.ui.events.ReadyToRetrievePrivateGroupList;
import com.tosslab.jandi.app.ui.events.RetrieveChannelList;
import com.tosslab.jandi.app.ui.events.RetrievePrivateGroupList;
import com.tosslab.jandi.app.ui.lists.PrivateGroupEntityItemListAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EFragment(R.layout.fragment_main_channel_list)
public class MainPrivateGroupListFragment extends Fragment {
    private final Logger log = Logger.getLogger(MainPrivateGroupListFragment.class);

    @ViewById(R.id.main_list_channels)
    ListView mListViewPrivateGroups;
    @Bean
    PrivateGroupEntityItemListAdapter mPrivateGroupListAdapter;

    private Context mContext;

    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        mListViewPrivateGroups.setAdapter(mPrivateGroupListAdapter);
        EventBus.getDefault().post(new ReadyToRetrievePrivateGroupList());
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
     * Event from MainTabActivity
     * @param event
     */
    public void onEvent(RetrievePrivateGroupList event) {
        mPrivateGroupListAdapter.retrieveList(event.privateGroups);
    }
}
