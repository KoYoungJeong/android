package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.MentionListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.component.DaggerMentionListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.module.MentionListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter.MentionListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view.MentionListView;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MentionListFragment extends Fragment implements MentionListView, ListScroller {

    @Inject
    MentionListPresenter presenter;

    @Bind(R.id.vg_refresh)
    SwipeRefreshLayout vgRefresh;

    @Bind(R.id.lv_mentions)
    RecyclerView lvMyPage;

    @Bind(R.id.progress_mentions)
    ProgressBar pbMyPage;

    @Bind(R.id.progress_mentions_more_loading)
    ProgressBar pbMoreLoading;

    @Bind(R.id.v_empty_mentions)
    View vEmptyLayout;

    private MentionListAdapter adapter;

    private MentionMessageMoreRequestHandler moreRequestHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectComponent();
    }

    private void injectComponent() {
        DaggerMentionListComponent.builder()
                .mentionListModule(new MentionListModule(this))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage_mention_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        initSwipeRefreshLayout();

        initMentionListView();
        initMoreLoadingProgress();

        presenter.onInitializeMyPage(false);
    }

    private void initSwipeRefreshLayout() {
        vgRefresh.setColorSchemeResources(R.color.jandi_accent_color);

        vgRefresh.setOnRefreshListener(() -> {
            presenter.onInitializeMyPage(true);
        });

        vgRefresh.post(() -> {
            int start = lvMyPage.getPaddingTop();
            int end = start + vgRefresh.getProgressCircleDiameter();
            vgRefresh.setProgressViewOffset(false, start, end);
        });
    }

    private void initMentionListView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        lvMyPage.setLayoutManager(layoutManager);

        adapter = new MentionListAdapter();
        moreRequestHandler = new MentionMessageMoreRequestHandler();
        adapter.setOnLoadMoreCallback(moreRequestHandler);
        lvMyPage.setAdapter(adapter);

        adapter.setOnMentionClickListener(mention -> {
            presenter.onClickMention(mention);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.ChooseMention);
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (vEmptyLayout != null) {
                    if (adapter.getItemCount() > 0) {
                        vEmptyLayout.setVisibility(View.GONE);
                    } else {
                        vEmptyLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 멘션목록을 더 불러올 떄 보여지는 프로그레스바의 VISIBLE 처리를
     * animation 으로 하기 위함.
     */
    private void initMoreLoadingProgress() {
        pbMoreLoading.post(() -> {
            ViewGroup.LayoutParams layoutParams = pbMoreLoading.getLayoutParams();
            int bottomMargin =
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
            int translationY = pbMoreLoading.getMeasuredHeight() + bottomMargin;
            pbMoreLoading.animate().translationY(translationY);
        });
    }

    public void onEvent(SocketMessageCreatedEvent event) {
        ResMessages.Link link = event.getData().getLinkMessage();
        if (link.message == null) {
            return;
        }

        presenter.addMentionedMessage(link);
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        Observable.just(event.getData().getLinkId())
                .map(linkId -> adapter.indexOfLink(linkId))
                .filter(index -> index >= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(index -> {
                    adapter.remove(index);
                    adapter.notifyDataSetChanged();
                });

    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }

    @Override
    public void showProgress() {
        pbMyPage.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        if (isFinishing()) {
            return;
        }
        pbMyPage.setVisibility(View.GONE);
    }

    @Override
    public void setHasMore(boolean hasMore) {
        moreRequestHandler.setShouldRequestMore(hasMore);
    }

    @Override
    public void showEmptyMentionView() {
        if (isFinishing()) {
            return;
        }
        vEmptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearMentions() {
        adapter.clear();
    }

    @Override
    public void addMentions(List<MentionMessage> mentions) {
        adapter.addAll(mentions);
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
        if (isFinishing()) {
            return;
        }
        int bottomMargin =
                ((ViewGroup.MarginLayoutParams) pbMoreLoading.getLayoutParams()).bottomMargin;
        pbMoreLoading.animate()
                .setDuration(200)
                .translationY(pbMoreLoading.getMeasuredHeight() + bottomMargin);
    }

    @Override
    public void moveToFileDetailActivity(long fileId, long messageId) {
        FileDetailActivity_.intent(this)
                .fileId(fileId)
                .selectMessageId(messageId)
                .start();
    }

    @Override
    public void showUnknownEntityToast() {
        ColoredToast.show(JandiApplication.getContext()
                .getResources().getString(R.string.jandi_starmention_no_longer_in_topic));
    }

    @Override
    public void moveToMessageListActivity(
            long teamId, long entityId, int entityType, long roomId, long linkId) {

        MessageListV2Activity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .start();

    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void hideRefreshProgress() {
        vgRefresh.setRefreshing(false);
    }

    @Override
    public void clearLoadMoreOffset() {
        adapter.clearLoadMoreOffset();
    }

    @Override
    public void hideEmptyMentionView() {
        if (isFinishing()) {
            return;
        }
        vEmptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void addNewMention(MentionMessage mentionMessages) {
        if (adapter.indexOfLink(mentionMessages.getLinkId()) < 0) {
            adapter.add(0, mentionMessages);
        }
    }

    @Override
    public void moveToPollDetailActivity(long pollId) {
        PollDetailActivity.start(getActivity(), pollId);
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (!(event.isConnected())) {
            return;
        }

        presenter.reInitializeIfEmpty(adapter.getItemCount() <= 0);
    }

    private boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing();
    }

    @Override
    public void scrollToTop() {
        lvMyPage.scrollToPosition(0);
    }

    private class MentionMessageMoreRequestHandler implements MentionListAdapter.OnLoadMoreCallback {
        private boolean shouldRequestMore = true;

        public void setShouldRequestMore(boolean shouldRequestMore) {
            this.shouldRequestMore = shouldRequestMore;
        }

        @Override
        public void onLoadMore(long messageId) {
            if (!shouldRequestMore) {
                return;
            }
            presenter.loadMoreMentions(messageId);
        }
    }

}
