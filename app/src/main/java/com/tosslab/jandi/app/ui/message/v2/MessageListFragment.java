package com.tosslab.jandi.app.ui.message.v2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
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
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
import com.tosslab.jandi.app.local.database.rooms.marker.JandiMarkerDatabaseManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingMessage;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.to.queue.MessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.OldMessageQueue;
import com.tosslab.jandi.app.ui.message.to.queue.SendingMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.MarkerOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NewsMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalNewMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.NormalOldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.loader.OldMessageLoader;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;

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
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EFragment(R.layout.fragment_message_list)
public class MessageListFragment extends Fragment {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";

    private static final Logger logger = Logger.getLogger(MessageListFragment.class);

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
    private OldMessageLoader oldMessageLoader;
    private NewsMessageLoader newsMessageLoader;
    private MessageState messageState;
    private PublishSubject<MessageQueue> messagePublishSubject;
    private Subscription messageSubscription;
    private boolean isForeground;
    private File photoFileByCamera;

    @AfterInject
    void initObject() {
        messageState = new MessageState();
        messageState.setFirstItemId(lastMarker);

        messagePublishSubject = PublishSubject.create();

        messageSubscription = messagePublishSubject.observeOn(Schedulers.io())
                .subscribe(messageQueue -> {

                    switch (messageQueue.getQueueType()) {
                        case Saved:
                            getSavedMessageList();
                            break;
                        case Old:
                            if (oldMessageLoader != null) {
                                ResMessages resMessages = oldMessageLoader.load(((MessageState) messageQueue.getData()).getFirstItemId());

                                if (roomId <= 0) {
                                    roomId = resMessages.entityId;
                                    messageListPresenter.setMarkerInfo(teamId, roomId);
                                    messageListModel.updateMarkerInfo(teamId, roomId);
                                }

                            }
                            messageListModel.trackGetOldMessage(entityType);
                            break;
                        case New:
                            if (newsMessageLoader != null) {
                                MessageState data = (MessageState) messageQueue.getData();
                                int lastUpdateLinkId = data.getLastUpdateLinkId();
                                if (lastUpdateLinkId < 0 && oldMessageLoader != null) {
                                    oldMessageLoader.load(lastUpdateLinkId);
                                }
                                newsMessageLoader.load(lastUpdateLinkId);
                            }
                            break;
                        case Send:
                            SendingMessage data = (SendingMessage) messageQueue.getData();
                            int linkId = messageListModel.sendMessage(data.getLocalId(), data.getMessage());
                            if (linkId  > 0) {
                                messageListPresenter.updateDummyMessageState(data.getLocalId(), SendingState.Complete);
                                EventBus.getDefault().post(new RefreshNewMessageEvent());
                            } else {
                                messageListPresenter.updateDummyMessageState(data.getLocalId(), SendingState.Fail);
                            }
                            break;
                    }
                }, throwable -> {
                    logger.error("Message Publish Fail!!", throwable);
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
        messageListModel.updateMarkerInfo(teamId, roomId);

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

                MessageListFragment.this.onMessageItemClick(messageListPresenter.getItem(position));
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

        sendMessagePublisherEvent(new OldMessageQueue(messageState));
        sendMessagePublisherEvent(new NewMessageQueue(messageState));

        if (!messageListModel.isEnabledIfUser(entityId)) {
            messageListPresenter.disableChat();
        }

        insertEmptyMessage();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void insertEmptyMessage() {
        EntityManager entityManager = EntityManager.getInstance(getActivity());
        FormattedEntity entity = entityManager.getEntityById(entityId);
        if (!entity.isUser()) {
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
        final int FAVORITE_MENU_ITEM = 0;

        menu.clear();

        if (isFromSearch) {
            return;
        }

        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.message_list_menu_basic, menu);
        MenuItem item = menu.getItem(FAVORITE_MENU_ITEM);
        if (isFavorite) {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav);
            item.setTitle(R.string.jandi_unstarred);
        } else {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav_off);
            item.setTitle(R.string.jandi_starred);
        }

        // DirectMessage의 경우 확장 메뉴가 없음.
        if (!messageListModel.isDirectMessage(entityType)) {
            if (messageListModel.isMyTopic(entityId)) {
                if (messageListModel.isDefaultTopic(entityId)) {
                    inflater.inflate(R.menu.manipulate_my_entity_menu_default, menu);
                } else {
                    inflater.inflate(R.menu.manipulate_my_entity_menu, menu);
                }
            } else {
                if (messageListModel.isDefaultTopic(entityId)) {
                    inflater.inflate(R.menu.manipulate_entity_menu_default, menu);
                } else {
                    inflater.inflate(R.menu.manipulate_entity_menu, menu);
                }
            }
        } else {
            inflater.inflate(R.menu.manipulate_direct_message_menu, menu);

            FormattedEntity entityById = EntityManager.getInstance(getActivity()).getEntityById(entityId);
            if (entityById != null) {
                if (!TextUtils.equals(entityById.getUser().status, "enabled")) {
                    menu.removeItem(item.getItemId());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FormattedEntity entityById = EntityManager.getInstance(getActivity()).getEntityById(entityId);
        boolean isStarred;
        if (entityById != null ? entityById.isStarred : false) {
            isStarred = true;
        } else {
            isStarred = false;
        }
        MenuCommand menuCommand = messageListModel.getMenuCommand(new ChattingInfomations(getActivity(), entityId, entityType, isFromPush, isStarred), item);

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

        PushMonitor.getInstance().register(entityId);

        messageListModel.removeNotificationSameEntityId(entityId);
    }

    @Override
    public void onPause() {
        super.onPause();
        isForeground = false;

        if (!isFromSearch) {
            messageListModel.stopRefreshTimer();
        }

        messageListModel.saveMessages(teamId, entityId, messageListPresenter.getLastItemsWithoutDummy());
        messageListModel.saveTempMessage(teamId, entityId, messageListPresenter.getSendEditText());
        PushMonitor.getInstance().unregister(entityId);
    }

    @Background
    public void updateMarker() {
        try {
            if (messageState.getLastUpdateLinkId() > 0) {
                messageListModel.updateMarker(messageState.getLastUpdateLinkId());
            }
        } catch (JandiNetworkException e) {
            logger.error("set marker failed", e);
        } catch (Exception e) {
            logger.error("set marker failed", e);
        }
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
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    @Click(R.id.btn_send_message)
    void onSendClick() {

        String message = messageListPresenter.getSendEditText().trim();
        if (!TextUtils.isEmpty(message)) {
            messageListPresenter.setSendEditText("");
            // insert to db
            long localId = messageListModel.insertSendingMessage(teamId, entityId, message);

            FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();

            // insert to ui
            messageListPresenter.insertSendingMessage(localId, message, me.getName(), me.getUserLargeProfileUrl());

            // networking...
            sendMessagePublisherEvent(new SendingMessageQueue(new SendingMessage(localId, message)));

        }

    }

    public void onEvent(TopicInviteEvent event) {
        if (!isForeground) {
            return;
        }
        onOptionsItemSelected(new MenuBuilder(getActivity()).add(0, R.id.action_entity_invite, 0, ""));
    }

    public void onEventMainThread(SendCompleteEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.updateMessageIdAtSendingMessage(event.getLocalId(), event.getId());
    }

    public void onEventMainThread(SendFailEvent event) {
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

    public void onEvent(TeamInvitationsEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.handleInviteEvent(event);
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
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                logger.info("RequestFileUploadEvent : from gallery");
                messageListPresenter.openAlbumForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                try {
                    File directory = new File(GoogleImagePickerUtil.getDownloadPath());
                    photoFileByCamera = File.createTempFile("camera", ".jpg", directory);
                    messageListPresenter.openCameraForActivityResult(MessageListFragment.this, Uri.fromFile(photoFileByCamera));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                logger.info("RequestFileUploadEvent : from explorer");
                messageListPresenter.openExplorerForActivityResult(MessageListFragment.this);
                break;
            default:
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String realFilePath;
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:

                if (intent == null) {
                    return;
                }
                Uri data = intent.getData();

                if (data != null) {
                    realFilePath = ImageFilePath.getPath(getActivity(), data);
                    if (GoogleImagePickerUtil.isUrl(realFilePath)) {

                        String downloadDir = GoogleImagePickerUtil.getDownloadPath();
                        String downloadName = GoogleImagePickerUtil.getWebImageName();
                        ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(getActivity(), downloadDir, downloadName);
                        downloadImageAndShowFileUploadDialog(downloadProgress, realFilePath, downloadDir, downloadName);
                    } else {
                        showFileUploadDialog(realFilePath);
                    }
                }
                break;

            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                if (photoFileByCamera != null && photoFileByCamera.exists()) {
                    showFileUploadDialog(photoFileByCamera.getAbsolutePath());
                }

                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:

                realFilePath = intent.getStringExtra("GetPath") + File.separator + intent.getStringExtra("GetFileName");
                if (!TextUtils.isEmpty(realFilePath)) {
                    showFileUploadDialog(realFilePath);
                }
                break;
            default:
                break;
        }

    }

    @Background
    void saveAndShowFileUploadDialog(Bitmap bitmap) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHssmm");

        String path = GoogleImagePickerUtil.getDownloadPath() + "/camera" + dateFormat.format(System.currentTimeMillis()) + ".jpg";
        new File(path).delete();
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            showFileUploadDialog(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Background
    void downloadImageAndShowFileUploadDialog(ProgressDialog downloadProgress, String realFilePath, String downloadDir, String downloadName) {

        try {
            File file = GoogleImagePickerUtil.downloadFile(getActivity(), downloadProgress, realFilePath, downloadDir, downloadName);
            messageListPresenter.dismissProgressDialog(downloadProgress);
            showFileUploadDialog(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // File Upload 대화상자 보여주기
    @UiThread
    void showFileUploadDialog(String realFilePath) {
        // 업로드 파일 용량 체크

        if (messageListModel.isOverSize(realFilePath)) {
            messageListPresenter.exceedMaxFileSizeError();
        } else {
            DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, entityId);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }

    @TextChange(R.id.et_message)
    void onMessageEditChange(TextView tv, CharSequence text) {

        boolean isEmptyText = messageListModel.isEmpty(text);
        messageListPresenter.setEnableSendButton(!isEmptyText);

    }

    void onMessageItemClick(ResMessages.Link link) {

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            messageListPresenter.showDummyMessageDialog(dummyMessageLink.getLocalId());

            return;
        }

        if (messageListModel.isFileType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.messageId);
        } else if (messageListModel.isCommentType(link.message)) {
            messageListPresenter.moveFileDetailActivity(MessageListFragment.this, link.message.feedbackId);
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
            boolean isMyMessage = messageListModel.isMyMessage(textMessage.writerId) && !isFromSearch;
            messageListPresenter.showMessageMenuDialog(isMyMessage, textMessage);
        } else if (messageListModel.isCommentType(link.message)) {
            messageListPresenter.showMessageMenuDialog(((ResMessages.CommentMessage) link.message));
        } else if (messageListModel.isFileType(link.message)) {
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
        if (!isForeground) {
            return;
        }
        ProgressDialog uploadProgress = messageListPresenter.getUploadProgress(event);

        uploadFile(event, uploadProgress);
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
        } catch (JandiNetworkException e) {
            logger.error("Topic Delete Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
        } catch (Exception e) {
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

    }


    @Background
    void uploadFile(ConfirmFileUploadEvent event, ProgressDialog uploadProgressDialog) {
        boolean isPublicTopic = entityType == JandiConstants.TYPE_PUBLIC_TOPIC;
        try {
            JsonObject result = messageListModel.uploadFile(event, uploadProgressDialog, isPublicTopic);
            if (result.get("code") == null) {

                logger.error("Upload Success : " + result);
                messageListPresenter.showSuccessToast(getString(R.string.jandi_file_upload_succeed));
                messageListModel.trackUploadingFile(entityType, result);
            } else {
                logger.error("Upload Fail : Result : " + result);
                messageListPresenter.showFailToast(getString(R.string.err_file_upload_failed));
            }
            sendMessagePublisherEvent(new NewMessageQueue(messageState));
        } catch (ExecutionException e) {
            if (getActivity() != null) {
                messageListPresenter.showFailToast(getString(R.string.jandi_canceled));
            }
        } catch (Exception e) {
            logger.error("Upload Error : ", e);
            if (getActivity() != null) {
                messageListPresenter.showFailToast(getString(R.string.err_file_upload_failed));
            }
        } finally {
            if (getActivity() != null) {
                messageListPresenter.dismissProgressDialog(uploadProgressDialog);
            }
        }
    }

    @Background
    void deleteMessage(int messageType, int messageId) {
        messageListPresenter.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
                logger.debug("deleteMessageInBackground : succeed");
            }
        } catch (JandiNetworkException e) {
            logger.error("deleteMessageInBackground : FAILED", e);
        } catch (Exception e) {
            logger.error("deleteMessageInBackground : FAILED", e);
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

    public void onEvent(ChatCloseEvent event) {
        if (entityId == event.getCompanionId()) {
            getActivity().finish();
        }
    }

    public void onEvent(TopicDeleteEvent event) {
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

        } catch (JandiNetworkException e) {
            logger.error("modify failed " + e.getErrorInfo(), e);
            if (e.errCode == JandiNetworkException.DUPLICATED_NAME) {
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

}


