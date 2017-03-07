package com.tosslab.jandi.app.ui.maintab.tabs.topic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.TopicBadgeEvent;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderMoveCallEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseLazyFragment;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.TopicFolderAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.updated.UpdatedTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dagger.DaggerMainTopicListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dagger.MainTopicListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.EntityMenuDialogFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.TopicFolderDialogFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.IMarkerTopicFolderItem;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.presenter.MainTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.TopicFolderSettingActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.JoinableTopicListActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.util.BackPressConsumer;
import com.tosslab.jandi.app.ui.maintab.tabs.util.FloatingActionBarDetector;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity;
import com.tosslab.jandi.app.ui.search.main.SearchActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.views.FloatingActionMenu;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;

public class MainTopicListFragment extends BaseLazyFragment
        implements MainTopicListPresenter.View, BackPressConsumer, ListScroller, FloatingActionBarDetector {

    private static final int MOVE_MESSAGE_ACTIVITY = 702;

    @Nullable
    @InjectExtra
    long selectedEntity = -2;
    @Bind(R.id.rv_main_topic)
    RecyclerView lvMainTopic;
    @Bind(R.id.iv_main_topic_order)
    ImageView ivTopicOrder;
    @Bind(R.id.tv_main_topic_order_title)
    TextView tvSortTitle;
    @Bind(R.id.vg_fab_menu)
    FloatingActionMenu floatingActionMenu;

    @Inject
    MainTopicListPresenter mainTopicListPresenter;

    private LinearLayoutManager layoutManager;
    private AlertDialog createFolderDialog;
    private boolean isFirstLoadFragment = true;
    private TopicFolderAdapter topicFolderAdapter;
    private UpdatedTopicAdapter updatedTopicAdapter;

    public static MainTopicListFragment create(long selectedEntity) {
        Bundle args = new Bundle();
        args.putLong("selectedEntity", selectedEntity);
        MainTopicListFragment fragment = new MainTopicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_joined_topic_list, container, false);
        ButterKnife.bind(this, view);
        initTopicFolderAdapter();
        initUpdatedTopicAdapter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dart.inject(this, getArguments());
        DaggerMainTopicListComponent.builder()
                .mainTopicListModule(new MainTopicListModule(this))
                .build()
                .inject(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onLazyLoad(Bundle savedInstanceState) {
        initViews(savedInstanceState);
    }

    void initViews(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mainTopicListPresenter.onLoadFolderList();
        mainTopicListPresenter.initUpdatedTopicList();
        mainTopicListPresenter.onInitViewList();
        if (selectedEntity > 0) {
            setSelectedItem(selectedEntity);
            if (isCurrentFolder()) {
                scrollForFolder();
            } else {
                scrollForUpdate();
            }
        }

        mainTopicListPresenter.checkFloatingActionMenu();

        setListViewScroll();
    }

    private void initTopicFolderAdapter() {
        layoutManager = new LinearLayoutManager(getActivity());
        lvMainTopic.setLayoutManager(layoutManager);
        lvMainTopic.setItemAnimator(new DefaultItemAnimator());
        topicFolderAdapter = new TopicFolderAdapter();
        topicFolderAdapter.setOnFolderSettingClickListener(
                (folderId, folderName, folderSeq) -> showGroupSettingPopupView(folderId, folderName, folderSeq));

        topicFolderAdapter.setOnItemLongClickListener(topicItemData -> {
            mainTopicListPresenter.onChildItemLongClick(topicItemData);
        });

        topicFolderAdapter.setOnItemClickListener(topicItemData -> {
            topicFolderAdapter.stopAnimation();
            mainTopicListPresenter.onChildItemClick(topicItemData);
            topicFolderAdapter.notifyDataSetChanged();
        });
    }

    private void setListViewScroll() {
        MainTabActivity activity = (MainTabActivity) getActivity();

        lvMainTopic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    activity.setTabLayoutVisible(false);
                } else {
                    activity.setTabLayoutVisible(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoadFragment) {
            isFirstLoadFragment = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void setFloatingActionMenu(boolean showTopicMenus) {
        floatingActionMenu.addItem(R.drawable.btn_fab_item_folder_setting,
                getResources().getString(R.string.jandi_setting_folder), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    launchFolderSettionActivity();
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                            AnalyticsValue.Action.TapPlusButton_FolderManagement);

                });
        floatingActionMenu.addItem(R.drawable.btn_fab_item_create_folder,
                getResources().getString(R.string.jandi_create_folder), () -> {
                    if (floatingActionMenu.isOpened()) {
                        floatingActionMenu.close();
                    }
                    showCreateNewFolderDialog();
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                            AnalyticsValue.Action.TapPlusButton_CreateNewFolder);
                });
        if (showTopicMenus) {
            floatingActionMenu.addItem(R.drawable.btn_fab_item_go_unjoined,
                    getResources().getString(R.string.topic_menu_Browse_other_public_topics), () -> {
                        if (floatingActionMenu.isOpened()) {
                            floatingActionMenu.close();
                        }
                        onEvent(new JoinableTopicCallEvent());
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                                AnalyticsValue.Action.TapPlusButton_BrowseOtherTopics);
                    });
            floatingActionMenu.addItem(R.drawable.btn_fab_item_create_topic,
                    getResources().getString(R.string.jandi_create_topic), () -> {
                        if (floatingActionMenu.isOpened()) {
                            floatingActionMenu.close();
                        }
                        launchCreateTopicActivity();
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab,
                                AnalyticsValue.Action.TapPlusButton_CreateNewTopic);
                    });
        }

    }

    private void launchCreateTopicActivity() {
        Observable.just(1)
                .delay(250, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    selectedEntity = -2;
                    setSelectedItem(selectedEntity);
                    startActivity(Henson.with(getActivity())
                            .gotoTopicCreateActivity()
                            .build());
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
                    startActivity(Henson.with(getActivity())
                            .gotoTopicFolderSettingActivity()
                            .folderId(-1)
                            .mode(TopicFolderSettingActivity.FOLDER_SETTING)
                            .topicId(0)
                            .build()
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    );
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_main_search) {
            onSearchOptionSelect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onSearchOptionSelect() {
        startActivity(new Intent(getActivity(), SearchActivity.class));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.Search);
    }

    @Override
    public void changeTopicSort(boolean currentFolder, boolean changeToFolder) {
        if (currentFolder && !changeToFolder) {
            lvMainTopic.setAdapter(updatedTopicAdapter);
            tvSortTitle.setText(R.string.jandi_sort_updated);
            ivTopicOrder.setImageResource(R.drawable.topic_list_recent);
        } else if (!currentFolder && changeToFolder) {
            lvMainTopic.setAdapter(topicFolderAdapter);
            tvSortTitle.setText(R.string.jandi_sort_folder);
            ivTopicOrder.setImageResource(R.drawable.topic_list_default);
        }
    }

    @OnClick(R.id.vg_main_topic_order)
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
    public void showList(List<IMarkerTopicFolderItem> topicFolderItems) {
        topicFolderAdapter.setItems(topicFolderItems);
        topicFolderAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new TopicBadgeEvent());
    }

    @Override
    public void refreshList(List<IMarkerTopicFolderItem> topicFolderItems) {
        topicFolderAdapter.setItems(topicFolderItems);
        topicFolderAdapter.notifyDataSetChanged();
    }

    public void showGroupSettingPopupView(long folderId, String folderName, int seq) {
        TopicFolderDialogFragment.create(folderId, folderName, seq)
                .show(getFragmentManager(), "dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long lastReadLinkId) {
        startActivityForResult(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(entityId)
                .lastReadLinkId(lastReadLinkId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), MOVE_MESSAGE_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MOVE_MESSAGE_ACTIVITY) {
            if (updatedTopicAdapter == null || topicFolderAdapter == null) {
                return;
            }

            if (resultCode == Activity.RESULT_OK && (data != null && data.hasExtra(MessageListV2Activity.KEY_ENTITY_ID))) {
                long selectedEntity = data.getLongExtra(MessageListV2Activity.KEY_ENTITY_ID, -2);
                if (selectedEntity <= -2) {
                    return;
                }
                setSelectedItem(selectedEntity);
                if (isCurrentFolder()) {
                    if (topicFolderAdapter == null) {
                        return;
                    }
                    topicFolderAdapter.startAnimation();
                    mainTopicListPresenter.refreshList();
                } else {
                    if (updatedTopicAdapter == null) {
                        return;
                    }
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

    @Override
    public void showEntityMenuDialog(long entityId, long folderId) {
        EntityMenuDialogFragment.create(entityId, entityId, folderId)
                .show(getFragmentManager(), "dialog");
    }

    public void onEvent(JoinableTopicCallEvent event) {
        if (!isLoadedAll()) {
            return;
        }

        Intent intent = new Intent(getActivity(), JoinableTopicListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.BrowseOtherTopics);
    }

    public void onEventMainThread(TopicFolderMoveCallEvent event) {
        if (!isLoadedAll()) {
            return;
        }
        startActivity(Henson.with(getActivity())
                .gotoTopicFolderSettingActivity()
                .mode(TopicFolderSettingActivity.ITEM_FOLDER_CHOOSE)
                .topicId(event.getTopicId())
                .folderId(event.getFolderId())
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    public void onEvent(RetrieveTopicListEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(TopicFolderRefreshEvent event) {
        if (!isLoadedAll()) {
            return;
        }

        mainTopicListPresenter.refreshList();
    }

    public void onEvent(SocketTopicPushEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        mainTopicListPresenter.refreshList();
        mainTopicListPresenter.onRefreshUpdatedTopicList();
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        mainTopicListPresenter.refreshList();
        mainTopicListPresenter.onRefreshUpdatedTopicList();
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(RoomMarkerEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void onEvent(SocketMessageCreatedEvent event) {
        EventBus.getDefault().post(new TopicBadgeEvent());

        if (!isLoadedAll()) {
            return;
        }

        if (isCurrentFolder()) {
            mainTopicListPresenter.refreshList();
        } else {
            mainTopicListPresenter.onRefreshUpdatedTopicList();
        }
    }

    public void setSelectedItem(long selectedEntity) {
        this.selectedEntity = selectedEntity;

        if (topicFolderAdapter != null) {
            topicFolderAdapter.setSelectedEntity(selectedEntity);
        }

        if (updatedTopicAdapter != null) {
            updatedTopicAdapter.setSelectedEntity(selectedEntity);
        }
    }

    private void scrollForFolder() {
        int position = topicFolderAdapter.indexOfEntity(selectedEntity);
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
            topicFolderAdapter.startAnimation();
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
    public void showAlreadyHasFolderToast() {
        ColoredToast.showWarning(getString(R.string.jandi_folder_alread_has_name));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser) {
            if (floatingActionMenu != null) {
                floatingActionMenu.setVisibility(false);
            }
        } else {
            SprinklrScreenView.sendLog(ScreenViewProperty.MESSAGE_PANEL);
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.TopicsTab);
        }
    }

    @Override
    public void scrollToTop() {
        if (lvMainTopic != null) {
            lvMainTopic.scrollToPosition(0);
        }
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
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TapPlusButton);
            });
        }
    }

}