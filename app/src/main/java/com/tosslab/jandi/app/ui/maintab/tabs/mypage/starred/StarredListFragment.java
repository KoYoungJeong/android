package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.StarredListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.view.StarredListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.component.DaggerStarredListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.module.StarredListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor.StarredListPresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredListFragment extends Fragment implements StarredListPresenter.View, ListScroller {

    @Bind(R.id.btn_starred_list_all)
    View btnTabAll;

    @Bind(R.id.btn_starred_list_file)
    View btnTabFile;

    @Bind(R.id.lv_starred_list)
    RecyclerView lvStarredList;

    @Bind(R.id.vg_starred_list_empty)
    View vgEmptyStarredList;

    @Bind(R.id.tv_starred_list_empty)
    TextView tvEmptyMessage;

    @Bind(R.id.progress_starred_list_more_loading)
    View pbMoreLoading;

    @Inject
    StarredListPresenter starredListPresenter;
    @Inject
    StarredListDataView starredListDataView;

    private StarredListPresenter.StarredType starredType;
    private StarredMessageLoadMoreRequestHandler moreRequestHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_starred_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StarredListAdapter starredListAdapter = new StarredListAdapter();
        injectComponent(starredListAdapter);

        ButterKnife.bind(this, view);

        initStarredListView(starredListAdapter);

        initMoreLoadingProgress();

        setupTabs(starredType = StarredListPresenter.StarredType.All);

        starredListPresenter.onInitializeStarredList(starredType);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    void injectComponent(StarredListAdapter starredListAdapter) {
        DaggerStarredListComponent.builder()
                .starredListModule(new StarredListModule(starredListAdapter, this))
                .build()
                .inject(this);
    }

    private void initStarredListView(StarredListAdapter starredListAdapter) {
        lvStarredList.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvStarredList.setAdapter(starredListAdapter);

        moreRequestHandler = new StarredMessageLoadMoreRequestHandler();
        starredListDataView.setOnLoadMoreCallback(moreRequestHandler);
        starredListDataView.setOnItemClickListener(message -> {
            starredListPresenter.onStarredMessageClick(message);
        });
        starredListDataView.setOnItemLongClickListener(messageId -> {
            showUnStarDialog(messageId);
            return true;
        });
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

    @OnClick(R.id.btn_starred_list_all)
    void onAllTabClick(View view) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.Filter_All);
        if (view.isSelected()) {
            return;
        }
        setupTabs(starredType = StarredListPresenter.StarredType.All);

        starredListPresenter.onInitializeStarredList(starredType);
    }

    @OnClick(R.id.btn_starred_list_file)
    void onFileTabClick(View view) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.Filter_Files);
        if (view.isSelected()) {
            return;
        }

        setupTabs(starredType = StarredListPresenter.StarredType.File);

        starredListPresenter.onInitializeStarredList(starredType);
    }

    private void setupTabs(StarredListPresenter.StarredType starredType) {
        btnTabAll.setSelected(starredType == StarredListPresenter.StarredType.All);
        btnTabFile.setSelected(starredType == StarredListPresenter.StarredType.File);
    }

    @Override
    public void showEmptyLayout() {
        boolean isAllType = starredType == StarredListPresenter.StarredType.All;
        tvEmptyMessage.setText(isAllType
                ? R.string.jandi_starred_no_all : R.string.jandi_starred_no_file);
        vgEmptyStarredList.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyDataSetChanged() {
        starredListDataView.notifyDataSetChanged();
    }

    @Override
    public void showMoreProgress() {
        pbMoreLoading.animate()
                .setDuration(200)
                .translationY(0)
                .start();
    }

    @Override
    public void hideMoreProgress() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        int bottomMargin =
                ((ViewGroup.MarginLayoutParams) pbMoreLoading.getLayoutParams()).bottomMargin;
        pbMoreLoading.animate()
                .setDuration(200)
                .translationY(pbMoreLoading.getMeasuredHeight() + bottomMargin);
    }

    @Override
    public void setHasMore(boolean hasMore) {
        moreRequestHandler.setShouldRequestMore(hasMore);
    }

    @Override
    public void showUnStarSuccessToast() {
        ColoredToast.show(R.string.jandi_unpinned_message);
    }

    @Override
    public void moveToMessageList(long teamId, long roomId, int entityType, long linkId) {
        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(teamId)
                .entityId(roomId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .start();
    }

    @Override
    public void showUnJoinedTopicErrorToast() {
        ColoredToast.showError(R.string.jandi_starmention_no_longer_in_topic);
    }

    @Override
    public void moveToFileDetail(long fileMessageId, long selectMessageId) {
        FileDetailActivity_.intent(this)
                .fileId(fileMessageId)
                .selectMessageId(selectMessageId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
    }

    @Override
    public void moveToPollDetail(long pollId) {
        PollDetailActivity.start(getActivity(), pollId);
    }

    @Override
    public void hideEmptyLayout() {
        vgEmptyStarredList.setVisibility(View.GONE);
    }

    private void showUnStarDialog(long messageId) {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.jandi_starred_unstar_from_item)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    starredListPresenter.unStarMessage(messageId);
                }).setNegativeButton(R.string.jandi_cancel, null)
                .create()
                .show();
    }

    public void onEvent(DeleteFileEvent event) {
        long messageId = event.getId();
        if (messageId != -1) {
            starredListPresenter.onFileMessageDeleted(messageId);
        }
    }

    public void onEventMainThread(SocketMessageDeletedEvent event) {
        long messageId = event.getData().getMessageId();
        starredListPresenter.onMessageDeleted(messageId);
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (TextUtils.equals(event.getEventType(), "file_comment_deleted")) {
            starredListPresenter.onFileCommentMessageDeleted(event.getCommentId());
        }
    }

    public void onEvent(MessageStarEvent event) {
        if (event.isStarred()) {
            starredListPresenter.onMessageStarred(event.getMessageId(), starredType);
        } else {
            starredListPresenter.onMessageUnStarred(event.getMessageId());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == JandiConstants.TYPE_FILE_DETAIL_REFRESH
                && resultCode == Activity.RESULT_OK) {
            onFileDetailResult(data);
        }
    }

    private void onFileDetailResult(Intent data) {
        if (data != null && data.getBooleanExtra(MessageListV2Fragment.EXTRA_FILE_DELETE, false)) {
            long fileId = data.getLongExtra(MessageListV2Fragment.EXTRA_FILE_ID, -1);
            if (fileId != -1) {
                starredListPresenter.onFileMessageDeleted(fileId);
            }
        }
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (event.isConnected()) {
            starredListPresenter.reInitializeIfEmpty(starredType);
        }
    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }

    @Override
    public void scrollToTop() {
        lvStarredList.scrollToPosition(0);
    }

    private class StarredMessageLoadMoreRequestHandler implements StarredListAdapter.OnLoadMoreCallback {

        private boolean shouldRequestMore = true;

        public void setShouldRequestMore(boolean shouldRequestMore) {
            this.shouldRequestMore = shouldRequestMore;
        }

        @Override
        public void onLoadMore(long starredId) {
            if (!shouldRequestMore) {
                return;
            }

            starredListPresenter.onLoadMoreAction(starredType, starredId);
        }

    }
}
