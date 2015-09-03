package com.tosslab.jandi.app.ui.maintab.topics;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderMoveCallEvent;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.animator.GeneralItemAnimator;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.animator.RefactoredDefaultItemAnimator;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicFolderEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicPushEvent;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.topic.create.TopicCreateActivity_;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.topics.adapter.ExpandableTopicAdapter;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicItemData;
import com.tosslab.jandi.app.ui.maintab.topics.presenter.MainTopicListPresenter;
import com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.TopicFolderChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.topics.views.joinabletopiclist.JoinableTopicListActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.FAButtonUtil;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 8. 26..
 */
@EFragment(R.layout.fragment_joined_topic_list)
public class MainTopicListFragment extends Fragment implements MainTopicListPresenter.View {

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    @Bean(MainTopicListPresenter.class)
    MainTopicListPresenter mainTopicListPresenter;
    @ViewById(R.id.btn_main_topic_fab)
    View btnFA;

    private RecyclerView rvMainTopic;
    private RecyclerView.LayoutManager layoutManager;
    private ExpandableTopicAdapter adapter;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewExpandableItemManager recyclerViewExpandableItemManager;
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

        GoogleAnalyticsUtil.sendScreenName("MESSAGE_PANEL");


        rvMainTopic = (RecyclerView) getView().findViewById(R.id.rv_main_topic);
        layoutManager = new LinearLayoutManager(getActivity());

        final Parcelable eimSavedState = (savedInstanceState != null) ?
                savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;

        recyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        progressWheel = new ProgressWheel(getActivity());
    }

    @AfterViews
    void initViews() {
        mainTopicListPresenter.setView(this);
        mainTopicListPresenter.onInitList();
        FAButtonUtil.setFAButtonController(rvMainTopic, btnFA);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showList(TopicFolderListDataProvider topicFolderListDataProvider) {

        adapter = new ExpandableTopicAdapter(topicFolderListDataProvider);

        wrappedAdapter = recyclerViewExpandableItemManager.createWrappedAdapter(adapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);
        rvMainTopic.setLayoutManager(layoutManager);
        rvMainTopic.setAdapter(wrappedAdapter);  // requires *wrapped* adapter
        rvMainTopic.setItemAnimator(animator);
        rvMainTopic.setHasFixedSize(false);
        recyclerViewExpandableItemManager.attachRecyclerView(rvMainTopic);

        // 어떤 폴더에도 속하지 않는 토픽들을 expand된 상태에서 보여주기 위하여
        recyclerViewExpandableItemManager.expandGroup(adapter.getGroupCount() - 1);

        adapter.setOnChildItemClickListener((view, adapter, groupPosition, childPosition) -> {
            mainTopicListPresenter.onChildItemClick(adapter, groupPosition, childPosition);
        });

        adapter.setOnChildItemLongClickListener((view, adapter, groupPosition, childPosition) -> {
            mainTopicListPresenter.onChildItemLongClick(adapter, groupPosition, childPosition);
            return true;
        });

        adapter.setOnGroupItemClickListener((view, adapter, groupPosition) -> {
            ExpandableTopicAdapter expandableTopicAdapter = (ExpandableTopicAdapter) adapter;
            TopicFolderData topicFolderData = expandableTopicAdapter.getTopicFolderData(groupPosition);
            int folderId = topicFolderData.getFolderId();
            String folderName = topicFolderData.getTitle();
            showGroupSettingPopupView(view, folderId, folderName);
        });

    }

    public void showGroupSettingPopupView(View view, int folderId, String folderName) {

        View popupView = View.inflate(getActivity(), R.layout.popup_folder_setting, null);
        PopupWindow popup = new PopupWindow(popupView);
        popup.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setTouchable(true);
        popup.setFocusable(true);
        popup.setAnimationStyle(-1);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(view);

        TextView tvFolderSettingRename = (TextView) popupView.findViewById(R.id.tv_folder_setting_rename);
        TextView tvFolderSettingRemove = (TextView) popupView.findViewById(R.id.tv_folder_setting_remove);

        tvFolderSettingRename.setOnClickListener(v -> {
            showCreateNewFolderDialog(folderId, folderName);
            popup.dismiss();

        });

        tvFolderSettingRemove.setOnClickListener(v -> {
            mainTopicListPresenter.onDeleteTopicFolder(folderId);
            popup.dismiss();
        });

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
//        if (event.getEvent().equals("folder_create")) {
//
//        } else if (event.getEvent().equals("folder_update")) {
//
//        } else if (event.getEvent().equals("folder_deleted")) {
//
//        } else if (event.getEvent().equals("folder_item_created")) {
//
//        } else if (event.getEvent().equals("folder_item_deleted")) {
//
//        }
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
        recyclerViewExpandableItemManager.expandGroup(adapter.getGroupCount() - 1);
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
    }

    public void showCreateNewFolderDialog(int folderId, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText input = new EditText(getActivity());
        input.setText(name);
        input.setMaxLines(1);
        input.setCursorVisible(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSelection(name.length());

        builder.setView(input)
                .setTitle("Insert folder name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainTopicListPresenter.onRenameFolder(folderId, input.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void onEvent(SocketMessageEvent event) {

        mainTopicListPresenter.onRefreshList();
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


}