package com.tosslab.jandi.app.ui.maintab.tabs.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.adapter.MainChatListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter.MainChatListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter.MainChatListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.to.ChatItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.tabs.util.FloatingActionBarDetector;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.search.main.SearchActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_main_chat_list)
public class MainChatListFragment extends Fragment
        implements MainChatListPresenter.View, ListScroller, FloatingActionBarDetector {

    @Bean(MainChatListPresenterImpl.class)
    MainChatListPresenter mainChatListPresenter;

    @FragmentArg
    long selectedEntity;

    @ViewById(R.id.lv_main_chat_list)
    RecyclerView lvChat;

    @ViewById(R.id.layout_main_chat_list_empty)
    View emptyView;

    MainChatListAdapter mainChatListAdapter;
    private boolean foreground;

    @AfterInject
    void initObject() {
        mainChatListAdapter = new MainChatListAdapter(getActivity());
        mainChatListPresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        setHasOptionsMenu(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        lvChat.setLayoutManager(layoutManager);
        lvChat.addItemDecoration(new SimpleDividerItemDecoration());
        mainChatListAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            onEntityItemClick(position);
        });
        mainChatListAdapter.setOnRecyclerItemLongClickListener((view, adapter, position) -> {
            onEntityLongItemClick(((MainChatListAdapter) adapter).getItem(position));
            return true;
        });
        lvChat.setAdapter(mainChatListAdapter);

        mainChatListPresenter.initChatList(getActivity(), selectedEntity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
        mainChatListAdapter.startAnimation();
        mainChatListPresenter.onReloadChatList();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MessageTab);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshListView() {
        mainChatListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean hasChatItems() {
        return mainChatListAdapter != null && mainChatListAdapter.getItemCount() > 0;
    }

    @Override
    public List<ChatItem> getChatItems() {
        return mainChatListAdapter.getChatItems();
    }

    @UiThread
    @Override
    public void setChatItems(List<ChatItem> chatItems) {
        mainChatListAdapter.setChatItem(chatItems);
        mainChatListAdapter.notifyDataSetChanged();
    }

    @Override
    public ChatItem getChatItem(int position) {
        return mainChatListAdapter.getItem(position);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setSelectedItem(long selectedEntityId) {
        mainChatListAdapter.setSelectedEntity(selectedEntityId);
    }

    @Override
    public void moveMessageActivity(long teamId, long entityId, long roomId, long lastLinkId) {
        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(roomId)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .lastReadLinkId(lastLinkId)
                .start();
    }


    @UiThread
    @Override
    public void scrollToPosition(int selectedEntityPosition) {
        if (selectedEntityPosition > 0) {
            lvChat.smoothScrollToPosition(selectedEntityPosition - 1);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void startSelectedItemAnimation() {
        mainChatListAdapter.startAnimation();
        mainChatListAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setStarred(long entityId, boolean isStarred) {
        int position = mainChatListAdapter.findPosition(entityId);
        if (position >= 0) {
            mainChatListAdapter.getItem(position).starred(isStarred);
            mainChatListAdapter.notifyDataSetChanged();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showEmptyLayout() {
        emptyView.setVisibility(View.VISIBLE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void hideEmptyLayout() {
        emptyView.setVisibility(View.GONE);
    }

    public void onEventMainThread(ShowProfileEvent event) {
        if (foreground) {
            MemberProfileActivity_.intent(getActivity())
                    .memberId(event.userId)
                    .from(MemberProfileActivity.EXTRA_FROM_MAIN_CHAT)
                    .start();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsUtil.getProfileAction(event.userId, event.from));
        }
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        if (!foreground) {
            return;
        }
        mainChatListPresenter.onReloadChatList();
    }

    public void onEvent(RoomMarkerEvent event) {
        if (!foreground) {
            return;
        }
        mainChatListPresenter.onReloadChatList();
    }

    public void onEvent(SocketMessageCreatedEvent event) {
        if (!foreground) {
            return;
        }
        mainChatListPresenter.onReloadChatList();
    }

    public void onEvent(ChatListRefreshEvent event) {
        if (!foreground) {
            return;
        }
        mainChatListPresenter.onReloadChatList();
    }

    public void onEvent(MessagePushEvent event) {
        if (!foreground) {
            return;
        }

        if (TextUtils.equals(event.getEntityType(), PushRoomType.CHAT.getName())) {
            mainChatListPresenter.onReloadChatList();
        }
    }

    public void onEvent(RequestMoveDirectMessageEvent event) {

        if (foreground) {
            mainChatListPresenter.onMoveDirectMessage(getActivity(), event.userId);
        }
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        mainChatListPresenter.onEntityStarredUpdate(event.getId());
    }

    public void onEvent(MemberStarredEvent event) {
        mainChatListPresenter.onEntityStarredUpdate(event.getId());
    }

    public void onEvent(ProfileChangeEvent event) {
        if (!foreground) {
            return;
        }
        mainChatListPresenter.onReloadChatList();
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

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.Search);

    }

    public void onEvent(MainSelectTopicEvent event) {
        setSelectedItem(event.getSelectedEntity());
    }

    //TODO 메세지 진입시 네트워크 체킹 ?
    void onEntityItemClick(int position) {

        mainChatListPresenter.onEntityItemClick(getActivity(), position);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.ChooseDM);
        ChatItem chatItem = getChatItem(position);
        if (chatItem != null && chatItem.getEntityId() == TeamInfoLoader.getInstance().getJandiBot().getId()) {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.ChooseJANDI);
        }
    }

    void onEntityLongItemClick(ChatItem chatItem) {
        EntityMenuDialogFragment_.builder()
                .entityId(chatItem.getEntityId())
                .roomId(chatItem.getRoomId())
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @Click(R.id.btn_chat_list_no_messages)
    void chooseUser() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            startActivity(Henson.with(getActivity())
                    .gotoTeamMemberSearchActivity()
                    .isSelectMode(true)
                    .from(TeamMemberSearchActivity.EXTRA_FROM_INVITE_CHAT)
                    .build());

            getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.SelectTeamMember_EmptyData);
        }
    }

    @Override
    public void scrollToTop() {
        if (lvChat != null) {
            lvChat.scrollToPosition(0);
        }
    }

    @Override
    public void onDetectFloatAction(View btnFab) {
        if (btnFab != null) {
            btnFab.setOnClickListener(v -> {
                chooseUser();
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.SelectTeamMember);
            });
        }
    }
}
