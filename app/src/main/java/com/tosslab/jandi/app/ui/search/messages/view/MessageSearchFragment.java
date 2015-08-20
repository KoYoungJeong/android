package com.tosslab.jandi.app.ui.search.messages.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
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
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.messages.adapter.EntitySelectDialogAdatper;
import com.tosslab.jandi.app.ui.search.messages.adapter.MemberSelectDialogAdapter;
import com.tosslab.jandi.app.ui.search.messages.adapter.MessageSearchResultAdapter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenter;
import com.tosslab.jandi.app.ui.search.messages.presenter.MessageSearchPresenterImpl;
import com.tosslab.jandi.app.ui.search.messages.to.SearchResult;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EFragment(R.layout.fragment_message_search)
public class MessageSearchFragment extends Fragment implements MessageSearchPresenter.View, SearchActivity.SearchSelectView {

    @Bean(MessageSearchPresenterImpl.class)
    MessageSearchPresenter messageSearchPresenter;

    @FragmentArg
    int entityId;

    @ViewById(R.id.list_search_messages)
    RecyclerView searchListView;

    @ViewById(R.id.txt_search_scope_where)
    TextView entityTextView;

    @ViewById(R.id.txt_search_scope_who)
    TextView memberTextView;

    @ViewById(R.id.layout_search_scope)
    View scopeView;

    @ViewById(R.id.progress_message_search)
    ProgressBar progressBar;

    private Dialog memberSelectDialog;
    private Dialog entitySelectDialog;

    private MessageSearchResultAdapter messageSearchResultAdapter;
    private int scropMaxY;
    private int scropMinY;
    private boolean isFirstLayout = true;
    private boolean isForeground;
    private SearchActivity.OnSearchItemSelect onSearchItemSelect;
    private SearchActivity.OnSearchText onSearchText;

    @AfterViews
    void initObject() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.MESSAGE_SEARCH)
                        .build());

        messageSearchPresenter.setView(this);

        FragmentActivity parentActivity = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parentActivity);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        searchListView.setLayoutManager(linearLayoutManager);
        messageSearchResultAdapter = new MessageSearchResultAdapter(parentActivity);
        messageSearchResultAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.Adapter adapter, int position) {
                if (position > 0) {

                    SearchResult searchRecord = ((MessageSearchResultAdapter) adapter).getItem(position);
                    messageSearchPresenter.onRecordClick(searchRecord);

                    if (onSearchItemSelect != null) {
                        onSearchItemSelect.onSearchItemSelect();
                    }

                }
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
        if (entitySelectDialog != null && entitySelectDialog.isShowing()) {
            return;
        }

        if (entitySelectDialog == null) {

            Context context = getActivity();
            EntityManager entityManager = EntityManager.getInstance();

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.jandi_file_search_entity);

            EntitySelectDialogAdatper adapter = new EntitySelectDialogAdatper(context);
            dialog.setAdapter(adapter, (dialog1, which) -> {
                EntitySelectDialogAdatper.SimpleEntityInfo item = ((EntitySelectDialogAdatper) ((AlertDialog) dialog1).getListView().getAdapter()).getItem(which);
                EventBus.getDefault().post(new SelectEntityEvent(item.getId(), item.getName()));
            });

            List<FormattedEntity> categorizableEntities = entityManager.retrieveAccessableEntities();

            FormattedEntity me = entityManager.getMe();
            adapter.add(new EntitySelectDialogAdatper.SimpleEntityInfo(-1, context.getString(R.string.jandi_file_category_everywhere), -1, ""));

            Observable.from(categorizableEntities)
                    .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                    .filter(entity -> entity.getId() != me.getId())
                    .map(entity -> {

                        int id = entity.getId();
                        String name = entity.getName();
                        int type;
                        String photo;

                        if (entity.isPublicTopic()) {
                            type = JandiConstants.TYPE_PUBLIC_TOPIC;
                            photo = "";
                        } else if (entity.isPrivateGroup()) {
                            type = JandiConstants.TYPE_PRIVATE_TOPIC;
                            photo = "";
                        } else {
                            type = JandiConstants.TYPE_DIRECT_MESSAGE;
                            photo = entity.getUserSmallProfileUrl();
                        }

                        return new EntitySelectDialogAdatper.SimpleEntityInfo(type, name, id, photo);
                    })
                    .toSortedList((lhs, rhs) -> {
                        int lhsType = lhs.getType();
                        int rhsType = rhs.getType();

                        if ((lhsType == JandiConstants.TYPE_DIRECT_MESSAGE &&
                                rhsType == JandiConstants.TYPE_DIRECT_MESSAGE)
                                || (lhsType != JandiConstants.TYPE_DIRECT_MESSAGE &&
                                rhsType != JandiConstants.TYPE_DIRECT_MESSAGE)) {
                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        } else {
                            if (lhsType != JandiConstants.TYPE_DIRECT_MESSAGE) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    })
                    .subscribe(adapter::addAll);


            entitySelectDialog = dialog.create();
        }
        entitySelectDialog.show();
    }

    @Override
    public void showMemberDialog() {
        if (memberSelectDialog != null && memberSelectDialog.isShowing()) {
            return;
        }

        if (memberSelectDialog == null) {

            Context context = getActivity();
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.jandi_file_search_user);
            MemberSelectDialogAdapter adapter = new MemberSelectDialogAdapter(context);

            EntityManager entityManager = EntityManager.getInstance();
            List<FormattedEntity> formattedUsersWithoutMe = entityManager.getFormattedUsersWithoutMe();

            List<MemberSelectDialogAdapter.SimpleMemberInfo> simpleMemberInfos = new ArrayList<>();

            Observable.from(formattedUsersWithoutMe)
                    .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                    .map(entity -> new MemberSelectDialogAdapter.SimpleMemberInfo(entity.getId(), entity.getName(), entity.getUserSmallProfileUrl()))
                    .toSortedList((lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()))
                    .subscribe(simpleMemberInfos::addAll);


            FormattedEntity me = entityManager.getMe();

            simpleMemberInfos.add(0, new MemberSelectDialogAdapter.SimpleMemberInfo(me.getId(), me.getName(), me.getUserSmallProfileUrl()));
            simpleMemberInfos.add(0, new MemberSelectDialogAdapter.SimpleMemberInfo(0, context.getString(R.string.jandi_file_category_everyone), ""));

            adapter.addAll(simpleMemberInfos);

            dialogBuilder.setAdapter(adapter, (dialog, which) -> {

                MemberSelectDialogAdapter dialogAdapter = (MemberSelectDialogAdapter) ((AlertDialog) dialog).getListView().getAdapter();
                MemberSelectDialogAdapter.SimpleMemberInfo item = dialogAdapter.getItem(which);

                EventBus.getDefault().post(new SelectMemberEvent(item.getMemberId(), item.getName()));
            });

            memberSelectDialog = dialogBuilder.create();
        }
        memberSelectDialog.show();
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
    public void startMessageListActivity(int currentTeamId, int entityId, int entityType, boolean isStarred, int linkId) {
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(currentTeamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityId : entityType)
                .isFavorite(isStarred)
                .isFromSearch(true)
                .lastMarker(linkId)
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

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
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
    public void showInvalidNetworkDialog() {
        AlertUtil_.getInstance_(getActivity())
                .showCheckNetworkDialog(getActivity(), null);
    }

    @UiThread
    @Override
    public void showInvalidateEntityToast() {
        Resources resource = JandiApplication.getContext().getResources();
        ColoredToast.show(JandiApplication.getContext(), resource.getString(R.string.jandi_topic_was_removed));
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
    public void setOnSearchItemSelect(SearchActivity.OnSearchItemSelect onSearchItemSelect) {
        this.onSearchItemSelect = onSearchItemSelect;
    }

    @Override
    public void setOnSearchText(SearchActivity.OnSearchText onSearchText) {
        this.onSearchText = onSearchText;
    }
}
