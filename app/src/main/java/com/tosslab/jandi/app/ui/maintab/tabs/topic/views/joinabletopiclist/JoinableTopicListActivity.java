package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.adapter.JoinableTopicListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.component.DaggerJoinableTopicListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.module.JoinableTopicListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.presenter.JoinableTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicListView;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.TopicInfoDialog;
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

public class JoinableTopicListActivity extends BaseAppCompatActivity
        implements JoinableTopicListView {

    @Bind(R.id.et_joinable_topic_list_search)
    EditText etSearch;

    @Bind(R.id.lv_joinable_topics)
    RecyclerView lvJoinableTopics;

    @Bind(R.id.vg_joinable_topic_list_empty)
    View vgEmptySearchedTopic;
    @Bind(R.id.tv_joinable_topic_list_empty)
    TextView tvEmptySearchedTopic;
    @Bind(R.id.tv_joinable_topic_list_create_topic)
    TextView tvCreateTopic;

    @Inject
    JoinableTopicListPresenter mainTopicListPresenter;

    @Inject
    JoinableTopicDataView joinableTopicDataView;
    @Bind(R.id.btn_joinable_list_create_topic)
    View btnCreateTopic;
    private ProgressWheel progressWheel;
    private boolean isForeground = true;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JoinableTopicListAdapter adapter = new JoinableTopicListAdapter();
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

        mainTopicListPresenter.onSearchTopic(true, "");

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
        mainTopicListPresenter.stopSearchTopicQueue();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    void initJoinableTopicListView(final JoinableTopicListAdapter joinableTopicListAdapter) {
        layoutManager = new LinearLayoutManager(getBaseContext());
        lvJoinableTopics.setLayoutManager(layoutManager);
        lvJoinableTopics.addItemDecoration(new SimpleDividerItemDecoration());
        lvJoinableTopics.setAdapter(joinableTopicListAdapter);

        joinableTopicDataView.setOnTopicClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onTopicClick(position);
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
                .start();

        finish();
    }

    @Override
    public void showTopicInfoDialog(final TopicRoom item) {

        TopicInfoDialog dialog = TopicInfoDialog.instantiate(item);
        dialog.show(getSupportFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(
                AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.ViewTopicInfo);

    }

    private void setTopicInfoDialogEventListeners(TopicInfoDialog topicInfoDialog) {
        topicInfoDialog.setOnJoinClickListener((topicEntityId) -> {
            mainTopicListPresenter.onJoinTopic(topicEntityId);
            AnalyticsUtil.sendEvent(
                    AnalyticsValue.Screen.BrowseOtherTopics, AnalyticsValue.Action.JoinTopic);
        });

        topicInfoDialog.setOnDismissListener(mainTopicListPresenter::onShouldShowSelectedTopic);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof TopicInfoDialog) {
            setTopicInfoDialogEventListeners((TopicInfoDialog) fragment);
        }
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

    @Override
    public void showSelectedTopic(int position) {
        final View viewByPosition = layoutManager.findViewByPosition(position);
        if (viewByPosition == null) {
            return;
        }

        Resources resources = getResources();
        Integer colorFrom = resources.getColor(R.color.transparent);
        Integer colorTo = resources.getColor(R.color.jandi_accent_color_1f);
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(resources.getInteger(R.integer.highlight_animation_time));
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(1);
        colorAnimation.addUpdateListener(animator ->
                viewByPosition.setBackgroundColor((Integer) animator.getAnimatedValue()));

        colorAnimation.start();

    }

    @Override
    public void showEmptyQueryMessage(String query) {
        vgEmptySearchedTopic.setVisibility(View.VISIBLE);

        final String finalQuery = TextUtils.isEmpty(query) ? "" : query;

        Resources resources = JandiApplication.getContext().getResources();
        String hasNoSearchResult = resources.getString(R.string.jandi_has_no_search_result, finalQuery);
        tvEmptySearchedTopic.setText(hasNoSearchResult);

        String createNewTopic = resources.getString(R.string.jandi_create_new_topic, finalQuery);
        tvCreateTopic.setText(createNewTopic);

        btnCreateTopic.setOnClickListener(v -> {
            TopicCreateActivity_
                    .intent(this)
                    .expectTopicName(finalQuery)
                    .start();

            overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
        });
    }

    @Override
    public void hideEmptyQueryMessage() {
        vgEmptySearchedTopic.setVisibility(View.GONE);
    }

    public void onEvent(RetrieveTopicListEvent event) {
        if (!isForeground) {
            return;
        }
        mainTopicListPresenter.onSearchTopic(false, etSearch.getText());
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
        mainTopicListPresenter.onSearchTopic(false, query);
    }

}
