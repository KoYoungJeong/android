package com.tosslab.jandi.app.ui.message.v2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.widget.EditText;
import android.widget.ImageView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.messages.SendCompleteEvent;
import com.tosslab.jandi.app.events.messages.SendFailEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.files.upload.EntityFileUploadViewModelImpl;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.markdown.MarkdownLookUp;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketServiceStopEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.messagehandler.MessageLoadHandler;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.KeyboardAreaController;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by tee on 16. 2. 16..
 */
@EBean
public class MessageListV2Presenter {

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @RootContext
    Activity context;

    Fragment fragment;

    @SystemService
    ClipboardManager clipboardManager;

    @Bean
    MessageListModel messageListModel;

    @Bean
    AnnouncementViewModel announcementViewModel;

    @Bean
    AnnouncementModel announcementModel;

    @Bean
    KeyboardAreaController keyboardAreaViewModel;

    @Bean(value = EntityFileUploadViewModelImpl.class)
    FilePickerViewModel filePickerViewModel;

    @Bean
    FileUploadStateViewModel fileUploadStateViewModel;

    MentionControlViewModel mentionControlViewModel;

    View view;

    MessageAdapter messageAdapter;

    MessageLoadHandler messageHandler;

    private long teamId;
    private long roomId;
    private long entityId;
    private int entityType;

    private StickerInfo stickerInfo = NULL_STICKER;

    ///////////////////////////////////// Life cycle ///////////////////////////////////////////////
    public void onConfigurationChanged() {
        mentionControlViewModel.onConfigurationChanged();
        keyboardAreaViewModel.onConfigurationChanged();
    }

    public void onDestroy() {
        messageHandler.messageHandlingQueueUnsubscribe();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Init Presenter ///////////////////////////////////////////

    public void setView(View view) {
        this.view = view;
    }

    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////// Init (Setting & MessageHandling) ////////////////////////////////

    public int getScreenViewMode(int entityType) {
        return messageListModel.isPublicTopic(entityType)
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;
    }

    private void initMessageHandler(long teamId, long roomId, long entityId, long lastReadEntityId) {
        messageHandler = new MessageLoadHandler(teamId, roomId, entityId, lastReadEntityId,
                view, this, messageListModel);
        messageHandler.initQueue();
    }

    @Background
    public void initMessageList(
            long teamId, long roomId, long entityId, int entityType, long lastReadEntityId) {
        this.teamId = teamId;
        this.roomId = roomId;
        this.entityId = entityId;
        this.entityType = entityType;

        if (messageHandler == null) {
            initMessageHandler(teamId, roomId, entityId, lastReadEntityId);
        }
        messageListModel.setEntityInfo(entityType, entityId);
        messageHandler.getInitMessage();
    }

    public void initEditTextMessage() {
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
        view.setSendEditText(tempMessage);
    }

    public void initUserDisabled() {
        if (!messageListModel.isEnabledIfUser(entityId)) {
            view.setDisableUser();
        }
    }

    public void initDownloadState() {
        fileUploadStateViewModel.setEntityId(entityId);
        fileUploadStateViewModel.initDownloadState();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setEmptyMessageViewControl() {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(entityId);
        if (entity != EntityManager.UNKNOWN_USER_ENTITY && !entity.isUser()) {
            int topicMemberCount = entity.getMemberCount();
            int teamMemberCount = entityManager.getFormattedUsersWithoutMe().size();

            if (teamMemberCount <= 0) {
                view.showTeamMemberEmptyLayout();
            } else if (topicMemberCount <= 1) {
                view.showTopicMemberEmptyLayout();
            } else {
                view.clearEmptyMessageLayout();
            }

        } else {
            view.showMessageEmptyLayout();
        }
    }

    public void onEventBusRegister() {
        EventBus.getDefault().register(this);
        fileUploadStateViewModel.registerEventBus();
    }

    public void onEventBusUnregister() {
        EventBus.getDefault().unregister(this);
        fileUploadStateViewModel.unregisterEventBus();
    }

    // 현재 보고 있는 룸에서 노티피케이션 오지 않도록 함
    public void removeNotificationSameEntityId() {
        messageListModel.removeNotificationSameEntityId(roomId);
    }

    public void saveTempWritingMessage(String message) {
        messageListModel.saveTempMessage(roomId, message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////  Network & Socket ///////////////////////////////////////////////

    public void onEvent(NetworkConnectEvent event) {
        if (event.isConnected()) {
            if (getItemCount() <= 0) {
                // roomId 설정 후...
                messageHandler.getInitMessage();
            } else {
                messageHandler.refreshNewMessages();
            }
            view.dismissOfflineLayer();
        } else {
            view.showOfflineLayer();
            if (view.isForeground()) {
                view.showGrayToast(JandiApplication
                        .getContext().getString(R.string.jandi_msg_network_offline_warn));
            }
        }
    }

    public void onEvent(SocketMessageEvent event) {
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
            if (!view.isForeground()) {
                messageListModel.updateMarkerInfo(teamId, roomId);
                return;
            }

            messageHandler.refreshNewMessages();
        }
    }

    public void onEvent(SocketServiceStopEvent event) {
        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        if (!TextUtils.isEmpty(accessToken.getRefreshToken())) {
            // 토큰이 없으면 개망..-o-
            JandiSocketService.startServiceForcily(context);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Analytics //////////////////////////////////////////////////

    public void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsUtil.sendEvent(messageListModel.getScreen(entityId), action);
    }

    public void sendAnalyticsName() {
        AnalyticsUtil.sendScreenName(
                messageListModel.getScreen(entityId));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// View Control ////////////////////////////////////////

    public void messageItemClick(ResMessages.Link link, int itemPosition) {
        try {
            if (messageListModel.isFileType(link.message)) {
                view.moveFileDetailActivity(link.messageId, roomId, link.messageId);
                if (((ResMessages.FileMessage) link.message).content.type.startsWith("image")) {
                    AnalyticsUtil.sendEvent(
                            messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByPhoto);
                } else {
                    AnalyticsUtil.sendEvent(
                            messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByFile);
                }
            } else if (messageListModel.isCommentType(link.message)) {
                view.moveFileDetailActivity(link.message.feedbackId, roomId, link.messageId);
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByComment);
            } else if (messageListModel.isStickerCommentType(link.message)) {
                view.moveFileDetailActivity(link.message.feedbackId, roomId, link.messageId);
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByComment);
            }
        } catch (Exception e) {
            view.justRefresh();
        }

        int itemViewType = messageAdapter.getItemViewType(itemPosition);

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[itemViewType];
        switch (type) {
            case FileWithoutDivider:
            case File:
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByFile);
                break;
            case FileComment:
            case FileStickerComment:
                break;
            case CollapseStickerComment:
            case CollapseComment:
            case PureComment:
            case PureStickerComment:
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.FileView_ByComment);
                break;
        }

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
            view.showDummyMessageDialog(dummyMessageLink.getLocalId());
            return;
        }
    }

    public void messageItemLongClick(ResMessages.Link link) {
        try {
            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

                if (messageListModel.isFailedDummyMessage(dummyMessageLink)) {
                    view.showDummyMessageDialog(dummyMessageLink.getLocalId());
                }

                return;
            }

            if (link.message instanceof ResMessages.TextMessage) {
                ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
                boolean isDirectMessage = messageListModel.isDirectMessage(entityType);
                boolean isOwner = messageListModel.isTeamOwner();
                boolean isMyMessage = (messageListModel.isMyMessage(textMessage.writerId) || isOwner);
                view.showMessageMenuDialog(isDirectMessage, isMyMessage, textMessage);
            } else if (messageListModel.isCommentType(link.message)) {
                view.showMessageMenuDialog(((ResMessages.CommentMessage) link.message));
            } else if (messageListModel.isFileType(link.message)) {
            } else if (messageListModel.isStickerType(link.message)) {
                ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
                boolean isOwner = messageListModel.isTeamOwner();
                boolean isMyMessage = (messageListModel.isMyMessage(stickerMessage.writerId) || isOwner);
                if (!isMyMessage) {
                    return;
                }
                view.showStickerMessageMenuDialog(isMyMessage, stickerMessage);
            }
        } catch (Exception e) {
            view.justRefresh();
        }
    }

    public void setEmptyViewIfNeed() {
        int originItemCount = getItemCount();
        int itemCountWithoutEvent = getItemCountWithoutEvent();
        int eventCount = originItemCount - itemCountWithoutEvent;
        if (itemCountWithoutEvent > 0 || eventCount > 1) {
            // create 이벤트외에 다른 이벤트가 생성된 경우
            view.setEmptyLayoutVisible(false);
        } else {
            // 아예 메세지가 없거나 create 이벤트 외에는 생성된 이벤트가 없는 경우
            view.setEmptyLayoutVisible(true);
        }
    }

    public void buttonEnableControl(CharSequence message) {
        boolean isButtonEnable = messageListModel.isEmpty(message) && stickerInfo == NULL_STICKER;
        view.setEnableSendButton(!isButtonEnable);
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        if (!view.isForeground()) {
            return;
        }
        view.moveDirectMessageList(event.userId);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////// Menu Command /////////////////////////////////////////

    public boolean excuteMenuCommand(Fragment fragment, MenuItem item) {
        MenuCommand menuCommand = messageListModel.getMenuCommand(fragment, teamId, entityId, item);
        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// Load Old Message ////////////////////////////////////////////

    public void setUpOldMessage(List<ResMessages.Link> linkList,
                                int currentItemCount, boolean isFirstMessage) {
        if (currentItemCount == 0) {
            // 첫 로드라면...
            view.clearMessages();
            addAllMessage(0, linkList);
            view.moveLastPage();
            view.dismissLoadingView();
        } else {
            long latestVisibleLinkId = getFirstVisibleItemLinkId();
            int firstVisibleItemTop = view.getFirstVisibleItemTopFromListView();
            addAllMessage(0, linkList);
            moveToMessage(latestVisibleLinkId, firstVisibleItemTop);
        }

        if (!isFirstMessage) {
            setOldLoadingComplete();
        } else {
            setOldNoMoreLoading();
        }
    }

    public void onEvent(RefreshOldMessageEvent event) {
        if (!view.isForeground()) {
            return;
        }
        messageHandler.refreshOldMessages();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////// Load New Message /////////////////////////////////////////////

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setUpNewMessage(List<ResMessages.Link> linkList, long myId, boolean firstLoad) {
        int lastPosition = linkList.size() - 1;
        if (lastPosition < 0) {
            return;
        }

        int visibleLastItemPosition = view.getLastVisibleItemPositionFromListView();
        int lastItemPosition = getLastItemPosition();

        addAllMessage(lastItemPosition, linkList);

        ResMessages.Link lastUpdatedMessage = linkList.get(lastPosition);

        if (!firstLoad
                && visibleLastItemPosition >= 0
                && visibleLastItemPosition < lastItemPosition - 1
                && lastUpdatedMessage.fromEntity != myId) {
            if (!TextUtils.equals(lastUpdatedMessage.status, "archived")) {
                showPreviewIfNotLastItem();
            }
        } else {
            long messageId = lastUpdatedMessage.messageId;
            if (firstLoad) {
                moveLastReadLink();
                setUpLastReadLink();
                view.justRefresh();
            } else if (messageId <= 0) {
                if (lastUpdatedMessage.fromEntity != myId) {
                    moveToMessageById(lastUpdatedMessage.id, 0);
                }
            } else {
                moveToMessage(messageId, 0);
            }
        }
    }

    public void refreshNewMessage() {
        messageHandler.refreshNewMessages();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showPreviewIfNotLastItem() {
        if (view.isVisibleLastItem()) {
            return;
        }

        ResMessages.Link item = messageAdapter.getItem(messageAdapter.getItemCount() - 1);

        if (TextUtils.equals(item.status, "event")) {
            return;
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(item.message.writerId);
        view.setPreviewName(entity.getName());

        String url = ImageUtil.getImageFileUrl(entity.getUserSmallProfileUrl());
        Uri uri = Uri.parse(url);

        if (!EntityManager.getInstance().isBot(entity.getId())) {
            view.showPreviewProfileImage(false, uri);
        } else {
            view.showPreviewProfileImage(true, uri);
        }

        String message;
        if (item.message instanceof ResMessages.FileMessage) {
            message = ((ResMessages.FileMessage) item.message).content.title;
        } else if (item.message instanceof ResMessages.CommentMessage) {
            message = ((ResMessages.CommentMessage) item.message).content.body;
        } else if (item.message instanceof ResMessages.TextMessage) {
            message = ((ResMessages.TextMessage) item.message).content.body;
        } else if (item.message instanceof ResMessages.StickerMessage || item.message instanceof
                ResMessages.CommentStickerMessage) {
            message = String.format("(%s)", JandiApplication
                    .getContext().getString(R.string.jandi_coach_mark_stickers));
        } else {
            message = "";
        }

        SpannableStringBuilder builder =
                new SpannableStringBuilder(TextUtils.isEmpty(message) ? "" : message);

        MarkdownLookUp.text(builder)
                .plainText(true)
                .lookUp(JandiApplication.getContext());

        new MarkdownViewModel(builder, true).execute();

        builder.removeSpan(JandiURLSpan.class);
        view.setPreviewContent(builder);

        view.setPreviewVisible(true);
    }


    public void onEvent(RefreshNewMessageEvent event) {
        if (!view.isForeground()) {
            return;
        }
        messageHandler.refreshNewMessages();
    }

    public void onEvent(LinkPreviewUpdateEvent event) {
        int messageId = event.getMessageId();
        if (messageId <= 0) {
            return;
        }
        messageHandler.refreshLinkPreview(messageId);
    }

    public void onEvent(RefreshConnectBotEvent event) {
        view.justRefresh();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// Send Message ////////////////////////////////////////////////

    @Background
    public void sendMessage(String message) {
//        handleEasterEggSnowing(message);

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

        message = message.trim();
        ReqSendMessageV3 reqSendMessage = null;

        if (!TextUtils.isEmpty(message)) {
            if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE
                    && mentionControlViewModel.hasMentionMember()) {
                reqSendMessage = new ReqSendMessageV3(message, mentions);
            } else {
                reqSendMessage = new ReqSendMessageV3(message, new ArrayList<>());
            }
        }

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            StickerRepository.getRepository().upsertRecentSticker(
                    stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());

            sendSticker();
            if (!TextUtils.isEmpty(message)) {
                sendTextMessage(message, mentions, reqSendMessage);
            }

        } else {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            sendTextMessage(message, mentions, reqSendMessage);
        }

        view.setVisibleStickerPreview(false);
        stickerInfo = NULL_STICKER;
        view.setEnableSendButton(false);
        view.setSendEditText("");

        AnalyticsUtil.sendEvent(messageListModel.getScreen(entityId), AnalyticsValue.Action.Send);
    }

    private void sendTextMessage(String message,
                                 List<MentionObject> mentions, ReqSendMessageV3 reqSendMessage) {
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, message, mentions);
        if (localId > 0) {

            // insert to ui
            view.refreshAll();
            view.moveLastPage();

            // networking...
            messageHandler.sendTextMessage(localId, reqSendMessage);
        }
    }

    public void onEvent(SendCompleteEvent event) {
        if (!view.isForeground()) {
            return;
        }
    }

    public void onEvent(SendFailEvent event) {
        if (!view.isForeground()) {
            return;
        }
        view.refreshAll();
    }

    //    private void handleEasterEggSnowing(String message) {
//        if (isEasterEggMessage(message)) {
//            if (vgEasterEggSnow.getChildCount() > 0) {
//                return;
//            }
//
//            SnowView snowView = new SnowView(getActivity());
//            snowView.setLayoutParams(
//                    new FrameLayout.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            vgEasterEggSnow.addView(snowView);
//
//            SNOWING_EASTEREGG_STARTED = true;
//
//            messageListPresenter.justRefresh();
//        } else if ("설쏴지마".equals(message)) {
//            vgEasterEggSnow.removeAllViews();
//            SNOWING_EASTEREGG_STARTED = false;
//        }
//    }
//
//    private boolean isEasterEggMessage(String message) {
//        if (TextUtils.isEmpty(message)) {
//            return false;
//        }
//
//        return message.contains("눈")
//                || message.contains("雪")
//                || message.toLowerCase().contains("snow");
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////// Handle Message ////////////////////////////////////////////

    @Background
    void deleteMessage(int messageType, long messageId) {
        view.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STRING) {
                messageListModel.deleteMessage(messageId);
                LogUtil.d("deleteMessageInBackground : succeed");
            } else if (messageType == MessageItem.TYPE_STICKER
                    || messageType == MessageItem.TYPE_STICKER_COMMNET) {
                messageListModel.deleteSticker(messageId, messageType);
                LogUtil.d("deleteStickerInBackground : succeed");
            }
            MessageRepository.getRepository().deleteLinkByMessageId(messageId);
            deleteLinkByMessageId(messageId);

            messageListModel.trackMessageDeleteSuccess(messageId);

        } catch (RetrofitError e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            messageListModel.trackMessageDeleteFail(errorCode);
        } catch (Exception e) {
            LogUtil.e("deleteMessageInBackground : FAILED", e);
            messageListModel.trackMessageDeleteFail(-1);
        }
        view.dismissProgressWheel();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void deleteLinkByMessageId(long messageId) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.remove(position);
        view.refreshAll();
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!view.isForeground()) {
            return;
        }

        deleteMessage(event.messageType, event.messageId);

        AnalyticsUtil.sendEvent(messageListModel.getScreen(entityId)
                , AnalyticsValue.Action.MsgLongTap_Delete);

    }

    public void onEvent(DummyRetryEvent event) {
        if (!view.isForeground()) {
            return;
        }
        DummyMessageLink dummyMessage = getDummyMessage(event.getLocalId());
        dummyMessage.setStatus(SendMessage.Status.SENDING.name());
        view.justRefresh();
        if (dummyMessage.message instanceof ResMessages.TextMessage) {

            ResMessages.TextMessage dummyMessageContent =
                    (ResMessages.TextMessage) dummyMessage.message;
            List<MentionObject> mentionObjects = new ArrayList<>();

            if (dummyMessageContent.mentions != null) {
                Observable.from(dummyMessageContent.mentions)
                        .subscribe(mentionObjects::add);
            }

            messageHandler.sendTextMessage(event.getLocalId()
                    , new ReqSendMessageV3((dummyMessageContent.content.body), mentionObjects));
        } else if (dummyMessage.message instanceof ResMessages.StickerMessage) {
            ResMessages.StickerMessage stickerMessage =
                    (ResMessages.StickerMessage) dummyMessage.message;

            StickerInfo stickerInfo = new StickerInfo();
            stickerInfo.setStickerGroupId(stickerMessage.content.groupId);
            stickerInfo.setStickerId(stickerMessage.content.stickerId);

            messageHandler.sendStickerMessage(event.getLocalId(), stickerInfo);
        }
    }

    public void onEvent(DummyDeleteEvent event) {
        if (!view.isForeground()) {
            return;
        }
        DummyMessageLink dummyMessage = getDummyMessage(event.getLocalId());
        messageListModel.deleteDummyMessageAtDatabase(dummyMessage.getLocalId());
        view.refreshAll();
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!view.isForeground()) {
            return;
        }
        final ClipData clipData = ClipData.newPlainText("", event.contentString);
        clipboardManager.setPrimaryClip(clipData);
        AnalyticsUtil.sendEvent(messageListModel.getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Copy);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////// Message Starred //////////////////////////////////////////////

    public void updateMessageStarred(int messageId, boolean starred) {
        int itemCount = messageAdapter.getItemCount();
        for (int idx = 0; idx > itemCount; ++idx) {
            ResMessages.OriginalMessage message = messageAdapter.getItem(idx).message;
            if (message.id == messageId) {
                message.isStarred = starred;
                break;
            }
        }
    }

    public void onEvent(SocketMessageStarEvent event) {
        int messageId = event.getMessageId();
        boolean starred = event.isStarred();
        updateMessageStarred(messageId, starred);
    }

    @Background
    public void onEvent(MessageStarredEvent event) {
        if (!view.isForeground()) {
            return;
        }

        long messageId = event.getMessageId();
        switch (event.getAction()) {
            case STARRED:
                try {
                    messageListModel.registStarredMessage(teamId, messageId);
                    view.showSuccessToast(context.getString(R.string.jandi_message_starred));
                    modifyStarredInfo(messageId, true);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Star);
                break;
            case UNSTARRED:
                try {
                    messageListModel.unregistStarredMessage(teamId, messageId);
                    view.showSuccessToast(context.getString(R.string.jandi_unpinned_message));
                    modifyStarredInfo(messageId, false);
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
                AnalyticsUtil.sendEvent(
                        messageListModel.getScreen(entityId), AnalyticsValue.Action.MsgLongTap_Unstar);
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////// File ////////////////////////////////////////////////////

    private void startFileUpload(String title, long entityId, String filePath, String comment) {
        filePickerViewModel.startUpload(context, title, entityId, filePath, comment);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void changeToArchive(long messageId) {
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
            view.justRefresh();
        }
    }

    public void FileUpdateStatusArchived(int fileId) {
        changeToArchive(fileId);
        MessageRepository.getRepository().updateStatus(fileId, "archived");
    }

    public void onEvent(DeleteFileEvent event) {
        changeToArchive(event.getId());
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (!view.isForeground()) {
            messageListModel.updateMarkerInfo(teamId, roomId);
            return;
        }
        messageHandler.refreshNewMessages();
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!view.isForeground()) {
            return;
        }

        filePickerViewModel.selectFileSelector(event.type, fragment, entityId);

        AnalyticsValue.Action action;
        switch (event.type) {
            default:
            case FilePickerViewModel.TYPE_UPLOAD_GALLERY:
                action = AnalyticsValue.Action.Upload_Photo;
                break;
            case FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO:
                action = AnalyticsValue.Action.Upload_Camera;
                break;
            case FilePickerViewModel.TYPE_UPLOAD_EXPLORER:
                action = AnalyticsValue.Action.Upload_File;
                break;
        }

        AnalyticsValue.Screen screen = messageListModel.getScreen(entityId);
        AnalyticsUtil.sendEvent(screen, action);
    }

    public void onEvent(ConfirmFileUploadEvent event) {
        LogUtil.d("List fragment onEvent");
        if (!view.isForeground()) {
            return;
        }

        startFileUpload(event.title, event.entityId, event.realFilePath, event.comment);
    }

    public void onEvent(FileUploadFinishEvent event) {
        view.justRefresh();
    }


    public void onEvent(UnshareFileEvent event) {
        view.justRefresh();
    }

    public void requestUploadFileByActivityResult(Intent intent) {
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
    }

    public void uploadFileTakePhotoByActivityResult(int requestCode, Intent intent, String savedFilePath) {
        List<String> filePaths = null;

        if (savedFilePath == null) {
            filePaths = filePickerViewModel.getFilePath(context, requestCode, intent);
        } else {
            filePaths = new ArrayList<>();
            String filePath = savedFilePath;
            filePaths.add(filePath);
        }

        if (filePaths != null && filePaths.size() > 0) {
            view.moveFileUploadPreviewActivity(filePaths);
        }
    }

    public void uploadFileExplorerByActivityResult(int requestCode, Intent intent) {
        List<String> filePaths = filePickerViewModel.getFilePath(context, requestCode, intent);

        if (filePaths != null && filePaths.size() > 0) {
            view.moveFileUploadPreviewActivity(filePaths);
        }
    }

    public void setSavedInstancePhotoFile(Bundle outState) {
        if (filePickerViewModel.getUploadedFile() != null) {
            outState.putSerializable(
                    MessageListV2Fragment.EXTRA_NEW_PHOTO_FILE, filePickerViewModel.getUploadedFile());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////// Announcement //////////////////////////////////////////////

    @Background
    public void getAnnouncement() {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);
        view.dismissProgressWheel();
        announcementViewModel.setAnnouncement(announcement,
                announcementModel.isAnnouncementOpened(entityId));
    }

    @Background
    void checkAnnouncementExistsAndCreate(long messageId) {
        ResAnnouncement announcement = announcementModel.getAnnouncement(teamId, roomId);

        if (announcement == null || announcement.isEmpty()) {
            createAnnouncement(messageId);
            return;
        }

        announcementViewModel.showCreateAlertDialog((dialog, which) -> createAnnouncement(messageId));
    }

    @Background
    void createAnnouncement(long messageId) {
        view.showProgressWheel();
        announcementModel.createAnnouncement(teamId, roomId, messageId);

        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    @Background
    void deleteAnnouncement() {
        view.showProgressWheel();
        announcementModel.deleteAnnouncement(teamId, roomId);
        boolean isSocketConnected = JandiSocketManager.getInstance().isConnectingOrConnected();
        if (!isSocketConnected) {
            getAnnouncement();
        }
    }

    public void initAnnouncementListeners() {
        announcementViewModel.setOnAnnouncementCloseListener(() -> {
            announcementViewModel.openAnnouncement(false);
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, false);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat,
                    AnalyticsValue.Action.Accouncement_Minimize);
        });
        announcementViewModel.setOnAnnouncementOpenListener(() -> {
            announcementViewModel.openAnnouncement(true);
            announcementModel.setActionFromUser(true);
            announcementModel.updateAnnouncementStatus(teamId, roomId, true);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat,
                    AnalyticsValue.Action.Announcement_ExpandFromMinimize);
        });
    }

    public void setAnnouncementVisible(boolean visible) {
        announcementViewModel.setAnnouncementViewVisibility(visible);
    }

    public void onEvent(SocketAnnouncementEvent event) {
        SocketAnnouncementEvent.Type eventType = event.getEventType();
        switch (eventType) {
            case DELETED:
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat,
                        AnalyticsValue.Action.Accouncement_Delete);
            case CREATED:
                if (!view.isForeground()) {
                    messageListModel.updateMarkerInfo(teamId, roomId);
                    return;
                }
                getAnnouncement();
                break;
            case STATUS_UPDATED:
                if (!view.isForeground()) {
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
                AnalyticsUtil.sendEvent(messageListModel.getScreen(entityId),
                        AnalyticsValue.Action.MsgLongTap_Announce);
                break;
            case DELETE:
                deleteAnnouncement();
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// Sticker /////////////////////////////////////////////

    private void sendSticker() {
        long localId = messageListModel.insertSendingMessageIfCan(entityId, roomId, stickerInfo);
        if (localId > 0) {
            view.refreshAll();
            view.moveLastPage();

            messageHandler.sendStickerMessage(localId, stickerInfo);
            AnalyticsUtil.sendEvent(
                    messageListModel.getScreen(entityId), AnalyticsValue.Action.Sticker_Send);
        }
    }

    public void showStickerPreview(StickerInfo newStickerInfo) {
        StickerInfo oldSticker = stickerInfo;
        stickerInfo = newStickerInfo;
        view.setVisibleStickerPreview(true);
        if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId()
                || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
            view.showStickerPreview(stickerInfo);
        }
    }

    public void stickerPreviewClose(boolean isMessageEmpty) {
        stickerInfo = NULL_STICKER;

        view.setVisibleStickerPreview(false);
        if (mentionControlViewModel != null) {
            ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject();
            if (TextUtils.isEmpty(mentionInfoObject.getMessage())) {
                view.setEnableSendButton(false);
            }
        } else {
            if (isMessageEmpty) {
                view.setEnableSendButton(false);
            }
        }

        AnalyticsUtil.sendEvent(
                messageListModel.getScreen(entityId), AnalyticsValue.Action.Sticker_cancel);
    }

    public int getChatTypeForSticker() {
        return messageListModel.isUser(entityId) ?
                StickerViewModel.TYPE_MESSAGE : StickerViewModel.TYPE_TOPIC;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////// Mention ////////////////////////////////////////////

    public void initMentionControlViewModel(Activity activity, EditText etMessage) {
        List<Long> roomIds = new ArrayList<>();
        roomIds.add(roomId);
        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            view.setVisibleBtnMention(true);

            if (mentionControlViewModel == null) {
                mentionControlViewModel = MentionControlViewModel.newInstance(activity,
                        etMessage,
                        roomIds,
                        MentionControlViewModel.MENTION_TYPE_MESSAGE);
                mentionControlViewModel.setOnMentionShowingListener(
                        isShowing -> view.setVisibleBtnMention(!isShowing)
                );

                String readyMessage = messageListModel.getReadyMessage(roomId);
                mentionControlViewModel.setUpMention(readyMessage);
            } else {
                mentionControlViewModel.refreshSelectableMembers(teamId, roomIds);
            }

            // copy txt from mentioned edittext message
            mentionControlViewModel.registClipboardListener();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateMentionInfo() {
        mentionControlViewModel.refreshMembers(Arrays.asList(roomId));
    }

    public void writeMentionCharacter(EditText etMessage) {
        BaseInputConnection inputConnection = new BaseInputConnection(etMessage, true);
        if (messageListModel.needSpace(etMessage.getSelectionStart(), etMessage.getText().toString())) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        }
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_AT));
        keyboardAreaViewModel.openKeyboardIfNotOpen();
    }

    public void removeMentionClipboardListener() {
        // u must release listener for mentioned copy
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {

        if (!view.isForeground()) {
            return;
        }

        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////// Keyboard Area /////////////////////////////////////////////

    public void setKeyboardArea(ImageView button1, ImageView button2,
                                EditText editText, ViewGroup keyboardSpace) {
        keyboardAreaViewModel
                .initKeyboardArea(button1, button2, keyboardSpace, editText, view, this);
    }

    public void hideKeyboardAction() {
        keyboardAreaViewModel.hideKeyboard();
    }

    public void hideUploadMenuAction() {
        keyboardAreaViewModel.dismissUploadSelectorIfShow();
    }

    public void hideStickerMenuAction() {
        keyboardAreaViewModel.dismissStickerSelectorIfShow();
    }

    public void showUploadMenuAction() {
        keyboardAreaViewModel.showUploadMenuSelectorIfNotShow();
    }

    public KeyboardAreaController.ButtonAction getCurrentKeyButtonAction() {
        return keyboardAreaViewModel.getCurrentButtonAction();
    }

    public void hideAllKeyboardArea() {
        keyboardAreaViewModel.hideKeyboard();
        keyboardAreaViewModel.dismissUploadSelectorIfShow();
        keyboardAreaViewModel.dismissStickerSelectorIfShow();
    }

    public boolean isKeyboardOpened() {
        return keyboardAreaViewModel.isKeyboardOpened();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////// Profile & Member /////////////////////////////////////////////

    public void onEvent(ShowProfileEvent event) {
        if (!view.isForeground()) {
            return;
        }

        MemberProfileActivity_.intent(context)
                .memberId(event.userId)
                .from(messageListModel.getScreen(entityId) == AnalyticsValue.Screen.Message ?
                        MemberProfileActivity.EXTRA_FROM_MESSAGE : MemberProfileActivity.EXTRA_FROM_TOPIC_CHAT)
                .start();

        if (event.from != null) {
            AnalyticsValue.Screen screen = messageListModel.getScreen(entityId);
            AnalyticsUtil.sendEvent(screen, AnalyticsUtil.getProfileAction(event.userId, event.from));
        }
    }

    public void onEvent(ProfileChangeEvent event) {
        view.justRefresh();
    }

    public void onEvent(MemberStarredEvent memberStarredEvent) {
        if (memberStarredEvent.getId() == entityId) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
            view.setIsFavorite(entity.isStarred);
            view.refreshActionBar();
        }
    }

    public void onEventMainThread(TopicKickedoutEvent event) {
        if (roomId == event.getRoomId()) {
            view.finish();
            CharSequence topicName = view.getTopicTitle();
            String msg = JandiApplication.getContext().getString(R.string.jandi_kicked_message, topicName);
            view.showFailToast(msg);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Team/Topic/Chat //////////////////////////////////////////

    @Background
    void modifyEntity(ConfirmModifyTopicEvent event) {
        view.showProgressWheel();
        try {
            messageListModel.modifyTopicName(entityType, entityId, event.inputName);
            view.setChangedTopicTitle(event.inputName);
            messageListModel.trackChangingEntityName(entityType);
            EntityManager.getInstance().getEntityById(entityId).getEntity().name = event.inputName;
        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.showFailToast(context.getString(R.string.err_entity_duplicated_name));
            } else {
                view.showFailToast(context.getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            view.showFailToast(context.getString(R.string.err_entity_modify));
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Background
    void deleteTopic() {
        view.showProgressWheel();
        try {
            messageListModel.deleteTopic(entityId, entityType);
            messageListModel.trackDeletingEntity(entityType);
            view.finish();
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
        } finally {
            view.dismissProgressWheel();
        }
    }

    void updateRoomInfo() {
        messageListModel.updateMarkerInfo(teamId, roomId);
        setEmptyMessageViewControl();

        messageHandler.refreshNewMessages();
    }

    public void onEvent(ConfirmModifyTopicEvent event) {
        if (!view.isForeground()) {
            return;
        }

        modifyEntity(event);
    }

    public void onEvent(TopicInviteEvent event) {
        if (!view.isForeground()) {
            return;
        }
        view.moveMemberListActivity();
    }

    public void onEvent(ConfirmDeleteTopicEvent event) {
        if (!view.isForeground()) {
            return;
        }
        deleteTopic();
    }

    public void onEventMainThread(ChatCloseEvent event) {
        if (entityId == event.getCompanionId()) {
            view.finish();
        }
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (entityId == event.getId()) {
            view.finish();
        }
    }

    public void onEvent(TeamLeaveEvent event) {
        if (!messageListModel.isCurrentTeam(event.getTeamId())) {
            return;
        }

        if (event.getMemberId() == entityId) {
            view.showLeavedMemberDialog();
            view.setDisableUser();
        }
    }

    public void onEvent(RoomMarkerEvent event) {
        if (!view.isForeground()) {
            return;
        }
        view.justRefresh();
    }

    public void onEvent(SocketRoomMarkerEvent event) {
        if (!view.isForeground()) {
            return;
        }

        if (event.getRoom().getId() == roomId) {
            SocketRoomMarkerEvent.Marker marker = event.getMarker();
            MarkerRepository.getRepository().upsertRoomMarker(teamId, roomId,
                    marker.getMemberId(), marker.getLastLinkId());
            view.justRefresh();
        }
    }

    public void onEvent(TopicInfoUpdateEvent event) {
        if (event.getId() == entityId) {
            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
            view.setIsFavorite(entity.isStarred);
            view.refreshActionBar();
            if (view.isForeground()) {
                view.closeDialogFragment();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////// Presenter for Adapter ////////////////////////////////////////

    public void setFirstCursorLinkId(long firstCursorLinkId) {
        if (messageAdapter instanceof MainMessageListAdapter) {
            ((MainMessageListAdapter) messageAdapter).setFirstCursorLinkId(firstCursorLinkId);
        }
    }

    public void setMarkerInfo(long teamId, long roomId) {
        messageAdapter.setTeamId(teamId);
        messageAdapter.setRoomId(roomId);
        view.refreshAll();
    }

    public ResMessages.Link getItem(int position) {
        return messageAdapter.getItem(position);
    }

    public int getPosition(long messageId) {
        return messageAdapter.indexByMessageId(messageId);
    }

    public int getItemCountWithoutDummy() {
        return messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount();
    }

    public void setLastReadLinkId(long lastReadLinkId) {
        messageAdapter.setLastReadLinkId(lastReadLinkId);
    }

    public int getItemCount() {
        return messageAdapter.getItemCount();
    }

    public int getItemCountWithoutEvent() {
        int itemCount = messageAdapter.getItemCount();
        for (int idx = itemCount - 1; idx >= 0; --idx) {
            if (messageAdapter.getItemViewType(idx) == BodyViewHolder.Type.Event.ordinal()) {
                itemCount--;
            }
        }
        return itemCount;
    }

    public int getLastItemPosition() {
        return messageAdapter.getItemCount();
    }

    public int getItemViewType(int position) {
        return messageAdapter.getItemViewType(position);
    }

    public void setOldLoadingComplete() {
        messageAdapter.setOldLoadingComplete();
    }

    public void setOldNoMoreLoading() {
        messageAdapter.setOldNoMoreLoading();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessage(long messageId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexByMessageId(messageId);
        view.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessageById(long linkId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexOfLinkId(linkId);
        view.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void addAllMessage(int position, List<ResMessages.Link> messages) {
        messageAdapter.addAll(position, messages);
        view.refreshAll();
    }

    public long getFirstVisibleItemLinkId() {
        if (messageAdapter.getItemCount() > 0) {
            int firstVisibleItemPosition = view.getFirstVisibleItemPositionFromListView();
            if (firstVisibleItemPosition >= 0) {
                return messageAdapter.getItem(firstVisibleItemPosition).messageId;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setUpLastReadLinkIdIfPosition() {
        // 여기까지 읽었습니다가 마지막아이템을 가르키고 있을때만 position = -1 처리
        long lastReadLinkId = messageAdapter.getLastReadLinkId();
        int markerPosition = messageAdapter.indexOfLinkId(lastReadLinkId);
        if (markerPosition == messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount() - 1) {
            messageAdapter.setLastReadLinkId(-1);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveLastReadLink() {
        long lastReadLinkId = messageAdapter.getLastReadLinkId();

        if (lastReadLinkId <= 0) {
            return;
        }

        int position = messageAdapter.indexOfLinkId(lastReadLinkId);

        if (position > 0) {
            int measuredHeight = view.getListViewMeasuredHeight() / 2;
            if (measuredHeight <= 0) {
                measuredHeight = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                100f,
                                JandiApplication.getContext().getResources().getDisplayMetrics());
            }
            view.scrollToPositionWithOffset(
                    Math.min(messageAdapter.getItemCount() - 1, position + 1), measuredHeight);
        } else if (position < 0) {
            view.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private void setUpLastReadLink() {
        long lastReadLinkId = messageAdapter.getLastReadLinkId();
        int indexOfLinkId = messageAdapter.indexOfLinkId(lastReadLinkId);

        if (indexOfLinkId < 0) {
            return;
        }

        if (indexOfLinkId >= messageAdapter.getItemCount() - 1) {
            // 라스트 링크가 마지막 아이템인경우
            messageAdapter.setLastReadLinkId(-1);
        } else {
            ResMessages.Link item = messageAdapter.getItem(indexOfLinkId + 1);
            if (item instanceof DummyMessageLink) {
                // 마지막 아이템은 아니지만 다음 아이템이 더미인경우 마지막 아이템으로 간주
                messageAdapter.setLastReadLinkId(-1);
            }
        }
    }

    public void setMoreNewFromAdapter(boolean isMoreNew) {
        messageAdapter.setMoreFromNew(isMoreNew);
    }

    public ResMessages.Link getLastItemWithoutDummy() {
        int count = messageAdapter.getItemCount();
        for (int idx = count - 1; idx >= 0; --idx) {
            if (messageAdapter.getItem(idx) instanceof DummyMessageLink) {
                continue;
            }
            return messageAdapter.getItem(idx);
        }

        return null;
    }

    public DummyMessageLink getDummyMessage(long localId) {
        int position = messageAdapter.getDummeMessagePositionByLocalId(localId);
        return ((DummyMessageLink) messageAdapter.getItem(position));
    }

    public void setNewLoadingComplete() {
        messageAdapter.setNewLoadingComplete();
    }

    public void setNewNoMoreLoading() {
        messageAdapter.setNewNoMoreLoading();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void modifyStarredInfo(long messageId, boolean isStarred) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.modifyStarredStateByPosition(position, isStarred);
    }

    public long getRoomId() {
        return messageAdapter.getRoomId();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public interface View {
        void showOldLoadProgress();

        void setEmptyLayoutVisible(boolean visible);

        void justRefresh();

        void refreshAll();

        void dismissOfflineLayer();

        void showOfflineLayer();

        void clearMessages();

        void scrollToPositionWithOffset(int itemPosition, int firstVisibleItemTop);

        void scrollToPosition(int itemPosition);

        void moveLastPage();

        void dismissLoadingView();

        void showProgressWheel();

        void dismissProgressWheel();

        void dismissOldLoadProgress();

        void setPreviewName(String name);

        void showPreviewProfileImage(boolean isBot, Uri uri);

        void setPreviewContent(SpannableStringBuilder content);

        void setPreviewVisible(boolean isVisible);

        void showFailToast(String message);

        void finish();

        void showMessageEmptyLayout();

        void showTeamMemberEmptyLayout();

        void clearEmptyMessageLayout();

        void showTopicMemberEmptyLayout();

        void setVisibleBtnMention(boolean visible);

        void setEnableSendButton(boolean enabled);

        void setSendEditText(String text);

        void setVisibleStickerPreview(boolean visible);

        void showDummyMessageDialog(long localId);

        void showMessageMenuDialog(boolean isDirectMessage, boolean myMessage,
                                   ResMessages.TextMessage textMessage);

        void showMessageMenuDialog(ResMessages.CommentMessage commentMessage);

        void showStickerMessageMenuDialog(
                boolean myMessage, ResMessages.StickerMessage StickerMessage);

        void showStickerPreview(StickerInfo stickerInfo);

        void moveFileDetailActivity(long messageId, long roomId, long selectMessageId);

        void moveFileUploadPreviewActivity(List<String> filePaths);

        void moveMemberListActivity();

        void showSuccessToast(String message);

        void showGrayToast(String message);

        void showLeavedMemberDialog();

        void setDisableUser();

        void setChangedTopicTitle(String changedEntityName);

        String getTopicTitle();

        void moveDirectMessageList(long userId);

        void refreshActionBar();

        void closeDialogFragment();

        void setIsFavorite(boolean isFavorite);

        int getFirstVisibleItemPositionFromListView();

        int getFirstVisibleItemTopFromListView();

        int getListViewMeasuredHeight();

        int getLastVisibleItemPositionFromListView();

        boolean isForeground();

        boolean isVisibleLastItem();
    }

}
