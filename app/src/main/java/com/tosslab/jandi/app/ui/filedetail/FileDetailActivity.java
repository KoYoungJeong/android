package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.entities.MentionableMembersRefreshEvent;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.events.entities.ShowMoreSharedEntitiesEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileStarredStateChangeEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.repositories.ReadyCommentRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.Check;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.filedetail.adapter.FileDetailAdapter;
import com.tosslab.jandi.app.ui.filedetail.dagger.DaggerFileDetailComponent;
import com.tosslab.jandi.app.ui.filedetail.dagger.FileDetailModule;
import com.tosslab.jandi.app.ui.filedetail.views.FileSharedEntityChooseActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;
import com.tosslab.jandi.app.views.controller.SoftInputAreaController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FileDetailActivity extends BaseAppCompatActivity implements FileDetailPresenter.View {
    public static final String TAG = "FileDetail";

    public static final int REQUEST_CODE_SHARE = 0;
    public static final int REQUEST_CODE_PICK = 1;
    public static final int REQUEST_CODE_UNSHARE = 2;
    public static final int REQUEST_CODE_RETURN_FILE_ID = 3;

    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_STORAGE_PERMISSION_EXPORT = 102;
    public static final int REQ_STORAGE_PERMISSION_OPEN = 1033;

    private static final StickerInfo NULL_STICKER = new StickerInfo();

    @Nullable
    @InjectExtra
    boolean fromCarousel = false;

    @Nullable
    @InjectExtra
    long roomId = -1;

    @Nullable
    @InjectExtra
    long fileId;

    @Nullable
    @InjectExtra
    long selectMessageId = -1;

    @Inject
    FileDetailPresenter fileDetailPresenter;

    @Inject
    StickerViewModel stickerViewModel;

    ClipboardManager clipboardManager;
    InputMethodManager inputMethodManager;

    @Bind(R.id.lv_file_detail)
    RecyclerView listView;
    @Bind(R.id.toolbar_file_detail)
    Toolbar toolbar;
    @Bind(R.id.vg_file_detail_input_comment)
    ViewGroup vgInputWrapper;
    @Bind(R.id.et_message)
    BackPressCatchEditText etComment;
    @Bind(R.id.btn_send_message)
    View btnSend;
    @Bind(R.id.vg_file_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @Bind(R.id.vg_file_detail_soft_input_area)
    ViewGroup vgSoftInputArea;
    @Bind(R.id.iv_file_detail_preview_sticker_image)
    ImageView ivStickerPreview;
    @Bind(R.id.vg_file_detail_soft_input_detector)
    SoftInputDetectLinearLayout vgSoftInputDetector;
    @Bind(R.id.btn_show_mention)
    View ivMention;
    @Bind(R.id.btn_file_detail_action)
    ImageView btnAction;

    private SoftInputAreaController softInputAreaController;
    private MentionControlViewModel mentionControlViewModel;
    private FileDetailAdapter adapter;

    private ProgressWheel progressWheel;

    private boolean isForeground = true;
    private boolean isMyFile;
    private boolean isDeletedFile;
    private boolean isExternalShared;

    private StickerInfo stickerInfo = NULL_STICKER;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);
        ButterKnife.bind(this);
        Dart.inject(this);

        DaggerFileDetailComponent.builder()
                .fileDetailModule(new FileDetailModule(this))
                .build()
                .inject(this);

        initViews();

        EventBus.getDefault().register(this);

        btnSend.setEnabled(false);
    }

    void initViews() {
        setUpActionBar();

        initCommentEditText();

        initStickers();

        initSoftInputAreaController();

        initProgressWheel();

        initFileInfoViews();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initCommentEditText() {
        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });
    }

    @OnFocusChange(R.id.et_message)
    void onEditTextFocusChange(boolean focus) {
        if (focus) {
            sendAnalyticsEvent(AnalyticsValue.Action.MessageInputField);
        }
    }

    @OnTextChanged(value = R.id.et_message, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onCommentTextChange(CharSequence text) {
        setCommentSendButtonEnabled();
    }

    private void initStickers() {
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            if (oldSticker.getStickerGroupId() == groupId
                    && oldSticker.getStickerId().equals(stickerId)) {
                sendComment();
            } else {
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                vgStickerPreview.setVisibility(View.VISIBLE);
                StickerManager.getInstance()
                        .loadStickerDefaultOption(ivStickerPreview,
                                stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
                setCommentSendButtonEnabled();

                sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
            }
        });

        stickerViewModel.setType(StickerViewModel.TYPE_FILE_DETAIL);
    }

    private void initSoftInputAreaController() {
        softInputAreaController = new SoftInputAreaController(
                stickerViewModel,
                vgSoftInputDetector, vgSoftInputArea, btnAction, null,
                etComment);
        softInputAreaController.setOnStickerButtonClickListener(() -> {
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker);
        });
        softInputAreaController.init();
        softInputAreaController.setOnSoftInputAreaShowingListener((isShowing, softInputHeight) -> {
            View lastChild = listView.getChildAt(listView.getChildCount() - 1);
            int lastPosition = adapter.getItemCount() - 1;
            int childAdapterPosition = listView.getChildAdapterPosition(lastChild);
            if (lastChild != null
                    && childAdapterPosition == lastPosition) {
                if (isShowing) {
                    listView.post(() -> listView.smoothScrollBy(0, softInputHeight));
                }

            }
        });
    }

    private void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    private void setCommentSendButtonEnabled() {
        boolean hasText = !TextUtils.isEmpty(etComment.getText())
                && TextUtils.getTrimmedLength(etComment.getText()) > 0;
        boolean enabled = vgStickerPreview.getVisibility() == View.VISIBLE || hasText;
        btnSend.setEnabled(enabled);
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    private void initFileInfoViews() {
        layoutManager = new LinearLayoutManager(getBaseContext());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter = new FileDetailAdapter());

        adapter.setOnFileClickListener(this::onFileClick);
        adapter.setOnImageFileClickListener(this::onImageFileClick);
        adapter.setOnCommentClickListener(comment -> hideKeyboard());
        adapter.setOnCommentLongClickListener(this::onCommentLongClick);

        fileDetailPresenter.onInitializeFileDetail(fileId, true /* withProgress */);
    }

    private void initMentionControlViewModel(long messageId, List<Long> sharedTopicIds) {
        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(this,
                    etComment,
                    sharedTopicIds,
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);

            ReadyComment readyComment = ReadyCommentRepository.getRepository().getReadyComment(messageId);
            mentionControlViewModel.setUpMention(readyComment.getText());
            mentionControlViewModel.setOnMentionShowingListener(isShowing -> {
                if (mentionControlViewModel.hasMentionMember()) {
                    ivMention.setVisibility(isShowing ? View.GONE : View.VISIBLE);
                } else {
                    ivMention.setVisibility(View.GONE);
                }
            });

        } else {
            mentionControlViewModel.refreshMembers(sharedTopicIds);
        }
        ivMention.setVisibility(mentionControlViewModel.hasMentionMember() ? View.VISIBLE : View.GONE);

        removeClipboardListenerForMention();
        registerClipboardListenerForMention();
    }

    public void onEventMainThread(MentionableMembersRefreshEvent event) {
        if (!isForeground) {
            return;
        }

        if (mentionControlViewModel == null) {
            return;
        }

        setMentionButtonVisibility(mentionControlViewModel.hasMentionMember());
    }

    void setMentionButtonVisibility(boolean show) {
        ivMention.setVisibility(show
                ? View.VISIBLE : View.GONE);
    }

    public void registerClipboardListenerForMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }

    public void removeClipboardListenerForMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
    }

    @Override
    public void copyToClipboard(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ClipData clipData = ClipData.newPlainText(null, text);
        clipboardManager.setPrimaryClip(clipData);
    }

    @Override
    public void setExternalLinkToClipboard() {
        removeClipboardListenerForMention();

        String externalLink = getExternalLink();
        copyToClipboard(externalLink);
        showEnableExternalLinkSuccessToast();

        registerClipboardListenerForMention();

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_CopyLink);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        isForeground = false;

        dismissStickerPreview();
        dismissStickerSelectorIfShow();
        ReadyCommentRepository.getRepository().upsertReadyComment(new ReadyComment(fileId, etComment.getText().toString()));
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1)
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    adapter.notifyDataSetChanged();

                    if (softInputAreaController != null) {
                        softInputAreaController.onConfigurationChanged();
                    }

                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }

                });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (isDeletedFile) {
            return true;
        }

        if (isMyFile) {
            getMenuInflater().inflate(R.menu.file_detail_activity_my_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.file_detail_activity_menu, menu);
        }

        SubMenu subMenu = menu.findItem(R.id.menu_overflow).getSubMenu();
        if (isExternalShared) {
            MenuItem menuItem = subMenu.findItem(R.id.action_file_detail_enable_external_link);
            menuItem.setTitle(R.string.jandi_copy_link);
        } else {
            subMenu.removeItem(R.id.action_file_detail_disable_external_link);
        }

        return true;
    }

    @Override
    public void clearFileDetailAndComments() {
        adapter.clear();
    }

    @Override
    public void setFileDetail(ResMessages.FileMessage fileMessage, List<Long> sharedTopicIds,
                              boolean isMyFile, boolean isDeletedFile,
                              boolean isImageFile, boolean isExternalShared) {
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileMessage.content.title);
        }

        this.isMyFile = isMyFile;
        this.isDeletedFile = isDeletedFile;
        this.isExternalShared = isExternalShared;

        int viewType = isImageFile
                ? FileDetailAdapter.VIEW_TYPE_IMAGE : FileDetailAdapter.VIEW_TYPE_FILE;

        adapter.setRow(0, new FileDetailAdapter.Row<>(fileMessage, viewType));

        if (isDeletedFile) {
            vgInputWrapper.setVisibility(View.GONE);
        }

        initMentionControlViewModel(fileMessage.id, sharedTopicIds);

        supportInvalidateOptionsMenu();
    }

    @Override
    public void setComments(List<ResMessages.OriginalMessage> fileComments) {
        if (fileComments == null || fileComments.isEmpty()) {
            return;
        }

        List<FileDetailAdapter.Row<?>> rows = new ArrayList<>();
        rows.add(new FileDetailAdapter.Row<>(null, FileDetailAdapter.VIEW_TYPE_COMMENT_DIVIDER));
        Observable.range(0, fileComments.size())
                .map(index -> {
                    ResMessages.OriginalMessage current = fileComments.get(index);
                    boolean textComment = current instanceof ResMessages.CommentMessage;

                    boolean profile = true;
                    ResMessages.OriginalMessage before = null;
                    if (index > 0) {
                        before = fileComments.get(index - 1);
                        profile = current.writerId != before.writerId;
                    }

                    int viewType = getCommentViewType(textComment, profile);
                    return new FileDetailAdapter.Row<>(current, viewType);
                })
                .collect(() -> rows, List::add)
                .subscribe();

        adapter.addRows(rows);
    }

    private int getCommentViewType(boolean textComment, boolean profile) {
        int viewType;
        if (textComment) {
            if (profile) {
                viewType = FileDetailAdapter.VIEW_TYPE_COMMENT;
            } else {
                viewType = FileDetailAdapter.VIEW_TYPE_COMMENT_NO_PROFILE;
            }
        } else {
            if (profile) {
                viewType = FileDetailAdapter.VIEW_TYPE_STICKER;
            } else {
                viewType = FileDetailAdapter.VIEW_TYPE_STICKER_NO_PROFILE;
            }
        }
        return viewType;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void scrollToLastComment() {
        if (adapter.getItemCount() <= 0) {
            return;
        }

        listView.postDelayed(() -> layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0), 1000);
    }

    @Override
    public void setFilesStarredState(boolean starred) {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage != null) {
            fileMessage.isStarred = starred;
        }
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showDialog(Dialog dialog) {
        if (isFinishing()) {
            return;
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void dismissDialog(Dialog dialog) {
        if (isFinishing()) {
            return;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void showUnexpectedErrorToast() {
        showToast(getString(R.string.jandi_err_unexpected), true /* isError */);
    }

    @Override
    public void showStarredSuccessToast() {
        showToast(getString(R.string.jandi_message_starred), false /* isError */);
    }

    @Override
    public void showUnstarredSuccessToast() {
        showToast(getString(R.string.jandi_unpinned_message), false /* isError */);
    }

    @Override
    public void showCommentStarredSuccessToast() {
        showToast(getString(R.string.jandi_message_starred), false /* isError */);
    }

    @Override
    public void showCommentUnStarredSuccessToast() {
        showToast(getString(R.string.jandi_unpinned_message), true /* isError */);
    }

    @Override
    public void modifyCommentStarredState(long messageId, boolean starred) {
        adapter.modifyStarredStateByMessageId(messageId, starred);
        notifyDataSetChanged();
    }

    @Override
    public void showShareErrorToast() {
        showToast(getString(R.string.err_share), true /* isError */);
    }

    @Override
    public void showUnshareSuccessToast() {
        String title = getFileTitle();
        showToast(getString(R.string.jandi_unshare_succeed, title), false /* isError */);
    }

    @Override
    public void showUnshareErrorToast() {
        showToast(getString(R.string.err_unshare), true /* isError */);
    }

    @Override
    public void showDeleteSuccessToast() {
        String title = getFileTitle();
        showToast(getString(R.string.jandi_delete_succeed, title), false /* isError */);
    }

    @Override
    public void showDeleteErrorToast() {
        showToast(getString(R.string.err_delete_file), true /* isError */);
    }

    @Override
    public void showEnableExternalLinkSuccessToast() {
        showToast(getString(R.string.jandi_success_copy_clipboard_external_link), false /* isError */);
    }

    @Override
    public void showDisableExternalLinkSuccessToast() {
        showToast(getString(R.string.jandi_success_disable_external_link), false /* isError */);
    }

    void showToast(String message, boolean isError) {
        if (isError) {
            ColoredToast.showError(message);
        } else {
            ColoredToast.show(message);
        }
    }

    private String getFileTitle() {
        if (getSupportActionBar() != null) {
            CharSequence title = getSupportActionBar().getTitle();
            if (!TextUtils.isEmpty(title)) {
                return title.toString();
            }
        }

        String title = getString(R.string.jandi_tab_file);
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage != null && fileMessage.content != null) {
            title = fileMessage.content.title;
        }
        return title;
    }

    @Override
    public void showMoveToSharedTopicDialog(long entityId) {
        AlertUtil.showDialog(this,
                -1, /* title */
                R.string.jandi_move_entity_after_share, /* message */
                R.string.jandi_confirm, ((dialog, which) -> {
                    moveToSharedEntity(entityId);
                }), /* positive */
                -1, null,  /* neutral */
                R.string.jandi_cancel, null, /* cancel */
                true /* cancellable */);
    }

    @Override
    public void showCheckNetworkDialog(boolean shouldFinishWhenConfirm) {
        DialogInterface.OnClickListener confirmListener = null;
        if (shouldFinishWhenConfirm) {
            confirmListener = (dialog, which) -> finish();
        }

        AlertUtil.showCheckNetworkDialog(this, confirmListener);
    }

    @Override
    public void hideKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    @Override
    public void exportLink(String link) {
        Intent target = new Intent(Intent.ACTION_SEND);
        target.putExtra(Intent.EXTRA_TEXT, link);
        target.setType("text/plain");
        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            showToast(getString(R.string.jandi_err_unexpected), true);
        }
    }

    public void onFileClick(String fileUrl, MimeTypeUtil.SourceType sourceType) {
        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
        } else {
            startFileDownload();
        }
        AnalyticsUtil.sendEvent(
                AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewFile);
    }

    public void onImageFileClick(long fileMessageId, ResMessages.FileMessage fileMessage,
                                 boolean shouldOpenImmediately) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);

        if (fromCarousel) {
            finish();
            return;
        }

        if (roomId > 0) {
            Intent intent = CarouselViewerActivity.getCarouselViewerIntent(
                    FileDetailActivity.this, fileMessage.id, roomId)
                    .fromFileDetail(true)
                    .build();
            startActivityForResult(intent, REQUEST_CODE_RETURN_FILE_ID);
        } else {
            Intent intent = CarouselViewerActivity.getImageViewerIntent(this, fileMessage)
                    .fromFileDetail(true)
                    .build();
            startActivity(intent);
        }

    }

    public void onEvent(ShowProfileEvent event) {
        if (!isForeground) {
            return;
        }

        long userEntityId = event.userId;

        if (AccessLevelUtil.hasAccessLevel(userEntityId)) {
            startActivity(Henson.with(this)
                    .gotoMemberProfileActivity()
                    .memberId(userEntityId)
                    .from(MemberProfileActivity.EXTRA_FROM_FILE_DETAIL)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(this);
        }

        AnalyticsValue.Action action = event.isFromComment
                ? AnalyticsValue.Action.ViewProfile_FromComment : AnalyticsValue.Action.ViewProfile;

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, action);
    }

    public boolean onCommentLongClick(ResMessages.OriginalMessage comment) {
        showChooseDialogIfNeed(comment);
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap);
        return true;
    }

    private void showChooseDialogIfNeed(ResMessages.OriginalMessage comment) {
        if (comment == null) {
            return;
        }

        long myId = TeamInfoLoader.getInstance().getMyId();
        User me = TeamInfoLoader.getInstance().getUser(myId);

        boolean isMine = me != null
                && (me.getId() == comment.writerId || me.isTeamOwner());

        if (comment instanceof ResMessages.CommentMessage) {
            ManipulateMessageDialogFragment.newInstanceByCommentMessage(
                    (ResMessages.CommentMessage) comment, isMine)
                    .show(getSupportFragmentManager(), "choose_dialog");
        } else {
            if (!isMine) {
                return;
            }

            ManipulateMessageDialogFragment.newInstanceByStickerCommentMessage(
                    (ResMessages.CommentStickerMessage) comment, true)
                    .show(getSupportFragmentManager(), "choose_dialog");
        }
    }

    public void onEventMainThread(MoveSharedEntityEvent event) {
        long entityId = event.getEntityId();

        moveToSharedEntity(entityId);
    }

    private void moveToSharedEntity(long entityId) {
        sendAnalyticsEvent(AnalyticsValue.Action.TapSharedTopic);

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        int entityType;
        boolean isStarred = false;
        boolean isUser = false;
        boolean isBot = false;

        if (teamInfoLoader.isTopic(entityId)) {
            if (teamInfoLoader.isPublicTopic(entityId)) {
                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            }
            isStarred = teamInfoLoader.isStarred(entityId);
        } else if (teamInfoLoader.isUser(entityId)) {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            isStarred = teamInfoLoader.isStarredUser(entityId);
            isUser = true;
            if (teamInfoLoader.isJandiBot(entityId)) {
                isBot = true;
            }
        } else {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        }

        if (isUser || isBot) {
            moveToMessageListActivity(entityId, entityType, -1, isStarred);
            return;
        } else {
            TopicRoom topic = teamInfoLoader.getTopic(entityId);
            if (topic.isJoined()) {

                moveToMessageListActivity(entityId, entityType, entityId, isStarred);
            } else {
                fileDetailPresenter.joinAndMove(topic);

            }
        }
    }

    public void onEvent(ShowMoreSharedEntitiesEvent event) {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.shareEntities == null) {
            return;
        }

        long[] sharedEntitiesArray = getSharedEntitiesArray(fileMessage);
        startActivityForResult(Henson.with(this)
                        .gotoFileSharedEntityChooseActivity()
                        .fileId(fileId)
                        .mode(FileSharedEntityChooseActivity.MODE_PICK)
                        .sharedEntities(sharedEntitiesArray)
                        .build(),
                REQUEST_CODE_PICK);
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

    public void onEvent(FileStarredStateChangeEvent event) {
        boolean star = event.getStarredState();
        fileDetailPresenter.onChangeStarredState(fileId, star);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Star,
                star ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    public void onEvent(MessageStarredEvent event) {
        if (!isForeground) {
            return;
        }

        long messageId = event.getMessageId();

        AnalyticsValue.Screen screen = AnalyticsValue.Screen.FileDetail;
        AnalyticsValue.Action action = AnalyticsValue.Action.CommentLongTap_Star;
        switch (event.getAction()) {
            case STARRED:
                fileDetailPresenter.onChangeFileCommentStarredState(messageId, true);
                AnalyticsUtil.sendEvent(screen, action, AnalyticsValue.Label.On);
                break;
            case UNSTARRED:
                fileDetailPresenter.onChangeFileCommentStarredState(messageId, false);
                AnalyticsUtil.sendEvent(screen, action, AnalyticsValue.Label.Off);
                break;
        }
    }

    public void onEventMainThread(MessageStarEvent event) {
        long messageId = event.getMessageId();
        boolean starred = event.isStarred();

        if (messageId == fileId) {
            setFilesStarredState(starred);
            notifyDataSetChanged();
        }
    }

    public void onEvent(DeleteFileEvent event) {
        reInitializeOnEvent(event.getId());
    }

    public void onEvent(ShareFileEvent event) {
        reInitializeOnEvent(event.getId());
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (event.getFileId() == fileId) {
            if (event.isAdded()) {
                int position = adapter.findIndexOfMessageId(event.getCommentId());
                if (position < 0) {
                    fileDetailPresenter.onInitializeFileDetail(event.getFileId(), false);
                }
            } else {
                Observable.just(event)
                        .map(event1 -> adapter.findIndexOfMessageId(event1.getCommentId()))
                        .filter(position -> position >= 0)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::removeComment, Throwable::printStackTrace, () -> {
                            adapter.notifyDataSetChanged();
                        });
            }
        }
    }

    public void onEventMainThread(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        fileDetailPresenter.onDeleteComment(event.messageType, event.messageId, event.feedbackId);
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Delete);
    }

    public void onEventMainThread(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }

        copyToClipboard(event.contentString);

        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Copy);
    }

    public void onEvent(TopicDeleteEvent event) {
        if (!isForeground) {
            return;
        }

        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null) {
            return;
        }
        fileDetailPresenter.onTopicDeleted(event.getTopicId(), fileMessage.shareEntities);
    }

    private void reInitializeOnEvent(long entityId) {
        if (!isForeground) {
            return;
        }

        if (fileId == entityId) {
            fileDetailPresenter.onInitializeFileDetail(fileId, true /* withProgress */);
        }
    }

    public void startFileDownload() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(dialog -> {
            fileDetailPresenter.cancelCurrentDownloading();
        });
        progressDialog.setMessage("Downloading " + fileMessage.content.title);

        fileDetailPresenter.onOpenFile(fileMessage, progressDialog);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_overflow:
                onClickOverflow();
                break;
            case R.id.action_file_detail_download:
                download();
                break;
            case R.id.action_file_detail_share:
                share();
                break;
            case R.id.action_file_detail_unshare:
                unShare();
                break;
            case R.id.action_file_detail_export:
                export();
                break;
            case R.id.action_file_detail_delete:
                delete();
                break;
            case R.id.action_file_detail_enable_external_link:
                enableExternalLink();
                break;
            case R.id.action_file_detail_disable_external_link:
                disableExternalLink();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void onClickOverflow() {
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu);
    }

    void download() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        fileDetailPresenter.onDownloadAction(fileId, fileMessage.content);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Download);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void requestPermission(int requestCode, String... permissions) {
        super.requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(FileDetailActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::download)
                .addRequestCode(REQ_STORAGE_PERMISSION_EXPORT)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::export)
                .addRequestCode(REQ_STORAGE_PERMISSION_OPEN)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::startFileDownload)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(FileDetailActivity.this);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode,
                        permissions,
                        grantResults));
    }

    @Override
    public void startGoogleOrDropboxFileActivity(String fileUrl) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
    }

    @Override
    public void startExportedFileViewerActivity(File file, String mimeType) {
        Intent target = FileUtil.createFileIntent(file, mimeType);
        target.setAction(Intent.ACTION_SEND);

        if (mimeType != null) {
            Bundle extras = new Bundle();
            Uri uri = FileUtil.createOptimizedFileUri(file);
            extras.putParcelable(Intent.EXTRA_STREAM, uri);
            target.putExtras(extras);
        }

        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            showUnexpectedErrorToast();
        }
    }

    @Override
    public void startDownloadedFileViewerActivity(File file, String mimeType) {
        try {
            String mimeFromFile = getFileType(file);
            if (mimeFromFile == null) {
                mimeFromFile = mimeType;
            }
            Intent intent = FileUtil.createFileIntent(file, mimeFromFile);
            startActivity(intent);
            showToast(getString(R.string.jandi_file_downloaded_into, file.getPath()), false);
        } catch (ActivityNotFoundException e) {
            showToast(getString(R.string.err_unsupported_file_type, file), true);
        } catch (SecurityException e) {
            showToast(getString(R.string.err_unsupported_file_type, file), true);
        }
    }

    private String getFileType(File file) {
        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(
                    fileName.substring(idx + 1, fileName.length()).toLowerCase());
        }
        return null;
    }


    @Override
    public void moveToMessageListActivity(long entityId, int entityType, long roomId,
                                          boolean isStarred) {
        startActivity(Henson.with(FileDetailActivity.this)
                .gotoMessageListV2Activity()
                .teamId(TeamInfoLoader.getInstance().getTeamId())
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void deliverResultToMessageList() {
        Intent data = new Intent();
        data.putExtra(MessageListV2Fragment.EXTRA_FILE_DELETE, true);
        data.putExtra(MessageListV2Fragment.EXTRA_FILE_ID, fileId);
        setResult(RESULT_OK, data);
        finish();
    }

    private ResMessages.FileMessage getFileMessageFromAdapter() {
        return adapter.getItem(0);
    }

    void share() {
        RoomFilterActivity.startForResultWithTopicId(this, -1, REQUEST_CODE_SHARE);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Share);
    }

    void unShare() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.shareEntities == null) {
            return;
        }

        long[] sharedEntitiesArray = getSharedEntitiesArray(fileMessage);

        startActivityForResult(Henson.with(this)
                        .gotoFileSharedEntityChooseActivity()
                        .fileId(fileId)
                        .sharedEntities(sharedEntitiesArray)
                        .build(),
                REQUEST_CODE_UNSHARE);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_UnShare);
    }

    private long[] getSharedEntitiesArray(ResMessages.FileMessage fileMessage) {
        List<Long> sharedEntities = new ArrayList<>();
        Observable.from(fileMessage.shareEntities)
                .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .collect(() -> sharedEntities, List::add)
                .subscribe();

        long[] sharedEntitiesArray = new long[sharedEntities.size()];
        for (int i = 0; i < sharedEntitiesArray.length; i++) {
            sharedEntitiesArray[i] = sharedEntities.get(i);
        }
        return sharedEntitiesArray;
    }

    void export() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);

        fileDetailPresenter.onExportFile(fileMessage, progressDialog);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Export);
    }

    void delete() {
        AlertUtil.showConfirmDialog(this, R.string.jandi_action_delete,
                R.string.jandi_file_delete_message, (dialog, which) -> {
                    fileDetailPresenter.onDeleteFile(fileId, roomId);
                    sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Delete);
                }, true);
    }

    void enableExternalLink() {
        etComment.setSelection(etComment.getSelectionEnd());

        if (isExternalShared) {
            setExternalLinkToClipboard();
            return;
        }

        fileDetailPresenter.onEnableExternalLink(fileId);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_CreatePublicLink);
    }

    private String getExternalLink() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.content == null) {
            return "";
        }

        StringBuffer link = new StringBuffer(JandiConstantsForFlavors.getServiceBaseUrl())
                .append("file/")
                .append(fileMessage.content.externalCode);
        return link.toString();
    }

    void disableExternalLink() {
        AlertUtil.showDialog(this,
                R.string.jandi_disable_external_link,
                R.string.jandi_are_you_sure_disable_external_link,
                R.string.jandi_action_delete, (dialog, which) -> {
                    fileDetailPresenter.onDisableExternalLink(fileId);
                    sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_DeleteLink);
                }, /* positive */
                -1, null, /* neutral */
                R.string.jandi_cancel, null, /* negative */
                true);
    }

    @OnClick(R.id.btn_send_message)
    void sendComment() {
        hideKeyboard();


        ResultMentionsVO mentionInfo = getMentionInfo();
        String message = mentionInfo.getMessage().trim();
        List<MentionObject> mentions = mentionInfo.getMentions();
        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            dismissStickerPreview();
            long stickerGroupId = stickerInfo.getStickerGroupId();
            String stickerId = stickerInfo.getStickerId();

            StickerRepository.getRepository().upsertRecentSticker(stickerGroupId, stickerId);

            fileDetailPresenter.onSendCommentWithSticker(
                    fileId, stickerGroupId, stickerId, message, mentions);

            stickerInfo = NULL_STICKER;
            sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Send);
        } else {
            fileDetailPresenter.onSendComment(fileId, message, mentions);
            sendAnalyticsEvent(AnalyticsValue.Action.Send);
        }

        etComment.setText("");
    }

    @OnClick(R.id.iv_file_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        FileDetailActivity.this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        setCommentSendButtonEnabled();
        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_cancel);
    }

    @OnClick(R.id.btn_show_mention)
    void onMentionClick() {
        etComment.requestFocus();

        boolean needSpace = needSpace(etComment.getSelectionStart(), etComment.getText().toString());
        int keyEvent = KeyEvent.KEYCODE_AT;

        BaseInputConnection inputConnection = new BaseInputConnection(etComment, true);
        if (needSpace) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        }
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));

        if (softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputAreaAndShowSoftInput();
        } else {
            softInputAreaController.showSoftInput();
        }
    }

    private void dismissStickerSelectorIfShow() {
        if (stickerViewModel.isShow()) {
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

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, action);
    }

    private ResultMentionsVO getMentionInfo() {
        if (mentionControlViewModel != null) {
            return mentionControlViewModel.getMentionInfoObject();
        } else {
            return new ResultMentionsVO("", new ArrayList<>());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SHARE:
                onShareResult(resultCode, data);
                break;
            case REQUEST_CODE_PICK:
                onPickResult(resultCode, data);
                break;
            case REQUEST_CODE_UNSHARE:
                onUnshareResult(resultCode, data);
                break;
            case REQUEST_CODE_RETURN_FILE_ID:
                onCarouselResult(resultCode, data);
                break;
        }
    }

    void onShareResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null) {
            return;
        }

        boolean isTopic = data.getBooleanExtra(RoomFilterActivity.KEY_IS_TOPIC, true);

        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();

        if (fileMessage == null) {
            return;
        }

        long entityId = -1;

        if (isTopic) {
            entityId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1);
        } else {
            entityId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_MEMBER_ID, -1);
        }

        fileDetailPresenter.onShareAction(entityId, fileId);
    }

    void onPickResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID)) {
            return;
        }

        long entityId = data.getLongExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID, -1);
        moveToSharedEntity(entityId);
    }

    void onUnshareResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID)) {
            return;
        }

        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null) {
            return;
        }

        long entityId = data.getLongExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID, -1);
        fileDetailPresenter.onUnshareAction(entityId, fileMessage.id);
    }

    void onCarouselResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(CarouselViewerActivity.KEY_FILE_ID)) {
            return;
        }

        long fileId = data.getLongExtra(CarouselViewerActivity.KEY_FILE_ID, -1);
        if (fileId > 0 && this.fileId != fileId) {
            this.fileId = fileId;
            fileDetailPresenter.onInitializeFileDetail(this.fileId, true);
        }

    }

    @Override
    public void onBackPressed() {
        if (softInputAreaController != null && softInputAreaController.isSoftInputAreaShowing()) {
            softInputAreaController.hideSoftInputArea(true, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Pair<Integer, ResMessages.OriginalMessage> getCommentInfo(long messageId) {
        int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            Object item = adapter.getItem(i);
            if (item instanceof ResMessages.OriginalMessage) {
                ResMessages.OriginalMessage comment = (ResMessages.OriginalMessage) item;
                if (comment.id == messageId) {
                    return Pair.create(i, comment);
                }
            }
        }

        return Pair.create(-1, new ResMessages.OriginalMessage());
    }

    @Override
    public void removeComment(int position) {
        ResMessages.OriginalMessage before = null;
        if (position > 0) {
            Object obj = adapter.getItem(position - 1);
            if (obj instanceof ResMessages.CommentMessage
                    || obj instanceof ResMessages.CommentStickerMessage) {
                before = (ResMessages.OriginalMessage) obj;
            }
        }

        ResMessages.OriginalMessage after = null;
        if (position < adapter.getItemCount() - 1) {
            Object obj = adapter.getItem(position + 1);
            if (obj instanceof ResMessages.CommentMessage
                    || obj instanceof ResMessages.CommentStickerMessage) {
                after = (ResMessages.OriginalMessage) obj;
            }
        }

        adapter.remove(position);

        if (after != null) {
            int commentViewType;
            if (before == null) {

                boolean textComment = after instanceof ResMessages.CommentMessage;
                commentViewType = getCommentViewType(textComment, true);

            } else {

                boolean textComment = after instanceof ResMessages.CommentMessage;

                boolean profile = true;
                if (position > 0) {
                    profile = after.writerId != before.writerId;
                }

                commentViewType = getCommentViewType(textComment, profile);
            }
            adapter.setRow(position, new FileDetailAdapter.Row<>(after, commentViewType));

            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void showCommentDeleteErrorToast() {
        showToast(getString(R.string.err_entity_delete), true /* isError */);
    }

    @Override
    public void addComment(int adapterPosition, ResMessages.OriginalMessage comment) {
        boolean sticker = !(comment instanceof ResMessages.CommentMessage);
        boolean profile = true;
        if (adapter.getItemCount() > 0) {
            ResMessages.OriginalMessage before = getItemIfCommentType(adapter.getItem(adapterPosition - 1));
            if (before != null) {
                profile = before.writerId != comment.writerId;
            }
        }
        int viewType = getCommentViewType(!sticker, profile);
        adapter.addRow(adapterPosition, new FileDetailAdapter.Row<>(comment, viewType));
    }

    @Nullable
    private ResMessages.OriginalMessage getItemIfCommentType(Object item) {
        ResMessages.OriginalMessage message = null;
        if (item instanceof ResMessages.CommentMessage
                || item instanceof ResMessages.CommentStickerMessage) {
            message = (ResMessages.OriginalMessage) item;
        }
        return message;
    }

    @Override
    public void showNotAccessedFile() {
        showToast(getString(R.string.jandi_unshared_message), true);
    }

    @Override
    public void finish() {
        // FileListFragment 로 부터 시작된
        if (roomId <= 0) {
            int commentCount = getCommentCount();
            if (commentCount >= 0) {
                Intent intent = new Intent();
                intent.putExtra(FileListFragment.KEY_FILE_ID, fileId);
                intent.putExtra(FileListFragment.KEY_COMMENT_COUNT, commentCount);
                setResult(RESULT_OK, intent);
            }
        }
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public void checkPermission(String persmissionString, Check.HasPermission hasPermission, Check.NoPermission noPermission) {
        Permissions.getChecker()
                .activity(FileDetailActivity.this)
                .permission(() -> persmissionString)
                .hasPermission(hasPermission)
                .noPermission(noPermission)
                .check();
    }

    private int getCommentCount() {
        int[] commentCount = {-1};

        Observable.from(adapter.getRows())
                .filter(row -> {
                    Object item = row.getItem();
                    return item instanceof ResMessages.CommentMessage
                            || item instanceof ResMessages.CommentStickerMessage;
                })
                .toList()
                .subscribe(rows -> {
                    commentCount[0] = rows.size();
                });
        return commentCount[0];
    }

}
