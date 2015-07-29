package com.tosslab.jandi.app.ui.message.v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
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
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.files.upload.EntityFileUploadViewModelImpl;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
import com.tosslab.jandi.app.local.database.rooms.marker.JandiMarkerDatabaseManager;
import com.tosslab.jandi.app.local.database.sticker.JandiStickerDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.message.detail.TopicDetailActivity;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.SendingState;
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
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.StickerViewModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

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

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EFragment(R.layout.fragment_message_list)
public class MessageListFragment extends Fragment implements MessageListV2Activity.OnBackPressedListener {

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

    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;
    private MessageState messageState;
    private PublishSubject<MessageQueue> messagePublishSubject;
    private Subscription messageSubscription;
    private boolean isForeground;
    private File photoFileByCamera;
    private StickerInfo stickerInfo = NULL_STICKER;

    @AfterInject
    void initObject() {
        messageState = new MessageState();
        messageState.setFirstItemId(lastMarker);

        messagePublishSubject = PublishSubject.create();

        messageSubscription = messagePublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(messageQueue -> {

                    switch (messageQueue.getQueueType()) {
                        case Saved:
                            getSavedMessageList();
                            break;
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
            newsMessageLoader.setMessageSubscription(messageSubscription);

            MarkerOldMessageLoader oldMessageLoader = new MarkerOldMessageLoader(getActivity());
            oldMessageLoader.setMessageListModel(messageListModel);
            oldMessageLoader.setMessageListPresenter(messageListPresenter);
            oldMessageLoader.setMessageState(messageState);

            this.newsMessageLoader = newsMessageLoader;
            this.oldMessageLoader = oldMessageLoader;

            messageListPresenter.setMarker(lastMarker);
            messageListPresenter.setMoreNewFromAdapter(true);
            messageListPresenter.setGotoLatestLayoutVisible();
        } else {
            NormalNewMessageLoader newsMessageLoader = new NormalNewMessageLoader(getActivity());
            newsMessageLoader.setMessageListModel(messageListModel);
            newsMessageLoader.setMessageListPresenter(messageListPresenter);
            newsMessageLoader.setMessageState(messageState);
            newsMessageLoader.setMessageSubscription(messageSubscription);

            NormalOldMessageLoader oldMessageLoader = new NormalOldMessageLoader(getActivity());
            oldMessageLoader.setMessageListModel(messageListModel);
            oldMessageLoader.setMessageListPresenter(messageListPresenter);
            oldMessageLoader.setMessageState(messageState);
            oldMessageLoader.setEntityId(entityId);
            oldMessageLoader.setTeamId(teamId);

            this.newsMessageLoader = newsMessageLoader;
            this.oldMessageLoader = oldMessageLoader;
        }

        messageListPresenter.setMarkerInfo(teamId, roomId);
        messageListPresenter.setEntityInfo(entityId);
        messageListModel.updateMarkerInfo(teamId, roomId);
        fileUploadStateViewModel.setEntityId(entityId);

        JandiPreference.setKeyboardHeight(getActivity(), 0);
    }

    private void loadOldMessage(MessageQueue messageQueue) {
        if (oldMessageLoader != null) {
            ResMessages resMessages = oldMessageLoader.load(((MessageState) messageQueue.getData()).getFirstItemId());

            if (roomId <= 0) {
                roomId = resMessages.entityId;
                messageListPresenter.setMarkerInfo(teamId, roomId);
                messageListModel.updateMarkerInfo(teamId, roomId);
            }

        }
        messageListModel.trackGetOldMessage(entityType);
    }

    private void loadNewMessage(MessageQueue messageQueue) {
        if (newsMessageLoader != null) {
            MessageState data = (MessageState) messageQueue.getData();
            int lastUpdateLinkId = data.getLastUpdateLinkId();
            if (lastUpdateLinkId < 0 && oldMessageLoader != null) {
                oldMessageLoader.load(lastUpdateLinkId);
            }
            newsMessageLoader.load(lastUpdateLinkId);
        }
    }

    private void sendMessage(MessageQueue messageQueue) {
        SendingMessage data = (SendingMessage) messageQueue.getData();
        int linkId;
        if (data.getStickerInfo() != null) {
            linkId = messageListModel.sendStickerMessage(teamId, entityId, data.getStickerInfo(), data.getMessage());
        } else {
            linkId = messageListModel.sendMessage(data.getLocalId(), data.getMessage());
        }
        if (linkId > 0) {
            messageListPresenter.updateDummyMessageState(data.getLocalId(), SendingState.Complete);
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        } else {
            messageListPresenter.updateDummyMessageState(data.getLocalId(), SendingState.Fail);
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
        messageListPresenter.updateMessage(message);
    }

    private void getSavedMessageList() {
        List<ResMessages.Link> savedMessages = JandiMessageDatabaseManager.getInstance(getActivity()).getSavedMessages(teamId, entityId);
        if (savedMessages != null && !savedMessages.isEmpty()) {
            messageListPresenter.addAll(0, messageListModel.sortDescById(savedMessages));
            FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();
            List<ResMessages.Link> dummyMessages = messageListModel.getDummyMessages(teamId, entityId, me.getName(), me.getUserLargeProfileUrl());
            messageListPresenter.addDummyMessages(dummyMessages);

            messageListPresenter.moveLastPage();
            messageListPresenter.dismissLoadingView();
        } else {
            messageListPresenter.showMessageLoading();
        }
    }

    @AfterViews
    void initViews() {
        setUpActionbar();
        setHasOptionsMenu(true);

        messageListPresenter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.Adapter adapter, int position) {

                MessageListFragment.this.onMessageItemClick(messageListPresenter.getItem
                        (position), entityId);
            }
        });

        messageListPresenter.setOnItemLongClickListener(new MessageListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView.Adapter adapter, int position) {
                MessageListFragment.this.onMessageItemLongClick(messageListPresenter.getItem(position));
                return true;
            }
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

        messageListModel.setEntityInfo(entityType, entityId);

        String tempMessage = JandiMessageDatabaseManager.getInstance(getActivity()).getTempMessage(teamId, entityId);
        messageListPresenter.setSendEditText(tempMessage);

        if (!messageListModel.isEnabledIfUser(entityId)) {
            messageListPresenter.disableChat();
        }

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

        insertEmptyMessage();

        initAnnouncementListeners();

        sendMessagePublisherEvent(new OldMessageQueue(messageState));
        sendMessagePublisherEvent(new NewMessageQueue(messageState));
        sendMessagePublisherEvent(new CheckAnnouncementQueue());

        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(getActivity());

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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void insertEmptyMessage() {
        EntityManager entityManager = EntityManager.getInstance(getActivity());
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

        actionBar.setTitle(EntityManager.getInstance(getActivity()).getEntityNameById(entityId));
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
        FormattedEntity entityById = EntityManager.getInstance(getActivity()).getEntityById(entityId);
        boolean isStarred;
        isStarred = entityById != null ? entityById.isStarred : false;
        ChattingInfomations infomations = new ChattingInfomations(getActivity(), entityId, entityType, isFromPush, isStarred);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        sendMessagePublisherEvent(new NewMessageQueue(messageState));
        fileUploadStateViewModel.registerEventBus();
        PushMonitor.getInstance().register(entityId);

        messageListModel.removeNotificationSameEntityId(entityId);
        fileUploadStateViewModel.initDownloadState();

        EventBus.getDefault().post(new MainSelectTopicEvent(entityId));
    }

    @Override
    public void onPause() {

        fileUploadStateViewModel.unregisterEventBus();

        isForeground = false;

        if (!isFromSearch) {
            messageListModel.stopRefreshTimer();
        }

        messageListModel.saveMessages(teamId, entityId, messageListPresenter.getLastItemsWithoutDummy());
        messageListModel.saveTempMessage(teamId, entityId, messageListPresenter.getSendEditText());
        PushMonitor.getInstance().unregister(entityId);

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
//        messageListPresenter.restartMessageApp(entityId, entityType, isFavorite, teamId);
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

        String message = messageListPresenter.getSendEditText().trim();
        messageListPresenter.setSendEditText("");

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {

            JandiStickerDatabaseManager.getInstance(getActivity()).upsertRecentSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());

            sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(-1, message, new StickerInfo(stickerInfo))));

        } else if (!TextUtils.isEmpty(message)) {
            // insert to db
            long localId = messageListModel.insertSendingMessage(teamId, entityId, message);

            FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();

            // insert to ui
            messageListPresenter.insertSendingMessage(localId, message, me.getName(), me.getUserLargeProfileUrl());

            // networking...
            sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(localId, message)));

        }

        messageListPresenter.dismissStickerPreview();
        stickerInfo = NULL_STICKER;
        messageListPresenter.setEnableSendButton(false);

    }

    public void onEvent(TopicInviteEvent event) {
        if (!isForeground) {
            return;
        }
        onOptionsItemSelected(new MenuBuilder(getActivity()).add(0, R.id.action_entity_invite, 0, ""));
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
        messageListPresenter.updateDummyMessageState(event.getLocalId(), SendingState.Fail);
    }

    public void onEventMainThread(ChatModeChangeEvent event) {
        if (!isForeground) {
            return;
        }
        isFromSearch = false;
        messageListPresenter.setMarker(-1);
        NormalNewMessageLoader normalNewMessageLoader = new NormalNewMessageLoader(getActivity());
        normalNewMessageLoader.setMessageSubscription(messageSubscription);
        normalNewMessageLoader.setMessageState(messageState);
        normalNewMessageLoader.setMessageListPresenter(messageListPresenter);
        normalNewMessageLoader.setMessageListModel(messageListModel);
        newsMessageLoader = normalNewMessageLoader;

        NormalOldMessageLoader normalOldMessageLoader = new NormalOldMessageLoader(getActivity());
        normalOldMessageLoader.setMessageListModel(messageListModel);
        normalOldMessageLoader.setTeamId(teamId);
        normalOldMessageLoader.setEntityId(entityId);
        normalOldMessageLoader.setMessageListPresenter(messageListPresenter);
        normalOldMessageLoader.setMessageState(messageState);
        oldMessageLoader = normalOldMessageLoader;

        messageListPresenter.setMoreNewFromAdapter(false);

        getActivity().supportInvalidateOptionsMenu();

        if (event.isClicked()) {
            messageListPresenter.setGotoLatestLayoutShowProgress();
            loadLastMessage();
        } else {
            messageListModel.startRefreshTimer();
            messageListPresenter.setGotoLatestLayoutVisibleGone();
        }
    }

    // FIXME
    public void onEvent(TeamInvitationsEvent event) {
        if (!isForeground) {
            return;
        }

//        messageListPresenter.handleInviteEvent(event);
    }

    @Background
    void loadLastMessage() {
        newsMessageLoader.load(messageState.getLastUpdateLinkId());
        messageListPresenter.setGotoLatestLayoutVisibleGone();
        messageListPresenter.moveLastPage();
        messageListModel.startRefreshTimer();

    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }
        filePickerViewModel.selectFileSelector(event.type, MessageListFragment.this, entityId);
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
                    filePickerViewModel.showFileUploadDialog(getActivity(), getFragmentManager(), filePath.get(0), entityId);
                }
                break;
            case FileUploadPreviewActivity.REQUEST_CODE:
                break;
            default:
                break;
        }

    }

    @TextChange(R.id.et_message)
    void onMessageEditChange(TextView tv, CharSequence text) {

        boolean isEmptyText = messageListModel.isEmpty(text);
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
                    roomId);
        } else if (messageListModel.isCommentType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.message
                    .feedbackId, roomId);
        } else if (messageListModel.isStickerCommentType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.message
                    .feedbackId, roomId);
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
        dummyMessage.setSendingState(SendingState.Sending);
        messageListPresenter.justRefresh();
        sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(event.getLocalId(), ((ResMessages.TextMessage) dummyMessage.message).content.body)));
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
        DialogFragment newFragment = DeleteMessageDialogFragment.newInstance(event, false);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // 삭제 확인
    public void onEvent(ConfirmDeleteMessageEvent event) {
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
        filePickerViewModel.startUpload(getActivity(), event.title, event.entityId, event.realFilePath, event.comment);
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

        EntityManager entityManager = EntityManager.getInstance(getActivity());
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
        } catch (RetrofitError e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
        } catch (Exception e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
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

            if (!isForeground) {
                return;
            }
            updateRoomInfo();
        } else {
            if (!isForeground) {
                messageListModel.updateMarkerInfo(teamId, roomId);
                return;
            }

            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        }
    }

    public void onEvent(SocketLinkPreviewMessageEvent event) {
        SocketLinkPreviewMessageEvent.Message message = event.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        sendMessagePublisherEvent(new UpdateMessageQueue(teamId, message.getId()));
    }

    @Background
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

        if (isFromSearch) {
            return;
        }

        if (event.getRoom().getId() == roomId) {
            SocketRoomMarkerEvent.Marker marker = event.getMarker();
            JandiMarkerDatabaseManager.getInstance(getActivity()).updateMarker(teamId, roomId, marker.getMemberId(), marker.getLastLinkId());
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
            FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);
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
            FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);
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
            EntityManager.getInstance(getActivity()).getEntityById(entityId).getEntity().name = event.inputName;

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

        return false;
    }
}