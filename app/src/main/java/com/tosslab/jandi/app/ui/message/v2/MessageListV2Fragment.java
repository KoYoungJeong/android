package com.tosslab.jandi.app.ui.message.v2;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.files.upload.EntityFileUploadViewModelImpl;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.to.queue.NewMessageQueue;
import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.imeissue.EditableAccomodatingLatinIMETypeNullIssues;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tee on 16. 2. 16..
 */

@EFragment(R.layout.fragment_message_list)
public class MessageListV2Fragment extends Fragment implements
        MessageListV2Activity.OnBackPressedListener,
        MessageListV2Activity.OnKeyPressListener,
        MessageListV2Presenter.View {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_WINDOW_PERMISSION = 102;

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @Bean
    MessageListV2Presenter messageListPresenter;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    @Bean
    KeyboardHeightModel keyboardHeightModel;
    @Bean
    StickerViewModel stickerViewModel;
    @Bean
    UploadMenuViewModel uploadMenuViewModel;
    @Bean(value = EntityFileUploadViewModelImpl.class)
    FilePickerViewModel filePickerViewModel;
    @Bean
    FileUploadStateViewModel fileUploadStateViewModel;
    @Bean
    AnnouncementModel announcementModel;
    @Bean
    AnnouncementViewModel announcementViewModel;

    MentionControlViewModel mentionControlViewModel;

    @FragmentArg
    int entityType;
    @FragmentArg
    long entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    boolean isFromPush = false;
    @FragmentArg
    long teamId;
    @FragmentArg
    long lastReadLinkId = -1;
    @FragmentArg
    long roomId;
    @FragmentArg
    long firstCursorLinkId = -1;

    @ViewById(R.id.lv_messages)
    RecyclerView lvMessages;
    @ViewById(R.id.btn_send_message)
    View sendButton;
    @ViewById(R.id.et_message)
    BackPressCatchEditText etMessage;
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
    SimpleDraweeView ivSticker;
    @ViewById(R.id.vg_message_offline)
    View vgOffline;
    @ViewById(R.id.progress_message)
    View oldProgressBar;
    @ViewById(R.id.lv_messages)
    RecyclerView messageListView;
    @ViewById(R.id.btn_message_action_button_1)
    ImageView btnActionButton1;
    @ViewById(R.id.btn_message_action_button_2)
    ImageView btnActionButton2;
    @ViewById(R.id.btn_show_mention)
    ImageView btnShowMention;
    @ViewById(R.id.vg_option_space)
    ViewGroup vgOptionSpace;
    @ViewById(R.id.vg_easteregg_snow)
    FrameLayout vgEasterEggSnow;

    private OfflineLayer offlineLayer;

    private ProgressWheel progressWheel;

    private MessageAdapter messageAdapter;

    private StickerInfo stickerInfo = NULL_STICKER;

    private File photoFileByCamera;

    private boolean isForeground = true;
    private LinearLayoutManager layoutManager;
    private String tempMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        PushMonitor.getInstance().register(roomId);
    }

    @Override
    public void onPause() {
        isForeground = false;
        PushMonitor.getInstance().unregister(roomId);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @AfterViews
    void initViews() {
        setUpActionbar();
        setHasOptionsMenu(true);

        trackScreenView();

        initPresenter();

        initOffLineLayer();

        initProgressWheel();

        initMessageEditText();

        initStickerViewModel();

        initUploadViewModel();

        initMessageListView();

        initUserStatus();

        initAnnouncement();

        initMessages(true /* withProgress */);

        showCoachMarkIfNeed();
    }

    private void initPresenter() {
        messageListPresenter.setView(this);
        messageListPresenter.onInitMessageState(lastReadLinkId);
        messageListPresenter.setEntityInfo(entityType, entityId);
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

    private void trackScreenView() {
        int screenView = entityType == JandiConstants.TYPE_PUBLIC_TOPIC
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, screenView)
                        .build());

        AnalyticsValue.Screen screen = isInDirectMessage()
                ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
        AnalyticsUtil.sendScreenName(screen);
    }

    private void initOffLineLayer() {
        offlineLayer = new OfflineLayer(vgOffline);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(getActivity());
    }

    private void initMessageEditText() {
        etMessage.setOnKeyListener((v, keyCode, event) -> {
            LogUtil.d("In etMessage KeyCode : " + keyCode);
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {

                if (!event.isShiftPressed()) {
//                    sendMessage();
                    return true;
                } else {
                    return false;
                }
            }

            if (event.getUnicodeChar() ==
                    (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)) {
                //We are ignoring this character, and we want everyone else to ignore it, too, so
                // we return true indicating that we have handled it (by ignoring it).
                return true;
            }
            return false;
        });

        etMessage.setOnClickListener(v -> {
//            dismissStickerSelectorIfShow();
//            dismissUploadSelectorIfShow();
        });

        etMessage.setOnBackPressListener(() -> {
            if (keyboardHeightModel.isOpened()) {
                //키보드가 열려져 있고 그 위에 스티커가 있는 상태에서 둘다 제거 할때 속도를 맞추기 위해 딜레이를 줌
                Observable.just(1)
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
//                            dismissStickerSelectorIfShow();
//                            dismissUploadSelectorIfShow();
                        });
            }
            return false;
        });

        TextCutter.with(etMessage)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });

    }

    private void initStickerViewModel() {
        stickerViewModel.setOptionSpace(vgOptionSpace);
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            stickerInfo = new StickerInfo();
            stickerInfo.setStickerGroupId(groupId);
            stickerInfo.setStickerId(stickerId);
//            showStickerPreview(oldSticker, stickerInfo);
//            messageListPresenter.setSendButtonEnabled(true);
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
        });

//        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId) -> sendMessage());

        stickerViewModel.setType(isInDirectMessage()
                ? StickerViewModel.TYPE_MESSAGE : StickerViewModel.TYPE_TOPIC);

        stickerViewModel.setStickerButton(btnActionButton2);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsValue.Screen screen = isInDirectMessage()
                ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
        AnalyticsUtil.sendEvent(screen, action);
    }

    private void initUploadViewModel() {
        uploadMenuViewModel.setOptionSpace(vgOptionSpace);
    }

    private void initAnnouncement() {
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

        if (!isInDirectMessage()) {
            messageListPresenter.onInitAnnouncement(teamId, entityId);
        }
    }

    private void initMessageListView() {
        messageAdapter = new MainMessageListAdapter(getActivity().getBaseContext());
        MessageListHeaderAdapter messageListHeaderAdapter =
                new MessageListHeaderAdapter(getContext(), messageAdapter);
        lvMessages.setAdapter(messageAdapter);
        lvMessages.setItemAnimator(null);

        layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        lvMessages.setLayoutManager(layoutManager);

        StickyHeadersItemDecoration stickyHeadersItemDecoration = new StickyHeadersBuilder()
                .setAdapter(messageAdapter)
                .setRecyclerView(lvMessages)
                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
                .build();

        lvMessages.addItemDecoration(stickyHeadersItemDecoration);

        // 아이템 클릭 했을 때의 액션
        messageAdapter.setOnItemClickListener((adapter, position) -> {
            // hide all
//            messageListPresenter.messageItemClick(messageListPresenter.getItem(position), position);
        });

        // 아이템 롱클릭했을때 액션
        messageAdapter.setOnItemLongClickListener((adapter, position) -> {
//            messageListPresenter.messageItemLongClick(messageListPresenter.getItem(position));
            return true;
        });

        lvMessages.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // TODO
            }
            return false;
        });

        // 스크롤 했을 때 동작
        messageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int lastAdapterItemPosition = recyclerView.getAdapter().getItemCount() - 1;

                boolean isShowingLastItem = lastVisibleItemPosition == lastAdapterItemPosition;
                if (isShowingLastItem) {
//                    setPreviewVisible(false);
                }
            }
        });
    }

    private void initUserStatus() {
        messageListPresenter.onDetermineUserStatus(entityId);
    }

    private void initMessages(boolean withProgress) {
        if (roomId <= 0) {
            retrieveRoomId(withProgress);
            return;
        }

        int currentItemCountWithoutDummy = getCurrentItemCountWithoutDummy();

        messageListPresenter.onInitMessages(
                teamId, roomId, entityId, currentItemCountWithoutDummy, withProgress);
    }

    private void initMentionControlViewModel(String readyMessage) {
        List<Long> roomIds = new ArrayList<>();
        roomIds.add(roomId);

        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(),
                    etMessage,
                    roomIds,
                    MentionControlViewModel.MENTION_TYPE_MESSAGE);
            mentionControlViewModel.setOnMentionShowingListener(
                    isShowing -> btnShowMention.setVisibility(!isShowing ? View.VISIBLE : View.GONE));

            mentionControlViewModel.setUpMention(readyMessage);
        } else {
            mentionControlViewModel.refreshSelectableMembers(teamId, roomIds);
        }

        // copy txt from mentioned edittext message
        mentionControlViewModel.registClipboardListener();
    }

    private int getCurrentItemCountWithoutDummy() {
        return messageAdapter != null
                ? (messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount())
                : 0;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showDisabledUserLayer() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setAnnouncement(ResAnnouncement announcement, boolean shouldOpenAnnouncement) {
        announcementViewModel.setAnnouncement(announcement, shouldOpenAnnouncement);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressView() {
        vgProgressForMessageList.animate()
                .alpha(1.0f)
                .setDuration(150);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressView() {
        vgProgressForMessageList.animate()
                .alpha(0.0f)
                .setDuration(250);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void retrieveRoomId(boolean withProgress) {
        messageListPresenter.onRetrieveRoomId(entityId, withProgress);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setRoomId(long roomId, boolean withProgress) {
        this.roomId = roomId;

        LogUtil.e("tony", "roomId = " + roomId);

        initMessages(withProgress);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void initRoomInfo(long roomId, String readyMessage) {
        etMessage.setText(readyMessage);

        initMentionControlViewModel(readyMessage);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showInvalidEntityToast() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setMoreNewFromAdapter(boolean isMoreNew) {
        messageAdapter.setMoreFromNew(isMoreNew);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setNewLoadingComplete() {
        messageAdapter.setNewLoadingComplete();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setMarkerInfo(long roomId) {
        messageAdapter.setTeamId(teamId);
        messageAdapter.setRoomId(roomId);
        messageAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setLastReadLinkId(long lastReadLinkId) {
        messageAdapter.setLastReadLinkId(lastReadLinkId);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpOldMessage(List<ResMessages.Link> records,
                                int currentItemCount, boolean isFirstMessage) {
        if (currentItemCount == 0) {
            // 첫 로드라면...
            clearMessages();

            messageAdapter.addAll(0, records);
            messageAdapter.notifyDataSetChanged();

            layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);

        } else {

            long latestVisibleLinkId = getFirstVisibleItemLinkId();
            int firstVisibleItemTop = getFirstVisibleItemTop();

            messageAdapter.addAll(0, records);
            messageAdapter.notifyDataSetChanged();

            moveToMessage(latestVisibleLinkId, firstVisibleItemTop);
        }

        if (!isFirstMessage) {
            messageAdapter.setOldLoadingComplete();
        } else {
            messageAdapter.setOldNoMoreLoading();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpNewMessage(List<ResMessages.Link> records, long myId,
                                boolean isFirstLoad,
                                boolean moveToLinkId) {
        int location = records.size() - 1;
        if (location < 0) {
            return;
        }

        int visibleLastItemPosition = getLastVisibleItemPosition();
        int lastItemPosition = getLastItemPosition();

        messageAdapter.addAll(lastItemPosition, records);
        notifyDataSetChanged();

        ResMessages.Link lastUpdatedMessage = records.get(location);
        if (!isFirstLoad
                && visibleLastItemPosition >= 0
                && visibleLastItemPosition < lastItemPosition - 1
                && lastUpdatedMessage.fromEntity != myId) {
//            showPreviewIfNotLastItem();
        } else {
            long messageId = lastUpdatedMessage.messageId;

            if (isFirstLoad) {

                moveLastReadLink();
                setUpLastReadLink(myId);

            } else if (messageId <= 0) {
                if (lastUpdatedMessage.fromEntity != myId) {
                    moveToMessageById(lastUpdatedMessage.id, 0);
                }
            } else {
                moveToMessage(messageId, 0);
            }
        }
    }

    private int getLastItemPosition() {
        return messageAdapter.getItemCount();
    }

    private int getLastVisibleItemPosition() {
        return layoutManager.findLastVisibleItemPosition();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessageById(long linkId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexOfLinkId(linkId);
        layoutManager.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    private void setUpLastReadLink(long myId) {
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

    private long getFirstVisibleItemLinkId() {
        if (messageAdapter.getItemCount() > 0) {
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition >= 0) {
                return messageAdapter.getItem(firstVisibleItemPosition).messageId;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private int getFirstVisibleItemTop() {
        View childAt = lvMessages.getLayoutManager().getChildAt(0);
        if (childAt != null) {
            return childAt.getTop();
        } else {
            return 0;
        }
    }

    private void moveToMessage(long messageId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexByMessageId(messageId);
        layoutManager.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    @TextChange(R.id.et_message)
    void onMessageChanged(TextView tv, CharSequence text) {

        boolean isEmptyText = TextUtils.isEmpty(text.toString().trim()) && stickerInfo == NULL_STICKER;
        setSendButtonEnabled(!isEmptyText);

    }

    @Click(R.id.btn_send_message)
    void sendMessage() {
        String message = etMessage.getText().toString();

        List<MentionObject> mentions = new ArrayList<>();
        ResultMentionsVO mentionVO = getMentionVO();
        if (mentionVO != null) {
            message = mentionVO.getMessage();
            mentions.addAll(mentionVO.getMentions());
        }

        message = message.trim();

        ReqSendMessageV3 reqSendMessageV3 = null;
        if (!TextUtils.isEmpty(message)) {
            reqSendMessageV3 =
                    (!isInDirectMessage() && mentionControlViewModel.hasMentionMember())
                            ? new ReqSendMessageV3(message, mentions)
                            : new ReqSendMessageV3(message, new ArrayList<>());
        }

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            sendStickerMessage();
            if (!TextUtils.isEmpty(message)) {
                sendTextMessage(message, mentions, reqSendMessageV3);
            }
        } else {
            if (!TextUtils.isEmpty(message)) {
                sendTextMessage(message, mentions, reqSendMessageV3);
            }
        }

        dismissStickerPreview();
        stickerInfo = NULL_STICKER;
        setSendButtonEnabled(false);
        setMessageIntoEditText("");

        sendAnalyticsEvent(AnalyticsValue.Action.Send);
    }

    private void sendStickerMessage() {
        long stickerGroupId = stickerInfo.getStickerGroupId();
        String stickerId = stickerInfo.getStickerId();
        StickerRepository.getRepository().upsertRecentSticker(stickerGroupId, stickerId);

        messageListPresenter.sendStickerMessage(teamId, roomId, entityId, stickerInfo);

        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Send);
    }

    private void sendTextMessage(String message,
                                 List<MentionObject> mentions,
                                 ReqSendMessageV3 reqSendMessage) {
        messageListPresenter.sendTextMessage(
                teamId, roomId, entityId, message, mentions, reqSendMessage);
    }

    public void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    public void setSendButtonEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    public void setMessageIntoEditText(String text) {
        tempMessage = text;
        etMessage.setText(tempMessage);
    }

    @Nullable
    private ResultMentionsVO getMentionVO() {
        return !isInDirectMessage() ? mentionControlViewModel.getMentionInfoObject() : null;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOldLoadProgress() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEmptyLayoutVisible(boolean visible) {
//        AlertUtil.showConfirmDialog(getActivity(), "Hello", null, true);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDataSetChanged() {
        LogUtil.i("tony", "notifyDataSetChanged");
        messageAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOfflineLayer() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOfflineLayer() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearMessages() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void scrollToPositionWithOffset(int itemPosition, int firstVisibleItemTop) {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void scrollToPosition(int itemPosition) {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLastPage() {
        layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOldLoadProgress() {

    }

    @Override
    public void setNewNoMoreLoading() {

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

        int currentItemCountWithoutDummy = getCurrentItemCountWithoutDummy();

        if (TextUtils.equals(messageType, "topic_leave") ||
                TextUtils.equals(messageType, "topic_join") ||
                TextUtils.equals(messageType, "topic_invite")) {

            messageListPresenter.updateRoomInfo(
                    teamId, roomId, entityId, currentItemCountWithoutDummy, true);

            updateMentionInfo();
        } else {
            if (!isForeground) {
                messageListPresenter.updateMarker(teamId, roomId);
                return;
            }

            if (roomId > 0) {
                LogUtil.e("tony", "call new message");
                messageListPresenter.addNewMessageQueue(
                        teamId, roomId, currentItemCountWithoutDummy, true);
            }
        }
    }

    public void onEvent(RefreshNewMessageEvent event) {
        if (!isForeground) {
            return;
        }

        if (roomId > 0) {
            messageListPresenter.addNewMessageQueue(
                    teamId, roomId, getCurrentItemCountWithoutDummy(), true);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateMentionInfo() {
        mentionControlViewModel.refreshMembers(Arrays.asList(roomId));
    }

    @Nullable
    @Override
    public synchronized ResMessages.Link getLastItemFromAdapterWithoutDummy() {
        int count = messageAdapter.getItemCount();
        for (int idx = count - 1; idx >= 0; --idx) {
            if (messageAdapter.getItem(idx) instanceof DummyMessageLink) {
                continue;
            }
            return messageAdapter.getItem(idx);
        }

        return null;
    }

    @Override
    public void setUpLastReadLinkIdIfPosition() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
    }

    @Override
    public void moveLastReadLink() {

    }

    @Override
    public void insertTeamMemberEmptyLayout() {

    }

    @Override
    public void insertTopicMemberEmptyLayout() {

    }

    @Override
    public void clearEmptyMessageLayout() {

    }

    @Override
    public void insertMessageEmptyLayout() {

    }

    private void showCoachMarkIfNeed() {
        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(
                entityType == JandiConstants.TYPE_DIRECT_MESSAGE, getActivity());
    }

    private boolean isInDirectMessage() {
        return entityType == JandiConstants.TYPE_DIRECT_MESSAGE;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean onKey(int keyCode, KeyEvent event) {
        return false;
    }
}