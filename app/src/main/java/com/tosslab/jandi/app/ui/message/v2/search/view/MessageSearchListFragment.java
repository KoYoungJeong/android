package com.tosslab.jandi.app.ui.message.v2.search.view;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
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
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_message_list)
public class MessageSearchListFragment extends Fragment implements MessageSearchListPresenter.View {
    @FragmentArg
    int entityType;
    @FragmentArg
    int entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    int teamId;
    @FragmentArg
    int lastMarker = -1;
    @FragmentArg
    int roomId;
    @ViewById(R.id.lv_messages)
    RecyclerView lvMessages;

    @ViewById(R.id.btn_send_message)
    View sendButton;

    @ViewById(R.id.et_message)
    EditText etMessage;

    @SystemService
    ClipboardManager clipboardManager;

    @SystemService
    InputMethodManager inputMethodManager;

    @ViewById(R.id.vg_messages_preview_last_item)
    View vgPreview;

    @ViewById(R.id.iv_message_preview_user_profile)
    SimpleDraweeView ivPreviewProfile;

    @ViewById(R.id.tv_message_preview_user_name)
    TextView tvPreviewUserName;

    @ViewById(R.id.tv_message_preview_content)
    TextView tvPreviewContent;

    @ViewById(R.id.vg_messages_input)
    View vgMessageInput;

    @ViewById(R.id.vg_messages_go_to_latest)
    View vgMoveToLatest;

    @ViewById(R.id.vg_messages_disable_alert)
    View vDisabledUser;

    @ViewById(R.id.layout_messages_empty)
    LinearLayout layoutEmpty;

    @ViewById(R.id.layout_messages_loading)
    View vgProgressForMessageList;

    @ViewById(R.id.img_go_to_latest)
    View vMoveToLatest;

    @ViewById(R.id.progress_go_to_latest)
    View progressGoToLatestView;

    @ViewById(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;

    @ViewById(R.id.iv_messages_preview_sticker_image)
    ImageView imgStickerPreview;

    @ViewById(R.id.vg_message_offline)
    View vgOffline;

    @Bean
    MessageListPresenter messageListPresenter;

    @Bean
    AnnouncementViewModel announcementViewModel;

    @Bean(MessageSearchListPresenterImpl.class)
    MessageSearchListPresenter messageSearchListPresenter;

    private boolean isForeground;
    private boolean isRoomInit;

    @AfterInject
    void initObject() {

        messageSearchListPresenter.setView(this);
        messageSearchListPresenter.setDefaultInfos(teamId, roomId, entityId, lastMarker, entityType);

        SendMessageRepository.getRepository().deleteAllOfCompletedMessages();

        messageListPresenter.initAdapter(true);

        messageListPresenter.setMarker(lastMarker);
        messageListPresenter.setMoreNewFromAdapter(true);
        messageListPresenter.setGotoLatestLayoutVisible();


        messageListPresenter.setEntityInfo(entityId);
    }

    @AfterViews
    void initViews() {
        int screenView = EntityManager.getInstance().getEntityById(entityId).isPublicTopic()
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, screenView)
                        .build());

        setUpActionbar();
        setHasOptionsMenu(true);

        initMessageList();

        messageSearchListPresenter.checkEnabledUser(entityId);
        messageSearchListPresenter.onInitRoomInfo();
        announcementViewModel.setOnAnnouncementOpenListener(() -> {
            announcementViewModel.openAnnouncement(true);
            messageSearchListPresenter.onAccouncementOpen();
            AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.Announcement_ExpandFromMinimize);

        });
        announcementViewModel.setOnAnnouncementCloseListener(() -> {
            announcementViewModel.openAnnouncement(false);
            messageSearchListPresenter.onAnnouncementClose();
            AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.Accouncement_Minimize);
        });

        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(entityType == JandiConstants.TYPE_DIRECT_MESSAGE, getActivity());

        AnalyticsUtil.sendScreenName(getScreen(entityId));

    }

    @Override
    public AnalyticsValue.Screen getScreen(int entityId) {
        return EntityManager
                .getInstance()
                .getEntityById(entityId).isUser() ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
    }

    @Override
    public void showMessageMenuDialog(boolean isDirectMessage, boolean isMyMessage, ResMessages.TextMessage textMessage) {
        messageListPresenter.showMessageMenuDialog(isDirectMessage, isMyMessage, textMessage);
    }

    @Override
    public void showMessageMenuDialog(ResMessages.CommentMessage message) {
        messageListPresenter.showMessageMenuDialog(message);
    }

    @Override
    public void deleteLinkByMessageId(int messageId) {
        messageListPresenter.deleteLinkByMessageId(messageId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return messageSearchListPresenter.onOptionItemSelected(MessageSearchListFragment.this, item, teamId, entityId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        messageSearchListPresenter.onDestory();
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;

        PushMonitor.getInstance().register(roomId);

        if (isRoomInit) {
            messageSearchListPresenter.onRequestNewMessage();
            EventBus.getDefault().post(new MainSelectTopicEvent(roomId));
        }

        if (NetworkCheckUtil.isConnected()) {
            messageListPresenter.dismissOfflineLayer();
        } else {
            messageListPresenter.showOfflineLayer();
        }
    }

    @Override
    public void onPause() {

        isForeground = false;

        PushMonitor.getInstance().unregister(roomId);

        super.onPause();
    }

    private void initMessageList() {
        messageListPresenter.setOnItemClickListener((adapter, position) -> {
            try {
                messageListPresenter.hideKeyboard();
                messageSearchListPresenter.onMessageItemClick(MessageSearchListFragment.this, messageListPresenter.getItem(position), entityId);
            } catch (Exception e) {
                messageListPresenter.justRefresh();
            }

            int itemViewType = adapter.getItemViewType(position);

            BodyViewHolder.Type type = BodyViewHolder.Type.values()[itemViewType];
            switch (type) {
                case FileWithoutDivider:
                case File:
                    AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.FileView_ByFile);
                    break;
                case FileComment:
                case FileStickerComment:
                    break;
                case CollapseStickerComment:
                case CollapseComment:
                case PureComment:
                case PureStickerComment:
                    AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.FileView_ByComment);
                    break;
            }
        });

        messageListPresenter.setOnItemLongClickListener((adapter, position) -> {
            try {
                messageSearchListPresenter.onMessageItemLongClick(messageListPresenter.getItem(position));
            } catch (Exception e) {
                messageListPresenter.justRefresh();
            }
            AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap);

            return true;
        });

        lvMessages.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                    messageListPresenter.setPreviewVisibleGone();

                }
            }
        });
    }

    private void setUpActionbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            activity.setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        }

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(EntityManager.getInstance().getEntityNameById(entityId));
    }

    @Click(R.id.vg_messages_go_to_latest)
    void onGotoLatestClick() {
        int itemCount = messageListPresenter.getItemCount();
        int firstCursorLinkId = -1;
        if (itemCount > 0) {
            firstCursorLinkId = messageListPresenter.getItem(0).id;
        }
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(roomId)
                .firstCursorLinkId(firstCursorLinkId)
                .lastMarker(EntityManager.getInstance().getEntityById(entityId).lastLinkId)
                .start();

    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        messageSearchListPresenter.deleteMessage(event.messageType, event.messageId);

        AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Delete);

    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.copyToClipboard(event.contentString);
        AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Copy);
    }

    public void onEvent(ProfileChangeEvent event) {
        messageListPresenter.justRefresh();
    }

    public void onEvent(ConfirmModifyTopicEvent event) {

        if (!isForeground) {
            return;
        }

        messageSearchListPresenter.onModifyEntity(event.inputName);
    }


    public void onEvent(MessageStarredEvent event) {
        if (!isForeground) {
            return;
        }

        int messageId = event.getMessageId();
        switch (event.getAction()) {
            case STARRED:
                messageSearchListPresenter.registStarredMessage(teamId, messageId);

                AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Star);
                break;
            case UNSTARRED:
                messageSearchListPresenter.unregistStarredMessage(teamId, messageId);
                AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Unstar);
                break;
        }
    }

    public void onEvent(UnshareFileEvent event) {
        messageListPresenter.justRefresh();
    }

    public void onEventMainThread(ChatCloseEvent event) {
        if (entityId == event.getCompanionId()) {
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (entityId == event.getId()) {
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicKickedoutEvent event) {
        if (roomId == event.getRoomId()) {
            getActivity().finish();
            CharSequence topicName = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle();
            String msg = JandiApplication.getContext().getString(R.string.jandi_kicked_message, topicName);
            messageListPresenter.showFailToast(msg);
        }
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        if (event.getId() == entityId) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
            isFavorite = entity.isStarred;
            refreshActionbar();
            if (isForeground) {
                closeDialogFragment();
            }
        }
    }

    @UiThread
    void closeDialogFragment() {
        android.app.Fragment dialogFragment = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null && dialogFragment instanceof android.app.DialogFragment) {
            ((android.app.DialogFragment) dialogFragment).dismiss();
        }
    }

    public void onEvent(TeamLeaveEvent event) {

        messageSearchListPresenter.onTeamLeave(event.getTeamId(), event.getMemberId());

    }

    public void onEvent(RefreshNewMessageEvent event) {
        if (!isForeground) {
            return;
        }
        if (isRoomInit) {
            messageSearchListPresenter.onRequestNewMessage();
        }
    }

    public void onEvent(RefreshOldMessageEvent event) {
        if (!isForeground) {
            return;
        }

        messageSearchListPresenter.onRequestOldMessage();

    }

    public void onEvent(DeleteFileEvent event) {
        messageListPresenter.changeToArchive(event.getId());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void refreshActionbar() {
        setUpActionbar();
        getActivity().invalidateOptionsMenu();
    }


    public void onEvent(ShowProfileEvent event) {
        if (!isForeground) {
            return;
        }

        MemberProfileActivity_.intent(getActivity())
                .memberId(event.userId)
                .from(getScreen(entityId) == AnalyticsValue.Screen.Message ?
                        MemberProfileActivity.EXTRA_FROM_MESSAGE : MemberProfileActivity.EXTRA_FROM_TOPIC_CHAT)
                .start();

        if (event.from != null) {

            AnalyticsValue.Screen screen = getScreen(entityId);
            AnalyticsUtil.sendEvent(screen, AnalyticsUtil.getProfileAction(event.userId, event.from));
        }

    }

    public void onEvent(RoomMarkerEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.justRefresh();
    }

    public void onEvent(SocketRoomMarkerEvent event) {
        if (!isForeground) {
            return;
        }

        if (event.getRoom().getId() == roomId) {
            SocketRoomMarkerEvent.Marker marker = event.getMarker();
            MarkerRepository.getRepository().upsertRoomMarker(teamId, roomId, marker.getMemberId(), marker
                    .getLastLinkId());
            messageListPresenter.justRefresh();
        }
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {

        if (!isForeground) {
            return;
        }

        EntityManager entityManager = EntityManager.getInstance();
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .isFromPush(false)
                .start();
    }

    public void onEvent(NetworkConnectEvent event) {
        if (event.isConnected()) {
            if (messageListPresenter.getItemCount() <= 0) {
                // roomId 설정 후...
                messageSearchListPresenter.onInitRoomInfo();
            } else {
                if (isRoomInit) {
                    messageSearchListPresenter.onRequestNewMessage();
                }
            }

            messageListPresenter.dismissOfflineLayer();

        } else {

            messageListPresenter.showOfflineLayer();

            if (isForeground) {
                messageListPresenter.showGrayToast(JandiApplication.getContext().getString(R.string.jandi_msg_network_offline_warn));
            }

        }
    }


    @UiThread
    @Override
    public void modifyEntitySucceed(String changedEntityName) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(changedEntityName);
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        messageListPresenter.showFailToast(message);
    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        messageListPresenter.showSuccessToast(message);
    }

    @Override
    public void modifyStarredInfo(int messageId, boolean starred) {
        messageListPresenter.modifyStarredInfo(messageId, starred);
    }

    @Override
    public void showLeavedMemberDialog(int entityId) {
        messageListPresenter.showLeavedMemberDialog(entityId);
    }

    @Override
    public void setDisableUser() {
        messageListPresenter.setDisableUser();
    }

    @Override
    public void setDisabledUser() {
        sendLayoutVisibleGone();
        vDisabledUser.setVisibility(View.VISIBLE);
        setPreviewVisibleGone();
    }

    @Override
    public void setRoomId(int roomId) {
        this.roomId = roomId;
        messageListPresenter.setMarkerInfo(teamId, roomId);
    }

    @Override
    public void setLastReadLinkId(int realLastLinkId) {
        messageListPresenter.setLastReadLinkId(realLastLinkId);
    }

    @Override
    public void setRoomInit(boolean isRoomInit) {
        this.isRoomInit = isRoomInit;
    }

    @Override
    public boolean isForeground() {
        return isForeground;
    }

    @Override
    public void dismissProgressWheel() {
        messageListPresenter.dismissProgressWheel();
    }

    @Override
    public void setAnnouncement(ResAnnouncement announcement, boolean announcementOpened) {
        announcementViewModel.setAnnouncement(announcement, announcementOpened);
    }

    @Override
    public void openAnnouncement(boolean opened) {
        announcementViewModel.openAnnouncement(opened);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCreateAlertDialog(DialogInterface.OnClickListener onConfirmClick) {
        announcementViewModel.showCreateAlertDialog(onConfirmClick);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        messageListPresenter.showProgressWheel();
    }

    @Override
    public void updateMarkerNewMessage(ResMessages newMessage, boolean isLastLinkId, boolean firstLoad) {
        messageListPresenter.updateMarkerNewMessage(newMessage, isLastLinkId, firstLoad);
    }

    @Override
    public int getFirstVisibleItemLinkId() {
        return messageListPresenter.getFirstVisibleItemLinkId();
    }

    @Override
    public int getItemCount() {
        return messageListPresenter.getItemCount();
    }

    @Override
    public void dismissLoadingView() {
        messageListPresenter.dismissLoadingView();
    }

    @Override
    public int getFirstVisibleItemTop() {
        return messageListPresenter.getFirstVisibleItemTop();
    }

    @Override
    public void updateMarkerMessage(int linkId, ResMessages oldMessage, boolean noFirstLoad,
                                    boolean isFirstMessage, int latestVisibleMessageId,
                                    int firstVisibleItemTop) {
        messageListPresenter.updateMarkerMessage(linkId, oldMessage, noFirstLoad,
                isFirstMessage, latestVisibleMessageId,
                firstVisibleItemTop);
    }

    @Override
    public void showDummyMessageDialog(long localId) {
        messageListPresenter.showDummyMessageDialog(localId);
    }

    @Override
    public void moveFileDetailActivity(Fragment fragment, int messageId, int roomId, int selectedMessageId) {
        messageListPresenter.moveFileDetailActivity(fragment, messageId, roomId, selectedMessageId);
    }

    public void sendLayoutVisibleGone() {
        if (vgMessageInput != null) {
            vgMessageInput.setVisibility(View.GONE);
        }
    }

    public void setPreviewVisibleGone() {
        if (vgPreview != null) {
            vgPreview.setVisibility(View.GONE);
        }
    }


    public void onEvent(SocketAnnouncementEvent event) {
        SocketAnnouncementEvent.Type eventType = event.getEventType();
        switch (eventType) {
            case DELETED:
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.Accouncement_Delete);
            case CREATED:
                messageSearchListPresenter.onCreatedAnnouncement(isForeground, isRoomInit);

                break;
            case STATUS_UPDATED:
                messageSearchListPresenter.onUpdateAnnouncement(isForeground, isRoomInit, event.getData());
                break;
        }
    }

    public void onEvent(AnnouncementEvent event) {
        switch (event.getAction()) {
            case CREATE:
                messageSearchListPresenter.checkAnnouncementExistsAndCreate(event.getMessageId());
                AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Announce);
                break;
            case DELETE:
                messageSearchListPresenter.onDeleteAnnouncement();
                break;
        }
    }

}
