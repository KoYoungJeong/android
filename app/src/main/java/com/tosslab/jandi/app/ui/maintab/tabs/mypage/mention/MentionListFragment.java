package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.absence.AbsenceInfoUpdatedEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MentionMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.MentionListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.MentionListHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.view.MentionListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dagger.DaggerMentionListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dagger.MentionListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter.MentionListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view.MentionListView;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.TabFocusListener;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MentionListFragment extends Fragment implements MentionListView, ListScroller, TabFocusListener {

    @Inject
    MentionListPresenter presenter;

    @Inject
    MentionListDataView mentionListDataView;

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

    @Bind(R.id.vg_mypage_mention_root)
    ViewGroup vgRoot;

    private MentionMessageMoreRequestHandler moreRequestHandler;

    private boolean isVisible = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage_mention_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MentionListAdapter adapter = new MentionListAdapter();
        injectComponent(adapter);

        initMentionListView(adapter);
        initSwipeRefreshLayout();
        initMoreLoadingProgress();

        if (getUserVisibleHint()) {
            onFocus();
        }
    }

    private void setListViewScroll() {
        if (getActivity() instanceof MainTabActivity) {
            MainTabActivity activity = (MainTabActivity) getActivity();
            lvMyPage.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mentionListDataView.getItemCount() > 10) {
                        if (dy > 0) {
                            activity.setTabLayoutVisible(false);
                        } else {
                            activity.setTabLayoutVisible(true);
                        }
                    }
                }
            });
        }
    }

    private void injectComponent(MentionListAdapter adapter) {
        DaggerMentionListComponent.builder()
                .mentionListModule(new MentionListModule(adapter, this))
                .build()
                .inject(this);
    }

    private void initSwipeRefreshLayout() {
        vgRefresh.setColorSchemeResources(R.color.jandi_accent_color);

        vgRefresh.setOnRefreshListener(() -> {
            presenter.onInitializeMyPage(true, true);
        });

        vgRefresh.post(() -> {
            int start = lvMyPage.getPaddingTop();
            int end = start + vgRefresh.getProgressCircleDiameter();
            vgRefresh.setProgressViewOffset(false, start, end);
        });
    }

    private void initMentionListView(final MentionListAdapter adapter) {
        final LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity().getBaseContext());
        lvMyPage.setLayoutManager(layoutManager);

        adapter.setHasStableIds(true);
        lvMyPage.addItemDecoration(new StickyHeadersBuilder()
                .setAdapter(adapter)
                .setSticky(true)
                .setRecyclerView(lvMyPage)
                .setStickyHeadersAdapter(new MentionListHeaderAdapter(adapter), false)
                .build());

        moreRequestHandler = new MentionMessageMoreRequestHandler();
        adapter.setOnLoadMoreCallback(moreRequestHandler);
        lvMyPage.setAdapter(adapter);
        adapter.setOnMentionClickListener(mention -> {
            presenter.onClickMention(mention);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.ChooseMention);
        });

        adapter.setOnMentionLongClickListener(mention -> {
            DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByMentionedMessage(
                    mention);
            newFragment.show(getChildFragmentManager(), "dioalog");
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionLongTap);

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

        setListViewScroll();
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

    public void onEvent(MentionMessageEvent event) {
        ResMessages.Link link = event.getLink();

        if (link.message == null) {
            return;
        }

        presenter.addMentionedMessage(link);
    }

    public void onEvent(MessageStarredEvent event) {
        if (isResumed()) {
            presenter.onStarred(event.getMessageId());
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionLongTap_Star);
        }
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (isResumed()) {
            Completable.fromAction(() -> {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", event.contentString));
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                ColoredToast.show(R.string.jandi_copied_to_clipboard);
            });
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionLongTap_Copy);
        }
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        presenter.removeMentionedMessage(event.getData().getLinkId());
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
        startActivity(Henson.with(getActivity())
                .gotoFileDetailActivity()
                .fileId(fileId)
                .selectMessageId(messageId)
                .build());
    }

    @Override
    public void showUnknownEntityToast() {
        ColoredToast.showError(JandiApplication.getContext()
                .getResources().getString(R.string.jandi_starmention_no_longer_in_topic));
    }

    @Override
    public void moveToMessageListActivity(
            long teamId, long entityId, int entityType, long roomId, long linkId) {

        long limitedLinkId = TeamInfoLoader.getInstance().getTeamUsage().getLimitedLinkId();

        if (limitedLinkId != -1 && linkId < limitedLinkId) {
            showUsageLimitDialog();
            return;
        }

        startActivity(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

    }

    public void showUsageLimitDialog() {
        new AlertDialog.Builder(getContext(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.pricingplan_restrictions_view_message_alert_title)
                .setMessage(R.string.pricingplan_restrictions_view_message_alert_body)
                .setNegativeButton(this.getText(R.string.intercom_close), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.pricingplan_restrictions_fileupload_popup_seedetail,
                        (dialog, which) -> {
                            movePricePlan(getContext());
                        }).show();
    }

    private void movePricePlan(Context context) {
        if (context != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            String lang = locale.getLanguage();
            String url = "https://www.jandi.com/landing/kr/pricing";

            if (TextUtils.equals(lang, "en")) {
                url = "www.jandi.com/landing/en/pricing";
            } else if (TextUtils.equals(lang, "ja")) {
                url = "www.jandi.com/landing/jp/pricing";
            } else if (TextUtils.equals(lang, "ko")) {
                url = "www.jandi.com/landing/kr/pricing";
            } else if (TextUtils.equals(lang, "zh-cn")) {
                url = "www.jandi.com/landing/zh-cn/pricing";
            } else if (TextUtils.equals(lang, "zh-tw")) {
                url = "www.jandi.com/landing/zh-tw/pricing";
            }

            ApplicationUtil.startWebBrowser(context, url);
        }
    }


    @Override
    public void notifyDataSetChanged() {
        mentionListDataView.notifyDataSetChanged();
    }

    @Override
    public void hideRefreshProgress() {
        vgRefresh.setRefreshing(false);
    }

    @Override
    public void clearLoadMoreOffset() {
        mentionListDataView.clearLoadMoreOffset();
    }

    @Override
    public void hideEmptyMentionView() {
        if (isFinishing()) {
            return;
        }
        vEmptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void moveToPollDetailActivity(long pollId) {
        PollDetailActivity.start(getActivity(), pollId);
    }

    @Override
    public void successStarredMessage() {
        ColoredToast.show(R.string.jandi_message_starred);
    }

    @Override
    public void failStarredMessage() {
        ColoredToast.showError(R.string.err_network);
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (!(event.isConnected())) {
            return;
        }

        presenter.reInitializeIfEmpty();
    }

    private boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing();
    }

    @Override
    public void scrollToTop() {
        lvMyPage.scrollToPosition(0);
    }

    @Override
    public void onFocus() {
        if (presenter != null) {
            presenter.onUpdateMentionMarker();
            presenter.onInitializeMyPage(false, true);
        }
    }

    public void onEventMainThread(AbsenceInfoUpdatedEvent event) {
        getActivity().invalidateOptionsMenu();
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
