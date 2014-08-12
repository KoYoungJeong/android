package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Handler;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveChannelList;
import com.tosslab.jandi.app.ui.events.RetrieveChannelList;
import com.tosslab.jandi.app.ui.lists.ChannelEntityItemListAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
@EFragment(R.layout.fragment_main_channel_list)
public class MainChannelListFragment extends Fragment {
    private final Logger log = Logger.getLogger(MainChannelListFragment.class);

    @ViewById(R.id.main_list_channels)
    ListView mListViewChannels;
    @Bean
    ChannelEntityItemListAdapter mChannelListAdapter;

    private Context mContext;

    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        mListViewChannels.setAdapter(mChannelListAdapter);
        EventBus.getDefault().post(new ReadyToRetrieveChannelList());
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
    public void onEvent(RetrieveChannelList event) {
        mChannelListAdapter.retrieveList(event.joinedChannels, event.unJoinedChannels);
    }

    @ItemClick
    void main_list_channelsItemClicked(final ResLeftSideMenu.Channel channel) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageActivity_.intent(mContext)
                        .entityType(JandiConstants.TYPE_CHANNEL)
                        .entityId(channel.id)
                        .entityName(channel.name)
                        .start();
            }
        }, 250);
    }
}
