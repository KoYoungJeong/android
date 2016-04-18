package com.tosslab.jandi.app.ui.message.v2;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.ChatCloseEvent;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
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
import com.tosslab.jandi.app.events.messages.TopicInviteEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.MainFileUploadControllerImpl;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.services.socket.to.SocketServiceStopEvent;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.MainMessageListAdapter;
import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListHeaderAdapter;
import com.tosslab.jandi.app.ui.message.v2.dialog.DummyMessageDialog_;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.AnnouncementViewModel;
import com.tosslab.jandi.app.ui.message.v2.viewmodel.FileUploadStateViewModel;
import com.tosslab.jandi.app.ui.offline.OfflineLayer;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.TutorialCoachMarkUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.imeissue.EditableAccomodatingLatinIMETypeNullIssues;
import com.tosslab.jandi.app.utils.transform.TransformConfig;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.spannable.JandiURLSpan;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 16. 2. 16..
 */
@OptionsMenu(R.menu.message_list_menu_basic)
@EFragment(R.layout.fragment_message_list)
public class MessageListV2Fragment extends Fragment implements
        MessageListV2Presenter.View,
        MessageListV2Activity.OnBackPressedListener,
        MessageListV2Activity.OnKeyPressListener {

    public static final String EXTRA_FILE_DELETE = "file_delete";
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_WINDOW_PERMISSION = 102;

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
    InvitationDialogExecutor invitationDialogExecutor;

    @Bean
    KeyboardHeightModel keyboardHeightModel;
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

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.lv_messages)
    RecyclerView lvMessages;
    @ViewById(R.id.btn_send_message)
    View btnSend;
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
    SimpleDraweeView ivSticker;
    @ViewById(R.id.vg_message_offline)
    View vgOffline;
    @ViewById(R.id.progress_message)
    View oldProgressBar;
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

    @ViewById(R.id.vg_messages_member_status_alert)
    View vgMemberStatusAlert;
    @ViewById(R.id.iv_messages_member_status_alert)
    ImageView ivMemberStatusAlert;
    @ViewById(R.id.tv_messages_member_status_alert)
    TextView tvMemberStatusAlert;

    private OfflineLayer offlineLayer;

    private ProgressWheel progressWheel;

    private MainMessageListAdapter messageAdapter;

    private StickerInfo stickerInfo = NULL_STICKER;

    private File photoFileByCamera;

    private boolean isForeground = true;
    private LinearLayoutManager layoutManager;

    private ButtonAction currentButtonAction = ButtonAction.KEYBOARD;

    private Room room;
    private MessagePointer messagePointer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (savedInstanceState != null) {
            photoFileByCamera = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;

        PushMonitor.getInstance().register(roomId);

        fileUploadStateViewModel.registerEventBus();
        fileUploadStateViewModel.initDownloadState();

        messageListPresenter.restoreStatus();
        if (messageAdapter.getItemCount() > 0) {
            messageAdapter.notifyItemRangeChanged(0, messageAdapter.getItemCount());
        }
        messageListPresenter.addNewMessageQueue(true);
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
                    saveCacheAndNotifyDataSetChanged(null);

                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                    if (stickerViewModel != null) {
                        stickerViewModel.onConfigurationChanged();
                    }
                    if (uploadMenuViewModel != null) {
                        uploadMenuViewModel.onConfigurationChanged();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(getActivity())
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        () -> Observable.just(1)
                                .delay(300, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    int keyboardHeight =
                                            JandiPreference.getKeyboardHeight(JandiApplication.getContext());
                                    showUploadMenuSelectorIfNotShow(keyboardHeight);
                                }, Throwable::printStackTrace))
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

        dismissStickerSelectorIfShow();
        dismissUploadSelectorIfShow();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fileUploadController.getUploadedFile() != null) {
            outState.putSerializable(EXTRA_NEW_PHOTO_FILE, fileUploadController.getUploadedFile());
        }
    }


    @Override
    public void onDestroy() {
        messageListPresenter.unSubscribeMessageQueue();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @AfterInject
    void initObjects() {
        JandiPreference.setKeyboardHeight(getActivity(), -1);

        room = Room.create(entityId, roomId, isFromPush);
        messagePointer = MessagePointer.create(firstCursorLinkId, lastReadLinkId);
    }

    @AfterViews
    void initViews() {
        setUpActionbar();
        setHasOptionsMenu(true);

        trackScreenView();

        initPresenter();

        initOffLineLayer();

        initProgressWheel();

        initEmptyLayout();

        initMessageEditText();

        initStickerViewModel();

        initFileUploadStateViewModel();

        initUploadViewModel();

        initActionListeners();

        initMessageListView();

        initUserStatus();

        initAnnouncement();

        initMessages(true /* withProgress */);

        showCoachMarkIfNeed();
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
        if (activity.getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            activity.setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        }

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        actionBar.setTitle(EntityManager.getInstance().getEntityNameById(room.getEntityId()));
    }

    private void trackScreenView() {
        int screenView = entityType == JandiConstants.TYPE_PUBLIC_TOPIC
                ? ScreenViewProperty.PUBLIC_TOPIC : ScreenViewProperty.PRIVATE_TOPIC;

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
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

        etMessage.setOnClickListener(v -> {
            dismissStickerSelectorIfShow();
            dismissUploadSelectorIfShow();
        });

        etMessage.setOnBackPressListener(() -> {
            if (keyboardHeightModel.isOpened()) {
                //키보드가 열려져 있고 그 위에 스티커가 있는 상태에서 둘다 제거 할때 속도를 맞추기 위해 딜레이를 줌
                Observable.just(1)
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
                            dismissStickerSelectorIfShow();
                            dismissUploadSelectorIfShow();
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
            showStickerPreview(oldSticker, stickerInfo);
            setSendButtonEnabled(true);
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
        });

        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId) -> sendMessage());

        stickerViewModel.setType(isInDirectMessage()
                ? StickerViewModel.TYPE_MESSAGE : StickerViewModel.TYPE_TOPIC);

        stickerViewModel.setStickerButton(btnActionButton2);
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
        loadOption.scaleType = ScalingUtils.ScaleType.CENTER_CROP;
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

    private void initFileUploadStateViewModel() {
        fileUploadStateViewModel.setEntityId(entityId);
    }

    private void initUploadViewModel() {
        uploadMenuViewModel.setOptionSpace(vgOptionSpace);
    }

    private void initActionListeners() {
        keyboardHeightModel.addOnKeyboardShowListener((isShowing) -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
            announcementViewModel.setAnnouncementViewVisibility(!visibility);
        });

        stickerViewModel.setOnStickerLayoutShowListener(isShow -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
            announcementViewModel.setAnnouncementViewVisibility(!visibility);
        });

        uploadMenuViewModel.setOnUploadLayoutShowListener(isShow -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
            announcementViewModel.setAnnouncementViewVisibility(!visibility);
        });

        uploadMenuViewModel.setOnClickUploadEventListener(() -> {
            if (keyboardHeightModel.isOpened()) {
                keyboardHeightModel.hideKeyboard();
            }
            currentButtonAction = ButtonAction.KEYBOARD;
            setActionButtons();
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
        messageAdapter = new MainMessageListAdapter(getActivity().getBaseContext());
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
            onMessageItemClick(position);
        });

        // 아이템 롱클릭했을때 액션
        messageAdapter.setOnItemLongClickListener((adapter, position) -> {
            onMessageLongClick(position);
            return true;
        });

        lvMessages.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                keyboardHeightModel.hideKeyboard();
                dismissStickerSelectorIfShow();
                dismissUploadSelectorIfShow();
            }
            return false;
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
            }
        });
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

        btnShowMention.setVisibility(View.VISIBLE);

        List<Long> roomIds = new ArrayList<>();
        roomIds.add(room.getRoomId());

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

        mentionControlViewModel.registClipboardListener();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showDisabledUserLayer() {
        vgMessageInput.setVisibility(View.GONE);
        int dp_2 = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()));
        ((RelativeLayout.LayoutParams) vgMemberStatusAlert.getLayoutParams())
                .setMargins(dp_2, dp_2, dp_2, dp_2);
        vgMemberStatusAlert.setVisibility(View.VISIBLE);
        vgMemberStatusAlert.setBackgroundColor(getResources().getColor(R.color.jandi_disabled_user_background));
        ivMemberStatusAlert.setImageResource(R.drawable.icon_disabled_members_bar);
        tvMemberStatusAlert.setText(R.string.jandi_disabled_user);
        setPreviewVisible(false);
        vgMemberStatusAlert.setOnClickListener(null);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showInactivedUserLayer() {
        ((RelativeLayout.LayoutParams) vgMemberStatusAlert.getLayoutParams()).setMargins(0, 0, 0, 0);
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
    public void setAnnouncement(ResAnnouncement announcement, boolean shouldOpenAnnouncement) {
        announcementViewModel.setAnnouncement(announcement, shouldOpenAnnouncement);
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
                .getResources().getString(R.string.err_messages_invaild_entity);
        showToast(message, true /* isError */);
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
        saveCacheAndNotifyDataSetChanged(null);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpOldMessage(boolean isFirstLoad, boolean isFirstMessage) {
        if (isFirstLoad) {
            // 첫 로드라면...
            clearMessages();

            saveCacheAndNotifyDataSetChanged(() -> {
                layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
                if (!isFirstMessage) {
                    messageAdapter.setOldLoadingComplete();
                } else {
                    messageAdapter.setOldNoMoreLoading();
                }
            });

        } else {
            saveCacheAndNotifyDataSetChangedForAdding(() -> {
                if (!isFirstMessage) {
                    messageAdapter.setOldLoadingComplete();
                } else {
                    messageAdapter.setOldNoMoreLoading();
                }

            });

        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpNewMessage(List<ResMessages.Link> records, long myId,
                                boolean isFirstLoad,
                                boolean moveToLinkId) {
        final int location = records.size() - 1;
        if (location < 0) {
            return;
        }

        int visibleLastItemPosition = layoutManager.findLastVisibleItemPosition();
        int lastItemPosition = messageAdapter.getItemCount();

        messageAdapter.addAll(lastItemPosition, records);

        saveCacheAndNotifyDataSetChanged(() -> {
            ResMessages.Link lastUpdatedMessage = records.get(location);
            if (!isFirstLoad
                    && visibleLastItemPosition >= 0
                    && visibleLastItemPosition < lastItemPosition - 1
                    && lastUpdatedMessage.fromEntity != myId) {
                showPreviewIfNotLastItem();
            } else {
                long messageId = lastUpdatedMessage.messageId;

                if (isFirstLoad) {
                    moveLastReadLink();
                } else {
                    moveToMessage(messageId, 0);
                }
            }
        });
    }

    @UiThread
    public void showPreviewIfNotLastItem() {
        boolean isLastItemShowing =
                layoutManager.findFirstVisibleItemPosition() == messageAdapter.getItemCount() - 1;
        if (isLastItemShowing) {
            return;
        }

        ResMessages.Link item = messageAdapter.getItem(messageAdapter.getItemCount() - 1);

        if (TextUtils.equals(item.status, "event")) {
            return;
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(item.message.writerId);
        tvPreviewUserName.setText(entity.getName());

        String url = ImageUtil.getImageFileUrl(entity.getUserSmallProfileUrl());
        Uri uri = Uri.parse(url);
        if (!EntityManager.getInstance().isBot(entity.getId())) {
            ImageUtil.loadProfileImage(ivPreviewProfile, uri, R.drawable.profile_img);
        } else {
            RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
                    TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);

            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .backgroundColor(Color.WHITE)
                    .roundingParams(circleRoundingParams)
                    .load(uri)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void moveToMessageById(long linkId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.indexOfLinkId(linkId);
        layoutManager.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
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
        View childAt = layoutManager.getChildAt(0);
        if (childAt != null) {
            return childAt.getTop();
        } else {
            return 0;
        }
    }

    private void moveToMessage(long messageId, int firstVisibleItemTop) {
        int itemPosition = messageAdapter.getLastIndexByMessageId(messageId);
        layoutManager.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    private void moveToMessage(int itemPosition, int firstVisibleItemTop) {
        layoutManager.scrollToPositionWithOffset(itemPosition, firstVisibleItemTop);
    }

    private void onMessageItemClick(int position) {
        keyboardHeightModel.hideKeyboard();
        dismissStickerSelectorIfShow();
        dismissUploadSelectorIfShow();

        ResMessages.Link link = messageAdapter.getItem(position);
        if (link == null) {
            return;
        }

        if (link instanceof DummyMessageLink) {
            showDummyMessageDialog(((DummyMessageLink) link));
            return;
        }

        FileDetailActivity_.IntentBuilder_ intentBuilder = FileDetailActivity_.intent(this);
        intentBuilder.roomId(room.getRoomId());
        intentBuilder.selectMessageId(link.messageId);

        AnalyticsValue.Action action = null;

        if (link.message instanceof ResMessages.FileMessage) {
            intentBuilder.fileId(link.messageId);
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
            action = fileMessage.content.type.startsWith("image")
                    ? AnalyticsValue.Action.FileView_ByPhoto
                    : AnalyticsValue.Action.FileView_ByFile;
        } else if (link.message instanceof ResMessages.CommentMessage) {
            intentBuilder.fileId(link.message.feedbackId);
            action = AnalyticsValue.Action.FileView_ByComment;
        } else if (link.message instanceof ResMessages.CommentStickerMessage) {
            intentBuilder.fileId(link.message.feedbackId);
            action = AnalyticsValue.Action.FileView_ByComment;
        } else {
            intentBuilder = null;
        }

        if (action != null) {
            sendAnalyticsEvent(action);
        }

        if (intentBuilder != null) {
            intentBuilder.startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
            getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }

    private void onMessageLongClick(int position) {
        ResMessages.Link link = messageAdapter.getItem(position);
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
        DummyMessageDialog_.builder()
                .localId(localId)
                .build()
                .show(getActivity().getFragmentManager(), "dialog");
    }

    private void dismissStickerSelectorIfShow() {
        if (stickerViewModel.isShow()) {
            stickerViewModel.dismissStickerSelector(true);
            currentButtonAction = ButtonAction.KEYBOARD;
            setActionButtons();
        }
    }

    private void dismissUploadSelectorIfShow() {
        if (uploadMenuViewModel.isShow()) {
            uploadMenuViewModel.dismissUploadSelector(true);
            currentButtonAction = ButtonAction.KEYBOARD;
            setActionButtons();
        }
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
                .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
    }

    private void showPreviewForUploadFiles(int requestCode, Intent intent) {
        List<String> filePaths = fileUploadController.getFilePath(getActivity(), requestCode, intent);
        if (filePaths != null && filePaths.size() > 0) {
            FileUploadPreviewActivity_.intent(this)
                    .singleUpload(true)
                    .realFilePathList(new ArrayList<>(filePaths))
                    .selectedEntityIdToBeShared(entityId)
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

        if (currentButtonAction != ButtonAction.KEYBOARD) {
            if (currentButtonAction == ButtonAction.STICKER || currentButtonAction == ButtonAction.UPLOAD) {
                if (keyboardHeightModel.isOpened()) {
                    dismissStickerSelectorIfShow();
                    dismissUploadSelectorIfShow();
                } else {
                    keyboardHeightModel.showKeyboard();
                }
                currentButtonAction = ButtonAction.KEYBOARD;
                setActionButtons();
            }
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

    @Click(R.id.btn_message_action_button_1)
    public void handleActionButton1() {
        int keyboardHeight = JandiPreference.getKeyboardHeight(getActivity().getApplicationContext());
        switch (currentButtonAction) {
            case KEYBOARD:
                showUploadMenuSelectorIfNotShow(keyboardHeight);
                break;
            case STICKER:
                showUploadMenuSelectorIfNotShow(keyboardHeight);
                break;
            case UPLOAD:
                if (keyboardHeightModel.isOpened()) {
                    dismissUploadSelectorIfShow();
                } else {
                    dismissUploadSelectorIfShow();
                    keyboardHeightModel.showKeyboard();
                }
                break;
        }
    }

    @Click(R.id.btn_message_action_button_2)
    public void handleActionButton2() {
        int keyboardHeight = JandiPreference.getKeyboardHeight(getActivity().getApplicationContext());
        switch (currentButtonAction) {
            case KEYBOARD:
                showStickerSelectorIfNotShow(keyboardHeight);
                break;
            case UPLOAD:
                showStickerSelectorIfNotShow(keyboardHeight);
                break;
            case STICKER:
                sendAnalyticsEvent(AnalyticsValue.Action.Sticker);
                if (keyboardHeightModel.isOpened()) {
                    dismissStickerSelectorIfShow();
                } else {
                    dismissStickerSelectorIfShow();
                    keyboardHeightModel.showKeyboard();
                }
                break;
        }
    }

    private void showStickerSelectorIfNotShow(int height) {
        if (!stickerViewModel.isShow()) {
            if (isCanDrawWindowOverlay()) {
                stickerViewModel.showStickerSelector(height);
                Observable.just(1)
                        .delay(100, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
                            if (uploadMenuViewModel.isShow()) {
                                uploadMenuViewModel.dismissUploadSelector(false);
                            }
                        });
                currentButtonAction = ButtonAction.STICKER;
                setActionButtons();
            } else {
                // Android M (23) 부터 적용되는 시나리오
                requestWindowPermission();
            }
        }
    }

    private void showUploadMenuSelectorIfNotShow(int height) {
        if (!uploadMenuViewModel.isShow()) {
            if (isCanDrawWindowOverlay()) {
                Permissions.getChecker()
                        .activity(getActivity())
                        .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .hasPermission(() -> {
                            uploadMenuViewModel.showUploadSelector(height);
                            Observable.just(1)
                                    .delay(100, TimeUnit.MILLISECONDS)
                                    .subscribe(i -> {
                                        if (stickerViewModel.isShow()) {
                                            stickerViewModel.dismissStickerSelector(false);
                                        }
                                    });
                            currentButtonAction = ButtonAction.UPLOAD;
                            setActionButtons();
                            sendAnalyticsEvent(AnalyticsValue.Action.Upload);
                        })
                        .noPermission(() -> {
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, REQ_STORAGE_PERMISSION);
                        })
                        .check();
            } else {
                requestWindowPermission();
            }
        }
    }

    private boolean isCanDrawWindowOverlay() {
        boolean canDraw;
        if (SdkUtils.isMarshmallow()) {
            canDraw = Settings.canDrawOverlays(getActivity());
        } else {
            canDraw = true;
        }
        return canDraw;
    }

    private void requestWindowPermission() {
        String packageName = JandiApplication.getContext().getPackageName();
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        startActivityForResult(intent, REQ_WINDOW_PERMISSION);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setActionButtons() {
        switch (currentButtonAction) {
            case STICKER:
                btnActionButton1.setImageResource(R.drawable.chat_icon_upload);
                btnActionButton2.setImageResource(R.drawable.chat_icon_keypad);
                break;
            case UPLOAD:
                btnActionButton1.setImageResource(R.drawable.chat_icon_keypad);
                btnActionButton2.setImageResource(R.drawable.chat_icon_emoticon);
                break;
            case KEYBOARD:
                btnActionButton1.setImageResource(R.drawable.chat_icon_upload);
                btnActionButton2.setImageResource(R.drawable.chat_icon_emoticon);
                break;
        }
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
        moveLastPage();
    }

    public void setSendButtonEnabled(boolean enabled) {
        btnSend.setEnabled(enabled);
    }

    @Nullable
    private ResultMentionsVO getMentionVO() {
        return !isInDirectMessage() ? mentionControlViewModel.getMentionInfoObject() : null;
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
    @Override
    public void saveCacheAndNotifyDataSetChanged(
            MainMessageListAdapter.NotifyDataSetChangedCallback callback) {
        messageAdapter.saveCacheAndNotifyDataSetChanged(callback);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void saveCacheAndNotifyDataSetChangedForAdding(
            MainMessageListAdapter.NotifyDataSetChangedCallback callback) {
        messageAdapter.saveCacheAndNotifyDataSetChangedForAdding(callback);
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
    public void clearMessages() {
        messageAdapter.clear();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLastPage() {
        layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setNewNoMoreLoading() {
        messageAdapter.setNewNoMoreLoading();
    }

    public void onEvent(SocketMessageEvent event) {
        boolean isSameRoomId = false;
        String messageType = event.getMessageType();

        if (!TextUtils.equals(messageType, "file_comment")) {

            isSameRoomId = event.getRoom().getId() == room.getRoomId();
        } else {
            for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                if (room.getRoomId() == messageRoom.getId()) {
                    isSameRoomId = true;
                    break;
                }
            }
        }

        if (!isSameRoomId) {
            return;
        }

        if (TextUtils.equals(messageType, "topic_leave")
                || TextUtils.equals(messageType, "topic_join")
                || TextUtils.equals(messageType, "topic_invite")) {

            if (isForeground) {
                initEmptyLayout();
            }

            messageListPresenter.updateRoomInfo(true);

            updateMentionInfo();
        } else {
            messageListPresenter.updateMarker();

            if (!isForeground) {
                return;
            }

            if (room.getRoomId() > 0) {
                messageListPresenter.addNewMessageQueue(true);
            }
        }
    }

    public void onEvent(RefreshNewMessageEvent event) {
        if (!isForeground) {
            return;
        }

        if (room.getRoomId() > 0) {
            messageListPresenter.addNewMessageQueue(true);
        }
    }

    public void onEvent(RefreshOldMessageEvent event) {
        if (!isForeground) {
            return;
        }


        if (room.getRoomId() > 0) {
            messageListPresenter.addOldMessageQueue(true);
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

        saveCacheAndNotifyDataSetChanged(null);
    }

    public void onEvent(SocketRoomMarkerEvent event) {
        if (!isForeground) {
            return;
        }

        if (event.getRoom().getId() == room.getRoomId()) {
            SocketRoomMarkerEvent.Marker marker = event.getMarker();
            messageListPresenter.onRoomMarkerChange(
                    room.getTeamId(), room.getRoomId(), marker.getMemberId(), marker.getLastLinkId());
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
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (entityId == event.getId()) {
            getActivity().finish();
        }
    }

    public void onEventMainThread(TopicKickedoutEvent event) {
        if (room.getRoomId() == event.getRoomId()) {
            getActivity().finish();
            CharSequence topicName = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle();
            String msg = JandiApplication.getContext().getString(R.string.jandi_kicked_message, topicName);
            showToast(msg, true /* isError */);
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void closeDialogFragment() {
        android.app.Fragment dialogFragment =
                getActivity().getFragmentManager().findFragmentByTag("dialog");
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

    public void onEvent(ProfileChangeEvent event) {
        saveCacheAndNotifyDataSetChanged(null);
    }

    public void onEvent(RefreshConnectBotEvent event) {
        saveCacheAndNotifyDataSetChanged(null);
    }

    public void onEvent(ConfirmModifyTopicEvent event) {
        if (!isForeground) {
            return;
        }

        messageListPresenter.onModifyEntityAction(entityType, entityId, event.inputName);
    }

    public void onEvent(SocketAnnouncementEvent event) {
        SocketAnnouncementEvent.Type eventType = event.getEventType();
        switch (eventType) {
            case DELETED:
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.TopicChat, AnalyticsValue.Action.Accouncement_Delete);
            case CREATED:
                if (!isForeground) {
                    messageListPresenter.updateMarker();
                    return;
                }

                if (room.getRoomId() > 0) {
                    messageListPresenter.addNewMessageQueue(true);
                    messageListPresenter.onInitAnnouncement();
                }
                break;
            case STATUS_UPDATED:
                if (!isForeground) {
                    messageListPresenter.setAnnouncementActionFrom(false);
                    messageListPresenter.updateMarker();
                    return;
                }
                SocketAnnouncementEvent.Data data = event.getData();
                if (data != null) {
                    messageListPresenter.onChangeAnnouncementOpenStatusAction(data.isOpened());
                }
                messageListPresenter.setAnnouncementActionFrom(false);
                break;
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

                sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap_Star);
                break;
            case UNSTARRED:
                messageListPresenter.onMessageUnStarredAction(messageId);

                sendAnalyticsEvent(AnalyticsValue.Action.MsgLongTap_Unstar);
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
        saveCacheAndNotifyDataSetChanged(null);
    }

    public void onEvent(DummyRetryEvent event) {
        if (!isForeground) {
            return;
        }

        long localId = event.getLocalId();
        DummyMessageLink dummyMessage = getDummyMessageLink(localId);
        dummyMessage.setStatus(SendMessage.Status.SENDING.name());

        saveCacheAndNotifyDataSetChanged(() -> {
            if (dummyMessage.message instanceof ResMessages.TextMessage) {

                ResMessages.TextMessage dummyMessageContent = (ResMessages.TextMessage) dummyMessage.message;
                List<MentionObject> mentionObjects = new ArrayList<>();

                if (dummyMessageContent.mentions != null) {
                    Observable.from(dummyMessageContent.mentions)
                            .subscribe(mentionObjects::add);
                }

                messageListPresenter.addSendingMessageQueue(
                        localId, dummyMessageContent.content.body, null, mentionObjects);
            } else if (dummyMessage.message instanceof ResMessages.StickerMessage) {
                ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) dummyMessage.message;

                StickerInfo stickerInfo1 = new StickerInfo();
                stickerInfo1.setStickerGroupId(stickerMessage.content.groupId);
                stickerInfo1.setStickerId(stickerMessage.content.stickerId);

                messageListPresenter.addSendingMessageQueue(localId, "", stickerInfo1, new ArrayList<>());
            }
        });


    }

    private DummyMessageLink getDummyMessageLink(long localId) {
        int position = messageAdapter.getDummyMessagePositionByLocalId(localId);

        return ((DummyMessageLink) messageAdapter.getItem(position));
    }

    public void onEvent(DummyDeleteEvent event) {
        if (!isForeground) {
            return;
        }
        DummyMessageLink dummyMessage = getDummyMessageLink(event.getLocalId());
        messageListPresenter.onDeleteDummyMessageAction(dummyMessage.getLocalId());
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

    public void onEvent(ConfirmDeleteTopicEvent event) {
        if (!isForeground) {
            return;
        }
        messageListPresenter.onDeleteTopicAction();
    }

    public void onEvent(FileUploadFinishEvent event) {
        saveCacheAndNotifyDataSetChanged(null);
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

    public void onEvent(DeleteFileEvent event) {
        changeLinkStatusToArchive(event.getId());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void changeLinkStatusToArchive(long messageId) {
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

    public void onEvent(FileCommentRefreshEvent event) {
        if (!isForeground) {
            messageListPresenter.updateMarker();
            return;
        }

        if (room.getRoomId() > 0) {
            messageListPresenter.addNewMessageQueue(true);
        }
    }

    public void onEvent(TeamLeaveEvent event) {
        messageListPresenter.onTeamLeaveEvent(event.getTeamId(), event.getMemberId());
    }

    public void onEvent(SocketMessageStarEvent event) {
        int messageId = event.getMessageId();
        boolean starred = event.isStarred();

        int index = messageAdapter.indexByMessageId(messageId);

        messageAdapter.modifyStarredStateByPosition(index, starred);
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
        MembersListActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(entityId)
                .type(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC)
                .start();
    }

    public void onEvent(SendCompleteEvent event) {
        if (!isForeground) {
            return;
        }
    }

    public void onEvent(SendFailEvent event) {
        if (!isForeground) {
            return;
        }
        saveCacheAndNotifyDataSetChanged(null);
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }

        fileUploadController.selectFileSelector(event.type, this, entityId);

        AnalyticsValue.Action action;
        switch (event.type) {
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
    }

    public void onEvent(NetworkConnectEvent event) {
        if (event.isConnected()) {
            if (messageAdapter.getItemCount() <= 0) {
                // roomId 설정 후...
                initMessages(true);
            } else {
                if (room.getRoomId() > 0) {
                    messageListPresenter.addNewMessageQueue(true);
                }
            }

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
        mentionControlViewModel.refreshMembers(Arrays.asList(room.getRoomId()));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setUpLastReadLinkIdIfPosition() {
        // 마커가 마지막아이템을 가르키고 있을때만 position = -1 처리
        long lastReadLinkId = messagePointer.getLastReadLinkId();
        int markerPosition = messageAdapter.indexOfLinkId(lastReadLinkId);
        if (markerPosition ==
                messageAdapter.getItemCount() - messageAdapter.getDummyMessageCount() - 1) {
            messagePointer.setLastReadLinkId(-1);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
        getActivity().finish();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLastReadLink() {
        long lastReadLinkId = messagePointer.getLastReadLinkId();

        if (lastReadLinkId <= 0) {
            return;
        }

        int position = messageAdapter.indexOfLinkId(lastReadLinkId);

        if (position > 0) {
            int measuredHeight = lvMessages.getMeasuredHeight() / 2;
            if (measuredHeight <= 0) {
                measuredHeight = (int) UiUtils.getPixelFromDp(100f);
            }
            position = Math.min(messageAdapter.getItemCount() - 1, position + 1);
            layoutManager.scrollToPositionWithOffset(position, measuredHeight);
        } else if (position < 0) {
            layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void updateLinkPreviewMessage(ResMessages.TextMessage message) {
        long messageId = message.id;
        int index = messageAdapter.indexByMessageId(messageId);
        if (index < 0) {
            return;
        }

        ResMessages.Link link = messageAdapter.getItem(index);
        if (!(link.message instanceof ResMessages.TextMessage)) {
            return;
        }
        link.message = message;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void insertTeamMemberEmptyLayout() {

        if (vgEmptyLayout == null) {
            return;
        }
        vgEmptyLayout.removeAllViews();
        View view = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.view_team_member_empty, vgEmptyLayout, true);
        View.OnClickListener onClickListener = v -> {
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_TOPIC_CHAT);
            invitationDialogExecutor.execute();
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
    public void showDuplicatedTopicName() {
        String message = JandiApplication.getContext()
                .getResources()
                .getString(R.string.err_entity_duplicated_name);
        showToast(message, true /* isError */);
    }

    @Override
    public void showModifyEntityError() {
        String message = JandiApplication.getContext()
                .getResources()
                .getString(R.string.err_entity_modify);
        showToast(message, true /* isError */);
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
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByTextMessage(
                textMessage, isMyMessage, isDirectMessage);
        newFragment.show(getActivity().getSupportFragmentManager(), "dioalog");
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
    public void deleteLinkByMessageId(long messageId) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.remove(position);
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
                .getResources().getString(R.string.jandi_unpinned_message);

        showToast(msg, false /* isError */);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void modifyStarredInfo(long messageId, boolean isStarred) {
        int position = messageAdapter.indexByMessageId(messageId);
        messageAdapter.modifyStarredStateByPosition(position, isStarred);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissUserStatusLayout() {
        vgMemberStatusAlert.setVisibility(View.GONE);
    }

    private void showCoachMarkIfNeed() {
        TutorialCoachMarkUtil.showCoachMarkTopicIfNotShown(
                entityType == JandiConstants.TYPE_DIRECT_MESSAGE, getActivity());
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
        if (stickerViewModel.isShow()) {
            dismissStickerSelectorIfShow();
            return true;
        }

        if (uploadMenuViewModel.isShow()) {
            dismissUploadSelectorIfShow();
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

    enum ButtonAction {
        UPLOAD, STICKER, KEYBOARD
    }
}