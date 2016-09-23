package com.tosslab.jandi.app.ui.maintab.tabs.topic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderMoveCallEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.libraries.advancerecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.updated.UpdatedTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.TopicFolderDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.presenter.MainTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.TopicFolderSettingActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.TopicFolderSettingActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.JoinableTopicListActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.util.BackPressConsumer;
import com.tosslab.jandi.app.ui.maintab.tabs.util.FloatingActionBarDetector;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.SearchActivity;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.views.FloatingActionMenu;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tee on 15. 8. 26..
 */
@EFragment(R.layout.fragment_joined_topic_list)
public class MainTopicListFragment extends Fragment
        implements MainTopicListPresenter.View, BackPressConsumer, ListScroller, FloatingActionBarDetector {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";
    private static final int MOVE_MESSAGE_ACTIVITY = 702;

    @FragmentArg
    long selectedEntity = -2;
    @Bean(MainTopicListPresenter.class)
    MainTopicListPresenter mainTopicListPresenter;
    @ViewById(R.id.rv_main_topic)
    RecyclerView lvMainTopic;

    @ViewById(R.id.iv_main_topic_order)
    ImageView ivTopicOrder;

    @ViewById(R.id.tv_main_topic_order_title)
    TextView tvSortTitle;

    @ViewById(R.id.vg_fab_menu)
    FloatingActionMenu floatingActionMenu;

    private LinearLayoutManager layoutManager;
    private ExpandableTopicAdapter expandableTopicAdapter;
    private RecyclerViewExpandableItemManager expandableItemManager;

    private ProgressWheel progressWheel;

    private AlertDialog createFolderDialog;
    private RecyclerView.Adapter wrappedAdapter;

    private UpdatedTopicAdapter updatedTopicAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.ScreenView)
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
        setHasOptionsMenu(true);
        lvMainTopic.setLayoutManager(layoutManager);
        initUpdatedTopicAdapter();
        mainTopicListPresenter.onLoadList();
        mainTopicListPresenter.initUpdatedTopicList();
        mainTopicListPresenter.onInitViewList();
        if (selectedEntity > 0) {
            setSelectedItem(selectedEntity);
            if (isCurrentFolder()) {
                scrollAndAnimateForSelectedItem();
            } else {
                scrollForUpdate();
            }
        }

        setFloatingActionMenu();
    }

    private void initUpdatedTopicAdapter() {
        updatedTopicAdapter = new UpdatedTopicAdapter(getActivity());
        updatedTopicAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            updatedTopicAdapter.stopAnimation();
            Topic item = ((UpdatedTopicAdapter) adapter).getItem(position);
            mainTopicListPresenter.onUpdatedTopicClick(item);
            updatedTopicAdapter.notifyDataSetChanged();
        });

        updatedTopicAdapter.setOnRecyclerItemLongClickListener((view, adapter, position) -> {
            Topic item = ((UpdatedTopicAdapter) adapter).getItem(position);
            mainTopicListPresenter.onUpdatedTopicLongClick(item);
            updatedTopicAdapter.notifyDataSetChanged();
            return false;
        });
    }

    private void setFloatingActionMenu() {
        floatingActionMenu.addItem(R.drawable.btn_fab_item_folder_setting,
                getResources().getString(R.string.jandi_setting_folder), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    launchFolderSettionActivity();
                });
        floatingActionMenu.addItem(R.drawable.btn_fab_item_create_folder,
                getResources().getString(R.string.jandi_create_folder), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    showCreateNewFolderDialog();
                });
        floatingActionMenu.addItem(R.drawable.btn_fab_item_go_unjoined,
                getResources().getString(R.string.jandi_browse_other_topics), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    onEvent(new JoinableTopicCallEvent());
                });
        floatingActionMenu.addItem(R.drawable.btn_fab_item_create_topic,
                getResources().getString(R.string.jandi_create_topic), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    launchCreateTopicActivity();
                });

    }

    private void launchCreateTopicActivity() {
        Observable.just(1)
                .delay(250, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    selectedEntity = -2;
                    setSelectedItem(selectedEntity);
                    TopicCreateActivity_
                            .intent(MainTopicListFragment.this)
                            .start();
                    getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                            AnalyticsValue.Action.CreateNewTopic);
                });
    }

    private void launchFolderSettionActivity() {
        Observable.just(1)
                .delay(250, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    selectedEntity = -2;
                    setSelectedItem(selectedEntity);
                    TopicFolderSettingActivity_.intent(getActivity())
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .extra("mode", TopicFolderSettingActivity.FOLDER_SETTING)
                            .extra("folderId", -1)
                            .start();
                });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        FragmentActivity activity = getActivity();
        if (activity instanceof MainTabActivity) {
            activity.getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        }
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        startActivity(new Intent(getActivity(), SearchActivity.class));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.Search);
    }

    @Override
    public void changeTopicSort(boolean currentFolder, boolean changeToFolder) {
        if (currentFolder && !changeToFolder) {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
            lvMainTopic.setAdapter(updatedTopicAdapter);
            tvSortTitle.setText(R.string.jandi_sort_updated);
            ivTopicOrder.setImageResource(R.drawable.topic_list_recent);
        } else if (!currentFolder && changeToFolder) {
            mainTopicListPresenter.refreshList();
            lvMainTopic.setAdapter(wrappedAdapter);  // requires *wrapped* expandableTopicAdapter
            lvMainTopic.setHasFixedSize(false);
            tvSortTitle.setText(R.string.jandi_sort_folder);
            ivTopicOrder.setImageResource(R.drawable.topic_list_folder);
        }
    }

    @Click(R.id.vg_main_topic_order)
    void onOrderTitleClick() {
        boolean currentFolder = isCurrentFolder();
        changeTopicSort(currentFolder, !currentFolder);
        JandiPreference.setLastTopicOrderType(!currentFolder ? 0 : 1);

        if (currentFolder) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                    AnalyticsValue.Action.ChangeTopicOrder,
                    AnalyticsValue.Label.UpdateDate);
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                    AnalyticsValue.Action.ChangeTopicOrder,
                    AnalyticsValue.Label.Folder);
        }
    }

    private boolean isCurrentFolder() {
        RecyclerView.Adapter adapter = lvMainTopic.getAdapter();
        return adapter != null && !(adapter instanceof UpdatedTopicAdapter);
    }

    @Override
    public void setUpdatedItems(List<Topic> topics) {
        updatedTopicAdapter.setItems(topics);
        updatedTopicAdapter.notifyDataSetChanged();
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showList(TopicFolderListDataProvider topicFolderListDataProvider) {

        expandableTopicAdapter = new ExpandableTopicAdapter(topicFolderListDataProvider);

        wrappedAdapter = expandableItemManager.createWrappedAdapter(expandableTopicAdapter);

        // ListView를 Set함
        expandableItemManager.attachRecyclerView(lvMainTopic);
        // 어떤 폴더에도 속하지 않는 토픽들을 expand된 상태에서 보여주기 위하여
        expandableItemManager.expandGroup(expandableTopicAdapter.getGroupCount() - 1);

        expandableItemManager.setOnGroupCollapseListener((groupPosition, fromUser) -> {
            TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(groupPosition);
            mainTopicListPresenter.onFolderCollapse(topicFolderData);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolderCollapse);
        });

        expandableItemManager.setOnGroupExpandListener((groupPosition, fromUser) -> {
            TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(groupPosition);
            mainTopicListPresenter.onFolderExpand(topicFolderData);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolderExpand);
        });

        expandableTopicAdapter.setOnChildItemClickListener((view, adapter, groupPosition, childPosition)
                -> mainTopicListPresenter.onChildItemClick(adapter, groupPosition, childPosition));

        expandableTopicAdapter.setOnChildItemLongClickListener((view, adapter, groupPosition, childPosition) -> {
            mainTopicListPresenter.onChildItemLongClick(adapter, groupPosition, childPosition);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicSubMenu);
            return true;
        });

        expandableTopicAdapter.setOnGroupItemClickListener((view, adapter, groupPosition) -> {
            ExpandableTopicAdapter expandableTopicAdapter = (ExpandableTopicAdapter) adapter;
            TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(groupPosition);
            long folderId = topicFolderData.getFolderId();
            String folderName = topicFolderData.getTitle();
            showGroupSettingPopupView(view, folderId, folderName, topicFolderData.getSeq());
        });

        mainTopicListPresenter.getUnreadCount(Observable.from(getJoinedTopics()))
                .subscribe(unreadCount -> {
                    EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
                });
        setFolderExpansion();
    }

    public void showGroupSettingPopupView(View view, long folderId, String folderName, int seq) {
        TopicFolderDialogFragment_.builder()
                .folderId(folderId)
                .folderName(folderName)
                .seq(seq)
                .build()
                .show(getFragmentManager(), "dialog");
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
    public void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long lastReadLinkId) {
        MessageListV2Activity_.intent(MainTopicListFragment.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .lastReadLinkId(lastReadLinkId)
                .startForResult(MOVE_MESSAGE_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MOVE_MESSAGE_ACTIVITY) {

            if (resultCode == Activity.RESULT_OK && (data != null && data.hasExtra(MessageListV2Activity.KEY_ENTITY_ID))) {
                long selectedEntity = data.getLongExtra(MessageListV2Activity.KEY_ENTITY_ID, -2);
                if (selectedEntity <= -2) {
                    return;
                }

                setSelectedItem(selectedEntity);
                if (isCurrentFolder()) {
                    mainTopicListPresenter.refreshList();
                    expandableTopicAdapter.startAnimation();
                    expandableTopicAdapter.notifyDataSetChanged();
                } else {
                    if (TeamInfoLoader.getInstance().isTopic(selectedEntity)) {
                        int position = updatedTopicAdapter.indexOfEntity(selectedEntity);
                        if (position >= 0) {
                            Topic item = updatedTopicAdapter.getItem(position);
                            if (item != null) {
                                item.setUnreadCount(TeamInfoLoader.getInstance().getTopic(selectedEntity).getUnreadCount());
                            }
                        }
                    }
                    updatedTopicAdapter.startAnimation();
                    updatedTopicAdapter.notifyDataSetChanged();
                }
            }

        }

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDatasetChangedForFolder() {
        expandableTopicAdapter.notifyDataSetChanged();
        //빼먹지 말아야 함.
        int groupPosition = expandableTopicAdapter.getGroupCount() - 1;
        if (groupPosition >= 0) {
            expandableItemManager.expandGroup(groupPosition);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDatasetChangedForUpdated() {
        updatedTopicAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateGroupBadgeCount() {
        expandableTopicAdapter.updateGroupBadgeCount();
    }

    @Override
    public void showEntityMenuDialog(long entityId, long folderId) {
        EntityMenuDialogFragment_.builder()
                .entityId(entityId)
                .roomId(entityId)
                .folderId(folderId)
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshList(TopicFolderListDataProvider topicFolderListDataProvider) {
        expandableTopicAdapter.setProvider(topicFolderListDataProvider);
        notifyDatasetChangedForFolder();
        mainTopicListPresenter.getUnreadCount(Observable.from(getJoinedTopics()))
                .subscribe(unreadCount -> {
                    EventBus.getDefault().post(new TopicBadgeEvent(unreadCount > 0, unreadCount));
                });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setFolderExpansion() {
        List<FolderExpand> folderExpands = mainTopicListPresenter.onGetFolderExpands();
        if (expandableTopicAdapter.getGroupCount() > 1 && folderExpands != null && !folderExpands.isEmpty()) {
            int groupCount = expandableTopicAdapter.getGroupCount();
            HashMap<Long, Boolean> folderExpandMap = new HashMap<>();

            for (FolderExpand folderExpand : folderExpands) {
                folderExpandMap.put(folderExpand.getFolderId(), folderExpand.isExpand());
            }

            for (int idx = 0; idx < groupCount; idx++) {
                TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(idx);
                long folderId = topicFolderData.getFolderId();
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

    private List<TopicItemData> getJoinedTopics() {
        return expandableTopicAdapter.getAllTopicItemData();
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
        Intent intent = new Intent(getActivity(), JoinableTopicListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.BrowseOtherTopics);
    }

    public void onEvent(TopicFolderMoveCallEvent event) {
        TopicFolderSettingActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .mode(TopicFolderSettingActivity.ITEM_FOLDER_CHOOSE)
                .topicId(event.getTopicId())
                .folderId(event.getFolderId())
                .start();
    }

    public void onEvent(RetrieveTopicListEvent event) {
        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(TopicFolderRefreshEvent event) {
        mainTopicListPresenter.refreshList();
    }

    public void onEvent(SocketTopicPushEvent event) {
        mainTopicListPresenter.refreshList();
        mainTopicListPresenter.onRefreshUpdatedTopicList();
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        mainTopicListPresenter.refreshList();
        mainTopicListPresenter.onRefreshUpdatedTopicList();
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(RoomMarkerEvent event) {
        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(SocketMessageCreatedEvent event) {
        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setSelectedItem(long selectedEntity) {
        this.selectedEntity = selectedEntity;
        if (expandableTopicAdapter != null) {
            expandableTopicAdapter.setSelectedEntity(selectedEntity);
        }

        if (updatedTopicAdapter != null) {
            updatedTopicAdapter.setSelectedEntity(selectedEntity);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void scrollAndAnimateForSelectedItem() {
        long selectedEntity = expandableTopicAdapter.getSelectedEntity();
        if (selectedEntity <= 0) {
            return;
        }

        int groupPosition = -1;
        int childPosition = 0;
        TopicFolderListDataProvider provider = expandableTopicAdapter.getProvider();

        int groupCount = provider.getGroupCount();
        if (groupCount > 0) {
            for (int i = 0; i < groupCount; i++) {
                int childCount = provider.getChildCount(i);
                for (int j = 0; j < childCount; j++) {
                    TopicItemData childItem = provider.getChildItem(i, j);
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
                TopicItemData childItem = provider.getChildItem(0, i);
                if (childItem.getEntityId() == selectedEntity) {
                    childPosition = i;
                    break;
                }
            }
        }

        if (groupPosition == -1) {
            expandableTopicAdapter.startAnimation();
            expandableTopicAdapter.notifyDataSetChanged();
            return;
        }

        if (!expandableItemManager.isGroupExpanded(groupPosition)) {
            expandableItemManager.expandGroup(groupPosition);
        }

        long packedPositionForChild =
                RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, childPosition);

        int flatPosition = expandableItemManager.getFlatPosition(packedPositionForChild);

        int offset = lvMainTopic.getMeasuredHeight() / 3;
        if (offset <= 0) {
            offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    JandiApplication.getContext().getResources().getDisplayMetrics());
        }

        layoutManager.scrollToPositionWithOffset(flatPosition, offset);

        lvMainTopic.postDelayed(() -> {
            expandableTopicAdapter.startAnimation();
            expandableTopicAdapter.notifyDataSetChanged();
        }, 300);
    }

    private void scrollForUpdate() {

        int position = updatedTopicAdapter.indexOfEntity(selectedEntity);
        if (position < 0) {
            return;
        }
        int offset = lvMainTopic.getMeasuredHeight() / 3;
        if (offset <= 0) {
            offset = JandiApplication.getContext()
                    .getResources()
                    .getDisplayMetrics()
                    .heightPixels / 3;
        }


        layoutManager.scrollToPositionWithOffset(position, offset);

        lvMainTopic.postDelayed(() -> {
            updatedTopicAdapter.startAnimation();
        }, 300);
    }

    public void showCreateNewFolderDialog() {
        if (createFolderDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),
                    R.style.JandiTheme_AlertDialog_FixWidth_300);

            RelativeLayout rootView = (RelativeLayout) LayoutInflater
                    .from(getContext()).inflate(R.layout.dialog_fragment_input_text, null);

            TextView tvTitle = (TextView) rootView.findViewById(R.id.tv_popup_title);
            EditText etInput = (EditText) rootView.findViewById(R.id.et_dialog_input_text);
            etInput.setHint(R.string.jandi_title_name);
            tvTitle.setText(R.string.jandi_folder_insert_name);

            builder.setView(rootView)
                    .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                        mainTopicListPresenter.createNewFolder(etInput.getText().toString().trim());
                        etInput.setText("");
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MoveToaFolder, AnalyticsValue.Action.NewFolder);
                    })
                    .setNegativeButton(R.string.jandi_cancel, (dialog, which) -> {
                        etInput.setText("");
                        dialog.cancel();
                    });

            createFolderDialog = builder.create();
            createFolderDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            etInput.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() <= 0) {
                        createFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        createFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            });
        }
        createFolderDialog.show();
        createFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    @UiThread
    public void showAlreadyHasFolderToast() {
        ColoredToast.showWarning(getString(R.string.jandi_folder_alread_has_name));
    }

    @Click(R.id.fab_menu_button)
    void onFabMenuClick() {
        if (floatingActionMenu != null && !(floatingActionMenu.isOpened())) {
            floatingActionMenu.open();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser) {
            if (floatingActionMenu != null) {
                floatingActionMenu.setVisibility(false);
            }
        }
    }

    @Override
    public void scrollToTop() {
        lvMainTopic.scrollToPosition(0);
    }

    @Override
    public boolean consumeBackPress() {

        if (floatingActionMenu != null && floatingActionMenu.isOpened()) {
            floatingActionMenu.close();
            return true;
        }

        return false;
    }

    @Override
    public void onDetectFloatAction(View btnFab) {
        if (btnFab != null) {
            btnFab.setOnClickListener(v -> {
                if (floatingActionMenu == null) {
                    return;
                }
                floatingActionMenu.setVisibility(true);
                floatingActionMenu.setupButtonLocation(btnFab);
                floatingActionMenu.open();
            });
        }
    }
}