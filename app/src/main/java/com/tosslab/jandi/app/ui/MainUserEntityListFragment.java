package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.events.ReadyToRetrieveUserList;
import com.tosslab.jandi.app.events.RetrieveUserList;
import com.tosslab.jandi.app.events.StickyEntityManager;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.UserEntityItemListAdapter;

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
@EFragment(R.layout.fragment_main_user_list)
public class MainUserEntityListFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(MainEntityListFragment.class);

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

    @ItemClick
    void main_list_usersItemClicked(final FormattedEntity user) {
        // 알람 카운트가 있던 아이템이면 이를 0으로 바꾼다.
        user.alarmCount = 0;
        mUserListAdapter.notifyDataSetChanged();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity instanceof MainTabActivity_) {
                    EntityManager entityManager = ((MainTabActivity_)activity).getEntityManager();

                    MessageListActivity_.intent(mContext)
                            .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                            .entityId(user.getEntity().id)
                            .entityName(user.getEntity().name)
                            .isMyEntity(false)
                            .start();

                    EventBus.getDefault().postSticky(new StickyEntityManager(entityManager));
                }
            }
        }, 250);
    }
}
