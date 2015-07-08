package com.tosslab.jandi.app.ui.maintab.topic;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerStickyHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicView {

    @RootContext
    Context context;

    @ViewById(R.id.list_main_topic)
    RecyclerView rvTopic;

    @ViewById(R.id.btn_main_topic_fab)
    View btnFA;

    TopicRecyclerAdapter topicListAdapter;
    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(context);
        progressWheel.init();
        topicListAdapter = new TopicRecyclerAdapter(context);
        topicListAdapter.setHasStableIds(true);
    }

    @AfterViews
    void initViews() {

        rvTopic.setLayoutManager(new LinearLayoutManager(context));
        rvTopic.addItemDecoration(new SimpleDividerItemDecoration(context));

        TopicRecyclerStickyHeaderAdapter headerAdapter =
                new TopicRecyclerStickyHeaderAdapter(context, topicListAdapter);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(topicListAdapter)
                .setRecyclerView(rvTopic)
                .setStickyHeadersAdapter(headerAdapter, false)
                .build();
        rvTopic.addItemDecoration(stickyHeadersItemDecoration);
        rvTopic.setAdapter(topicListAdapter);

//        FAButtonUtil.setFAButtonController(rvTopic, btnFA);

    }

    @UiThread
    public void moveToMessageActivity(final int entityId, final int entityType, final boolean isStarred, int teamId) {
        MessageListV2Activity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .isFavorite(isStarred)
                .start();
    }

    public void setEntities(Observable<Topic> joinTopics, Observable<Topic> unjoinTopics) {

        topicListAdapter.clear();

        joinTopics
                .toSortedList((lhs, rhs) -> {

                    if (lhs.isStarred() && rhs.isStarred()) {
                        if (lhs.isPublic() && !rhs.isPublic()) {
                            return 1;
                        } else if (!lhs.isPublic() && rhs.isPublic()) {
                            return -1;
                        } else {
                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }

                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        if (lhs.isPublic() && !rhs.isPublic()) {
                            return 1;
                        } else if (!lhs.isPublic() && rhs.isPublic()) {
                            return -1;
                        } else {
                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }
                    }

                })
                .subscribe(topicListAdapter::addAll);

        unjoinTopics.toSortedList((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                .subscribe(topicListAdapter::addAll);

        topicListAdapter.notifyDataSetChanged();

    }

    @UiThread
    public void showToast(String message) {
        ColoredToast.show(context, message);
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError(context, message);
    }

    @UiThread
    public void showProgressWheel() {
        dismissProgressWheel();
        progressWheel.show();
    }

    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public List<Topic> getJoinedTopics() {
        List<Topic> topics = topicListAdapter.getTopics();

        List<Topic> list = new ArrayList<Topic>();

        Observable.from(topics)
                .filter(topic -> topic.isJoined())
                .collect(() -> list, (topics1, topic1) -> topics1.add(topic1))
                .subscribe();

        return list;
    }

    @UiThread
    public void refreshList() {
        topicListAdapter.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnRecyclerItemClickListener onItemClickListener) {
        topicListAdapter.setOnRecyclerItemClickListener(onItemClickListener);
    }

    public void setOnItemLongClickListener(OnRecyclerItemLongClickListener onItemLongClickListener) {

        topicListAdapter.setOnRecyclerItemLongClickListener(onItemLongClickListener);
    }
}
