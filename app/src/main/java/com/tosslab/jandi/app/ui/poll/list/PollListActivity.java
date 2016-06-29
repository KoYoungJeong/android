package com.tosslab.jandi.app.ui.poll.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.poll.list.adapter.PollListAdapter;
import com.tosslab.jandi.app.ui.poll.list.adapter.view.PollListDataView;
import com.tosslab.jandi.app.ui.poll.list.component.DaggerPollListComponent;
import com.tosslab.jandi.app.ui.poll.list.module.PollListModule;
import com.tosslab.jandi.app.ui.poll.list.presenter.PollListPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollListActivity extends BaseAppCompatActivity
        implements PollListPresenter.View {

    @Inject
    PollListPresenter pollListPresenter;
    @Inject
    PollListDataView pollListDataView;

    @Bind(R.id.toolbar_poll_list)
    Toolbar toolbar;
    @Bind(R.id.lv_poll_list)
    RecyclerView lvPollList;
    @Bind(R.id.vg_poll_list_empty)
    View vgEmptyPollList;
    @Bind(R.id.progress_poll_list_more_loading)
    ProgressBar pbMoreLoading;

    private MorePollListRequestHandler moreRequestHandler;
    private ProgressWheel progressWheel;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, PollListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list);

        PollListAdapter pollListAdapter = new PollListAdapter();
        injectComponent(pollListAdapter);

        ButterKnife.bind(this);

        setupActionBar();

        initProgressWheel();

        initMoreLoadingProgress();

        initPollListView(pollListAdapter);

        initPollList();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initPollList() {
        pollListPresenter.onInitializePollList();
    }

    private void initPollListView(PollListAdapter pollListAdapter) {
        moreRequestHandler = new MorePollListRequestHandler();
        pollListAdapter.setOnLoadMoreCallback(moreRequestHandler);
        pollListAdapter.setOnPollClickListener(poll -> {
            PollDetailActivity.start(PollListActivity.this, poll.getId());
        });

        lvPollList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        lvPollList.setAdapter(pollListAdapter);
    }

    private void injectComponent(PollListAdapter adapter) {
        DaggerPollListComponent.builder()
                .pollListModule(new PollListModule(this, adapter))
                .build()
                .inject(this);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle("투표");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
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
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
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
