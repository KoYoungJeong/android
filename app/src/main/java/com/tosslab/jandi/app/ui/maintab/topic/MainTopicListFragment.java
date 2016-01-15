package com.tosslab.jandi.app.ui.maintab.topic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
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
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
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
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
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
    int selectedEntity = -2;
    @Bean(MainTopicListPresenter.class)
    MainTopicListPresenter mainTopicListPresenter;
    @ViewById(R.id.btn_main_topic_fab)
    View btnFA;
    @ViewById(R.id.rv_main_topic)
    RecyclerView lvMainTopic;

    private LinearLayoutManager layoutManager;
    private ExpandableTopicAdapter adapter;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewExpandableItemManager expandableItemManager;
    private boolean isFirstForRetrieve = true;
    private ProgressWheel progressWheel;
    private boolean hasOnResumed = false;

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

        layoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ?
                savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;

        expandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        progressWheel = new ProgressWheel(getActivity());
    }

    @AfterInject
    void initObjects() {
        mainTopicListPresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        mainTopicListPresenter.onLoadList();
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
    public void showList(TopicFolderListDataProvider topicFolderListDataProvider) {

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
        scrollAndAnimateForSelectedItem();

        setFolderExpansion();
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
        if (adapter != null && hasOnResumed) {
            scrollAndAnimateForSelectedItem();
        }
        hasOnResumed = true;
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
                .start();
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setFolderExpansion() {
        List<FolderExpand> folderExpands = mainTopicListPresenter.onGetFolderExpands();
        LogUtil.e(folderExpands.size() + "");
        if (adapter.getGroupCount() > 1 && folderExpands != null && !folderExpands.isEmpty()) {
            int groupCount = adapter.getGroupCount();
            HashMap<Integer, Boolean> folderExpandMap = new HashMap<>();

            for (FolderExpand folderExpand : folderExpands) {
                folderExpandMap.put(Integer.valueOf(folderExpand.getFolderId()), folderExpand.isExpand());
            }

            for (int idx = 0; idx < groupCount; idx++) {
                TopicFolderData topicFolderData = adapter.getTopicFolderData(idx);
                int folderId = Integer.valueOf(topicFolderData.getFolderId());
                if (folderExpandMap.get(folderId) != null) {
                    if (folderExpandMap.get(folderId)) {
                        if (!expandableItemManager.isGroupExpanded(idx)) {
                            expandableItemManager.expandGroup(idx);
                        }
                    } else {
                        if (expandableItemManager.isGroupExpanded(idx)) {
                            expandableItemManager.collapseGroup(idx);
                        }
                    }
                }
            }
        }
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
        List<ResFolder> topicFolders = mainTopicListPresenter.onGetTopicFolders();
        List<ResFolderItem> topicFolderItems = mainTopicListPresenter.onGetTopicFolderItems();
        mainTopicListPresenter.onRefreshList(topicFolders, topicFolderItems, true);
    }

    public void onEvent(SocketTopicFolderEvent event) {
        mainTopicListPresenter.onRefreshList(null, null, false);
    }

    public void onEvent(SocketTopicPushEvent event) {
        List<ResFolder> topicFolders = mainTopicListPresenter.onGetTopicFolders();
        List<ResFolderItem> topicFolderItems = mainTopicListPresenter.onGetTopicFolderItems();
        mainTopicListPresenter.onRefreshList(topicFolders, topicFolderItems, true);
    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "chat")) {
            return;
        }
        // 내부적으로 메세지만 갱신시키도록 변경
        mainTopicListPresenter.onNewMessage(event);
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
    public void scrollAndAnimateForSelectedItem() {
        int selectedEntity = adapter.getSelectedEntity();
        if (selectedEntity <= 0) {
            return;
        }

        LogUtil.d("TopicList", "selectedEntity = " + selectedEntity);

        int groupPosition = -1;
        int childPosition = 0;
        TopicFolderListDataProvider provider = adapter.getProvider();

        int groupCount = provider.getGroupCount();
        if (groupCount > 0) {
            for (int i = 0; i < groupCount; i++) {
                int childCount = provider.getChildCount(i);
                for (int j = 0; j < childCount; j++) {
                    TopicItemData childItem = (TopicItemData) provider.getChildItem(i, j);
                    if (childItem.getEntityId() == selectedEntity) {
                        groupPosition = i;
                        childPosition = j;
                        break;
                    }
                }
            }
        } else {
            groupPosition = 0;
            for (int i = 0; i < provider.getChildCount(0); i++) {
                TopicItemData childItem = (TopicItemData) provider.getChildItem(0, i);
                if (childItem.getEntityId() == selectedEntity) {
                    childPosition = i;
                    break;
                }
            }
        }

        LogUtil.e("TopicList", "groupPosition = " + groupPosition + " childPosition = " + childPosition);

        if (groupPosition == -1) {
            adapter.startAnimation();
            adapter.notifyDataSetChanged();
            return;
        }

        if (!expandableItemManager.isGroupExpanded(groupPosition)) {
            expandableItemManager.expandGroup(groupPosition);
        }

        long packedPositionForChild =
                RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);

        LogUtil.d("TopicList", "packedPositionForChild = " + packedPositionForChild);

        int flatPosition = expandableItemManager.getFlatPosition(packedPositionForChild);

        LogUtil.i("TopicList", "flatPosition = " + flatPosition);

        int offset = lvMainTopic.getMeasuredHeight() / 2;
        if (offset <= 0) {
            offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    JandiApplication.getContext().getResources().getDisplayMetrics());
        }

        layoutManager.scrollToPositionWithOffset(flatPosition, offset);

        lvMainTopic.postDelayed(() -> {
            adapter.startAnimation();
            adapter.notifyDataSetChanged();
        }, 300);
    }

}