package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.model.UnjoinTopicDialog;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.presenter.JoinableTopicListPresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tee on 15. 8. 30..
 */

@EActivity(R.layout.activity_unjoined_topic_list)
public class JoinableTopicListActivity extends BaseAppCompatActivity
        implements JoinableTopicListPresenter.View {

    @ViewById(R.id.rv_unjoined_topic)
    RecyclerView lvUnjoinedTopic;

    @Bean
    JoinableTopicListPresenter mainTopicListPresenter;

    private TopicRecyclerAdapter adapter;
    private ProgressWheel progressWheel;

    private boolean isForeground = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
    }

    @Override
    public void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @AfterInject
    void initObject() {

        progressWheel = new ProgressWheel(this);
        adapter = new TopicRecyclerAdapter(getApplicationContext());
        adapter.setHasStableIds(true);

        mainTopicListPresenter.setView(this);
    }

    @AfterViews
    void initView() {

        setupActionBar();

        lvUnjoinedTopic.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lvUnjoinedTopic.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        lvUnjoinedTopic.setAdapter(adapter);

        adapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onItemClick(adapter, position);
        });

        adapter.setOnRecyclerItemLongClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onItemLongClick(adapter, position);
            return true;
        });

        mainTopicListPresenter.onInitUnjoinedTopics();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.BrowseOtherTopics);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setEntities(Observable<Topic> unjoinEntities) {
        adapter.clear();
        unjoinEntities
                .toSortedList((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                .subscribe(entities -> {
                    if (entities.size() == 0) {
                        JoinableTopicListActivity.this.finish();
                        showToast(getString(R.string.jandi_no_topic_to_join));
                    } else {
                        adapter.addAll(entities);
                    }
                });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId) {
        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .lastMarker(markerLinkId)
                .isFavorite(starred)
                .startForResult(MainTabActivity.REQ_START_MESSAGE);
        finish();
    }

    @Override
    public void showUnjoinDialog(Topic item) {

        UnjoinTopicDialog dialog = UnjoinTopicDialog.instantiate(item);
        dialog.setOnJoinClickListener(
                (dialog1, which) -> {
                    mainTopicListPresenter.onJoinTopic(getApplicationContext(), item);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.JoinTopic);
                });
        dialog.show(getSupportFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.ViewTopicInfo);

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDatasetChanged() {
        adapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        progressWheel.show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showToast(String message) {
        ColoredToast.show(getApplicationContext(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(getApplicationContext(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void onEvent(SocketTopicPushEvent event) {
        if (!isForeground) {
            return;
        }
        mainTopicListPresenter.onInitUnjoinedTopics();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle("Join topic");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
