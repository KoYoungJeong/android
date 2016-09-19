package com.tosslab.jandi.app.ui.search.messages.view;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SelectMemberEvent;
import com.tosslab.jandi.app.events.search.MoreSearchRequestEvent;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.events.search.SelectEntityEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.file.view.FileSearchActivity;
import com.tosslab.jandi.app.ui.search.messages.adapter.MessageSearchResultAdapter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenterImpl;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;
import com.tosslab.jandi.app.ui.selector.room.RoomSelector;
import com.tosslab.jandi.app.ui.selector.room.RoomSelectorImpl;
import com.tosslab.jandi.app.ui.selector.user.UserSelector;
import com.tosslab.jandi.app.ui.selector.user.UserSelectorImpl;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EFragment(R.layout.fragment_message_search)
public class MessageSearchFragment extends Fragment implements MessageSearchPresenter.View, FileSearchActivity.SearchSelectView {

    @Bean(MessageSearchPresenterImpl.class)
    MessageSearchPresenter messageSearchPresenter;

    @FragmentArg
    long entityId;

    @ViewById(R.id.list_search_messages)
    RecyclerView searchListView;

    @ViewById(R.id.txt_search_scope_where)
    TextView entityTextView;

    @ViewById(R.id.layout_search_scope_where)
    View vgEntity;

    @ViewById(R.id.txt_search_scope_who)
    TextView memberTextView;

    @ViewById(R.id.layout_search_scope_who)
    View vgMember;

    @ViewById(R.id.layout_search_scope)
    View scopeView;

    @ViewById(R.id.progress_message_search)
    ProgressBar progressBar;

    private MessageSearchResultAdapter messageSearchResultAdapter;
    private int scropMaxY;
    private int scropMinY;
    private boolean isFirstLayout = true;
    private boolean isForeground;
    private FileSearchActivity.OnSearchItemSelect onSearchItemSelect;
    private FileSearchActivity.OnSearchText onSearchText;

    @AfterViews
    void initObject() {
        SprinklrScreenView.sendLog(ScreenViewProperty.MESSAGE_SEARCH);

        messageSearchPresenter.setView(this);

        FragmentActivity parentActivity = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parentActivity);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        searchListView.setLayoutManager(linearLayoutManager);
        messageSearchResultAdapter = new MessageSearchResultAdapter(parentActivity);
        messageSearchResultAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            if (position > 0) {
                SearchResult searchRecord = ((MessageSearchResultAdapter) adapter).getItem(position);
                messageSearchPresenter.onRecordClick(searchRecord);
                if (onSearchItemSelect != null) {
                    onSearchItemSelect.onSearchItemSelect();
                }
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseSearchResult);
            }
        });

        searchListView.setAdapter(messageSearchResultAdapter);

        scropMaxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        scropMinY = 0;

        searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final int offset = (int) (dy * .66f);


                final float futureScropViewPosY = scopeView.getY() - offset;

                if (futureScropViewPosY <= scropMinY) {
                    scopeView.setY(scropMinY);
                } else if (futureScropViewPosY >= scropMaxY) {
                    scopeView.setY(scropMaxY);
                } else {
                    scopeView.setY(futureScropViewPosY);
                }

                EventBus.getDefault().post(new SearchResultScrollEvent(MessageSearchFragment.this.getClass(), offset));

            }
        });

        if (entityId > 0) {
            messageSearchPresenter.onInitEntityId(entityId);
        }

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MsgSearch);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        isForeground = true;
    }

    @Override
    public void onPause() {
        isForeground = false;
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Click(R.id.layout_search_scope_where)
    void onEntityClick() {
        messageSearchPresenter.onEntityClick();
    }

    @Click(R.id.layout_search_scope_who)
    void onMemberClick() {
        messageSearchPresenter.onMemberClick();
    }

    public void onEvent(MoreSearchRequestEvent event) {

        if (!isForeground) {
            return;
        }

        messageSearchPresenter.onMoreSearchRequest();
    }

    public void onEvent(SelectEntityEvent event) {
        if (!isForeground) {
            return;
        }

        if (onSearchText != null) {
            String searchText = onSearchText.getSearchText();
            messageSearchPresenter.onSelectEntity(event.getEntityId(), event.getName(), searchText);
        } else {
            messageSearchPresenter.onSelectEntity(event.getEntityId(), event.getName(), null);
        }

        onSearchHeaderReset();
    }

    public void onEvent(SelectMemberEvent event) {
        if (!isForeground) {
            return;
        }

        if (onSearchText != null) {
            String searchText = onSearchText.getSearchText();
            messageSearchPresenter.onSelectMember(event.getMemberId(), event.getName(), searchText);
        } else {
            messageSearchPresenter.onSelectMember(event.getMemberId(), event.getName(), null);
        }
        onSearchHeaderReset();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearSearchResult() {
        messageSearchResultAdapter.clear();
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void addSearchResult(List<SearchResult> searchRecords) {
        messageSearchResultAdapter.addAll(searchRecords);
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEntityDialog() {

        setUpCategoryView(vgEntity, entityTextView, true);

        List<TopicRoom> allTopics = new ArrayList<>();
        Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .collect(() -> allTopics, List::add)
                .subscribe();
        List<Member> users = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .collect((Func0<ArrayList<Member>>) ArrayList::new, ArrayList::add)
                .toBlocking().first();
        if (TeamInfoLoader.getInstance().hasJandiBot()) {
            users.add(0, TeamInfoLoader.getInstance().getJandiBot());
        }

        RoomSelector roomSelector = new RoomSelectorImpl(allTopics, users);

        roomSelector.setOnRoomSelectListener(item -> {
            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                EventBus.getDefault().post(new SelectEntityEvent(-1, getString(R.string.jandi_search_category_everywhere)));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.AllTopic);
            } else {
                EventBus.getDefault().post(new SelectEntityEvent(item.getEntityId(), item.getName()));
                if (item.isUser()) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.Member);
                } else {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseTopicFilter, AnalyticsValue.Label.Topic);
                }
            }
            roomSelector.dismiss();
        });

        roomSelector.setOnRoomDismissListener(
                () -> {
                    setUpCategoryView(vgEntity, entityTextView, false);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.CloseTopicFilter);

                });

        roomSelector.show(vgEntity);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.OpenTopicFilter);
    }

    @Override
    public void showMemberDialog() {
        setUpCategoryView(vgMember, memberTextView, true);

        UserSelector userSelector = new UserSelectorImpl();
        userSelector.setOnUserSelectListener(item -> {
            if (item.getType() == JandiConstants.Entity.TYPE_EVERYWHERE) {
                EventBus.getDefault().post(new SelectMemberEvent(-1, getString(R.string.jandi_search_category_everyone)));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseMemberFilter, AnalyticsValue.Label.AllMember);
            } else {
                EventBus.getDefault().post(new SelectMemberEvent(item.getEntityId(), item.getName()));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.ChooseMemberFilter, AnalyticsValue.Label.Member);
            }
            userSelector.dismiss();
        });

        userSelector.setOnUserDismissListener(
                () -> {
                    setUpCategoryView(vgMember, memberTextView, false);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.CloseMemberFilter);
                });

        userSelector.show(vgMember);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MsgSearch, AnalyticsValue.Action.OpenMemberFilter);

    }

    private void setUpCategoryView(View backgroundView, TextView textView, boolean isFocused) {
        if (isFocused) {
            backgroundView.setBackgroundColor(backgroundView.getResources().getColor(R.color.jandi_primary_color_focus));
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.file_arrow_up, 0);
        } else {
            backgroundView.setBackgroundColor(Color.TRANSPARENT);
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.file_arrow_down, 0);

        }
    }

    @Override
    public void setEntityName(String name) {
        entityTextView.setText(name);
    }

    @Override
    public void setMemberName(String name) {
        memberTextView.setText(name);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setQueryResult(String query, int totalCount) {
        messageSearchResultAdapter.setQueryKeyword(query, totalCount, true);
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showLoading(String query) {
        messageSearchResultAdapter.addHeader(query);
        messageSearchResultAdapter.setQueryKeyword(query, -1, false);
        messageSearchResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void startMessageListActivity(long currentTeamId, long entityId, int entityType, long roomId, boolean isStarred, long linkId) {
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(currentTeamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromSearch(true)
                .lastReadLinkId(linkId)
                .start();
    }

    @Override
    public void setOnLoadingReady() {
        messageSearchResultAdapter.setOnLoadingReady();
    }

    @Override
    public void setOnLoadingEnd() {
        messageSearchResultAdapter.setOnLoadingEnd();

    }

    @UiThread
    @Override
    public void showMoreLoadingProgressBar() {
        progressBar.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
        progressBar.setAnimation(animation);
        animation.startNow();
    }

    @UiThread
    @Override
    public void dismissMoreLoadingProgressBar() {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_bottom);
        progressBar.setAnimation(animation);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.GONE);
            }
        });

        animation.startNow();
    }

    @UiThread
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), null);
    }

    @UiThread
    @Override
    public void showInvalidateEntityToast() {
        Resources resource = JandiApplication.getContext().getResources();
        ColoredToast.show(resource.getString(R.string.jandi_topic_was_removed));
    }

    @Override
    public void onNewQuery(String query) {
        if (TextUtils.isEmpty(query) || TextUtils.getTrimmedLength(query) <= 0) {
            return;
        }
        messageSearchPresenter.onSearchRequest(query);
    }

    @Override
    public void onSearchHeaderReset() {
        if (scopeView != null && !isFirstLayout) {
            scopeView.setY(scropMaxY);
            EventBus.getDefault().post(
                    new SearchResultScrollEvent(MessageSearchFragment.this.getClass(), -scropMaxY));
        }
    }

    @Override
    public void initSearchLayoutIfFirst() {
        isFirstLayout = false;
    }

    @Override
    public void setOnSearchItemSelect(FileSearchActivity.OnSearchItemSelect onSearchItemSelect) {
        this.onSearchItemSelect = onSearchItemSelect;
    }

    @Override
    public void setOnSearchText(FileSearchActivity.OnSearchText onSearchText) {
        this.onSearchText = onSearchText;
    }

}
