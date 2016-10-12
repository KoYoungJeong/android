package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter;

import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.Date;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public interface PollListPresenter {

    String TAG = PollListPresenter.class.getSimpleName();

    void onInitializePollList();

    void onLoadMorePollList(Date lastItemFinishedAt);

    void onPollDataChanged(SocketPollEvent.Type type, Poll poll);

    void removeOfTopics(long topicId);

    interface View {

        void showProgress();

        void dismissProgress();

        void showUnExpectedErrorToast();

        void notifyDataSetChanged();

        void setHasMore(boolean hasMore);

        void showLoadMoreProgress();

        void dismissLoadMoreProgress();
    }

}
