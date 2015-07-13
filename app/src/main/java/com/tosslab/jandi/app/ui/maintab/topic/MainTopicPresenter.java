package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicPresenter {

    @ViewById(R.id.list_main_topic)
    RecyclerView topicListView;

    TopicRecyclerAdapter topicListAdapter;
    private ProgressWheel progressWheel;

    void initObject(Activity activity) {
        progressWheel = new ProgressWheel(activity);
        topicListAdapter = new TopicRecyclerAdapter(activity.getApplicationContext());
    }

    @AfterViews
    void initViews() {
        topicListView.setAdapter(topicListAdapter);
    }

    @UiThread
    public void moveToMessageActivity(Context context,
                                      final int entityId, final int entityType,
                                      final boolean isStarred, int teamId) {
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

    @UiThread
    public void showToast(Context context, String message) {
        ColoredToast.show(context, message);
    }

    @UiThread
    public void showErrorToast(Context context, String message) {
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

    @UiThread
    public void refreshList() {
        topicListAdapter.notifyDataSetChanged();
    }
}
