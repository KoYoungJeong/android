package com.tosslab.jandi.app.ui.message.v2.search.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListSearchAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog;
import com.tosslab.jandi.app.ui.message.v2.search.dagger.DaggerMessageSearchListComponent;
import com.tosslab.jandi.app.ui.message.v2.search.dagger.MessageSearchListModule;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.DateAnimator;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.RecyclerScrollStateListener;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

public class MessageSearchListFragment extends Fragment implements MessageSearchListPresenter.View {
    @Nullable
    @InjectExtra
    int entityType;
    @Nullable
    @InjectExtra
    long entityId;
    @Nullable
    @InjectExtra
    boolean isFavorite = false;
    @Nullable
    @InjectExtra
    long teamId;
    @Nullable
    @InjectExtra
    long lastMarker = -1;
    @Nullable
    @InjectExtra
    long roomId;
    @Bind(R.id.lv_messages)
    RecyclerView lvMessages;

    @Bind(R.id.btn_send_message)
    View sendButton;

    @Bind(R.id.et_message)
    EditText etMessage;

    InputMethodManager inputMethodManager;

    @Bind(R.id.vg_messages_preview_last_item)
    View vgPreview;

    @Bind(R.id.iv_message_preview_user_profile)
    ImageView ivPreviewProfile;

    @Bind(R.id.tv_message_preview_user_name)
    TextView tvPreviewUserName;

    @Bind(R.id.tv_message_preview_content)
    TextView tvPreviewContent;

    @Bind(R.id.vg_messages_input)
    View vgMessageInput;

    @Bind(R.id.vg_messages_go_to_latest)
    View vgMoveToLatest;

    @Bind(R.id.vg_messages_member_status_alert)
    View vDisabledUser;

    @Bind(R.id.vg_messages_read_only_and_disable)
    View vgReadOnly;
    @Bind(R.id.tv_messages_read_only_and_disable_title)
    TextView tvReadOnlyTitle;
    @Bind(R.id.tv_messages_read_only_and_disable_description)
    TextView tvReadOnlyDescription;

    @Bind(R.id.layout_messages_empty)
    LinearLayout layoutEmpty;

    @Bind(R.id.layout_messages_loading)
    View vgProgressForMessageList;

    @Bind(R.id.iv_go_to_latest)
    View vMoveToLatest;

    @Bind(R.id.progress_go_to_latest)
    View progressGoToLatestView;

    @Bind(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;

    @Bind(R.id.iv_messages_preview_sticker_image)
    ImageView imgStickerPreview;

    @Bind(R.id.vg_message_offline)
    View vgOffline;
    @Bind(R.id.progress_message)
    View oldProgressBar;

    AnnouncementViewModel announcementViewModel;

    @Inject
    MessageSearchListPresenter messageSearchListPresenter;
    DateAnimator dateAnimator;
    @Bind(R.id.tv_messages_date_divider)
    TextView tvMessageDate;
    private boolean isForeground;
    private boolean isRoomInit;
    private MessageListSearchAdapter messageAdapter;
    private OfflineLayer offlineLayer;
    private ProgressWheel progressWheelForAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dart.inject(this, getArguments());
        DaggerMessageSearchListComponent.builder()
                .messageSearchListModule(new MessageSearchListModule(this))
                .build()
                .inject(this);
        initObject();
        initViews();
    }

    void initObject() {

        messageSearchListPresenter.setDefaultInfos(teamId, roomId, entityId, lastMarker, entityType);

        SendMessageRepository.getRepository().deleteAllOfCompletedMessages();

        messageAdapter = new MessageListSearchAdapter(getActivity());

        messageAdapter.setMarker(lastMarker);
        messageAdapter.setMoreFromNew(true);
        messageAdapter.setEntityId(entityId);

        progressWheelForAction = new ProgressWheel(getActivity());

        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    void initViews() {
        int screenView = TeamInfoLoader.getInstance().isPublicTopic(entityId)
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        SprinklrScreenView.sendLog(screenView);

        setUpActionbar();
        setHasOptionsMenu(true);
        sendLayoutInvisible();
        showGotoLatestView();
        setPreviewVisibleGone();

        initMessageList();

        offlineLayer = new OfflineLayer(vgOffline);
        if (!NetworkCheckUtil.isConnected()) {
            offlineLayer.showOfflineView();
        }

        messageSearchListPresenter.checkEnabledUser(entityId);
        messageSearchListPresenter.onInitRoomInfo();
        if (announcementViewModel == null) {
            announcementViewModel = new AnnouncementViewModel(getContext(), getView());
        }
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

        AnalyticsUtil.sendScreenName(getScreen(entityId));

    }

    private void showGotoLatestView() {
        if (vgMoveToLatest != null) {
            vgMoveToLatest.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public AnalyticsValue.Screen getScreen(long entityId) {
        return TeamInfoLoader
                .getInstance()
                .isUser(entityId) ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
    }

    @Override
    public void showMessageMenuDialog(boolean isDirectMessage, boolean isMyMessage, ResMessages.TextMessage textMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment
                .newInstanceByTextMessage(textMessage, isMyMessage, isDirectMessage);
        newFragment.show(getFragmentManager(), "dioalog");
    }

    @Override
    public void showMessageMenuDialog(ResMessages.CommentMessage commentMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment
                .newInstanceByCommentMessage(commentMessage, false);
        newFragment.show(getFragmentManager(), "dioalog");
    }

    @Override
    public void deleteLinkByMessageId(long messageId) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.remove(position);
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuCommand menuCommand = MenuCommandBuilder.init((AppCompatActivity) getActivity())
                .with(this)
                .teamId(teamId)
                .entityId(entityId)
                .build(item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }
        return false;
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
            dismissOfflineLayer();
        } else {
            showOfflineLayer();
        }
    }

    void dismissOfflineLayer() {
        offlineLayer.dismissOfflineView();
    }

    void showOfflineLayer() {
        offlineLayer.showOfflineView();
    }


    @Override
    public void onPause() {

        isForeground = false;

        PushMonitor.getInstance().unregister(roomId);

        super.onPause();
    }

    private void initMessageList() {

        vgProgressForMessageList.setAlpha(0f);
        vgProgressForMessageList.animate()
                .alpha(1.0f)
                .setDuration(150);

        lvMessages.setAdapter(messageAdapter);
        lvMessages.setItemAnimator(null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        lvMessages.setLayoutManager(layoutManager);

        MessageListHeaderAdapter messageListHeaderAdapter = new MessageListHeaderAdapter(getActivity(), messageAdapter);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(messageAdapter)
                .setRecyclerView(lvMessages)
                .setSticky(false)
                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
                .build();

        lvMessages.addItemDecoration(stickyHeadersItemDecoration);

        messageAdapter.setOnItemClickListener((adapter, position) -> {
            try {
                hideKeyboard();
                messageSearchListPresenter.onMessageItemClick(MessageSearchListFragment.this, messageAdapter.getItem(position), entityId);
            } catch (Exception e) {
            }
        });

        messageAdapter.setOnItemLongClickListener((adapter, position) -> {
            try {
                messageSearchListPresenter.onMessageItemLongClick(messageAdapter.getItem(position));
            } catch (Exception e) {
            }
            AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap);

            return true;
        });

        dateAnimator = new DateAnimator(tvMessageDate);
        RecyclerScrollStateListener recyclerScrollStateListener = new RecyclerScrollStateListener();
        recyclerScrollStateListener.setListener(scrolling -> {
            if (scrolling) {
                dateAnimator.show();
            } else {
                dateAnimator.hide();
            }
        });

        // 스크롤 했을 때 동작
        lvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() == messageAdapter.getItemCount() - 1) {
                    if (messageAdapter.isEndOfLoad()) {
                        onGotoLatestClick();
                    }
                }

                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                Date date = ((MessageListSearchAdapter) recyclerView.getAdapter()).getItemDate(firstVisibleItemPosition);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    tvMessageDate.setText(DateTransformator.getTimeStringForDivider(calendar.getTimeInMillis()));
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                recyclerScrollStateListener.onScrollState(newState);
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

        actionBar.setTitle(TeamInfoLoader.getInstance().getName(entityId));
    }

    @OnClick(R.id.vg_messages_go_to_latest)
    void onGotoLatestClick() {
        int itemCount = messageAdapter.getItemCount();
        long firstCursorLinkId = -1;
        if (itemCount > 0) {
            firstCursorLinkId = messageAdapter.getItem(0).id;
        }
        startActivity(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(roomId)
                .firstCursorLinkId(firstCursorLinkId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        getActivity().overridePendingTransition(0, 0);
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
        copyToClipboard(event.contentString);
        AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Copy);
    }

    private void copyToClipboard(String contentString) {
        final ClipData clipData = ClipData.newPlainText("", contentString);
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void onEvent(ProfileChangeEvent event) {
        justRefresh();
    }

    private void justRefresh() {
        Completable.fromCallable(() -> {

            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                messageAdapter.notifyItemRangeChanged(0, itemCount);
            }
            resetUnreadCnt();
            return true;
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    /**
     * @see MessageListV2Presenter#resetUnreadCnt()
     */
    public void resetUnreadCnt() {
        if (messageAdapter.getItemCount() <= 0) {
            return;
        }

        if (TeamInfoLoader.getInstance().isChat(roomId)) {
            long companionId = TeamInfoLoader.getInstance().getChat(roomId).getCompanionId();
            User user = TeamInfoLoader.getInstance().getUser(companionId);
            if (user != null && user.isDisabled()) {

                int count = messageAdapter.getItemCount();
                for (int idx = 0; idx < count; idx++) {
                    messageAdapter.getItem(idx).unreadCnt = 0;
                }
                return;
            }
        }

        List<Marker> markers = RoomMarkerRepository.getInstance().getRoomMarkers(roomId);

        List<Long> memberLastReadLinks = new ArrayList<>();

        for (Marker marker : markers) {
            memberLastReadLinks.add(marker.getReadLinkId());
        }

        Collections.sort(memberLastReadLinks);

        int unreadCnt = 0;

        while ((unreadCnt < memberLastReadLinks.size() - 1) && memberLastReadLinks.get(unreadCnt) < 0) {
            unreadCnt++;
        }

        long linkCursor = memberLastReadLinks.get(unreadCnt);

        for (int j = 0; j < messageAdapter.getItemCount(); j++) {
            if (messageAdapter.getItem(j).id <= linkCursor) {
                messageAdapter.getItem(j).unreadCnt = unreadCnt;
            } else {
                while (unreadCnt < memberLastReadLinks.size() - 1 &&
                        linkCursor == memberLastReadLinks.get(unreadCnt)) {
                    unreadCnt++;
                }
                linkCursor = memberLastReadLinks.get(unreadCnt);
                messageAdapter.getItem(j).unreadCnt = unreadCnt;
            }
        }
    }

    public void onEvent(MessageStarredEvent event) {
        if (!isForeground) {
            return;
        }

        long messageId = event.getMessageId();
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
        justRefresh();
    }

    public void onEventMainThread(ChatCloseEvent event) {
        if (entityId == event.getCompanionId()) {
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (entityId == event.getTopicId()) {
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicKickedoutEvent event) {
        if (roomId == event.getRoomId()) {
            getActivity().finish();
            CharSequence topicName = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle();
            String msg = JandiApplication.getContext().getString(R.string.jandi_kicked_message, topicName);
            showFailToast(msg);
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

    public void onEventMainThread(DeleteFileEvent event) {
        changeToArchive(event.getId());
    }

    void changeToArchive(long messageId) {
        int position = messageAdapter.indexByMessageId(messageId);
        String archivedStatus = "archived";
        if (position > 0) {
            ResMessages.Link item = messageAdapter.getItem(position);
            item.message.status = archivedStatus;
            item.message.createTime = new Date();

        }

        List<Integer> commentIndexes = messageAdapter.indexByFeedbackId(messageId);

        for (Integer commentIndex : commentIndexes) {
            ResMessages.Link item = messageAdapter.getItem(commentIndex);
            item.feedback.status = archivedStatus;
            item.feedback.createTime = new Date();
        }

        if (position >= 0 || commentIndexes.size() > 0) {

            messageAdapter.notifyItemRangeChanged(0, messageAdapter.getItemCount());
        }
    }

    public void onEventMainThread(ShowProfileEvent event) {
        if (!isForeground) {
            return;
        }

        if (AccessLevelUtil.hasAccessLevel(event.userId)) {
            startActivity(Henson.with(getActivity())
                    .gotoMemberProfileActivity()
                    .memberId(event.userId)
                    .from(getScreen(entityId) == AnalyticsValue.Screen.Message ?
                            MemberProfileActivity.EXTRA_FROM_MESSAGE : MemberProfileActivity.EXTRA_FROM_TOPIC_CHAT)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(getActivity());
        }

        if (event.from != null) {

            AnalyticsValue.Screen screen = getScreen(entityId);
            AnalyticsUtil.sendEvent(screen, AnalyticsUtil.getProfileAction(event.userId, event.from));
        }

    }

    public void onEvent(RoomMarkerEvent event) {
        if (!isForeground) {
            return;
        }

        if (event.getRoomId() == roomId) {
            justRefresh();
        }
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {

        if (!isForeground) {
            return;
        }


        startActivity(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .teamId(TeamInfoLoader.getInstance().getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .isFromPush(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (event.isConnected()) {
            if (messageAdapter.getItemCount() <= 0) {
                // roomId 설정 후...
                messageSearchListPresenter.onInitRoomInfo();
            } else {
                if (isRoomInit) {
                    messageSearchListPresenter.onRequestNewMessage();
                }
            }

            dismissOfflineLayer();

        } else {

            showOfflineLayer();

            if (isForeground) {
                showGrayToast(JandiApplication.getContext().getString(R.string.jandi_msg_network_offline_warn));
            }

        }
    }

    void showGrayToast(String message) {
        ColoredToast.showGray(message);
    }

    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void modifyStarredInfo(long messageId, boolean starred) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.modifyStarredStateByPosition(position, starred);
    }

    @Override
    public void showLeavedMemberDialog(long entityId) {
        String name = TeamInfoLoader.getInstance().getName(entityId);
        String msg = JandiApplication.getContext().getString(R.string.jandi_no_long_team_member, name);

        AlertUtil.showConfirmDialog(getActivity(), msg, null, false);
    }

    @Override
    public void showOldLoadingProgress() {

        Completable.fromAction(() -> {

            if (oldProgressBar.getVisibility() != View.GONE) {
                return;
            }

            oldProgressBar.setVisibility(View.VISIBLE);

            Animation inAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0f);
            inAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
            inAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            inAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());

            oldProgressBar.startAnimation(inAnim);
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    @Override
    public void dismissOldLoadingProgress() {
        Completable.fromAction(() -> {
            if (oldProgressBar.getVisibility() != View.VISIBLE) {
                return;
            }

            Animation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, -1f);
            outAnim.setDuration(oldProgressBar.getContext().getResources().getInteger(R.integer.duration_short_anim));
            outAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            outAnim.setStartTime(AnimationUtils.currentAnimationTimeMillis());

            outAnim.setAnimationListener(new SimpleEndAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    oldProgressBar.setVisibility(View.GONE);
                }
            });
            oldProgressBar.startAnimation(outAnim);

        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public void dismissUserStatusLayout() {
        vDisabledUser.setVisibility(View.GONE);
        vgReadOnly.setVisibility(View.GONE);
    }

    @Override
    public void setInavtiveUser() {
        vDisabledUser.setVisibility(View.VISIBLE);
        vgReadOnly.setVisibility(View.GONE);
    }

    @Override
    public void showReadOnly(boolean readOnly) {
        vgReadOnly.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            vgReadOnly.setOnClickListener(v -> { });
        } else {
            vgReadOnly.setOnClickListener(null);
        }
    }

    @Override
    public void setDisabledUser() {
        vDisabledUser.setVisibility(View.GONE);
        vgReadOnly.setVisibility(View.VISIBLE);
        tvReadOnlyTitle.setText(R.string.room_member_disabled_alert_title);
        tvReadOnlyDescription.setText(R.string.room_member_disabled_alert_body);
    }


    @Override
    public void movePollDetailActivity(long pollId) {
        PollDetailActivity.start(getActivity(), pollId);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void setRoomId(long roomId) {
        this.roomId = roomId;
        messageAdapter.setTeamId(teamId);
        messageAdapter.setRoomId(roomId);
    }

    @Override
    public void setLastReadLinkId(long realLastLinkId) {
        messageAdapter.setLastReadLinkId(realLastLinkId);
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
        Completable.fromAction(() -> {
            if (progressWheelForAction != null && progressWheelForAction.isShowing()) {
                progressWheelForAction.dismiss();
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public void setAnnouncement(Announcement announcement) {
        announcementViewModel.setAnnouncement(announcement);
    }

    @Override
    public void openAnnouncement(boolean opened) {
        announcementViewModel.openAnnouncement(opened);
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();

        if (progressWheelForAction != null) {
            progressWheelForAction.show();
        }

    }

    @Override
    public void updateMarkerNewMessage(ResMessages newMessage, boolean isLastLinkId, boolean firstLoad) {
        Completable.fromAction(() -> {
            long firstVisibleItemLinkId = getFirstVisibleItemLinkId();
            int firstVisibleItemTop = getFirstVisibleItemTop();
            int lastItemPosition = messageAdapter.getItemCount();

            messageAdapter.addAll(lastItemPosition, newMessage.records);
            justRefresh();
            if (!firstLoad && firstVisibleItemLinkId > 0) {
                moveToMessage(firstVisibleItemLinkId, firstVisibleItemTop);
            }
            if (!isLastLinkId) {
                messageAdapter.setNewLoadingComplete();
            } else {
                messageAdapter.setNewNoMoreLoading();
            }

        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    void moveToMessage(long messageId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexByMessageId(messageId);
        ((LinearLayoutManager) lvMessages.getLayoutManager()).scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @Override
    public long getFirstVisibleItemLinkId() {
        if (messageAdapter.getItemCount() > 0) {
            int firstVisibleItemPosition = ((LinearLayoutManager) lvMessages.getLayoutManager()).findFirstVisibleItemPosition();
            if (firstVisibleItemPosition >= 0) {
                return messageAdapter.getItem(firstVisibleItemPosition).messageId;
            } else {
                return -1;
            }
        } else {
            return -1;
        }

    }

    @Override
    public int getItemCount() {
        return messageAdapter.getItemCount();
    }

    @Override
    public void dismissLoadingView() {
        Completable.fromAction(() -> {
            vgProgressForMessageList.animate()
                    .alpha(0f)
                    .setDuration(250);

        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public int getFirstVisibleItemTop() {
        View childAt = lvMessages.getLayoutManager().getChildAt(0);
        if (childAt != null) {
            return childAt.getTop();
        } else {
            return 0;
        }
    }

    @Override
    public void updateMarkerMessage(long linkId, ResMessages oldMessage, boolean noFirstLoad,
                                    boolean isFirstMessage, long latestVisibleMessageId,
                                    int firstVisibleItemTop) {
        Completable.fromAction(() -> {
            messageAdapter.addAll(0, oldMessage.records);
            justRefresh();
            if (latestVisibleMessageId > 0) {
                moveToMessage(latestVisibleMessageId, firstVisibleItemTop);
            } else {
                // if has no first item...

                long messageId = -1;
                for (ResMessages.Link record : oldMessage.records) {
                    if (record.id == linkId) {
                        messageId = record.messageId;
                    }
                }
                if (messageId > 0) {
                    int yPosition = JandiApplication.getContext()
                            .getResources()
                            .getDisplayMetrics().heightPixels * 2 / 5;
                    moveToMessage(messageId, yPosition);
                } else {
                    moveToMessage(oldMessage.records.get(oldMessage.records.size() - 1).messageId, firstVisibleItemTop);
                }
            }

            if (!isFirstMessage) {
                messageAdapter.setOldLoadingComplete();
            } else {
                messageAdapter.setOldNoMoreLoading();
            }

            if (!noFirstLoad) {
                dismissLoadingView();
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public void showDummyMessageDialog(long localId) {
        DummyMessageDialog.showDialog(getChildFragmentManager(), localId);

    }

    @Override
    public void moveFileDetailActivity(Fragment fragment, long messageId, long roomId, long selectedMessageId) {
        startActivityForResult(Henson.with(getActivity())
                .gotoFileDetailActivity()
                .fileId(messageId)
                .roomId(roomId)
                .selectMessageId(selectedMessageId)
                .build(), JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

    }

    public void sendLayoutInvisible() {
        if (vgMessageInput != null) {
            vgMessageInput.setVisibility(View.INVISIBLE);
        }
    }

    private void setPreviewVisibleGone() {
        if (vgPreview != null) {
            vgPreview.setVisibility(View.GONE);
        }
    }


    public void onEvent(SocketAnnouncementDeletedEvent event) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.Accouncement_Delete);
        announcementViewModel.setAnnouncement(null);
    }


    public void onEvent(AnnouncementUpdatedEvent event) {
        messageSearchListPresenter.onUpdateAnnouncement(isFavorite, isRoomInit, event.isOpened());
    }

    public void onEvent(SocketAnnouncementCreatedEvent event) {
        messageSearchListPresenter.onCreatedAnnouncement(isRoomInit);
    }

    public void onEvent(AnnouncementEvent event) {
        switch (event.getAction()) {
            case CREATE:
                AnalyticsUtil.sendEvent(getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Announce);
                break;
            case DELETE:
                messageSearchListPresenter.onDeleteAnnouncement();
                break;
        }
    }

    void hideKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

}
