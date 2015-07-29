package com.tosslab.jandi.app.ui.maintab.chat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.chat.adapter.MainChatListAdapter;
import com.tosslab.jandi.app.ui.maintab.chat.presenter.MainChatListPresenter;
import com.tosslab.jandi.app.ui.maintab.chat.presenter.MainChatListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.FAButtonUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_main_chat_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainChatListFragment extends Fragment implements MainChatListPresenter.View {

    @Bean(MainChatListPresenterImpl.class)
    MainChatListPresenter mainChatListPresenter;

    @FragmentArg
    int selectedEntity;

    @ViewById(R.id.lv_main_chat_list)
    ListView chatListView;

    @ViewById(R.id.layout_main_chat_list_empty)
    View emptyView;

    MainChatListAdapter mainChatListAdapter;

    @AfterInject
    void initObject() {
        mainChatListAdapter = new MainChatListAdapter(getActivity());
        mainChatListPresenter.setView(this);
    }


    @AfterViews
    void initViews() {

        chatListView.setEmptyView(emptyView);
        chatListView.setAdapter(mainChatListAdapter);

        FAButtonUtil.setFAButtonController(chatListView, getView().findViewById(R.id.btn_main_chat_fab));

        mainChatListPresenter.onInitChatList(getActivity(), selectedEntity);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshListView() {
        mainChatListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean hasChatItems() {
        return mainChatListAdapter != null && mainChatListAdapter.getCount() > 0;
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
    public void setSelectedItem(int selectedEntityId) {
        mainChatListAdapter.setSelectedEntity(selectedEntityId);
        mainChatListAdapter.notifyDataSetChanged();
    }

    @Override
    public void moveMessageActivity(int teamId, int entityId, int roomId, boolean isStarred, int lastLinkId) {
        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(entityId)
                .roomId(roomId)
                .isFavorite(isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .lastMarker(lastLinkId)
                .start();
    }

    @UiThread
    @Override
    public void scrollToPosition(int selectedEntityPosition) {
        if (selectedEntityPosition > 0) {
            chatListView.setSelection(selectedEntityPosition - 1);
        }
    }

    public void onEventMainThread(ProfileDetailEvent event) {
        UserInfoDialogFragment_.builder().entityId(event.getEntityId()).build().show(getFragmentManager(), "dialog");
    }

    public void onEvent(RetrieveTopicListEvent event) {
        mainChatListPresenter.onReloadChatList(getActivity());
    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "file_comment")) {
            for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                if (TextUtils.equals(messageRoom.getType(), "chat")) {
                    mainChatListPresenter.onReloadChatList(getActivity());
                    return;
                }
            }
        } else {

            if (TextUtils.equals(event.getRoom().getType(), "chat")) {
                mainChatListPresenter.onReloadChatList(getActivity());
            }
        }
    }

    public void onEvent(MessagePushEvent event) {
        if (TextUtils.equals(event.getEntityType(), "user")) {
            mainChatListPresenter.onReloadChatList(getActivity());
        }
    }

    public void onEvent(RequestMoveDirectMessageEvent event) {

        mainChatListPresenter.onMoveDirectMessage(getActivity(), event.userId);
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();
    }

    public void onEvent(MainSelectTopicEvent event) {
        setSelectedItem(event.getSelectedEntity());
    }

    //TODO 메세지 진입시 네트워크 체킹 ?
    @ItemClick(R.id.lv_main_chat_list)
    void onEntityItemClick(int position) {

        mainChatListPresenter.onEntityItemClick(getActivity(), position);
    }

    @ItemLongClick(R.id.lv_main_chat_list)
    void onEntityLongItemClick(ChatItem chatItem) {
        EntityMenuDialogFragment_.builder()
                .entityId(chatItem.getEntityId())
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @Click({R.id.btn_main_chat_fab, R.id.layout_main_chat_list_empty})
    void onAddClick() {
        EntityChooseActivity_.intent(getActivity())
                .type(EntityChooseActivity.Type.MESSAGES.name())
                .start();
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
    }

}
