package com.tosslab.jandi.app.ui.maintab.topic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerStickyHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.model.UnjoinTopicDialog;
import com.tosslab.jandi.app.ui.maintab.topic.presenter.MainTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.topic.presenter.MainTopicListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FAButtonUtil;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_topic_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainTopicListFragment extends Fragment implements MainTopicListPresenter.View {

    @Bean(MainTopicListPresenterImpl.class)
    MainTopicListPresenter mainTopicListPresenter;

    @FragmentArg
    int selectedEntity = -1;

    @ViewById(R.id.list_main_topic)
    RecyclerView rvTopic;

    @ViewById(R.id.btn_main_topic_fab)
    View btnFA;

    TopicRecyclerAdapter topicListAdapter;
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
        btnFA.setAnimation(null);
        btnFA.setVisibility(View.VISIBLE);

        topicListAdapter.startAnimation();
        mainTopicListPresenter.onRefreshTopicList();
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

        progressWheel = new ProgressWheel(getActivity());
        topicListAdapter = new TopicRecyclerAdapter(getActivity());
        topicListAdapter.setHasStableIds(true);

        mainTopicListPresenter.setView(this);
        mainTopicListPresenter.onInitTopics(getActivity());
    }

    @AfterViews
    void initView() {

        rvTopic.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvTopic.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        TopicRecyclerStickyHeaderAdapter headerAdapter =
                new TopicRecyclerStickyHeaderAdapter(getActivity(), topicListAdapter);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(topicListAdapter)
                .setRecyclerView(rvTopic)
                .setStickyHeadersAdapter(headerAdapter, false)
                .build();
        rvTopic.addItemDecoration(stickyHeadersItemDecoration);
        rvTopic.setAdapter(topicListAdapter);

        FAButtonUtil.setFAButtonController(rvTopic, btnFA);


        topicListAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onItemClick(getActivity(), adapter, position);
        });

        topicListAdapter.setOnRecyclerItemLongClickListener((view, adapter, position) -> {
            mainTopicListPresenter.onItemLongClick(getActivity(), adapter, position);
            return true;
        });

        mainTopicListPresenter.onFocusTopic(selectedEntity);

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEntities(Observable<Topic> joinTopics, Observable<Topic> unjoinTopics) {

        topicListAdapter.clear();

        joinTopics
                .toSortedList((lhs, rhs) -> {

                    if (lhs.isStarred() && rhs.isStarred()) {
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    }

                })
                .subscribe(topicListAdapter::addAll);

        unjoinTopics.toSortedList((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                .subscribe(topicListAdapter::addAll);

        topicListAdapter.notifyDataSetChanged();
    }

    @Override
    public List<Topic> getJoinedTopics() {
        List<Topic> topics = topicListAdapter.getTopics();

        List<Topic> list = new ArrayList<Topic>();

        Observable.from(topics)
                .filter(topic -> topic.isJoined())
                .collect(() -> list, (topics1, topic1) -> topics1.add(topic1))
                .subscribe();

        return list;
    }

    @Override
    public void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId) {
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .isFavorite(starred)
                .startForResult(MainTabActivity.REQ_START_MESSAGE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setSelectedItem(int selectedEntity) {
        this.selectedEntity = selectedEntity;
        topicListAdapter.setSelectedEntity(selectedEntity);
    }

    @Override
    public void showUnjoinDialog(Topic item) {

        UnjoinTopicDialog dialog = UnjoinTopicDialog.instantiate(item);
        dialog.setOnJoinClickListener(
                (dialog1, which) -> mainTopicListPresenter.onJoinTopic(getActivity(), item));
        dialog.show(getFragmentManager(), "dialog");

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDatasetChanged() {
        topicListAdapter.notifyDataSetChanged();
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
        ColoredToast.show(getActivity(), message);

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(getActivity(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showEntityMenuDialog(Topic item) {
        EntityMenuDialogFragment_.builder().entityId(item.getEntityId())
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @Override
    public void scrollToPosition(int selectedEntityPosition) {

        if (selectedEntityPosition > 0) {
            rvTopic.getLayoutManager().scrollToPosition(selectedEntityPosition - 1);
        }
    }

    @Click(R.id.btn_main_topic_fab)
    void onAddTopicClick() {
        TopicCreateActivity_
                .intent(MainTopicListFragment.this)
                .start();

        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
    }


    public void onEvent(MainSelectTopicEvent event) {
        selectedEntity = event.getSelectedEntity();
        setSelectedItem(selectedEntity);
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();
    }

    public void onEventMainThread(RetrieveTopicListEvent event) {

        mainTopicListPresenter.onInitTopics(getActivity());
        setSelectedItem(selectedEntity);

    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "chat")) {
            return;
        }

        mainTopicListPresenter.onNewMessage(event);

    }

    public void onEvent(SocketTopicPushEvent event) {
        if (!isForeground) {
            return;
        }
        mainTopicListPresenter.onInitTopics(getActivity());
    }
}
