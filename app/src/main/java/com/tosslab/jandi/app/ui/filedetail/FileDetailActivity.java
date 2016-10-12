package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.ImageView;

import com.github.johnpersano.supertoasts.SuperToast;
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
import com.tosslab.jandi.app.ui.filedetail.views.FileShareActivity;
import com.tosslab.jandi.app.ui.filedetail.views.FileShareActivity_;
import com.tosslab.jandi.app.ui.filedetail.views.FileSharedEntityChooseActivity;
import com.tosslab.jandi.app.ui.filedetail.views.FileSharedEntityChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.views.BackPressCatchEditText;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;
import com.tosslab.jandi.app.views.controller.SoftInputAreaController;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
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

    @Extra
    boolean fromCarousel = false;

    @Extra
    long roomId = -1;

    @Extra
    long fileId;

    @Extra
    long selectMessageId = -1;

    @Bean
    FileDetailPresenter fileDetailPresenter;

    @Bean
    StickerViewModel stickerViewModel;
    @SystemService
    ClipboardManager clipboardManager;
    @SystemService
    InputMethodManager inputMethodManager;
    @ViewById(R.id.lv_file_detail)
    RecyclerView listView;
    @ViewById(R.id.toolbar_file_detail)
    Toolbar toolbar;
    @ViewById(R.id.vg_file_detail_input_comment)
    ViewGroup vgInputWrapper;
    @ViewById(R.id.et_message)
    BackPressCatchEditText etComment;
    @ViewById(R.id.btn_send_message)
    View btnSend;
    @ViewById(R.id.vg_file_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.vg_file_detail_soft_input_area)
    ViewGroup vgSoftInputArea;
    @ViewById(R.id.iv_file_detail_preview_sticker_image)
    ImageView ivStickerPreview;
    @ViewById(R.id.vg_file_detail_soft_input_detector)
    SoftInputDetectLinearLayout vgSoftInputDetector;
    @ViewById(R.id.btn_show_mention)
    View ivMention;
    @ViewById(R.id.btn_file_detail_action)
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

    @AfterViews
    void initViews() {
        setUpActionBar();

        initCommentEditText();

        initStickers();

        initSoftInputAreaController();

        initProgressWheel();

        initFileInfoViews();
    }

    private void initCommentEditText() {
        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });
    }

    @FocusChange(R.id.et_message)
    void onEditTextFocusChange(boolean focus, View view) {
        if (focus) {
            sendAnalyticsEvent(AnalyticsValue.Action.MessageInputField);
        }
    }

    @AfterTextChange(R.id.et_message)
    void onCommentTextChange(Editable editable) {
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
                stickerViewModel, null,
                vgSoftInputDetector, vgSoftInputArea, null, btnAction,
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter = new FileDetailAdapter());

        adapter.setOnFileClickListener(this::onFileClick);
        adapter.setOnImageFileClickListener(this::onImageFileClick);
        adapter.setOnCommentClickListener(comment -> hideKeyboard());
        adapter.setOnCommentLongClickListener(this::onCommentLongClick);

        fileDetailPresenter.setView(this);
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
                }
            });
        } else {
            mentionControlViewModel.refreshMembers(sharedTopicIds);
        }

        removeClipboardListenerForMention();
        registerClipboardListenerForMention();
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
        ivMention.setVisibility(show
                ? View.VISIBLE : View.GONE);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void registerClipboardListenerForMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void removeClipboardListenerForMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void copyToClipboard(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ClipData clipData = ClipData.newPlainText(null, text);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void clearFileDetailAndComments() {
        adapter.clear();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setComments(List<ResMessages.OriginalMessage> fileComments) {
        if (fileComments == null || fileComments.isEmpty()) {
            return;
        }

        List<FileDetailAdapter.Row<?>> rows = new ArrayList<>();
        rows.add(new FileDetailAdapter.Row<>(null, FileDetailAdapter.VIEW_TYPE_COMMENT_DIVIDER));
        Observable.from(fileComments)
                .subscribe(commentMessage -> {
                    boolean isSticker = !(commentMessage instanceof ResMessages.CommentMessage);
                    int viewType = isSticker
                            ? FileDetailAdapter.VIEW_TYPE_STICKER
                            : FileDetailAdapter.VIEW_TYPE_COMMENT;
                    rows.add(new FileDetailAdapter.Row<>(commentMessage, viewType));
                });

        adapter.addRows(rows);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @UiThread(delay = 100)
    @Override
    public void scrollToLastComment() {
        if (adapter.getItemCount() <= 0) {
            return;
        }
        listView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setFilesStarredState(boolean starred) {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage != null) {
            fileMessage.isStarred = starred;
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showDialog(Dialog dialog) {
        if (isFinishing()) {
            return;
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog(boolean shouldFinishWhenConfirm) {
        DialogInterface.OnClickListener confirmListener = null;
        if (shouldFinishWhenConfirm) {
            confirmListener = (dialog, which) -> finish();
        }

        AlertUtil.showCheckNetworkDialog(this, confirmListener);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showKeyboard() {
        inputMethodManager.showSoftInput(getCurrentFocus(), 0);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void hideKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

        MemberProfileActivity_.intent(this)
                .memberId(userEntityId)
                .from(MemberProfileActivity.EXTRA_FROM_FILE_DETAIL)
                .start();

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

    public void onEvent(MoveSharedEntityEvent event) {
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
        FileSharedEntityChooseActivity_.intent(this)
                .fileId(fileId)
                .mode(FileSharedEntityChooseActivity.MODE_PICK)
                .sharedEntities(sharedEntitiesArray)
                .startForResult(REQUEST_CODE_PICK);
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

    public void onEvent(MessageStarEvent event) {
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
                        .subscribe(position -> adapter.remove(position), Throwable::printStackTrace, () -> {
                            adapter.notifyDataSetChanged();
                        });
            }
        }
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        fileDetailPresenter.onDeleteComment(event.messageType, event.messageId, event.feedbackId);
        sendAnalyticsEvent(AnalyticsValue.Action.CommentLongTap_Delete);
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
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
        fileDetailPresenter.onTopicDeleted(event.getId(), fileMessage.shareEntities);
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

    @OptionsItem(R.id.menu_overflow)
    void onClickOverflow() {
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu);
    }

    @OptionsItem(R.id.action_file_detail_download)
    void download() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.content == null) {
            return;
        }

        fileDetailPresenter.onDownloadAction(fileId, fileMessage.content);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Download);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void startGoogleOrDropboxFileActivity(String fileUrl) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void startExportedFileViewerActivity(File file, String mimeType) {
        Intent target = new Intent(Intent.ACTION_SEND);
        Uri parse = Uri.parse(file.getAbsolutePath());
        target.setDataAndType(parse, mimeType);
        Bundle extras = new Bundle();
        extras.putParcelable(Intent.EXTRA_STREAM, Uri.fromFile(file));
        target.putExtras(extras);
        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            showUnexpectedErrorToast();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void startDownloadedFileViewerActivity(File file, String mimeType) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mimeType);
            startActivity(intent);
            showToast(getString(R.string.jandi_file_downloaded_into, file.getPath()), false);
        } catch (ActivityNotFoundException e) {
            showToast(getString(R.string.err_unsupported_file_type, file), true);
        } catch (SecurityException e) {
            showToast(getString(R.string.err_unsupported_file_type, file), true);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveToMessageListActivity(long entityId, int entityType, long roomId,
                                          boolean isStarred) {
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(TeamInfoLoader.getInstance().getTeamId())
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    @OptionsItem(R.id.action_file_detail_share)
    void share() {
        FileShareActivity_.intent(this)
                .fileId(fileId)
                .startForResult(REQUEST_CODE_SHARE);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Share);
    }

    @OptionsItem(R.id.action_file_detail_unshare)
    void unShare() {
        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null || fileMessage.shareEntities == null) {
            return;
        }

        long[] sharedEntitiesArray = getSharedEntitiesArray(fileMessage);

        FileSharedEntityChooseActivity_.intent(this)
                .fileId(fileId)
                .sharedEntities(sharedEntitiesArray)
                .startForResult(REQUEST_CODE_UNSHARE);

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

    @OptionsItem(R.id.action_file_detail_export)
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
    }

    @OptionsItem(R.id.action_file_detail_delete)
    void delete() {
        AlertUtil.showConfirmDialog(this, R.string.jandi_action_delete,
                R.string.jandi_file_delete_message, (dialog, which) -> {
                    fileDetailPresenter.onDeleteFile(fileId, roomId);
                    sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Delete);
                }, true);
    }

    @OptionsItem(R.id.action_file_detail_enable_external_link)
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

        StringBuffer link = new StringBuffer(JandiConstantsForFlavors.SERVICE_BASE_URL)
                .append("file/")
                .append(fileMessage.content.externalCode);
        return link.toString();
    }

    @OptionsItem(R.id.action_file_detail_disable_external_link)
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

    @Click(R.id.btn_send_message)
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

    @Click(R.id.iv_file_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        FileDetailActivity.this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        setCommentSendButtonEnabled();
        sendAnalyticsEvent(AnalyticsValue.Action.Sticker_cancel);
    }

    @Click(R.id.btn_show_mention)
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

    @OnActivityResult(REQUEST_CODE_SHARE)
    void onShareResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(FileShareActivity.KEY_ENTITY_ID)) {
            return;
        }

        ResMessages.FileMessage fileMessage = getFileMessageFromAdapter();
        if (fileMessage == null) {
            return;
        }

        long entityId = data.getLongExtra(FileShareActivity.KEY_ENTITY_ID, -1);
        fileDetailPresenter.onShareAction(entityId, fileId);
    }

    @OnActivityResult(REQUEST_CODE_PICK)
    void onPickResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID)) {
            return;
        }

        long entityId = data.getLongExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID, -1);
        moveToSharedEntity(entityId);
    }

    @OnActivityResult(REQUEST_CODE_UNSHARE)
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

    @OnActivityResult(REQUEST_CODE_RETURN_FILE_ID)
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
        adapter.remove(position);
    }

    @Override
    public void showCommentDeleteErrorToast() {
        showToast(getString(R.string.err_entity_delete), true /* isError */);
    }

    @Override
    public void addComment(int adapterPosition, ResMessages.OriginalMessage comment) {
        boolean isSticker = !(comment instanceof ResMessages.CommentMessage);
        int viewType = isSticker
                ? FileDetailAdapter.VIEW_TYPE_STICKER
                : FileDetailAdapter.VIEW_TYPE_COMMENT;
        adapter.addRow(adapterPosition, new FileDetailAdapter.Row<>(comment, viewType));
    }

    @Override
    public void showNotAccessedFile() {
        showToast(getString(R.string.jandi_unshared_message), true);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @OptionsItem(android.R.id.home)
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
