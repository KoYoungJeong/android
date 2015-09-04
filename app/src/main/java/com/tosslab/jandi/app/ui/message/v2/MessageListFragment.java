package com.tosslab.jandi.app.ui.message.v2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.messages.ChatModeChangeEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMensionEvent;
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.files.upload.EntityFileUploadViewModelImpl;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel_;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.UpdateMessage;
import com.tosslab.jandi.app.ui.message.to.queue.CheckAnnouncementQueue;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.SendingMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.UpdateMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NewsMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalNewMessageLoader_;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader_;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.StickerViewModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.app.utils.imeissue.EditableAccomodatingLatinIMETypeNullIssues;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EFragment(R.layout.fragment_message_list)
public class MessageListFragment extends Fragment implements MessageListV2Activity
        .OnBackPressedListener, MessageListV2Activity.OnKeyPressListener {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @FragmentArg
    int entityType;
    @FragmentArg
    int entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    boolean isFromPush = false;
    @FragmentArg
    int teamId;
    @FragmentArg
    boolean isFromSearch = false;
    @FragmentArg
    int lastMarker = -1;
    @FragmentArg
    int roomId;

    @ViewById(R.id.list_messages)
    RecyclerView messageListView;

    @ViewById(R.id.btn_send_message)
    Button sendButton;

    @ViewById(R.id.et_message)
    EditText messageEditText;

    @ViewById(R.id.rv_list_search_members)
    RecyclerView rvListSearchMembers;


    @Bean
    MessageListPresenter messageListPresenter;

    @Bean
    MessageListModel messageListModel;

    @Bean
    KeyboardHeightModel keyboardHeightModel;

    @Bean
    StickerViewModel stickerViewModel;


    @Bean(value = EntityFileUploadViewModelImpl.class)
    FilePickerViewModel filePickerViewModel;

    @Bean
    FileUploadStateViewModel fileUploadStateViewModel;

    @Bean
    AnnouncementModel announcementModel;

    @Bean
    AnnouncementViewModel announcementViewModel;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    MentionControlViewModel mentionControlViewModel;

    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;
    private MessageState messageState;
    private PublishSubject<MessageQueue> messagePublishSubject;
    private Subscription messageSubscription;
    private boolean isForeground;
    private File photoFileByCamera;
    private StickerInfo stickerInfo = NULL_STICKER;
    private boolean isRoomInit;

    @AfterInject
    void initObject() {
        messageState = new MessageState();

        messagePublishSubject = PublishSubject.create();

        messageSubscription = messagePublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(messageQueue -> {

                    switch (messageQueue.getQueueType()) {
                        case Old:
                            loadOldMessage(messageQueue);
                            break;
                        case New:
                            loadNewMessage(messageQueue);
                            break;
                        case Send:
                            sendMessage(messageQueue);
                            break;
                        case CheckAnnouncement:
                            getAnnouncement();
                            break;
                        case Update:
                            updateMessage(messageQueue);
                            break;
                    }
                }, throwable -> {
                    LogUtil.e("Message Publish Fail!!", throwable);
                }, () -> {

                });

        if (isFromSearch) {
            MarkerNewMessageLoader newsMessageLoader = new MarkerNewMessageLoader(getActivity());
            newsMessageLoader.setMessageListModel(messageListModel);
            newsMessageLoader.setMessageListPresenter(messageListPresenter);
            newsMessageLoader.setMessageState(messageState);

            MarkerOldMessageLoader oldMessageLoader = new MarkerOldMessageLoader(getActivity());
            oldMessageLoader.setMessageListModel(messageListModel);
            oldMessageLoader.setMessageListPresenter(messageListPresenter);
            oldMessageLoader.setMessageState(messageState);

            this.newsMessageLoader = newsMessageLoader;
            this.oldMessageLoader = oldMessageLoader;

            messageListPresenter.setMarker(lastMarker);
            messageListPresenter.setMoreNewFromAdapter(true);
            messageListPresenter.setGotoLatestLayoutVisible();

            messageState.setFirstItemId(lastMarker);

        } else {
            NormalNewMessageLoader newsMessageLoader = NormalNewMessageLoader_.getInstance_(getActivity());
            newsMessageLoader.setMessageListModel(messageListModel);
            newsMessageLoader.setMessageListPresenter(messageListPresenter);
            newsMessageLoader.setMessageState(messageState);

            NormalOldMessageLoader oldMessageLoader = NormalOldMessageLoader_.getInstance_(getActivity());
            oldMessageLoader.setMessageListModel(messageListModel);
            oldMessageLoader.setMessageListPresenter(messageListPresenter);
            oldMessageLoader.setMessageState(messageState);
            oldMessageLoader.setTeamId(teamId);

            this.newsMessageLoader = newsMessageLoader;
            this.oldMessageLoader = oldMessageLoader;

            messageState.setFirstItemId(lastMarker);
        }

        messageListPresenter.setEntityInfo(entityId);
        fileUploadStateViewModel.setEntityId(entityId);

        keyboardHeightModel.addOnKeyboardShowListener((isShowing) -> {
            announcementViewModel.setAnnouncementViewVisibility(!isShowing);
        });

        JandiPreference.setKeyboardHeight(getActivity(), 0);
    }

    @AfterViews
    void initViews() {
        int screenView = messageListModel.isPublicTopic(entityType)
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, screenView)
                        .build());

        GoogleAnalyticsUtil.sendScreenName(screenView == ScreenViewProperty.PRIVATE_TOPIC ?
                "PRIVATE_TOPIC" : "PUBLIC_TOPIC");

        setUpActionbar();
        setHasOptionsMenu(true);

        initMessageList();

        messageListModel.setEntityInfo(entityType, entityId);

        String tempMessage;
        if (messageListModel.isUser(entityId)) {

            if (roomId > 0) {
                tempMessage = messageListModel.getReadyMessage(roomId);
            } else {
                tempMessage = "";
            }
        } else {
            tempMessage = messageListModel.getReadyMessage(entityId);

        }
        messageListPresenter.setSendEditText(tempMessage);

        if (!messageListModel.isEnabledIfUser(entityId)) {
            messageListPresenter.disableChat();
        }

        initKeyboardEvent();

        initStickerViewModel();

        insertEmptyMessage();

        initAnnouncementListeners();

        sendInitMessage();

        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(getActivity());

    }

    private void initKeyboardEvent() {

        messageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {


                LogUtil.d("In messageEditText KeyCode : " + keyCode);

                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && getResources().getConfiguration().keyboard != Configuration
                        .KEYBOARD_NOKEYS) {

                    if (!event.isShiftPressed()) {
                        onSendClick();
                        return true;
                    } else {
                        return false;
                    }
                }

                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    //We only look at ACTION_DOWN in this code, assuming that ACTION_UP is redundant.
                    // If not, adjust accordingly.
                    return false;
                } else if (event.getUnicodeChar() ==
                        (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)) {
                    //We are ignoring this character, and we want everyone else to ignore it, too, so
                    // we return true indicating that we have handled it (by ignoring it).
                    return true;
                }

                return false;
            }
        });

    }

    private void initStickerViewModel() {
        stickerViewModel.setOnStickerClick(new StickerViewModel.OnStickerClick() {
            @Override
            public void onStickerClick(int groupId, String stickerId) {
                StickerInfo oldSticker = stickerInfo;
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                showStickerPreview(oldSticker, stickerInfo);
                messageListPresenter.setEnableSendButton(true);
            }
        });
    }

    private void initMessageList() {
        messageListPresenter.setOnItemClickListener((adapter, position) -> {
            try {
                onMessageItemClick(messageListPresenter.getItem(position), entityId);
            } catch (Exception e) {
                messageListPresenter.justRefresh();
            }
        });

        messageListPresenter.setOnItemLongClickListener((adapter, position) -> {
            try {
                onMessageItemLongClick(messageListPresenter.getItem(position));
            } catch (Exception e) {
                messageListPresenter.justRefresh();
            }
            return true;
        });

        ((RecyclerView) getView().findViewById(R.id.list_messages)).setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() == ((MessageListAdapter) recyclerView.getAdapter()).getCount() - 1) {
                    messageListPresenter.setPreviewVisibleGone();

                }
            }
        });
    }

    @Background
    void sendInitMessage() {
        if (roomId <= 0) {
            boolean user = EntityManager.getInstance().getEntityById(entityId).isUser();

            if (!user) {
                roomId = entityId;
            } else if (NetworkCheckUtil.isConnected()) {

                int roomId = initRoomId();

                if (roomId > 0) {
                    this.roomId = roomId;
                }
            }
        }
        if (!isFromSearch) {
            int savedLastLinkId = MarkerRepository.getRepository()
                    .getMyMarker(roomId, messageListModel.getMyId()).getLastLinkId();
            messageState.setFirstItemId(Math.max(savedLastLinkId, lastMarker));
        }

        messageListPresenter.setMarkerInfo(teamId, roomId);
        messageListModel.updateMarkerInfo(teamId, roomId);
        messageListModel.setRoomId(roomId);

        int lastReadLinkId = messageListModel.getLastReadLinkId(roomId, entityId);
        messageListPresenter.setLastReadLinkId(lastReadLinkId);

        sendMessagePublisherEvent(new CheckAnnouncementQueue());
        sendMessagePublisherEvent(new OldMessageQueue(messageState));

        isRoomInit = true;

        if (isForeground) {
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        }

    }

    int initRoomId() {
        try {
            ResMessages oldMessage = messageListModel.getOldMessage(-1, 1);
            return oldMessage.entityId;
        } catch (RetrofitError e) {
            e.printStackTrace();
        }


        return -1;
    }

    private void loadOldMessage(MessageQueue messageQueue) {
        if (oldMessageLoader != null) {
            ResMessages resMessages = oldMessageLoader.load(roomId, ((MessageState) messageQueue
                    .getData()).getFirstItemId());

            if (resMessages != null && roomId <= 0) {
                roomId = resMessages.entityId;
                messageListPresenter.setMarkerInfo(teamId, roomId);
                messageListModel.updateMarkerInfo(teamId, roomId);
                messageListModel.setRoomId(roomId);
            }

        }
        messageListModel.trackGetOldMessage(entityType);
    }

    private void loadNewMessage(MessageQueue messageQueue) {


        if (newsMessageLoader != null) {
            MessageState data = (MessageState) messageQueue.getData();
            int lastUpdateLinkId = data.getLastUpdateLinkId();

            if (lastUpdateLinkId < 0 && oldMessageLoader != null) {
                oldMessageLoader.load(roomId, lastUpdateLinkId);
            }

            newsMessageLoader.load(roomId, lastUpdateLinkId);
        }
    }

    private void sendMessage(MessageQueue messageQueue) {
        SendingMessage data = (SendingMessage) messageQueue.getData();
        int linkId;
        List mentions = data.getMentions();
        if (data.getStickerInfo() != null) {
            linkId = messageListModel.sendStickerMessage(teamId, entityId, data.getStickerInfo(), data.getMessage(), mentions);
        } else {
            linkId = messageListModel.sendMessage(data.getLocalId(), data.getMessage(), mentions);
        }
        if (linkId > 0) {
            messageListPresenter.updateDummyMessageState(data.getLocalId(), SendMessage.Status.COMPLETE);
            if (!JandiSocketManager.getInstance().isConnectingOrConnected()) {
                // 소켓이 안 붙어 있으면 임의로 갱신 요청
                EventBus.getDefault().post(new RefreshNewMessageEvent());
            }
        } else {
            messageListPresenter.updateDummyMessageState(data.getLocalId(), SendMessage.Status.FAIL);
        }
    }

    private void getAnnouncement() {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);
        messageListPresenter.dismissProgressWheel();
        announcementViewModel.setAnnouncement(announcement, announcementModel.isAnnouncementOpened(entityId));
    }

    private void updateMessage(MessageQueue messageQueue) {
        UpdateMessage updateMessage = (UpdateMessage) messageQueue.getData();
        ResMessages.OriginalMessage message =
                messageListModel.getMessage(teamId, updateMessage.getMessageId());

        if (message != null && message instanceof ResMessages.TextMessage) {
            MessageRepository.getRepository().upsertTextMessage((ResMessages.TextMessage) message);
        }

        messageListPresenter.updateMessage(message);
    }

    private void showStickerPreview(StickerInfo oldSticker, StickerInfo stickerInfo) {
        messageListPresenter.showStickerPreview(stickerInfo);
        if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId() || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
            messageListPresenter.loadSticker(stickerInfo);
        }
    }

    @Click(R.id.iv_messages_preview_sticker_close)
    void onStickerPreviewClose() {
        MessageListFragment.this.stickerInfo = NULL_STICKER;
        messageListPresenter.dismissStickerPreview();
        messageListPresenter.setEnableSendButton(false);

    }

    @Click(R.id.vg_message_offline)
    void onOfflineLayerClick() {
        messageListPresenter.dismissOfflineLayer();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void insertEmptyMessage() {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(entityId);
        if (entity != null && !entity.isUser()) {
            int topicMemberCount = entity.getMemberCount();
            int teamMemberCount = entityManager.getFormattedUsersWithoutMe().size();

            if (teamMemberCount <= 0) {
                messageListPresenter.insertTeamMemberEmptyLayout();
            } else if (topicMemberCount <= 1) {
                messageListPresenter.insertTopicMemberEmptyLayout();
            } else {
                messageListPresenter.clearEmptyMessageLayout();
            }

        } else {
            messageListPresenter.insertMessageEmptyLayout();
        }
    }

    private void sendMessagePublisherEvent(MessageQueue messageQueue) {
        if (!messageSubscription.isUnsubscribed()) {
            messagePublishSubject.onNext(messageQueue);
        }
    }

    private void setUpActionbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            activity.setSupportActionBar(toolbar);
        }

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(EntityManager.getInstance().getEntityNameById(entityId));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();

        if (isFromSearch) {
            return;
        }

        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.message_list_menu_basic, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FormattedEntity entityById = EntityManager.getInstance().getEntityById(entityId);
        boolean isStarred;
        isStarred = entityById != null ? entityById.isStarred : false;
        ChattingInfomations infomations =
                new ChattingInfomations(getActivity(), teamId, entityId, entityType, isFromPush, isStarred);
        MenuCommand menuCommand = messageListModel.getMenuCommand(MessageListFragment.this,
                infomations,
                item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (savedInstanceState != null) {
            photoFileByCamera = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_NEW_PHOTO_FILE, photoFileByCamera);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageSubscription.unsubscribe();
        EventBus.getDefault().unregister(this);

        // u must release listener for mentioned copy
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        fileUploadStateViewModel.registerEventBus();

        PushMonitor.getInstance().register(roomId);
        messageListModel.removeNotificationSameEntityId(roomId);

        fileUploadStateViewModel.initDownloadState();

        if (isRoomInit) {
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
            EventBus.getDefault().post(new MainSelectTopicEvent(roomId));
        }

        List<Integer> roomIds = new ArrayList<>();
        roomIds.add(roomId);

        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            if (mentionControlViewModel == null) {
                mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(),
                        messageEditText, rvListSearchMembers, messageListView,
                        roomIds,
                        MentionControlViewModel.MENTION_TYPE_MESSAGE);

                // copy txt from mentioned edittext message
                mentionControlViewModel.registClipboardListener();
            } else {
                mentionControlViewModel.refreshSelectableMembers(roomIds);
            }
        }

        if (NetworkCheckUtil.isConnected()) {
            messageListPresenter.dismissOfflineLayer();
        } else {
            messageListPresenter.showOfflineLayer();
        }
    }

    @Override
    public void onPause() {

        fileUploadStateViewModel.unregisterEventBus();

        isForeground = false;

        if (roomId > 0) {
            messageListModel.saveTempMessage(roomId, messageListPresenter.getSendEditText());
        }
        PushMonitor.getInstance().unregister(roomId);

        super.onPause();
    }

    @Background
    public void updateMarker() {
        try {
            if (messageState.getLastUpdateLinkId() > 0) {
                messageListModel.updateMarker(messageState.getLastUpdateLinkId());
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        }
    }

    @Click(R.id.btn_message_sticker)
    void onStickerClick(View view) {
        boolean selected = view.isSelected();

        if (selected) {
            stickerViewModel.dismissStickerSelector();
        } else {
            int keyboardHeight = JandiPreference.getKeyboardHeight(getActivity());
            if (keyboardHeight > 0) {
                messageListPresenter.hideKeyboard();
                stickerViewModel.showStickerSelector(keyboardHeight);
                if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
                    keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                        if (isShow) {
                            stickerViewModel.dismissStickerSelector();
                        }
                    });
                }
            } else {
                initKeyboardHeight();
            }
        }
    }


    private void initKeyboardHeight() {
        EditText etMessage = messageListPresenter.getSendEditTextView();
        keyboardHeightModel.setOnKeyboardHeightCaptureListener(() -> {
            onStickerClick(getView().findViewById(R.id.btn_message_sticker));
            keyboardHeightModel.setOnKeyboardHeightCaptureListener(null);

        });

        etMessage.requestFocus();
        messageListPresenter.showKeyboard();
    }

    @Click(R.id.ll_messages_go_to_latest)
    void onGotoLatestClick() {
        if (!(oldMessageLoader instanceof NormalOldMessageLoader)) {
            EventBus.getDefault().post(new ChatModeChangeEvent(true));
        }
    }

    @Click(R.id.layout_messages_preview_last_item)
    void onPreviewClick() {
        messageListPresenter.setPreviewVisibleGone();
        messageListPresenter.moveLastPage();
    }

    @Click(R.id.btn_upload_file)
    void onUploadClick() {
        filePickerViewModel.showFileUploadTypeDialog(getFragmentManager());
    }

    @Click(R.id.btn_send_message)
    void onSendClick() {

        String message = messageEditText.getText().toString();
        List<MentionObject> mentions;

        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            ResultMentionsVO mentionInfos = mentionControlViewModel.getMentionInfoObject();
            if (mentionInfos != null) {
                message = mentionInfos.getMessage();
                mentions = mentionInfos.getMentions();
            } else {
                mentions = new ArrayList<>();
            }
        } else {
            mentions = new ArrayList<>();
        }

        ReqSendMessageV3 reqSendMessage = null;

        if (!TextUtils.isEmpty(message)) {
            if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE && mentionControlViewModel.hasMentionMember()) {
                mentionControlViewModel.clear();
                reqSendMessage = new ReqSendMessageV3(message, mentions);
            } else {
                reqSendMessage = new ReqSendMessageV3(message, null);
            }
        }

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            StickerRepository.getRepository().upsertRecentSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
            sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(-1, message, new StickerInfo(stickerInfo), mentions)));
        } else {
            if (TextUtils.isEmpty(message)) {
                return;
            }

            // TODO 데이터 베이스에 삽입해야함.
            long localId;
            if (messageListModel.isUser(entityId)) {
                if (roomId > 0) {
                    localId = messageListModel.insertSendingMessage(roomId, message, mentions);
                } else {
                    // roomId 를 할당받지 못하면 메세지를 보내지 않음
                    return;
                }
            } else {
                localId = messageListModel.insertSendingMessage(entityId, message, mentions);
            }
            FormattedEntity me = EntityManager.getInstance().getMe();
            // insert to ui
            messageListPresenter.insertSendingMessage(localId, message, me.getName(),
                    me.getUserLargeProfileUrl(), mentions);
            // networking...
            sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(localId, reqSendMessage)));
        }

        messageListPresenter.dismissStickerPreview();
        stickerInfo = NULL_STICKER;
        messageListPresenter.setEnableSendButton(false);
        messageListPresenter.setSendEditText("");

    }

    public void onEvent(TopicInviteEvent event) {
        if (!isForeground) {
            return;
        }
        inviteMembersToEntity();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void inviteMembersToEntity() {
        int teamMemberCountWithoutMe = EntityManager.getInstance().getFormattedUsersWithoutMe().size();

        if (teamMemberCountWithoutMe <= 0) {
            invitationDialogExecutor.execute();
        } else {
            InvitationViewModel invitationViewModel = InvitationViewModel_.getInstance_(getActivity());
            invitationViewModel.inviteMembersToEntity(getActivity(), roomId);
        }
    }

    public void onEvent(SendCompleteEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.updateMessageIdAtSendingMessage(event.getLocalId(), event.getId());
    }

    public void onEvent(SendFailEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.updateDummyMessageState(event.getLocalId(), SendMessage.Status.FAIL);
    }

    public void onEventMainThread(ChatModeChangeEvent event) {
        if (!isForeground) {
            return;
        }
        isFromSearch = false;
        messageListPresenter.setMarker(-1);
        NormalNewMessageLoader normalNewMessageLoader = NormalNewMessageLoader_.getInstance_(getActivity());
        normalNewMessageLoader.setMessageListModel(messageListModel);
        normalNewMessageLoader.setMessageState(messageState);
        normalNewMessageLoader.setMessageListPresenter(messageListPresenter);
        newsMessageLoader = normalNewMessageLoader;

        NormalOldMessageLoader normalOldMessageLoader = NormalOldMessageLoader_.getInstance_(getActivity());
        normalOldMessageLoader.setMessageListModel(messageListModel);
        normalOldMessageLoader.setMessageState(messageState);
        normalOldMessageLoader.setMessageListPresenter(messageListPresenter);
        normalOldMessageLoader.setTeamId(teamId);
        oldMessageLoader = normalOldMessageLoader;

        int lastReadLinkId = messageListModel.getLastReadLinkId(roomId, entityId);
        messageListPresenter.setLastReadLinkId(lastReadLinkId);

        messageListPresenter.setMoreNewFromAdapter(false);

        getActivity().supportInvalidateOptionsMenu();

        if (event.isClicked()) {
            messageListPresenter.setGotoLatestLayoutShowProgress();
            loadLastMessage();
        } else {
            messageListPresenter.setGotoLatestLayoutVisibleGone();
        }
    }

    public void onEvent(TeamInvitationsEvent event) {
        if (!isForeground) {
            return;
        }
    }

    @Background
    void loadLastMessage() {
        newsMessageLoader.load(roomId, messageState.getLastUpdateLinkId());
        messageListPresenter.setGotoLatestLayoutVisibleGone();
        messageListPresenter.moveLastPage();

    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }
        filePickerViewModel.selectFileSelector(event.type, MessageListFragment.this, entityId);
    }

    public void onEvent(NetworkConnectEvent event) {
        if (event.isConnected()) {
            if (messageListPresenter.getItemCount() <= 0) {
                // roomId 설정 후...
                sendInitMessage();
            } else {
                sendMessagePublisherEvent(new NewMessageQueue(messageState));
            }

            messageListPresenter.dismissOfflineLayer();

        } else {

            messageListPresenter.showOfflineLayer();

            if (isForeground) {
                messageListPresenter.showGrayToast(JandiApplication.getContext().getString(R.string.jandi_msg_network_offline_warn));
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case FilePickerViewModel.TYPE_UPLOAD_GALLERY:
                break;
            case FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO:
            case FilePickerViewModel.TYPE_UPLOAD_EXPLORER:
                List<String> filePath = filePickerViewModel.getFilePath(getActivity(), requestCode, intent);
                if (filePath != null && filePath.size() > 0) {
                    FileUploadPreviewActivity_.intent(this)
                            .singleUpload(true)
                            .realFilePathList(new ArrayList<>(filePath))
                            .selectedEntityIdToBeShared(entityId)
                            .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
                }
                break;
            case FileUploadPreviewActivity.REQUEST_CODE:
                if (intent != null
                        && intent.getSerializableExtra(
                        FileUploadPreviewActivity.KEY_SINGLE_FILE_UPLOADVO) != null) {
                    final FileUploadVO fileUploadVO = (FileUploadVO) intent.getSerializableExtra(
                            FileUploadPreviewActivity.KEY_SINGLE_FILE_UPLOADVO);
                    startFileUpload(
                            fileUploadVO.getFileName(),
                            fileUploadVO.getEntity(),
                            fileUploadVO.getFilePath(),
                            fileUploadVO.getComment());
                }
                break;
            default:
                break;
        }
    }

    @TextChange(R.id.et_message)
    void onMessageEditChange(TextView tv, CharSequence text) {

        boolean isEmptyText = messageListModel.isEmpty(text) && stickerInfo == NULL_STICKER;
        messageListPresenter.setEnableSendButton(!isEmptyText);

    }

    void onMessageItemClick(ResMessages.Link link, int entityId) {
        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            messageListPresenter.showDummyMessageDialog(dummyMessageLink.getLocalId());

            return;
        }

        if (messageListModel.isFileType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.messageId,
                    roomId, link.messageId);
        } else if (messageListModel.isCommentType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.message
                    .feedbackId, roomId, link.messageId);
        } else if (messageListModel.isStickerCommentType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.message
                    .feedbackId, roomId, link.messageId);
        }
    }

    @OnActivityResult(TopicDetailActivity.REQUEST_DETAIL)
    void onTopicDetailResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        boolean leave = data.getBooleanExtra(TopicDetailActivity.EXTRA_LEAVE, false);

        if (leave) {
            getActivity().finish();
        }
    }

    @OnActivityResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH)
    void onFileDetailResult(Intent data) {
        if (data != null && data.getBooleanExtra(EXTRA_FILE_DELETE, false)) {
            int fileId = data.getIntExtra(EXTRA_FILE_ID, -1);
            if (fileId != -1) {
                messageListPresenter.changeToArchive(fileId);
                MessageRepository.getRepository().updateStatus(fileId, "archived");
            }
        } else {
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        }
    }

    void onMessageItemLongClick(ResMessages.Link link) {

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            if (messageListModel.isFailedDummyMessage(dummyMessageLink)) {
                messageListPresenter.showDummyMessageDialog(dummyMessageLink.getLocalId());
            }

            return;
        }

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            boolean isDirectMessage = messageListModel.isDirectMessage(entityType);
            boolean isMyMessage = messageListModel.isMyMessage(textMessage.writerId) && !isFromSearch;
            messageListPresenter.showMessageMenuDialog(isDirectMessage, isMyMessage, textMessage);
        } else if (messageListModel.isCommentType(link.message)) {
            messageListPresenter.showMessageMenuDialog(((ResMessages.CommentMessage) link.message));
        } else if (messageListModel.isFileType(link.message)) {
        } else if (messageListModel.isStickerType(link.message)) {
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
            boolean isMyMessage = messageListModel.isMyMessage(stickerMessage.writerId) && !isFromSearch;

            if (!isMyMessage) {
                return;
            }
            messageListPresenter.showStickerMessageMenuDialog(isMyMessage, stickerMessage);
        }
    }

    public void onEvent(DummyRetryEvent event) {
        if (!isForeground) {
            return;
        }
        DummyMessageLink dummyMessage = messageListPresenter.getDummyMessage(event.getLocalId());
        dummyMessage.setStatus(SendMessage.Status.SENDING.name());
        messageListPresenter.justRefresh();
        ResMessages.TextMessage dummyMessageContent = (ResMessages.TextMessage) dummyMessage.message;

        List<MentionObject> mentionObjects = new ArrayList<>();

        if (dummyMessageContent.mentions != null) {
            Observable.from(dummyMessageContent.mentions)
                    .subscribe(mentionObjects::add);
        }

        sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(event.getLocalId(),
                new ReqSendMessageV3((dummyMessageContent.content.body), mentionObjects))));
    }

    public void onEvent(DummyDeleteEvent event) {
        if (!isForeground) {
            return;
        }
        DummyMessageLink dummyMessage = messageListPresenter.getDummyMessage(event.getLocalId());
        messageListModel.deleteDummyMessageAtDatabase(dummyMessage.getLocalId());
        messageListPresenter.deleteDummyMessageAtList(event.getLocalId());
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        deleteMessage(event.messageType, event.messageId);

    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.copyToClipboard(event.contentString);
    }

    public void onEvent(ConfirmFileUploadEvent event) {
        LogUtil.d("List fragment onEvent");
        if (!isForeground) {
            return;
        }

        startFileUpload(event.title, event.entityId, event.realFilePath, event.comment);
    }

    private void startFileUpload(String title, int entityId, String filePath, String comment) {
        filePickerViewModel.startUpload(getActivity(), title, entityId, filePath, comment);
    }

    public void onEvent(ConfirmDeleteTopicEvent event) {
        if (!isForeground) {
            return;
        }
        deleteTopic();
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
                .isFromPush(isFromPush)
                .start();
    }

    @Background
    void deleteTopic() {

        messageListPresenter.showProgressWheel();
        try {
            messageListModel.deleteTopic(entityId, entityType);
            messageListModel.trackDeletingEntity(entityType);
            messageListPresenter.finish();
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

    }

    @Background
    void deleteMessage(int messageType, int messageId) {
        messageListPresenter.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
                LogUtil.d("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_STICKER
                    || messageType == MessageItem.TYPE_STICKER_COMMNET) {
                messageListModel.deleteSticker(messageId, messageType);
                LogUtil.d("deleteStickerInBackground : succeed");
            }
            messageListPresenter.deleteLinkByMessageId(messageId);
            MessageRepository.getRepository().deleteLinkByMessageId(messageId);

            messageListModel.trackMessageDeleteSuccess(messageId);

        } catch (RetrofitError e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            messageListModel.trackMessageDeleteFail(errorCode);
        } catch (Exception e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            messageListModel.trackMessageDeleteFail(-1);
        }
        messageListPresenter.dismissProgressWheel();
    }

    public void onEvent(RefreshOldMessageEvent event) {
        if (!isForeground) {
            return;
        }

        if (!messageState.isFirstMessage()) {
            sendMessagePublisherEvent(new OldMessageQueue(messageState));
        }
    }

    public void onEvent(DeleteFileEvent event) {

        messageListPresenter.changeToArchive(event.getId());

    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (!isForeground) {
            messageListModel.updateMarkerInfo(teamId, roomId);
            return;
        }
        sendMessagePublisherEvent(new NewMessageQueue(messageState));
    }

    public void onEvent(RefreshNewMessageEvent event) {
        if (!isForeground) {
            return;
        }
        sendMessagePublisherEvent(new NewMessageQueue(messageState));
    }

    public void onEvent(SocketMessageStarEvent event) {
        int messageId = event.getMessageId();
        boolean starred = event.isStarred();

        messageListPresenter.updateMessageStarred(messageId, starred);
    }

    public void onEvent(SocketMessageEvent event) {
        if (isFromSearch) {
            return;
        }
        boolean isSameRoomId = false;
        String messageType = event.getMessageType();

        if (!TextUtils.equals(messageType, "file_comment")) {

            isSameRoomId = event.getRoom().getId() == roomId;
        } else {
            for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                if (roomId == messageRoom.getId()) {
                    isSameRoomId = true;
                    break;
                }
            }
        }

        if (!isSameRoomId) {
            return;
        }

        if (TextUtils.equals(messageType, "topic_leave") ||
                TextUtils.equals(messageType, "topic_join") ||
                TextUtils.equals(messageType, "topic_invite")) {

            updateRoomInfo();

            updateMentionInfo();
        } else {
            if (!isForeground) {
                messageListModel.updateMarkerInfo(teamId, roomId);
                return;
            }

            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateMentionInfo() {
        mentionControlViewModel.refreshMembers(Arrays.asList(roomId));
    }

    public void onEvent(SocketLinkPreviewMessageEvent event) {
        SocketLinkPreviewMessageEvent.Message message = event.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        sendMessagePublisherEvent(new UpdateMessageQueue(teamId, message.getId()));
    }

    void updateRoomInfo() {
        messageListModel.updateMarkerInfo(teamId, roomId);
        insertEmptyMessage();

        sendMessagePublisherEvent(new NewMessageQueue(messageState));
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

    public void onEvent(RequestUserInfoEvent event) {

        if (!isForeground) {
            return;
        }

        UserInfoDialogFragment_.builder().entityId(event.userId).build().show(getFragmentManager(), "dialog");
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

    public void onEvent(MemberStarredEvent memberStarredEvent) {
        if (memberStarredEvent.getId() == entityId) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
            isFavorite = entity.isStarred;
            refreshActionbar();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void refreshActionbar() {
        setUpActionbar();
        getActivity().invalidateOptionsMenu();
    }

    public void onEvent(ProfileChangeEvent event) {
        messageListPresenter.justRefresh();
    }

    public void onEvent(ConfirmModifyTopicEvent event) {

        if (!isForeground) {
            return;
        }

        modifyEntity(event);
    }


    @Background
    void modifyEntity(ConfirmModifyTopicEvent event) {
        messageListPresenter.showProgressWheel();
        try {
            messageListModel.modifyTopicName(entityType, entityId, event.inputName);

            modifyEntitySucceed(event.inputName);

            messageListModel.trackChangingEntityName(entityType);
            EntityManager.getInstance().getEntityById(entityId).getEntity().name = event.inputName;

        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                messageListPresenter.showFailToast(getString(R.string.err_entity_duplicated_name));
            } else {
                messageListPresenter.showFailToast(getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            messageListPresenter.showFailToast(getString(R.string.err_entity_modify));
        } finally {
            messageListPresenter.dismissProgressWheel();
        }
    }

    @UiThread
    void modifyEntitySucceed(String changedEntityName) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(changedEntityName);
    }

    /*
    Announcement
     */
    private void initAnnouncementListeners() {
        announcementViewModel.setOnAnnouncementCloseListener(() -> {
            announcementViewModel.openAnnouncement(false);
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, false);
        });
        announcementViewModel.setOnAnnouncementOpenListener(() -> {
            announcementViewModel.openAnnouncement(true);
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, true);
        });
    }

    public void onEvent(SocketAnnouncementEvent event) {
        SocketAnnouncementEvent.Type eventType = event.getEventType();
        switch (eventType) {
            case CREATED:
            case DELETED:
                if (!isForeground) {
                    messageListModel.updateMarkerInfo(teamId, roomId);
                    return;
                }
                sendMessagePublisherEvent(new NewMessageQueue(messageState));
                sendMessagePublisherEvent(new CheckAnnouncementQueue());
                break;
            case STATUS_UPDATED:
                if (!isForeground) {
                    announcementModel.setActionFromUser(false);
                    messageListModel.updateMarkerInfo(teamId, roomId);
                    return;
                }
                SocketAnnouncementEvent.Data data = event.getData();
                if (data != null) {
                    if (!announcementModel.isActionFromUser()) {
                        announcementViewModel.openAnnouncement(data.isOpened());
                    }
                }
                announcementModel.setActionFromUser(false);
                break;
        }
    }

    public void onEvent(AnnouncementEvent event) {
        switch (event.getAction()) {
            case CREATE:
                checkAnnouncementExistsAndCreate(event.getMessageId());
                break;
            case DELETE:
                deleteAnnouncement();
                break;
        }
    }

    @Background
    public void onEvent(MessageStarredEvent event) {

        if (!isForeground) {
            return;
        }

        int messageId = event.getMessageId();
        switch (event.getAction()) {
            case STARRED:
                try {
                    messageListModel.registStarredMessage(teamId, messageId);
                    messageListPresenter.showSuccessToast(getString(R.string.jandi_message_starred));
                    messageListPresenter.modifyStarredInfo(messageId, true);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }

                break;
            case UNSTARRED:
                try {
                    messageListModel.unregistStarredMessage(teamId, messageId);
                    messageListPresenter.showSuccessToast(getString(R.string.jandi_unpinned_message));
                    messageListPresenter.modifyStarredInfo(messageId, false);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void onEvent(SelectedMemberInfoForMensionEvent event) {
        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    @Background
    void checkAnnouncementExistsAndCreate(int messageId) {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);

        if (announcement == null || announcement.isEmpty()) {
            createAnnouncement(messageId);
            return;
        }

        announcementViewModel.showCreateAlertDialog((dialog, which) -> createAnnouncement(messageId));
    }

    @Background
    void createAnnouncement(int messageId) {

        messageListPresenter.showProgressWheel();
        announcementModel.createAnnouncement(teamId, roomId, messageId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    @Background
    void deleteAnnouncement() {
        messageListPresenter.showProgressWheel();
        announcementModel.deleteAnnouncement(teamId, roomId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    @Override
    public boolean onBackPressed() {

        if (stickerViewModel.isShowStickerSelector()) {
            stickerViewModel.dismissStickerSelector();
            return true;
        }

        if (mentionControlViewModel != null && mentionControlViewModel.isMentionListVisible()) {
            mentionControlViewModel.dismissMentionList();
            return true;
        }

        return false;
    }

    @Override
    public boolean onKey(int keyCode, KeyEvent event) {

        if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_POUND)
                || (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_PERIOD)
                || (keyCode >= KeyEvent.KEYCODE_GRAVE && keyCode <= KeyEvent.KEYCODE_AT)) {
            if (!messageEditText.isFocused()) {
                messageEditText.requestFocus();
                messageEditText.setSelection(messageEditText.getText().length());
                return true;
            }
        }

        return false;
    }
}