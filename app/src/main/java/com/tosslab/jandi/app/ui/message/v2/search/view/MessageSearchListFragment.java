package com.tosslab.jandi.app.ui.message.v2.search.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListSearchAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenterImpl;
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

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_message_list)
public class MessageSearchListFragment extends Fragment implements MessageSearchListPresenter.View {
    @FragmentArg
    int entityType;
    @FragmentArg
    long entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    long teamId;
    @FragmentArg
    long lastMarker = -1;
    @FragmentArg
    long roomId;
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
    ImageView ivPreviewProfile;

    @ViewById(R.id.tv_message_preview_user_name)
    TextView tvPreviewUserName;

    @ViewById(R.id.tv_message_preview_content)
    TextView tvPreviewContent;

    @ViewById(R.id.vg_messages_input)
    View vgMessageInput;

    @ViewById(R.id.vg_messages_go_to_latest)
    View vgMoveToLatest;

    @ViewById(R.id.vg_messages_member_status_alert)
    View vDisabledUser;

    @ViewById(R.id.layout_messages_empty)
    LinearLayout layoutEmpty;

    @ViewById(R.id.layout_messages_loading)
    View vgProgressForMessageList;

    @ViewById(R.id.iv_go_to_latest)
    View vMoveToLatest;

    @ViewById(R.id.progress_go_to_latest)
    View progressGoToLatestView;

    @ViewById(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;

    @ViewById(R.id.iv_messages_preview_sticker_image)
    ImageView imgStickerPreview;

    @ViewById(R.id.vg_message_offline)
    View vgOffline;
    @ViewById(R.id.progress_message)
    View oldProgressBar;

    @Bean
    AnnouncementViewModel announcementViewModel;

    @Bean(MessageSearchListPresenterImpl.class)
    MessageSearchListPresenter messageSearchListPresenter;
    DateAnimator dateAnimator;
    @ViewById(R.id.tv_messages_date_divider)
    TextView tvMessageDate;
    private boolean isForeground;
    private boolean isRoomInit;
    private MessageListSearchAdapter messageAdapter;
    private OfflineLayer offlineLayer;
    private ProgressWheel progressWheelForAction;

    @AfterInject
    void initObject() {

        messageSearchListPresenter.setView(this);
        messageSearchListPresenter.setDefaultInfos(teamId, roomId, entityId, lastMarker, entityType);

        SendMessageRepository.getRepository().deleteAllOfCompletedMessages();

        messageAdapter = new MessageListSearchAdapter(getActivity());

        messageAdapter.setMarker(lastMarker);
        messageAdapter.setMoreFromNew(true);
        messageAdapter.setEntityId(entityId);

        progressWheelForAction = new ProgressWheel(getActivity());
    }

    @AfterViews
    void initViews() {
        int screenView = TeamInfoLoader.getInstance().isPublicTopic(entityId)
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        SprinklrScreenView.sendLog(screenView);

        setUpActionbar();
        setHasOptionsMenu(true);

        showGotoLatestView();
        setPreviewVisibleGone();

        initMessageList();

        offlineLayer = new OfflineLayer(vgOffline);
        if (!NetworkCheckUtil.isConnected()) {
            offlineLayer.showOfflineView();
        }

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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void deleteLinkByMessageId(long messageId) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.remove(position);
        messageAdapter.notifyDataSetChanged();
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
            dismissOfflineLayer();
        } else {
            showOfflineLayer();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissOfflineLayer() {
        offlineLayer.dismissOfflineView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @Click(R.id.vg_messages_go_to_latest)
    void onGotoLatestClick() {
        int itemCount = messageAdapter.getItemCount();
        long firstCursorLinkId = -1;
        if (itemCount > 0) {
            firstCursorLinkId = messageAdapter.getItem(0).id;
        }
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .entityType(entityType)
                .entityId(entityId)
                .teamId(teamId)
                .roomId(roomId)
                .firstCursorLinkId(firstCursorLinkId)
                .start();
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
        clipboardManager.setPrimaryClip(clipData);
    }

    public void onEvent(ProfileChangeEvent event) {
        justRefresh();
    }

    private void justRefresh() {
        int itemCount = messageAdapter.getItemCount();
        if (itemCount > 0) {
            messageAdapter.notifyItemRangeChanged(0, itemCount);
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
        changeToArchive(event.getId());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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


    @UiThread(propagation = UiThread.Propagation.REUSE)
    void refreshActionbar() {
        setUpActionbar();
        getActivity().invalidateOptionsMenu();
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


        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .teamId(TeamInfoLoader.getInstance().getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .isFromPush(false)
                .start();
    }

    public void onEvent(NetworkConnectEvent event) {
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showGrayToast(String message) {
        ColoredToast.showGray(message);
    }

    @UiThread
    @Override
    public void modifyEntitySucceed(String changedEntityName) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(changedEntityName);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOldLoadingProgress() {

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

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOldLoadingProgress() {

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
    }

    @Override
    public void dismissUserStatusLayout() {
        vDisabledUser.setVisibility(View.GONE);
    }

    @Override
    public void movePollDetailActivity(long pollId) {
        PollDetailActivity.start(getActivity(), pollId);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void setDisabledUser() {
        sendLayoutInvisible();
        vDisabledUser.setVisibility(View.VISIBLE);
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheelForAction != null && progressWheelForAction.isShowing()) {
            progressWheelForAction.dismiss();
        }
    }

    @Override
    public void setAnnouncement(Announcement announcement) {
        announcementViewModel.setAnnouncement(announcement);
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
        dismissProgressWheel();

        if (progressWheelForAction != null) {
            progressWheelForAction.show();
        }

    }

    @UiThread
    @Override
    public void updateMarkerNewMessage(ResMessages newMessage, boolean isLastLinkId, boolean firstLoad) {
        long firstVisibleItemLinkId = getFirstVisibleItemLinkId();
        int firstVisibleItemTop = getFirstVisibleItemTop();
        int lastItemPosition = messageAdapter.getItemCount();

        messageAdapter.addAll(lastItemPosition, newMessage.records);
        messageAdapter.notifyDataSetChanged();
        if (!firstLoad && firstVisibleItemLinkId > 0) {
            moveToMessage(firstVisibleItemLinkId, firstVisibleItemTop);
        }
        if (!isLastLinkId) {
            messageAdapter.setNewLoadingComplete();
        } else {
            messageAdapter.setNewNoMoreLoading();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissLoadingView() {
        vgProgressForMessageList.animate()
                .alpha(0f)
                .setDuration(250);
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

    @UiThread
    @Override
    public void updateMarkerMessage(long linkId, ResMessages oldMessage, boolean noFirstLoad,
                                    boolean isFirstMessage, long latestVisibleMessageId,
                                    int firstVisibleItemTop) {
        messageAdapter.addAll(0, oldMessage.records);

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
    }

    @Override
    public void showDummyMessageDialog(long localId) {
        DummyMessageDialog.showDialog(getChildFragmentManager(), localId);

    }

    @Override
    public void moveFileDetailActivity(Fragment fragment, long messageId, long roomId, long selectedMessageId) {
        FileDetailActivity_
                .intent(fragment)
                .fileId(messageId)
                .roomId(roomId)
                .selectMessageId(selectedMessageId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
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
