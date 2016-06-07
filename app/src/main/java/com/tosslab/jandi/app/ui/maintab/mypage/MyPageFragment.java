package com.tosslab.jandi.app.ui.maintab.mypage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.MentionToMeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.mypage.adapter.MyPageAdapter;
import com.tosslab.jandi.app.ui.maintab.mypage.component.DaggerMyPageComponent;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.ui.maintab.mypage.module.MyPageModule;
import com.tosslab.jandi.app.ui.maintab.mypage.presenter.MyPagePresenter;
import com.tosslab.jandi.app.ui.maintab.mypage.view.MyPageView;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity_;
import com.tosslab.jandi.app.ui.settings.main.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.KnockListener;
import com.tosslab.jandi.app.utils.ViewSlider;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.spannable.OwnerSpannable;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPageFragment extends Fragment implements MyPageView, ListScroller {

    @Inject
    MyPagePresenter presenter;

    @Bind(R.id.vg_refresh)
    SwipeRefreshLayout vgRefresh;

    @Bind(R.id.vg_mypage_profile)
    ViewGroup vgProfileLayout;

    @Bind(R.id.vg_mypage_my_profile_wrapper)
    ViewGroup vgMyProfileWrapper;
    @Bind(R.id.iv_mypage_my_profile)
    ImageView ivProfile;
    @Bind(R.id.tv_mypage_my_profile_name)
    TextView tvName;
    @Bind(R.id.tv_mypage_my_profile_email)
    TextView tvEmail;
    @Bind(R.id.btn_mypage_my_profile_setting)
    View btnSetting;

    @Bind(R.id.lv_mypage)
    RecyclerView lvMyPage;

    @Bind(R.id.progress_mypage)
    ProgressBar pbMyPage;

    @Bind(R.id.progress_mypage_more_loading)
    ProgressBar pbMoreLoading;

    @Bind(R.id.v_mypage_empty_mentions)
    View vEmptyLayout;

    private MyPageAdapter adapter;

    private MentionMessageMoreRequestHandler moreRequestHandler;

    private boolean isLaidOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLaidOut = false;

        injectComponent();

        setHasOptionsMenu(true);
    }

    private void injectComponent() {
        DaggerMyPageComponent.builder()
                .myPageModule(new MyPageModule(this))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        initSwipeRefreshLayout();

        initMentionListView();
        initMoreLoadingProgress();

        presenter.onRetrieveMyInfo();
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

        adapter = new MyPageAdapter();
        moreRequestHandler = new MentionMessageMoreRequestHandler();
        adapter.setOnLoadMoreCallback(moreRequestHandler);
        lvMyPage.setAdapter(adapter);

        lvMyPage.addOnScrollListener(new ViewSlider(vgProfileLayout));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maintab_mypage, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_mypage_setting) {
            SettingsActivity_.intent(this)
                    .start();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.Setting);
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public void onEvent(MentionToMeEvent event) {
        Date latestCreatedAt = adapter.getItem(0) != null
                ? adapter.getItem(0).getCreatedAt() : null;
        presenter.onNewMentionComing(event.getTeamId(), latestCreatedAt);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isLaidOut) {
            presenter.onRetrieveMyInfo();
        }

        isLaidOut = true;
    }

    @Override
    public void onDestroyView() {
        presenter.clearMentionInitializeQueue();
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @OnClick(R.id.btn_my_profile_move_to_starred)
    void moveToStarredListActivity() {
        StarMentionListActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .extra("type", StarMentionListActivity.TYPE_STAR_LIST)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.Stars);
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
    public void setMe(FormattedEntity me) {
        if (isFinishing() || me == null) {
            return;
        }

        String name = TextUtils.isEmpty(me.getName()) ? "" : me.getName();
        SpannableStringBuilder ssb = new SpannableStringBuilder(name);
        if (me.isTeamOwner()) {
            int start = ssb.length();
            String ownerText = JandiApplication.getContext()
                    .getResources().getString(R.string.jandi_team_owner);
            ssb.append(ownerText);

            int marginLeftDp = 4;
            OwnerSpannable ownerSpannable = new OwnerSpannable(ownerText, marginLeftDp);
            ssb.setSpan(ownerSpannable, start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvName.setText(ssb);

        String userEmail = TextUtils.isEmpty(me.getUserEmail()) ? "" : me.getUserEmail();
        tvEmail.setText(userEmail);

        String userLargeProfileUrl = me.getUserLargeProfileUrl();
        if (!TextUtils.isEmpty(userEmail)) {
            ImageUtil.loadProfileImage(ivProfile, userLargeProfileUrl, R.drawable.profile_img);
        }

        btnSetting.setOnClickListener(v -> {
            ModifyProfileActivity_.intent(this).start();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.EditProfile);
        });

        final long memberId = me.getId();
        ivProfile.setOnClickListener(v -> {
            MemberProfileActivity_.intent(getActivity())
                    .memberId(memberId)
                    .start();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.ViewMyProfile);
        });

        easterEggForLog();
    }

    private void easterEggForLog() {
        KnockListener knockListener = KnockListener.create()
                .expectKnockCount(10)
                .expectKnockedIn(3000)
                .onKnocked(() -> {
                    LogUtil.LOG = true;
                });
        tvName.setOnClickListener(v -> knockListener.knock());
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

    private boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing();
    }

    @Override
    public void scrollToTop() {
        vgProfileLayout.animate()
                .translationY(0);
        lvMyPage.scrollToPosition(0);
    }

    private class MentionMessageMoreRequestHandler implements MyPageAdapter.OnLoadMoreCallback {

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
