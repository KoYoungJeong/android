package com.tosslab.jandi.app.ui.message.v2;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.tosslab.jandi.app.files.upload.EntityFileUploadViewModelImpl;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.model.AnnouncementModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
//                    onSendClick();
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
//            messageListPresenter.setEnableSendButton(true);
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
        });

//        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId) -> onSendClick());

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
        messageAdapter = new MessageListAdapter(getActivity().getBaseContext());
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

        int currentItemCountWithoutDummy = messageAdapter != null
                ? (messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount())
                : 0;

        messageListPresenter.onInitMessages(
                teamId, roomId, entityId, currentItemCountWithoutDummy, withProgress);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showDisabledUserLayer() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setAnnouncement(ResAnnouncement announcement, boolean shouldOpenAnnouncement) {

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

        initReadyMessageInToEditText();

        initMessages(withProgress);
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOldLoadProgress() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setEmptyLayoutVisible(boolean visible) {
        AlertUtil.showConfirmDialog(getActivity(), "Hello", null, true);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void justRefresh() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshAll() {

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

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setReadyMessage(String text) {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOldLoadProgress() {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
    }

    // roomId 가 설정 된 이후 불려야 함
    private void initReadyMessageInToEditText() {
        messageListPresenter.onRetrieveReadyMessage(roomId, entityId);
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