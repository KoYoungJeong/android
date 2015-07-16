package com.tosslab.jandi.app.ui.maintab.topic.presenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;

import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 16..
 */
public interface MainTopicListPresenter {

    void setView(View view);

    void onInitTopics(Context context);

    void onItemClick(Context context, RecyclerView.Adapter adapter, int position);

    void onJoinTopic(Context context, Topic topic);

    void onNewMessage(SocketMessageEvent event);

    void onItemLongClick(Context context, RecyclerView.Adapter adapter, int position);

    interface View {

        void setEntities(Observable<Topic> joinEntities, Observable<Topic> unjoinEntities);

        List<Topic> getJoinedTopics();

        void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId);

        void setSelectedItem(int selectedEntity);

        void showUnjoinDialog(Topic item);

        void notifyDatasetChanged();

        void showProgressWheel();

        void showToast(String message);

        void showErrorToast(String message);

        void dismissProgressWheel();

        void showEntityMenuDialog(Topic item);
    }
}
