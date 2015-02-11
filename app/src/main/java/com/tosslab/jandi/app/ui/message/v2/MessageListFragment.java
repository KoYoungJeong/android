package com.tosslab.jandi.app.ui.message.v2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.to.SendingState;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.views.listeners.SimpleListViewScrollListener;

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
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

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

    @Bean
    MessageListPresenter messageListPresenter;

    @Bean
    MessageListModel messageListModel;

    private MessageState messageState;
    private PublishSubject<LoadType> messagePublishSubject;
    private Subscription messageSubscription;

    @AfterInject
    void initObject() {
        messageState = new MessageState();

        messagePublishSubject = PublishSubject.create();

        messageSubscription = messagePublishSubject.observeOn(Schedulers.io())
                .subscribe(loadType -> {

                    switch (loadType) {
                        case Saved:
                            getSavedMessageList();
                            break;
                        case Old:
                            getOldMessageList(messageState.getFirstItemId());
                            messageListModel.trackGetOldMessage(entityType);

                            break;
                        case New:
                            getNewMessageList(messageState.getLastUpdateLinkId());
                            break;
                    }
                }, throwable -> {

                }, () -> {

                });

    }

    private void getSavedMessageList() {
        List<ResMessages.Link> savedMessages = JandiMessageDatabaseManager.getInstance(getActivity()).getSavedMessages(teamId, entityId);
        if (savedMessages != null) {
            messageListPresenter.addAll(0, messageListModel.sortDescById(savedMessages));
            FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();
            List<ResMessages.Link> dummyMessages = messageListModel.getDummyMessages(teamId, entityId, me.getName(), me.getUserLargeProfileUrl());
            messageListPresenter.addDummyMessages(dummyMessages);

            messageListPresenter.moveLastPage();
        } else {
            messageListPresenter.showProgressWheel();
        }
    }

    @AfterViews
    void initViews() {

        setUpActionbar();
        setHasOptionsMenu(true);

        ((StickyListHeadersListView) getView().findViewById(R.id.list_messages)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMessageItemClick(messageListPresenter.getItem(position));
            }
        });

        ((StickyListHeadersListView) getView().findViewById(R.id.list_messages)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onMessageItemLonkClick(messageListPresenter.getItem(position));
                return true;
            }
        });

        ((StickyListHeadersListView) getView().findViewById(R.id.list_messages)).setOnScrollListener(new SimpleListViewScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    messageListPresenter.setPreviewVisibleGone();
                }

            }
        });

        messageListModel.setEntityInfo(entityType, entityId);

        String tempMessage = JandiMessageDatabaseManager.getInstance(getActivity()).getTempMessage(teamId, entityId);
        messageListPresenter.setSendEditText(tempMessage);

        sendMessagePublisherEvent(LoadType.Saved);
        sendMessagePublisherEvent(LoadType.Old);

    }

    private void sendMessagePublisherEvent(LoadType old) {
        if (!messageSubscription.isUnsubscribed()) {
            messagePublishSubject.onNext(old);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageSubscription.unsubscribe();
    }

    private void setUpActionbar() {
        final ActionBar actionBar = getActivity().getActionBar();
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

        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.message_list_menu_basic, menu);
        MenuItem item = menu.getItem(FAVORITE_MENU_ITEM);
        if (isFavorite) {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav);
        } else {
            item.setIcon(R.drawable.jandi_icon_actionbar_fav_off);
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
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean isStarred = EntityManager.getInstance(getActivity()).getEntityById(entityId).isStarred;
        MenuCommand menuCommand = messageListModel.getMenuCommand(new ChattingInfomations(getActivity(), entityId, entityType, isFromPush, isStarred), item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        messageListModel.startRefreshTimer();
        PushMonitor.getInstance().register(entityId);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        messageListModel.stopRefreshTimer();

        messageListModel.saveMessages(teamId, entityId, messageListPresenter.getLastItemsWithoutDummy());
        messageListModel.saveTempMessage(teamId, entityId, messageListPresenter.getSendEditText());
        PushMonitor.getInstance().unregister(entityId);
    }

    void getOldMessageList(int linkId) {
        try {

            ResMessages oldMessage = messageListModel.getOldMessage(linkId);

            Collections.sort(oldMessage.messages, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            messageState.setFirstItemId(oldMessage.firstIdOfReceivedList);
            messageState.setFirstMessage(oldMessage.isFirst);

            if (linkId == -1) {

                messageListPresenter.clearMessages();

                messageListPresenter.addAll(0, oldMessage.messages);

                FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();
                List<ResMessages.Link> dummyMessages = messageListModel.getDummyMessages(teamId, entityId, me.getName(), me.getUserLargeProfileUrl());
                messageListPresenter.addDummyMessages(dummyMessages);

                messageState.setLastUpdateLinkId(oldMessage.lastLinkId);
                messageListPresenter.moveLastPage();

                updateMarker();

            } else {

                int lastVisibleLinkId = messageListPresenter.getFirstVisibleItemLinkId();
                int firstVisibleItemTop = messageListPresenter.getFirstVisibleItemTop();
                messageListPresenter.addAll(0, oldMessage.messages);
                messageListPresenter.moveToMessage(lastVisibleLinkId, firstVisibleItemTop);

            }

            if (!oldMessage.isFirst) {
                messageListPresenter.setLoadingComplete();
            } else {
                if (linkId != -1) {
                    messageListPresenter.showNoMoreMessage();
                }
                messageListPresenter.setNoMoreLoading();
            }

        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        } finally {
            messageListPresenter.dismissProgressWheel();
        }
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

    void getNewMessageList(int linkId) {
        if (linkId <= 0) {
            return;
        }

        messageListModel.stopRefreshTimer();

        try {
            ResUpdateMessages newMessage = messageListModel.getNewMessage(linkId);

            if (newMessage.updateInfo.messageCount > 0) {
                int lastItemPosition = messageListPresenter.getLastItemPosition();
                messageListPresenter.addAll(lastItemPosition, newMessage.updateInfo.messages);
                messageState.setLastUpdateLinkId(newMessage.lastLinkId);
                updateMarker();

                ResMessages.Link lastUpdatedMessage = newMessage.updateInfo.messages.get(newMessage.updateInfo.messages.size() - 1);
                if (!messageListModel.isMyMessage(lastUpdatedMessage.message.writerId))
                    messageListPresenter.showPreviewIfNotLastItem();
            }


        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        } finally {
            if (!messageSubscription.isUnsubscribed()) {
                messageListModel.startRefreshTimer();
            }
        }
    }

    @Click(R.id.layout_messages_preview_last_item)
    void onPreviewClick() {
        messageListPresenter.moveLastPage();
    }

    @Click(R.id.btn_upload_file)
    void onUploadClick() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    @Click(R.id.btn_send_message)
    void onSendClick() {

        String message = messageListPresenter.getSendEditText();
        if (!(TextUtils.isEmpty(message))) {
            messageListPresenter.setSendEditText("");
            // insert to db
            long localId = messageListModel.insertSendingMessage(teamId, entityId, message);

            FormattedEntity me = EntityManager.getInstance(getActivity()).getMe();

            // insert to ui
            messageListPresenter.insertSendingMessage(localId, message, me.getName(), me.getUserLargeProfileUrl());

            // networking...
            messageListModel.sendMessage(localId, message);
        }

    }

    public void onEventMainThread(SendCompleteEvent event) {
        messageListPresenter.updateMessageIdAtSendingMessage(event.getLocalId(), event.getId());
    }

    public void onEventMainThread(SendFailEvent event) {
        messageListPresenter.updateDummyMessageState(event.getLocalId(), SendingState.Fail);
    }

    public void onEvent(RequestFileUploadEvent event) {
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                logger.info("RequestFileUploadEvent : from gallery");
                messageListPresenter.openAlbumForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                messageListPresenter.openCameraForActivityResult(MessageListFragment.this);
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

        if (resultCode != Activity.RESULT_OK || intent == null) {
            return;
        }

        String realFilePath;
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:

                Uri data = intent.getData();

                if (data == null) {
                    return;
                }

                realFilePath = ImageFilePath.getPath(getActivity(), data);
                if (GoogleImagePickerUtil.isUrl(realFilePath)) {

                    String downloadDir = GoogleImagePickerUtil.getDownloadPath();
                    String downloadName = GoogleImagePickerUtil.getWebImageName();
                    ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(getActivity(), downloadDir, downloadName);
                    downloadImageAndShowFileUploadDialog(downloadProgress, realFilePath, downloadDir, downloadName);
                } else {
                    showFileUploadDialog(realFilePath);
                }
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:

                realFilePath = intent.getStringExtra("GetPath") + File.separator + intent.getStringExtra("GetFileName");
                showFileUploadDialog(realFilePath);
                break;
            default:
                break;
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

            if (messageListModel.isFailedDummyMessage(dummyMessageLink)) {
                messageListPresenter.showDummyMessageDialog(dummyMessageLink.getLocalId());
            }

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
//        getNewMessageList(messageState.getLastUpdateLinkId());
        if (data != null && data.getBooleanExtra(EXTRA_FILE_DELETE, false)) {
            int fileId = data.getIntExtra(EXTRA_FILE_ID, -1);
            if (fileId != -1) {
                messageListPresenter.changeToArchive(fileId);
            }
        } else {
            sendMessagePublisherEvent(LoadType.New);
        }
    }

    void onFileDetailResult() {
        sendMessagePublisherEvent(LoadType.New);
    }

    void onMessageItemLonkClick(ResMessages.Link link) {

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

            if (messageListModel.isFailedDummyMessage(dummyMessageLink)) {
                messageListPresenter.showDummyMessageDialog(dummyMessageLink.getLocalId());
            }

            return;
        }

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            boolean isMyMessage = messageListModel.isMyMessage(textMessage.writerId);
            messageListPresenter.showMessageMenuDialog(isMyMessage, textMessage);
        } else if (link.message instanceof ResMessages.CommentMessage) {
            messageListPresenter.showMessageMenuDialog(((ResMessages.CommentMessage) link.message));
        }
    }

    public void onEvent(DummyRetryEvent event) {
        DummyMessageLink dummyMessage = messageListPresenter.getDummyMessage(event.getLocalId());
        dummyMessage.setSendingState(SendingState.Sending);
        messageListModel.sendMessage(dummyMessage.getLocalId(), ((ResMessages.TextMessage) dummyMessage.message).content.body);
    }

    public void onEvent(DummyDeleteEvent event) {
        DummyMessageLink dummyMessage = messageListPresenter.getDummyMessage(event.getLocalId());
        messageListModel.deleteDummyMessageAtDatabase(dummyMessage.getLocalId());
        messageListPresenter.deleteDummyMessageAtList(event.getLocalId());
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        DialogFragment newFragment = DeleteMessageDialogFragment.newInstance(event);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // 삭제 확인
    public void onEvent(ConfirmDeleteMessageEvent event) {
        deleteMessage(event.messageType, event.messageId);
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        messageListPresenter.copyToClipboard(event.contentString);
    }


    public void onEvent(ConfirmFileUploadEvent event) {
        ProgressDialog uploadProgress = messageListPresenter.getUploadProgress(event);

        uploadFile(event, uploadProgress);
    }

    public void onEvent(ConfirmDeleteTopicEvent event) {
        deleteTopic();
    }


    public void onEvent(final RequestMoveDirectMessageEvent event) {
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

        try {
            messageListModel.deleteTopic(entityId, entityType);
            messageListModel.trackDeletingEntity(entityType);
            messageListPresenter.finish();
        } catch (JandiNetworkException e) {
            logger.error("Topic Delete Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
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


            sendMessagePublisherEvent(LoadType.New);
        } catch (Exception e) {
            logger.error("Upload Error : ", e);
            messageListPresenter.showFailToast(getString(R.string.err_file_upload_failed));
        } finally {
            messageListPresenter.dismissProgressDialog(uploadProgressDialog);
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
        }
        messageListPresenter.dismissProgressWheel();
    }

    public void onEvent(RefreshOldMessageEvent event) {

        if (!messageState.isFirstMessage()) {
            sendMessagePublisherEvent(LoadType.Old);
//            getOldMessageList(messageState.getFirstItemId());
        }
    }

    public void onEvent(RefreshNewMessageEvent event) {
//        getNewMessageList(messageState.getLastUpdateLinkId());
        sendMessagePublisherEvent(LoadType.New);
    }

    public void onEvent(RequestUserInfoEvent event) {

        UserInfoDialogFragment_.builder().entityId(event.userId).build().show(getFragmentManager(), "dialog");
    }

    public void onEvent(ConfirmModifyTopicEvent event) {
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
        } finally {
            messageListPresenter.dismissProgressWheel();
        }
    }

    @UiThread
    void modifyEntitySucceed(String changedEntityName) {
        getActivity().getActionBar().setTitle(changedEntityName);
    }

    private enum LoadType {
        Saved, Old, New
    }
}


