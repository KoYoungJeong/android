package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.events.ReadyToRetrieveUserList;
import com.tosslab.jandi.app.ui.events.RetrieveUserList;
import com.tosslab.jandi.app.ui.lists.UserEntityItemListAdapter;

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
@EFragment(R.layout.fragment_main_user_list)
public class MainUserListFragment  extends Fragment {
    private final Logger log = Logger.getLogger(MainChannelListFragment.class);

    @ViewById(R.id.main_list_users)
    ListView mListViewUsers;
    @Bean
    UserEntityItemListAdapter mUserListAdapter;

    private Context mContext;

    @AfterViews
    void bindAdapter() {
        mContext = getActivity();
        mListViewUsers.setAdapter(mUserListAdapter);
        EventBus.getDefault().post(new ReadyToRetrieveUserList());
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
    public void onEvent(RetrieveUserList event) {
        mUserListAdapter.retrieveList(event.users);
    }
}
