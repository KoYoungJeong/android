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
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.adapter.JoinableTopicListAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.component.DaggerJoinableTopicListComponent;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.model.UnjoinTopicDialog;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.module.JoinableTopicListModule;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.presenter.JoinableTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.view.JoinableTopicDataView;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.view.JoinableTopicListView;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 8. 30..
 */
public class JoinableTopicListActivity extends BaseAppCompatActivity
        implements JoinableTopicListView {

    @Bind(R.id.lv_joinable_topics)
    RecyclerView lvJoinableTopics;

    @Inject
    JoinableTopicListPresenter mainTopicListPresenter;

    @Inject
    JoinableTopicDataView joinableTopicDataView;

    private ProgressWheel progressWheel;

    private boolean isForeground = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JoinableTopicListAdapter adapter = new JoinableTopicListAdapter(getApplicationContext());
        DaggerJoinableTopicListComponent.builder()
                .joinableTopicListModule(new JoinableTopicListModule(this, adapter))
                .build()
                .inject(this);

        setContentView(R.layout.activity_joinable_topic_list);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        progressWheel = new ProgressWheel(this);

        setupActionBar();

        initJoinableTopicListView(adapter);

        mainTopicListPresenter.onInitJoinableTopics();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.BrowseOtherTopics);
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

    void initJoinableTopicListView(final JoinableTopicListAdapter joinableTopicListAdapter) {
        lvJoinableTopics.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lvJoinableTopics.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        lvJoinableTopics.setAdapter(joinableTopicListAdapter);

        joinableTopicDataView.setOnTopicClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onTopicClick(position);
        });

        joinableTopicDataView.setOnTopicLongClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onTopicLongClick(position);
            return true;
        });
    }

    @Override
    public void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId,
                                      long lastReadLinkId) {
        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .lastReadLinkId(lastReadLinkId)
                .isFavorite(starred)
                .start();

        finish();
    }

    @Override
    public void showUnjoinDialog(final Topic item) {

        UnjoinTopicDialog dialog = UnjoinTopicDialog.instantiate(item);
        dialog.setOnJoinClickListener(
                (dialog1, which) -> {
                    mainTopicListPresenter.onJoinTopic(item);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.JoinTopic);
                });
        dialog.show(getSupportFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.ViewTopicInfo);

    }

    @Override
    public void notifyDataSetChanged() {
        joinableTopicDataView.notifyDataSetChanged();
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        progressWheel.show();
    }

    @Override
    public void showToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showHasNoTopicToJoinErrorToast() {
        ColoredToast.showError(R.string.jandi_no_topic_to_join);
    }

    @Override
    public void showJoinToTopicErrorToast() {
        ColoredToast.showError(R.string.err_entity_join);
    }

    @Override
    public void showJoinToTopicToast(String topicName) {
        ColoredToast.show(getString(R.string.jandi_message_join_entity, topicName));
    }

    public void onEvent(SocketTopicPushEvent event) {
        if (!isForeground) {
            return;
        }
        mainTopicListPresenter.onInitJoinableTopics();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(getString(R.string.jandi_browse_other_topics));
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

    @OnTextChanged(R.id.et_joinable_topic_list_search)
    void onSearchTopic(CharSequence query) {
        mainTopicListPresenter.onSearchTopic(query);
    }

}
