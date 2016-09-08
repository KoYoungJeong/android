package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.PollListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.view.PollListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.component.DaggerPollListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.module.PollListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter.PollListPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollListFragment extends Fragment implements PollListPresenter.View, ListScroller {

    @Inject
    PollListPresenter pollListPresenter;
    @Inject
    PollListDataView pollListDataView;

    @Bind(R.id.lv_poll_list)
    RecyclerView lvPollList;
    @Bind(R.id.vg_poll_list_empty)
    View vgEmptyPollList;
    @Bind(R.id.progress_poll_list)
    ProgressBar progressBar;
    @Bind(R.id.progress_poll_list_more_loading)
    ProgressBar pbMoreLoading;

    private MorePollListRequestHandler moreRequestHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_poll_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PollListAdapter pollListAdapter = new PollListAdapter();
        injectComponent(pollListAdapter);

        ButterKnife.bind(this, view);

        initMoreLoadingProgress();

        initPollListView(pollListAdapter);

        initPollList();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    private void initPollList() {
        pollListPresenter.onInitializePollList();
    }

    private void initPollListView(PollListAdapter pollListAdapter) {
        moreRequestHandler = new MorePollListRequestHandler();
        pollListAdapter.setOnLoadMoreCallback(moreRequestHandler);
        pollListAdapter.setOnPollClickListener(poll -> {
            PollDetailActivity.start(getActivity(), poll.getId());

            if ("created".equals(poll.getStatus())) {
                sendAnalyticsEvent(AnalyticsValue.Action.ViewPollDetail, AnalyticsValue.Label.ongoing);
            } else {
                sendAnalyticsEvent(AnalyticsValue.Action.ViewPollDetail, AnalyticsValue.Label.completed);
            }

        });

        lvPollList.setLayoutManager(new LinearLayoutManager(getContext()));
        lvPollList.setAdapter(pollListAdapter);
    }

    private void injectComponent(PollListAdapter adapter) {
        DaggerPollListComponent.builder()
                .pollListModule(new PollListModule(this, adapter))
                .build()
                .inject(this);
    }

    private void initMoreLoadingProgress() {
        pbMoreLoading.post(() -> {
            ViewGroup.LayoutParams layoutParams = pbMoreLoading.getLayoutParams();
            int bottomMargin =
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
            int translationY = pbMoreLoading.getMeasuredHeight() + bottomMargin;
            pbMoreLoading.animate().translationY(translationY);
        });
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showUnExpectedErrorToast() {
        ColoredToast.showError(R.string.jandi_err_unexpected);
    }

    @Override
    public void notifyDataSetChanged() {
        pollListDataView.notifyDataSetChanged();
    }

    @Override
    public void showEmptyView() {
        vgEmptyPollList.setVisibility(View.VISIBLE);
    }

    @Override
    public void setHasMore(boolean hasMore) {
        moreRequestHandler.setShouldRequestMore(hasMore);
    }

    @Override
    public void showLoadMoreProgress() {
        pbMoreLoading.animate()
                .setDuration(200)
                .translationY(0)
                .start();
    }

    @Override
    public void dismissLoadMoreProgress() {
        int bottomMargin =
                ((ViewGroup.MarginLayoutParams) pbMoreLoading.getLayoutParams()).bottomMargin;
        pbMoreLoading.animate()
                .setDuration(200)
                .translationY(pbMoreLoading.getMeasuredHeight() + bottomMargin);
    }

    public void onEvent(SocketPollEvent event) {
        pollListPresenter.onPollDataChanged(event.getType(), event.getPoll());
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Polls, action);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, AnalyticsValue.Label label) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Polls, action, label);
    }

    @Override
    public void scrollToTop() {
        lvPollList.scrollToPosition(0);
    }

    private class MorePollListRequestHandler implements PollListAdapter.OnLoadMoreCallback {

        private boolean shouldRequestMore = true;

        public void setShouldRequestMore(boolean shouldRequestMore) {
            this.shouldRequestMore = shouldRequestMore;
        }

        @Override
        public void onLoadMore(Date date) {
            if (!shouldRequestMore) {
                return;
            }

            pollListPresenter.onLoadMorePollList(date);
        }
    }
}
