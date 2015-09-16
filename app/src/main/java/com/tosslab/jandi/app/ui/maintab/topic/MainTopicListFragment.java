package com.tosslab.jandi.app.ui.maintab.topic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderMoveCallEvent;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.animator.GeneralItemAnimator;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.animator.RefactoredDefaultItemAnimator;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.TopicFolderDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topic.presenter.MainTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.topic.views.choosefolderlist.TopicFolderChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.views.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.JoinableTopicListActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.FAButtonUtil;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
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
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tee on 15. 8. 26..
 */
@EFragment(R.layout.fragment_joined_topic_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainTopicListFragment extends Fragment implements MainTopicListPresenter.View {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    @FragmentArg
    int selectedEntity = -1;
    @Bean(MainTopicListPresenter.class)
    MainTopicListPresenter mainTopicListPresenter;
    @ViewById(R.id.btn_main_topic_fab)
    View btnFA;
    @ViewById(R.id.rv_main_topic)
    RecyclerView lvMainTopic;

    private RecyclerView.LayoutManager layoutManager;
    private ExpandableTopicAdapter adapter;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewExpandableItemManager expandableItemManager;
    private boolean isFirst = true;

    private ProgressWheel progressWheel;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.MESSAGE_PANEL)
                        .build());

        AnalyticsUtil.sendScreenName("MESSAGE_PANEL");

        layoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ?
                savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;

        expandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        progressWheel = new ProgressWheel(getActivity());
    }

    @AfterViews
    void initViews() {
        mainTopicListPresenter.setView(this);
        mainTopicListPresenter.onLoadList();
        mainTopicListPresenter.onInitList();
        FAButtonUtil.setFAButtonController(lvMainTopic, btnFA);
        hasOptionsMenu();
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.Search);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showList(TopicFolderListDataProvider topicFolderListDataProvider, List<FolderExpand> folderExpands) {

        adapter = new ExpandableTopicAdapter(topicFolderListDataProvider);

        wrappedAdapter = expandableItemManager.createWrappedAdapter(adapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);
        lvMainTopic.setLayoutManager(layoutManager);
        lvMainTopic.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
        lvMainTopic.setItemAnimator(animator);
        lvMainTopic.setHasFixedSize(false);
        expandableItemManager.attachRecyclerView(lvMainTopic);

        // 어떤 폴더에도 속하지 않는 토픽들을 expand된 상태에서 보여주기 위하여
        expandableItemManager.expandGroup(adapter.getGroupCount() - 1);

        if (adapter.getGroupCount() > 1 && folderExpands != null && !folderExpands.isEmpty()) {
            int groupCount = adapter.getGroupCount();
            boolean expand;
            int seledtedGruopId = adapter.findGroupIdOfChildEntity(selectedEntity);
            for (int idx = 0; idx < groupCount; idx++) {
                TopicFolderData topicFolderData = adapter.getTopicFolderData(idx);
                expand = Observable.from(folderExpands)
                        .filter(folderExpand -> topicFolderData.getFolderId() == folderExpand.getFolderId()
                                || topicFolderData.getFolderId() == seledtedGruopId)
                        .map(FolderExpand::isExpand)
                        .firstOrDefault(false)
                        .toBlocking().first();
                if (expand) {
                    expandableItemManager.expandGroup(idx);
                }
            }
        }

        expandableItemManager.setOnGroupCollapseListener((groupPosition, fromUser) -> {
            TopicFolderData topicFolderData = adapter.getTopicFolderData(groupPosition);
            mainTopicListPresenter.onFolderCollapse(topicFolderData);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolderCollapse);
        });
        expandableItemManager.setOnGroupExpandListener((groupPosition, fromUser) -> {
            TopicFolderData topicFolderData = adapter.getTopicFolderData(groupPosition);
            mainTopicListPresenter.onFolderExpand(topicFolderData);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolderExpand);
        });

        adapter.setOnChildItemClickListener((view, adapter, groupPosition, childPosition)
                -> mainTopicListPresenter.onChildItemClick(adapter, groupPosition, childPosition));

        adapter.setOnChildItemLongClickListener((view, adapter, groupPosition, childPosition) -> {
            mainTopicListPresenter.onChildItemLongClick(adapter, groupPosition, childPosition);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicSubMenu);
            return true;
        });

        adapter.setOnGroupItemClickListener((view, adapter, groupPosition) -> {
            ExpandableTopicAdapter expandableTopicAdapter = (ExpandableTopicAdapter) adapter;
            TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(groupPosition);
            int folderId = topicFolderData.getFolderId();
            String folderName = topicFolderData.getTitle();
            showGroupSettingPopupView(view, folderId, folderName, topicFolderData.getSeq());
        });

        int unreadCount = mainTopicListPresenter.getUnreadCount(Observable.from(getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
        setSelectedItem(selectedEntity);
        adapter.startAnimation();
    }

    public void showGroupSettingPopupView(View view, int folderId, String folderName, int seq) {
        TopicFolderDialogFragment_.builder()
                .folderId(folderId)
                .folderName(folderName)
                .seq(seq)
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        btnFA.setAnimation(null);
        btnFA.setVisibility(View.VISIBLE);
        if (!isFirst) {
            mainTopicListPresenter.onRefreshList();
        } else {
            isFirst = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(JoinableTopicCallEvent event) {
        JoinableTopicListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.BrowseOtherTopics);
    }

    public void onEvent(TopicFolderMoveCallEvent event) {
        TopicFolderChooseActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .extra("topicId", event.getTopicId())
                .extra("folderId", event.getFolderId())
                .start();
    }

    public void onEvent(RetrieveTopicListEvent event) {
        mainTopicListPresenter.onRefreshList();
    }

    public void onEvent(SocketTopicFolderEvent event) {
        mainTopicListPresenter.onRefreshList();
    }

    @Override
    public void moveToMessageActivity(int entityId, int entityType, boolean starred, int teamId, int markerLinkId) {
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .lastMarker(markerLinkId)
                .isFavorite(starred)
                .startForResult(MainTabActivity.REQ_START_MESSAGE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDatasetChanged() {
        adapter.notifyDataSetChanged();
        //빼먹지 말아야 함.
        expandableItemManager.expandGroup(adapter.getGroupCount() - 1);
    }

    @Override
    public void updateGroupBadgeCount() {
        adapter.updateGroupBadgeCount();
    }

    @Override
    public void showEntityMenuDialog(int entityId, int folderId) {
        EntityMenuDialogFragment_.builder()
                .entityId(entityId)
                .folderId(folderId)
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshList(TopicFolderListDataProvider topicFolderListDataProvider) {
        adapter.setProvider(topicFolderListDataProvider);
        notifyDatasetChanged();

        int unreadCount = mainTopicListPresenter.getUnreadCount(Observable.from(getJoinedTopics()));
        EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
    }


    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "chat")) {
            return;
        }
        // 내부적으로 메세지만 갱신시키도록 변경
        mainTopicListPresenter.onNewMessage(event);

    }

    public void onEvent(SocketTopicPushEvent event) {
        mainTopicListPresenter.onRefreshList();
    }

    @Override
    public List<TopicItemData> getJoinedTopics() {
        return adapter.getAllTopicItemData();
    }

    @Click(R.id.btn_main_topic_fab)
    void onAddTopicClick() {
        TopicCreateActivity_
                .intent(MainTopicListFragment.this)
                .start();

        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.CreateNewTopic);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        progressWheel.show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void onEvent(MainSelectTopicEvent event) {
        selectedEntity = event.getSelectedEntity();
        setSelectedItem(selectedEntity);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setSelectedItem(int selectedEntity) {
        this.selectedEntity = selectedEntity;
        adapter.setSelectedEntity(selectedEntity);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void startAnimationSelectedItem() {
        adapter.startAnimation();
        adapter.notifyDataSetChanged();
    }

}