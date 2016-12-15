package com.tosslab.jandi.app.ui.message.v2;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.BaseInputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.MentionableMembersRefreshEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.entities.RefreshConnectBotEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.events.entities.TopicJoinEvent;
import com.tosslab.jandi.app.events.entities.TopicKickedoutEvent;
import com.tosslab.jandi.app.events.entities.TopicLeftEvent;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementUpdatedEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewClickEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RoomMarkerEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.poll.RequestCreatePollEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.socket.EventUpdateFinish;
import com.tosslab.jandi.app.events.socket.EventUpdateInProgress;
import com.tosslab.jandi.app.events.socket.EventUpdateStart;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.MainFileUploadControllerImpl;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketServiceStopEvent;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapterView;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.DateAnimator;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.MessageRecyclerViewManager;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.poll.create.PollCreateActivity;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.RecyclerScrollStateListener;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;
import com.tosslab.jandi.app.utils.imeissue.EditableAccomodatingLatinIMETypeNullIssues;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;
import com.tosslab.jandi.app.views.controller.SoftInputAreaController;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 2. 16..
 */
@OptionsMenu(R.menu.message_list_menu_basic)
@EFragment(R.layout.fragment_message_list)
public class MessageListV2Fragment extends Fragment implements MessageListV2Presenter.View,
        MessageListV2Activity.OnBackPressedListener,
        MessageListV2Activity.OnKeyPressListener {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_WINDOW_PERMISSION = 102;
    public static final String REQUEST_FILE_UPLOAD_EVENT_TYPE = "request_file_upload_event_type";
    private static final StickerInfo NULL_STICKER = new StickerInfo();
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

    @Bean
    MessageListV2Presenter messageListPresenter;

    @Bean
    StickerViewModel stickerViewModel;
    @Bean
    UploadMenuViewModel uploadMenuViewModel;
    @Bean(value = MainFileUploadControllerImpl.class)
    FileUploadController fileUploadController;
    @Bean
    FileUploadStateViewModel fileUploadStateViewModel;

    @Bean
    AnnouncementViewModel announcementViewModel;

    MentionControlViewModel mentionControlViewModel;
    DateAnimator dateAnimator;

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.vg_messages_soft_input_detector)
    SoftInputDetectLinearLayout vgSoftInputDetector;

    @ViewById(R.id.vg_messages_soft_input_area)
    ViewGroup vgSoftInputArea;

    @ViewById(R.id.lv_messages)
    RecyclerView lvMessages;
    @ViewById(R.id.btn_send_message)
    View btnSend;
    @ViewById(R.id.et_message)
    BackPressCatchEditText etMessage;
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

    @ViewById(R.id.tv_messages_date_divider)
    TextView tvMessageDate;

    @ViewById(R.id.layout_messages_empty)
    LinearLayout vgEmptyLayout;
    @ViewById(R.id.layout_messages_loading)
    View vgProgressForMessageList;
    @ViewById(R.id.iv_go_to_latest)
    View vMoveToLatest;
    @ViewById(R.id.progress_go_to_latest)
    View progressGoToLatest;
    @ViewById(R.id.vg_messages_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.iv_messages_preview_sticker_image)
    ImageView ivSticker;
    @ViewById(R.id.vg_message_offline)
    View vgOffline;
    @ViewById(R.id.progress_message)
    View oldProgressBar;
    @ViewById(R.id.btn_show_mention)
    ImageView btnShowMention;
    @ViewById(R.id.vg_easteregg_snow)
    FrameLayout vgEasterEggSnow;

    @ViewById(R.id.btn_message_action_button_1)
    ImageView btnAction1;
    @ViewById(R.id.btn_message_action_button_2)
    ImageView btnAction2;

    @ViewById(R.id.vg_messages_member_status_alert)
    View vgMemberStatusAlert;
    @ViewById(R.id.iv_messages_member_status_alert)
    ImageView ivMemberStatusAlert;
    @ViewById(R.id.tv_messages_member_status_alert)
    TextView tvMemberStatusAlert;

    @ViewById(R.id.vg_main_synchronize)
    View vgSynchronize;

    @ViewById(R.id.tv_synchronize)
    TextView tvSynchronize;


    private OfflineLayer offlineLayer;

    private ProgressWheel progressWheel;

    private StickerInfo stickerInfo = NULL_STICKER;

    private File photoFileByCamera;

    private boolean isForeground = true;
    private LinearLayoutManager layoutManager;

    private Room room;
    private MessagePointer messagePointer;
    private SoftInputAreaController softInputAreaController;
    private int requestFileUploadEventType = -1;
    private MessageRecyclerViewManager messageRecyclerViewManager;
    private MessageListAdapterView adapterView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (savedInstanceState != null) {
            photoFileByCamera = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
            requestFileUploadEventType = savedInstanceState.getInt(REQUEST_FILE_UPLOAD_EVENT_TYPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isForeground = true;
        messageListPresenter.onResumeOfView();

        PushMonitor.getInstance().register(roomId);

        fileUploadStateViewModel.registerEventBus();
        fileUploadStateViewModel.initDownloadState();

        messageListPresenter.restoreStatus();
        refreshMessages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuCommand menuCommand =
                MenuCommandBuilder.init((AppCompatActivity) getActivity())
                        .with(this)
                        .teamId(teamId)
                        .entityId(entityId)
                        .build(item);

        if (menuCommand != null) {
            menuCommand.execute(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 100ms - 100ms 간격으로 화면 갱신토록 함
        // 사유 1: 헤더가 사이즈 변경을 인식하는데 시간이 소요됨
        // 사유 2: 2번 하는 이유는 첫 100ms 에서 갱신안되는 단말을 위함...
        // 구형단말을 위한 배려 -_-v
        Observable.just(1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {

                    refreshMessages();

                    if (softInputAreaController != null) {
                        softInputAreaController.onConfigurationChanged();
                    }

                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(getActivity())
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        () -> handleFileUpload())
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(getActivity());
                })
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        UnLockPassCodeManager.getInstance().setUnLocked(true);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case FileUploadController.TYPE_UPLOAD_GALLERY:
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                showPreviewForUploadPhoto(requestCode, intent);
                break;
            case FileUploadController.TYPE_UPLOAD_EXPLORER:
                showPreviewForUploadFiles(requestCode, intent);
                break;
            case FileUploadPreviewActivity.REQUEST_CODE:
                upload(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        isForeground = false;

        PushMonitor.getInstance().unregister(room.getRoomId());
        if (fileUploadStateViewModel != null) {
            fileUploadStateViewModel.unregisterEventBus();
        }

        if (room.getRoomId() > 0) {
            messageListPresenter.onSaveTempMessageAction(etMessage.getText().toString());
        }

        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }

        messageListPresenter.onPauseOfView();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fileUploadController.getUploadedFile() != null) {
            outState.putSerializable(EXTRA_NEW_PHOTO_FILE, fileUploadController.getUploadedFile());
        }

        outState.putInt(REQUEST_FILE_UPLOAD_EVENT_TYPE, requestFileUploadEventType);
    }


    @Override
    public void onDestroy() {
        messageListPresenter.unSubscribeMessageQueue();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @AfterInject
    void initObjects() {
        room = Room.create(entityId, isFromPush);
        messagePointer = MessagePointer.create(lastReadLinkId);
    }

    @AfterViews
    void initViews() {
        setUpActionbar();
        setHasOptionsMenu(true);

        Completable.fromAction(this::trackScreenView)
                .subscribeOn(Schedulers.computation())
                .subscribe();

        initPresenter();

        initOffLineLayer();

        initProgressWheel();

        initEmptyLayout();

        initMessageEditText();

        initStickerViewModel();

        initFileUploadStateViewModel();

        initMessageListView();

        initUserStatus();

        initAnnouncement();

        initUploadMenuViewModel();

        initSoftInputAreaController();

        initActionListeners();

        initMessages(true /* withProgress */);

    }

    private void initSoftInputAreaController() {
        softInputAreaController = new SoftInputAreaController(
                stickerViewModel, uploadMenuViewModel,
                vgSoftInputDetector, vgSoftInputArea, btnAction1, btnAction2,
                etMessage);
        softInputAreaController.setOnUploadButtonClickListener(() -> {
            sendAnalyticsEvent(AnalyticsValue.Action.Upload);
        });
        softInputAreaController.setOnStickerButtonClickListener(() -> {
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker);
        });
        softInputAreaController.init();
    }

    private void initUploadMenuViewModel() {
        uploadMenuViewModel.setRoomType(isInDirectMessage()
                ? UploadMenuViewModel.RoomType.DM : UploadMenuViewModel.RoomType.TOPIC);
    }

    private void initEmptyLayout() {
        messageListPresenter.onInitializeEmptyLayout(entityId);
    }

    private void initPresenter() {
        messageListPresenter.setView(this);
        messageListPresenter.setRoom(room);
        messageListPresenter.setMessagePointer(messagePointer);
        messageListPresenter.setEntityInfo();
    }

    private void setUpActionbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            toolbar.setTitle(TeamInfoLoader.getInstance().getName(room.getEntityId()));
            activity.setSupportActionBar(toolbar);
        } else {
            actionBar.setTitle(TeamInfoLoader.getInstance().getName(room.getEntityId()));
        }

    }

    private void trackScreenView() {
        int screenView = entityType == JandiConstants.TYPE_PUBLIC_TOPIC
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        SprinklrScreenView.sendLog(screenView);

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
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {

                if (!event.isShiftPressed()) {
                    sendMessage();
                    return true;
                } else {
                    return false;
                }
            }

            if (event.getUnicodeChar() ==
                    (int) EditableAccomodatingLatinIMETypeNullIssues.ONE_UNPROCESSED_CHARACTER.charAt(0)) {
                return true;
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
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            if (oldSticker.getStickerGroupId() == groupId
                    && oldSticker.getStickerId().equals(stickerId)) {
                sendMessage();
            } else {
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                showStickerPreview(oldSticker, stickerInfo);
                setSendButtonEnabled(true);
                sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
            }
        });

        stickerViewModel.setType(isInDirectMessage()
                ? StickerViewModel.TYPE_MESSAGE : StickerViewModel.TYPE_TOPIC);
    }

    private void showStickerPreview(StickerInfo oldSticker, StickerInfo stickerInfo) {
        vgStickerPreview.setVisibility(View.VISIBLE);

        if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId()
                || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
            loadSticker(stickerInfo);
        }
    }

    public void loadSticker(StickerInfo stickerInfo) {
        StickerManager.LoadOptions loadOption = new StickerManager.LoadOptions();
        loadOption.scaleType = ImageView.ScaleType.CENTER_CROP;
        StickerManager.getInstance()
                .loadSticker(ivSticker,
                        stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(),
                        loadOption);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsValue.Screen screen = isInDirectMessage()
                ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
        AnalyticsUtil.sendEvent(screen, action);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, AnalyticsValue.Label label) {
        AnalyticsValue.Screen screen = isInDirectMessage()
                ? AnalyticsValue.Screen.Message : AnalyticsValue.Screen.TopicChat;
        AnalyticsUtil.sendEvent(screen, action, label);
    }

    private void initFileUploadStateViewModel() {
        fileUploadStateViewModel.setEntityId(entityId);
    }

    private void initActionListeners() {
        softInputAreaController.setOnSoftInputAreaShowingListener((isShowing, softInputAreaHeight) -> {
            announcementViewModel.setAnnouncementViewVisibility(!isShowing);
        });

        uploadMenuViewModel.setOnClickUploadEventListener(() -> {
            if (softInputAreaController.isSoftInputAreaShowing()) {
                softInputAreaController.hideSoftInputArea(true, true);
            }
        });
    }

    private void initAnnouncement() {
        announcementViewModel.setOnAnnouncementCloseListener(() -> {
            announcementViewModel.openAnnouncement(false);
            messageListPresenter.setAnnouncementActionFrom(true);
            messageListPresenter.onUpdateAnnouncement(false);
            sendAnalyticsEvent(AnalyticsValue.Action.Accouncement_Minimize);
        });
        announcementViewModel.setOnAnnouncementOpenListener(() -> {
            announcementViewModel.openAnnouncement(true);
            messageListPresenter.setAnnouncementActionFrom(true);
            messageListPresenter.onUpdateAnnouncement(true);
            sendAnalyticsEvent(AnalyticsValue.Action.Announcement_ExpandFromMinimize);
        });

        if (!isInDirectMessage()) {
            messageListPresenter.onInitAnnouncement();
        }
    }

    private void initMessageListView() {
        MainMessageListAdapter messageAdapter = new MainMessageListAdapter(getActivity().getBaseContext(), room);
        adapterView = messageAdapter;
        messageAdapter.setMessagePointer(messagePointer);
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
                .setSticky(false)
                .setStickyHeadersAdapter(messageListHeaderAdapter, false)
                .build();

        lvMessages.addItemDecoration(stickyHeadersItemDecoration);

        // 아이템 클릭 했을 때의 액션
        messageAdapter.setOnItemClickListener((adapter, position) -> {
            onMessageItemClick(messageAdapter.getItem(position));
        });

        // 아이템 롱클릭했을때 액션
        messageAdapter.setOnItemLongClickListener((adapter, position) -> {
            onMessageLongClick(messageAdapter.getItem(position));
            sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap);
            return true;
        });

        lvMessages.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                hideAllSoftInputArea();
            }
            return false;
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
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int lastAdapterItemPosition = recyclerView.getAdapter().getItemCount() - 1;

                boolean isShowingLastItem = lastVisibleItemPosition == lastAdapterItemPosition;
                if (isShowingLastItem) {
                    setPreviewVisible(false);
                }

                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                Date date = ((MainMessageListAdapter) recyclerView.getAdapter()).getItemDate(firstVisibleItemPosition);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    long timeInMillis = calendar.getTimeInMillis();
                    if (DateUtils.isToday(timeInMillis)) {
                        tvMessageDate.setText(R.string.today);
                    } else {
                        tvMessageDate.setText(DateTransformator.getTimeStringForDivider(timeInMillis));
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                recyclerScrollStateListener.onScrollState(newState);
            }
        });

        messageListPresenter.setAdapterModel(messageAdapter);
        messageRecyclerViewManager = new MessageRecyclerViewManager(lvMessages, messageAdapter);
    }

    private void hideAllSoftInputArea() {
        if (softInputAreaController == null) {
            return;
        }

        if (softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputArea(true, true);
        } else {
            if (softInputAreaController.isSoftInputShowing()) {
                softInputAreaController.hideSoftInput();
            }
        }
    }

    private void setPreviewVisible(boolean show) {
        if (vgPreview != null) {
            vgPreview.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void initUserStatus() {
        messageListPresenter.onDetermineUserStatus();
    }

    private void initMessages(boolean withProgress) {
        messageListPresenter.onInitMessages(withProgress);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void initMentionControlViewModel(String readyMessage) {
        if (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            return;
        }

        List<Long> roomIds = new ArrayList<>();
        roomIds.add(room.getRoomId());


        if (mentionControlViewModel == null) {

            Completable.fromEmitter(completableEmitter -> {

                mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(),
                        etMessage,
                        roomIds,
                        MentionControlViewModel.MENTION_TYPE_MESSAGE, () -> {

                            Completable.fromAction(() -> {

                                while (mentionControlViewModel == null) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                completableEmitter.onCompleted();
                            }).subscribeOn(Schedulers.computation()).subscribe();
                        });
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {

                        mentionControlViewModel.setUpMention(readyMessage);

                        mentionControlViewModel.registClipboardListener();

                        setMentionButtonVisibility(mentionControlViewModel.hasMentionMember());

                        mentionControlViewModel.setOnMentionShowingListener(
                                isShowing -> {
                                    btnShowMention.setVisibility(!isShowing ? View.VISIBLE : View.GONE);
                                });
                    }, Throwable::printStackTrace);
        }

        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showDisabledUserLayer() {
        vgMessageInput.setVisibility(View.GONE);
        vgMemberStatusAlert.setVisibility(View.VISIBLE);
        vgMemberStatusAlert.setBackgroundColor(getResources().getColor(R.color.jandi_black_b2));
        ivMemberStatusAlert.setImageResource(R.drawable.icon_disabled_members_bar);
        tvMemberStatusAlert.setText(R.string.jandi_disabled_user);
        setPreviewVisible(false);
        vgMemberStatusAlert.setOnClickListener(null);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showInactivedUserLayer() {
        vgMemberStatusAlert.setVisibility(View.VISIBLE);
        vgMemberStatusAlert.setBackgroundColor(getResources().getColor(R.color.jandi_black_de));
        ivMemberStatusAlert.setImageResource(R.drawable.bar_icon_info);
        tvMemberStatusAlert.setText(R.string.jandi_this_member_is_pending_to_join);
        vgMemberStatusAlert.setOnClickListener(v -> {
            onEvent(new ShowProfileEvent(entityId, ShowProfileEvent.From.SystemMessage));
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setAnnouncement(Announcement announcement) {
        announcementViewModel.setAnnouncement(announcement);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
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
    public void initRoomInfo(long roomId, String readyMessage) {

        EventBus.getDefault().post(new MainSelectTopicEvent(roomId));

        etMessage.setText(readyMessage);

        initMentionControlViewModel(readyMessage);
    }

    @Override
    public void showInvalidEntityToast() {
        String message = JandiApplication.getContext()
                .getResources().getString(R.string.jandi_not_in_topic);
        showToast(message, true /* isError */);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpOldMessage(boolean isFirstLoad) {

        if (isFirstLoad) {
            moveLastReadLink();
        } else {
            messageRecyclerViewManager.scrollToCachedFirst();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpNewMessage(List<ResMessages.Link> records, long myId,
                                boolean isFirstLoad) {
        final int location = records.size() - 1;
        if (location < 0) {
            return;
        }

        ResMessages.Link lastUpdatedMessage = records.get(location);
        if (!isFirstLoad
                && messageRecyclerViewManager.isScrollInMiddleAsLastStatus()
                && lastUpdatedMessage.fromEntity != myId) {
            showPreviewIfNotLastItem(lastUpdatedMessage);
        } else {
            if (isFirstLoad) {
                moveLastReadLink();
            } else {
                messageRecyclerViewManager.scrollToLinkId(lastUpdatedMessage.id);
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void moveLastReadLink() {
        long lastReadLinkId = messagePointer.getLastReadLinkId();

        if (lastReadLinkId <= 0) {
            messageRecyclerViewManager.scrollToLast();
            return;
        }
        int measuredHeight = lvMessages.getMeasuredHeight() / 2;
        if (measuredHeight <= 0) {
            measuredHeight = (int) UiUtils.getPixelFromDp(100f);
        }
        messageRecyclerViewManager.scrollToLinkId(lastReadLinkId, measuredHeight);
    }


    @UiThread
    public void showPreviewIfNotLastItem(ResMessages.Link lastUpdatedMessage) {

        ResMessages.Link item = lastUpdatedMessage;

        if (TextUtils.equals(item.status, "event")) {
            return;
        }

        long writerId = item.message.writerId;
        if (TeamInfoLoader.getInstance().isUser(writerId)) {
            User user = TeamInfoLoader.getInstance().getUser(item.message.writerId);
            tvPreviewUserName.setText(user.getName());
            ImageUtil.loadProfileImage(ivPreviewProfile, user.getPhotoUrl(), R.drawable.profile_img);
        } else if (TeamInfoLoader.getInstance().isBot(writerId)) {
            WebhookBot bot = TeamInfoLoader.getInstance().getBot(writerId);
            tvPreviewUserName.setText(bot.getName());
            Uri uri = Uri.parse(bot.getPhotoUrl());
            ImageLoader.newInstance()
                    .fragment(this)
                    .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new JandiProfileTransform(ivPreviewProfile.getContext(),
                            TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                            TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                            Color.WHITE))
                    .uri(uri)
                    .into(ivPreviewProfile);
        }

        String message;
        if (item.message instanceof ResMessages.FileMessage) {
            message = ((ResMessages.FileMessage) item.message).content.title;
        } else if (item.message instanceof ResMessages.CommentMessage) {
            message = ((ResMessages.CommentMessage) item.message).content.body;
        } else if (item.message instanceof ResMessages.TextMessage) {
            message = ((ResMessages.TextMessage) item.message).content.body;
        } else if (item.message instanceof ResMessages.StickerMessage
                || item.message instanceof ResMessages.CommentStickerMessage) {
            String format = JandiApplication.getContext()
                    .getResources().getString(R.string.jandi_coach_mark_stickers);
            message = String.format("(%s)", format);
        } else {
            message = "";
        }

        SpannableStringBuilder builder =
                new SpannableStringBuilder(TextUtils.isEmpty(message) ? "" : message);

        SpannableLookUp.text(builder)
                .hyperLink(true)
                .markdown(true)
                .lookUp(tvPreviewContent.getContext());

        builder.removeSpan(JandiURLSpan.class);
        tvPreviewContent.setText(builder);

        vgPreview.setVisibility(View.VISIBLE);
    }

    private void onMessageItemClick(ResMessages.Link link) {
        hideAllSoftInputArea();

        if (link instanceof DummyMessageLink) {
            showDummyMessageDialog(((DummyMessageLink) link));
            return;
        }

        if (link.message instanceof ResMessages.CommentMessage
                || link.message instanceof ResMessages.CommentStickerMessage) {

            if (ResMessages.FeedbackType.POLL.value().equals(link.feedbackType)) {

                PollDetailActivity.start(getActivity(), link.poll.getId());
                getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

            } else {
                sendAnalyticsEvent(AnalyticsValue.Action.FileView_ByComment);

                FileDetailActivity_.intent(this)
                        .roomId(room.getRoomId())
                        .selectMessageId(link.messageId)
                        .fileId(link.message.feedbackId)
                        .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
                getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        } else {
            if (link.message instanceof ResMessages.PollMessage) {

                PollDetailActivity.start(getActivity(), link.pollId);
                getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

            } else if (link.message instanceof ResMessages.FileMessage) {
                ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
                boolean isImageFile = fileMessage.content.type.startsWith("image");
                sendAnalyticsEvent(isImageFile
                        ? AnalyticsValue.Action.FileView_ByPhoto
                        : AnalyticsValue.Action.FileView_ByFile);

                if (isImageFile) {
                    Intent intent = CarouselViewerActivity.getCarouselViewerIntent(
                            getActivity(), fileMessage.id, room.getRoomId())
                            .build();
                    startActivityForResult(intent, JandiConstants.TYPE_FILE_DETAIL_REFRESH);
                } else {
                    FileDetailActivity_.intent(this)
                            .roomId(room.getRoomId())
                            .selectMessageId(link.messageId)
                            .fileId(link.messageId)
                            .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
                }
                getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

            }
        }
    }

    private void onMessageLongClick(ResMessages.Link link) {
        if (link == null) {
            return;
        }

        if (link instanceof DummyMessageLink) {
            DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
            if (TextUtils.equals(dummyMessageLink.getStatus(), SendMessage.Status.FAIL.name())) {
                showDummyMessageDialog(dummyMessageLink);
            }
            return;
        }

        messageListPresenter.onDetermineMessageMenuDialog(link.message);
    }

    private void showDummyMessageDialog(DummyMessageLink dummyMessageLink) {
        long localId = dummyMessageLink.getLocalId();
        DummyMessageDialog.showDialog(getChildFragmentManager(), localId);
    }

    private void showPreviewForUploadPhoto(int requestCode, Intent intent) {
        List<String> filePaths =
                fileUploadController.getFilePath(getActivity(), requestCode, intent);
        if (filePaths == null || filePaths.size() == 0) {
            filePaths = new ArrayList<>();
            String filePath = photoFileByCamera.getPath();
            filePaths.add(filePath);
        }

        FileUploadPreviewActivity_.intent(this)
                .singleUpload(true)
                .realFilePathList(new ArrayList<>(filePaths))
                .selectedEntityIdToBeShared(entityId)
                .from(FileUploadPreviewActivity.FROM_TAKE_PHOTO)
                .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
    }

    private void showPreviewForUploadFiles(int requestCode, Intent intent) {
        List<String> filePaths = fileUploadController.getFilePath(getActivity(), requestCode, intent);
        if (filePaths != null && filePaths.size() > 0) {
            FileUploadPreviewActivity_.intent(this)
                    .singleUpload(true)
                    .realFilePathList(new ArrayList<>(filePaths))
                    .selectedEntityIdToBeShared(entityId)
                    .from(FileUploadPreviewActivity.FROM_SELECT_FILE)
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
        }
    }

    private void upload(Intent intent) {
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

    @FocusChange(R.id.et_message)
    void onEditTextFocusChange(boolean focus, View view) {
        if (focus) {
            sendAnalyticsEvent(AnalyticsValue.Action.MessageInputField);
        }
    }

    @TextChange(R.id.et_message)
    void onMessageChanged(TextView tv, CharSequence text) {

        boolean isEmptyText = TextUtils.isEmpty(text.toString().trim()) && stickerInfo == NULL_STICKER;
        setSendButtonEnabled(!isEmptyText);

    }

    @Click(R.id.tv_messages_date_divider)
    void dateClick() {
        int firstVisibleItemPosition = ((LinearLayoutManager) lvMessages.getLayoutManager()).findFirstVisibleItemPosition();
        MainMessageListAdapter messageAdapter = (MainMessageListAdapter) lvMessages.getAdapter();

        Date time = messageAdapter.getItem(firstVisibleItemPosition).time;
        Calendar clickedCalenar = Calendar.getInstance();
        clickedCalenar.setTime(time);
        int clickedDay = clickedCalenar.get(Calendar.DAY_OF_YEAR);
        int tempPosition = firstVisibleItemPosition;
        Calendar tempCalendar;
        for (int visibleItemPosition = firstVisibleItemPosition; visibleItemPosition > 0; visibleItemPosition--) {
            ResMessages.Link item = messageAdapter.getItem(visibleItemPosition);
            tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(item.time);
            if (tempCalendar.get(Calendar.DAY_OF_YEAR) == clickedDay) {
                tempPosition = visibleItemPosition;
            } else {
                break;
            }
        }
        lvMessages.scrollToPosition(tempPosition);
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
                    (!isInDirectMessage()
                            && mentionControlViewModel != null
                            && mentionControlViewModel.hasMentionMember())
                            ? new ReqSendMessageV3(message, mentions)
                            : new ReqSendMessageV3(message, new ArrayList<>());
        }

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            sendStickerMessage();
        }

        if (!TextUtils.isEmpty(message)) {
            messageListPresenter.sendTextMessage(message, mentions, reqSendMessageV3);
            messageListPresenter.deleteReadyMessage();
        }

        dismissStickerPreview();
        stickerInfo = NULL_STICKER;
        setSendButtonEnabled(false);
        etMessage.setText("");

        sendAnalyticsEvent(AnalyticsValue.Action.Send);
    }

    private void sendStickerMessage() {
        long stickerGroupId = stickerInfo.getStickerGroupId();
        String stickerId = stickerInfo.getStickerId();
        StickerRepository.getRepository().upsertRecentSticker(stickerGroupId, stickerId);

        messageListPresenter.sendStickerMessage(stickerInfo);

        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Send);
    }

    @Click(R.id.btn_show_mention)
    void onMentionClick() {
        etMessage.requestFocus();

        BaseInputConnection inputConnection = new BaseInputConnection(etMessage, true);
        if (needSpace(etMessage.getSelectionStart(), etMessage.getText().toString())) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        }

        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_AT));

        if (softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputAreaAndShowSoftInput();
        } else {
            softInputAreaController.showSoftInput();
        }
    }

    private boolean needSpace(int cursorPosition, String message) {
        int selectionStart = cursorPosition;
        if (selectionStart > 0) {
            CharSequence charSequence = message.substring(selectionStart - 1, selectionStart);
            return !TextUtils.isEmpty(charSequence.toString().trim());
        }
        return false;
    }

    @Click(R.id.iv_messages_preview_sticker_close)
    void onStickerPreviewClose() {
        this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        if (mentionControlViewModel != null) {
            ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject();
            if (TextUtils.isEmpty(mentionInfoObject.getMessage())) {
                setSendButtonEnabled(false);
            }
        } else {
            if (TextUtils.isEmpty(etMessage.getText())) {
                setSendButtonEnabled(false);
            }
        }

        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_cancel);
    }

    public void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    @Click(R.id.vg_messages_preview_last_item)
    void onPreviewClick() {
        setPreviewVisible(false);
        messageRecyclerViewManager.scrollToLast();
    }

    public void setSendButtonEnabled(boolean enabled) {
        btnSend.setEnabled(enabled);
    }

    @Nullable
    private ResultMentionsVO getMentionVO() {
        if (!isInDirectMessage() && mentionControlViewModel != null) {
            return mentionControlViewModel.getMentionInfoObject();
        } else {
            return null;
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOldLoadProgress() {
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
    public void showEmptyView(boolean show) {
        vgEmptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Click(R.id.vg_message_offline)
    @Override
    public void dismissOfflineLayer() {
        offlineLayer.dismissOfflineView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showOfflineLayer() {
        offlineLayer.showOfflineView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLastPage() {
        messageRecyclerViewManager.scrollToLast();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissOldLoadProgress() {

        if (oldProgressBar.getVisibility() != View.VISIBLE) {
            return;
        }

        Animation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f);
        int duration = JandiApplication.getContext()
                .getResources().getInteger(R.integer.duration_short_anim);
        outAnim.setDuration(duration);
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

    public void onEvent(SocketMessageCreatedEvent event) {
        if (event.getData() != null
                && event.getData().getLinkMessage() != null
                && event.getData().getLinkMessage().toEntity != null
                && !event.getData().getLinkMessage().toEntity.isEmpty()
                && event.getData().getLinkMessage().toEntity.contains(room.getRoomId())) {
            if (messageListPresenter != null) {
                messageListPresenter.addNewMessageOfLocalQueue(event.getData().getLinkMessage());
            }
        }

    }

    public void onEvent(TopicInfoUpdateEvent event) {
        if (event.getId() == room.getRoomId()) {
            modifyTitle(TeamInfoLoader.getInstance().getTopic(room.getRoomId()).getName());
        }
    }

    public void onEvent(SocketMessageDeletedEvent event) {

        messageListPresenter.removeOfMessageId(event.getData().getMessageId());
    }

    public void onEvent(RefreshOldMessageEvent event) {
        if (!isForeground) {
            return;
        }


        if (room.getRoomId() > 0) {
            messageListPresenter.addOldMessageQueue();
        }
    }

    public void onEvent(MentionableMembersRefreshEvent event) {
        if (!isForeground) {
            return;
        }

        if (mentionControlViewModel == null) {
            return;
        }

        setMentionButtonVisibility(mentionControlViewModel.hasMentionMember());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setMentionButtonVisibility(boolean show) {
        btnShowMention.setVisibility(show
                ? View.VISIBLE : View.GONE);
    }

    public void onEvent(SocketPollEvent event) {
        if (room == null || messageListPresenter == null) {
            return;
        }

        if (event.getPoll() != null
                && event.getPoll().getTeamId() == room.getTeamId()
                && event.getPoll().getTopicId() == room.getRoomId()) {

            messageListPresenter.changePollData(event.getPoll());
        }
    }

    public void onEvent(LinkPreviewUpdateEvent event) {
        long messageId = event.getMessageId();

        if (messageId <= 0) {
            return;
        }

        messageListPresenter.addUpdateLinkPreviewMessageQueue(messageId);
    }

    public void onEvent(RoomMarkerEvent event) {
        if (!isForeground) {
            return;
        }


        if (event.getRoomId() == room.getRoomId()) {
            refreshMessages();
        }
    }

    public void onEvent(ShowProfileEvent event) {
        if (!isForeground) {
            return;
        }

        MemberProfileActivity_.intent(getActivity())
                .memberId(event.userId)
                .from(isInDirectMessage()
                        ? MemberProfileActivity.EXTRA_FROM_TOPIC_CHAT
                        : MemberProfileActivity.EXTRA_FROM_MESSAGE)
                .start();

        if (event.from != null) {
            sendAnalyticsEvent(AnalyticsUtil.getProfileAction(event.userId, event.from));
        }

    }

    public void onEventMainThread(ChatCloseEvent event) {
        if (entityId == event.getCompanionId()) {
            finish();
        }
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (room.getRoomId() == event.getTopicId()) {
            finish();
            String msg = JandiApplication.getContext().getString(R.string.topic_room_deletetopicconfirm);
            showToast(msg, true);
        }
    }

    public void onEventMainThread(TopicKickedoutEvent event) {
        if (room.getRoomId() == event.getRoomId()) {
            finish();
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            CharSequence topicName = null;
            if (actionBar != null) {
                topicName = actionBar.getTitle();
            }
            String msg = JandiApplication.getContext().getString(R.string.jandi_kicked_message, topicName);
            showToast(msg, true /* isError */);
        }
    }


    public void onEventMainThread(EventUpdateStart event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            vgSynchronize.setVisibility(View.VISIBLE);
            tvSynchronize.setText(R.string.jandi_syncing_message);
        }
    }

    public void onEventMainThread(EventUpdateInProgress event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            if (vgSynchronize.getVisibility() != View.VISIBLE) {
                vgSynchronize.setVisibility(View.VISIBLE);
            }
            int percent = (event.getProgress() * 100) / event.getMax();
            String syncMsg = JandiApplication.getContext().getString(R.string.jandi_syncing_message);
            tvSynchronize.setText(String.format(syncMsg + "...(%d\\%)", percent));
        }
    }

    public void onEventMainThread(EventUpdateFinish event) {
        if (vgSynchronize != null && tvSynchronize != null) {
            if (vgSynchronize.getVisibility() != View.GONE) {
                vgSynchronize.setVisibility(View.GONE);
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void closeDialogFragment() {
        android.app.Fragment dialogFragment =
                getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null && dialogFragment instanceof android.app.DialogFragment) {
            ((android.app.DialogFragment) dialogFragment).dismiss();
        }
    }

    public void onEvent(LinkPreviewClickEvent event) {

        String linkUrl = event.getLinkUrl();
        if (TextUtils.isEmpty(linkUrl)) {
            return;
        }

        ApplicationUtil.startWebBrowser(getActivity(), linkUrl);

        getActivity().overridePendingTransition(
                R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
    }

    public void onEvent(ProfileChangeEvent event) {
        refreshMessages();
        updateMentionInfo();
    }

    public void onEvent(RefreshConnectBotEvent event) {
        refreshMessages();
    }

    public void onEvent(SocketAnnouncementDeletedEvent event) {
        AnalyticsUtil.sendEvent(
                AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.Accouncement_Delete);
        announcementViewModel.setAnnouncement(null);
    }

    public void onEvent(AnnouncementUpdatedEvent event) {
        messageListPresenter.onChangeAnnouncementOpenStatusAction(event.isOpened());
        messageListPresenter.setAnnouncementActionFrom(false);
    }

    public void onEvent(SocketAnnouncementCreatedEvent event) {
        if (room.getRoomId() > 0) {
            messageListPresenter.onInitAnnouncement();
        }

    }

    public void onEvent(AnnouncementEvent event) {
        switch (event.getAction()) {
            case CREATE:
                messageListPresenter.onCheckAnnouncementExistsAndCreate(event.getMessageId());
                sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap_Announce);
                break;
            case DELETE:
                messageListPresenter.onDeleteAnnouncementAction();
                break;
        }
    }

    public void onEvent(MessageStarredEvent event) {
        if (!isForeground) {
            return;
        }

        long messageId = event.getMessageId();
        switch (event.getAction()) {
            case STARRED:
                messageListPresenter.onMessageStarredAction(messageId);

                sendAnalyticsEvent(
                        AnalyticsValue.Action.MsgLongTap_Star, AnalyticsValue.Label.On);
                break;
            case UNSTARRED:
                messageListPresenter.onMessageUnStarredAction(messageId);

                sendAnalyticsEvent(
                        AnalyticsValue.Action.MsgLongTap_Star, AnalyticsValue.Label.Off);
                break;
        }
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {

        if (!isForeground) {
            return;
        }

        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    public void onEvent(UnshareFileEvent event) {
        messageListPresenter.updateCachedTypeOfMessageId(event.getFileId());
    }

    public void onEvent(DummyRetryEvent event) {
        if (!isForeground) {
            return;
        }

        messageListPresenter.retryToSendDummyMessage(event.getLocalId());
    }

    public void onEvent(DummyDeleteEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.onDeleteDummyMessageAction(event.getLocalId());
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        messageListPresenter.onDeleteMessageAction(event.messageType, event.messageId);

        sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap_Delete);

    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }

        final ClipData clipData = ClipData.newPlainText("", event.contentString);
        clipboardManager.setPrimaryClip(clipData);
        sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap_Copy);
    }

    public void onEvent(ConfirmFileUploadEvent event) {
        if (!isForeground) {
            return;
        }

        startFileUpload(event.title, event.entityId, event.realFilePath, event.comment);
    }

    private void startFileUpload(String title, long entityId, String filePath, String comment) {
        fileUploadController.startUpload(getActivity(), title, entityId, filePath, comment);
    }

    public void onEvent(FileUploadFinishEvent event) {
        refreshMessages();
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
                .isFromPush(isFromPush)
                .start();
    }

    public void onEvent(DeleteFileEvent event) {
        messageListPresenter.changeLinkStatusToArchive(event.getId());
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (!isForeground) {
            return;
        }


        // 삭제된 메세지는 임의 처리
        Observable.just(event)
                .filter(event2 -> !event2.isAdded())
                .flatMap(event2 -> Observable.from(event2.getSharedRooms()))
                .takeFirst(rooomId -> rooomId == room.getRoomId())
                .subscribe(rooomId -> {
                    messageListPresenter.removeOfMessageId(event.getCommentId());
                });

    }

    public void onEvent(TeamLeaveEvent event) {
        messageListPresenter.onTeamLeaveEvent(event.getTeamId(), event.getMemberId());
    }

    public void onEvent(MessageStarEvent event) {
        long messageId = event.getMessageId();
        boolean starred = event.isStarred();

        messageListPresenter.updateStarredOfMessage(messageId, starred);
    }

    public void onEvent(SocketServiceStopEvent event) {
        if (!TextUtils.isEmpty(TokenUtil.getRefreshToken())) {
            // 토큰이 없으면 개망..-o-
            JandiSocketService.startServiceForcily(getActivity());
        }
    }

    public void onEvent(TopicInviteEvent event) {
        if (!isForeground) {
            return;
        }
        startActivity(Henson.with(getActivity())
                .gotoTeamMemberSearchActivity()
                .isSelectMode(true)
                .room_id(entityId)
                .from(TeamMemberSearchActivity.EXTRA_FROM_INVITE_TOPIC)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.InviteTeamMembers);
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }

        requestFileUploadEventType = event.type;

        Permissions.getChecker()
                .activity(getActivity())
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(this::handleFileUpload)
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, REQ_STORAGE_PERMISSION);
                })
                .check();

    }

    private void handleFileUpload() {
        if (requestFileUploadEventType == -1) {
            return;
        }

        fileUploadController.selectFileSelector(requestFileUploadEventType, this, entityId);

        AnalyticsValue.Action action;
        switch (requestFileUploadEventType) {
            default:
            case FileUploadController.TYPE_UPLOAD_GALLERY:
                action = AnalyticsValue.Action.Upload_Photo;
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                action = AnalyticsValue.Action.Upload_Camera;
                break;
            case FileUploadController.TYPE_UPLOAD_EXPLORER:
                action = AnalyticsValue.Action.Upload_File;
                break;
        }

        sendAnalyticsEvent(action);

        requestFileUploadEventType = -1;
    }

    public void onEvent(RequestCreatePollEvent event) {
        sendAnalyticsEvent(AnalyticsValue.Action.Upload_Poll);
        PollCreateActivity.start(getActivity(), room.getRoomId());
    }

    public void onEvent(NetworkConnectEvent event) {
        if (event.isConnected()) {
            messageListPresenter.onNetworkConnect();

            dismissOfflineLayer();
        } else {

            showOfflineLayer();

            if (isForeground) {
                showOfflineToast();
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showOfflineToast() {
        String message = JandiApplication.getContext()
                .getResources()
                .getString(R.string.jandi_msg_network_offline_warn);
        ColoredToast.showGray(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateMentionInfo() {
        if (mentionControlViewModel != null) {
            boolean popupShowing = etMessage.isPopupShowing();
            mentionControlViewModel.refreshMembers(Arrays.asList(room.getRoomId()));
            if (popupShowing) {
                etMessage.showDropDown();
                btnShowMention.setVisibility(View.GONE);
            } else {
                etMessage.dismissDropDown();
                btnShowMention.setVisibility(View.VISIBLE);
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
        FragmentActivity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    @Override
    public void insertTeamMemberEmptyLayout() {

        if (vgEmptyLayout == null) {
            return;
        }
        vgEmptyLayout.removeAllViews();
        View view = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.view_team_member_empty, vgEmptyLayout, true);
        View.OnClickListener onClickListener = v -> {
            InviteDialogExecutor.getInstance().executeInvite(getContext());
        };
        view.findViewById(R.id.img_chat_choose_member_empty).setOnClickListener(onClickListener);
        view.findViewById(R.id.btn_chat_choose_member_empty).setOnClickListener(onClickListener);

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void insertTopicMemberEmptyLayout() {

        if (vgEmptyLayout == null) {
            return;
        }

        vgEmptyLayout.removeAllViews();
        View view = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.view_topic_member_empty, vgEmptyLayout, true);
        view.findViewById(R.id.img_chat_choose_member_empty)
                .setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));
        view.findViewById(R.id.btn_chat_choose_member_empty)
                .setOnClickListener(v -> EventBus.getDefault().post(new TopicInviteEvent()));

    }

    public void onEvent(TopicLeftEvent event) {
        if (event.getTopicId() == room.getRoomId()) {
            updateMentionInfo();
        }
    }

    public void onEvent(TopicJoinEvent event) {
        if (event.getTopicId() == room.getRoomId()) {
            updateMentionInfo();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearEmptyMessageLayout() {
        if (vgEmptyLayout != null) {
            vgEmptyLayout.removeAllViews();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void insertMessageEmptyLayout() {

        if (vgEmptyLayout == null) {
            return;
        }
        vgEmptyLayout.removeAllViews();

        LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.view_message_list_empty, vgEmptyLayout, true);

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void modifyTitle(String name) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(name);
    }

    @Override
    public void openAnnouncement(boolean shouldOpenAnnouncement) {
        announcementViewModel.openAnnouncement(shouldOpenAnnouncement);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showAnnouncementCreateDialog(long messageId) {
        announcementViewModel.showCreateAlertDialog(
                (dialog, which) -> messageListPresenter.onCreateAnnouncement(messageId));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showStickerMessageMenuDialog(ResMessages.StickerMessage stickerMessage) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByStickerMessage(
                stickerMessage, true);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showTextMessageMenuDialog(ResMessages.TextMessage textMessage,
                                          boolean isDirectMessage, boolean isMyMessage) {
        if (isResumed()) {
            DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage(
                    textMessage, isMyMessage, isDirectMessage);
            newFragment.show(getChildFragmentManager(), "dioalog");
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCommentMessageMenuDialog(ResMessages.CommentMessage message) {
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(
                message, false);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showLeavedMemberDialog(String name) {
        String msg = JandiApplication.getContext()
                .getResources().getString(R.string.jandi_no_long_team_member, name);

        AlertUtil.showConfirmDialog(getActivity(), msg, null, false);
    }

    @Override
    public void showMessageStarSuccessToast() {
        String msg = JandiApplication.getContext()
                .getResources().getString(R.string.jandi_message_starred);

        showToast(msg, false /* isError */);
    }

    @Override
    public void showMessageUnStarSuccessToast() {
        String msg = JandiApplication.getContext()
                .getResources().getString(R.string.jandi_message_no_starred);

        showToast(msg, false /* isError */);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void modifyStarredInfo(long messageId, boolean isStarred) {

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissUserStatusLayout() {
        vgMemberStatusAlert.setVisibility(View.GONE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshMessages() {
        if (adapterView != null) {
            adapterView.refresh();
        }
    }

    @Override
    public void updateRecyclerViewInfo() {
        messageRecyclerViewManager.updateFirstVisibleItem();
        messageRecyclerViewManager.updateLastVisibleItem();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showToast(String message, boolean isError) {
        if (isError) {
            ColoredToast.showError(message);
        } else {
            ColoredToast.show(message);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void refreshActionbar() {
        setUpActionbar();
        getActivity().invalidateOptionsMenu();
    }

    private boolean isInDirectMessage() {
        return entityType == JandiConstants.TYPE_DIRECT_MESSAGE;
    }

    @Override
    public boolean onBackPressed() {
        if (softInputAreaController != null && softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputArea(true, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKey(int keyCode, KeyEvent event) {

        if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_POUND)
                || (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_PERIOD)
                || (keyCode >= KeyEvent.KEYCODE_GRAVE && keyCode <= KeyEvent.KEYCODE_AT)) {
            if (!etMessage.isFocused()) {
                etMessage.requestFocus();
                etMessage.setSelection(etMessage.getText().length());
                return true;
            }
        }

        return false;
    }
}