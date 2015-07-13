package com.tosslab.jandi.app.ui.maintab.topic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicListAdapter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicPresenter {

    @ViewById(R.id.list_main_topic)
    ExpandableListView topicListView;

    TopicListAdapter topicListAdapter;
    private ProgressWheel progressWheel;

    void initObject(Activity activity) {
        progressWheel = new ProgressWheel(activity);
        topicListAdapter = new TopicListAdapter(activity.getApplicationContext());
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

    public void setEntities(List<FormattedEntity> joinEntities, List<FormattedEntity> unjoinEntities) {

        topicListAdapter
                .joinEntities(joinEntities)
                .unjoinEntities(unjoinEntities)
                .notifyDataSetChanged();

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

    public List<FormattedEntity> getJoinedTopics() {
        return topicListAdapter.getJoinEntities();
    }

    @UiThread
    public void refreshList() {
        topicListAdapter.notifyDataSetChanged();
    }
}
