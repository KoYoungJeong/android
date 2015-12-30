package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.MessageStarredEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMensionEvent;
import com.tosslab.jandi.app.events.messages.SocketMessageStarEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.files.FileDetailCommentListAdapter;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.repositories.ReadyCommentRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.MixpanelAnalytics;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.filedetail.fileinfo.FileHeadManager;
import com.tosslab.jandi.app.ui.filedetail.views.FileShareActivity_;
import com.tosslab.jandi.app.ui.filedetail.views.FileUnshareActivity_;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListFragment;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.extracomponent.BackpressEditText;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static org.androidannotations.annotations.UiThread.Propagation;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAppCompatActivity implements FileDetailPresenter.View {

    public static final int INTENT_RETURN_TYPE_SHARE = 0;
    public static final int INTENT_RETURN_TYPE_UNSHARE = 1;
    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQ_STORAGE_PERMISSION_EXPORT = 102;
    private static final int REQ_WINDOW_PERMISSION = 103;
    private static final StickerInfo NULL_STICKER = new StickerInfo();
    public static

    @Extra
    int fileId;

    @Extra
    int selectMessageId = -1;

    @Extra
    int roomId = -1;

    @Bean
    FileDetailPresenter fileDetailPresenter;
    @Bean
    FileDetailCommentListAdapter fileDetailCommentListAdapter;
    @Bean
    FileHeadManager fileHeadManager;
    @ViewById(R.id.lv_file_detail_comments)
    ListView lvFileDetailComments;
    @ViewById(R.id.vg_file_detail_input_comment)
    View vgCommentLayout;
    @ViewById(R.id.et_message)
    BackpressEditText etComment;
    @ViewById(R.id.btn_send_message)
    View btnSend;
    @ViewById(R.id.vg_file_detail_preview_sticker)
    ViewGroup vgStickerPreview;
    @ViewById(R.id.iv_file_detail_preview_sticker_image)
    ImageView ivStickerPreview;
    @ViewById(R.id.lv_list_search_members)
    RecyclerView rvListSearchMembers;
    @ViewById(R.id.vg_option_space)
    ViewGroup vgOptionSpace;

    @ViewById(R.id.btn_show_mention)
    View ivMention;

    @Bean
    StickerViewModel stickerViewModel;
    @Bean
    KeyboardHeightModel keyboardHeightModel;
    @SystemService
    InputMethodManager inputMethodManager;

    private EntityManager entityManager;
    private boolean isMyFile;
    private boolean isDeleted = true;
    private boolean isForeground;
    private boolean isFromDeleteAction = false;
    private ProgressWheel progressWheel;
    private ProgressDialog progressDialog;
    private StickerInfo stickerInfo = NULL_STICKER;
    private MixpanelAnalytics mixpanelAnalytics;
    private Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities;
    private boolean isExternalShared;
    private ResMessages.FileMessage fileMessage;

    @AfterViews
    public void initForm() {
        mixpanelAnalytics = new MixpanelAnalytics();
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.FILE_DETAIL)
                        .build());

        setUpActionBar();

        addFileDetailViewAsListviewHeader();

        fileHeadManager.setRoomId(roomId);

        progressWheel = new ProgressWheel(this);

        entityManager = EntityManager.getInstance();

        fileDetailPresenter.setView(this);

        stickerViewModel.setOptionSpace(vgOptionSpace);
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo oldSticker = stickerInfo;
            stickerInfo = new StickerInfo();
            stickerInfo.setStickerGroupId(groupId);
            stickerInfo.setStickerId(stickerId);
            showStickerPreview();

            if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId()
                    || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
                loadSticker(stickerInfo);
            }
            setSendButtonSelected();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Sticker_Select);
        });

        lvFileDetailComments.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                hideSoftKeyboard();
                stickerViewModel.dismissStickerSelector(false);
            }
            return false;
        });

        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId) -> sendComment());

        stickerViewModel.setType(StickerViewModel.TYPE_FILE_DETAIL);

        stickerViewModel.setStickerButton(findViewById(R.id.btn_message_sticker));

        if (NetworkCheckUtil.isConnected()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, selectMessageId);
        } else if (!fileDetailPresenter.onLoadFromCache(fileId, selectMessageId)) {
            showCheckNetworkDialog();
        }

        JandiPreference.setKeyboardHeight(FileDetailActivity.this, -1);
        addStarredButtonExecution();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FileDetail);
        setEditTextTouchEvent();
        setKeyboardBackpressCallback();
    }

    private void setKeyboardBackpressCallback() {
        etComment.setOnBackPressListener(() -> {
            if (keyboardHeightModel.isOpened()) {
                //키보드가 열려져 있고 그 위에 스티커가 있는 상태에서 둘다 제거 할때 속도를 맞추기 위해 딜레이를 줌
                Observable.just(1)
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
                            if (stickerViewModel.isShow()) {
                                stickerViewModel.dismissStickerSelector(true);
                            }
                        });
            }
            return false;
        });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    private void addFileDetailViewAsListviewHeader() {
        // ListView(댓글에 대한 List)의 Header에 File detail 정보를 보여주는 View 연결한다.
        View header = fileHeadManager.getHeaderView();

        lvFileDetailComments.addHeaderView(header);
        lvFileDetailComments.setAdapter(fileDetailCommentListAdapter);
    }

    private void addStarredButtonExecution() {

        fileHeadManager.getStarredButton().setOnClickListener(v -> {
            boolean starred = v.isSelected();
            updateFileStarred(!starred);
            fileDetailPresenter.changeStarredFileMessageState(fileId, !starred);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, !starred ? AnalyticsValue.Action.TurnOnStar : AnalyticsValue.Action.TurnOffStar);

        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (isDeleted) {
            return true;
        }

        if (isMyFile) {
            getMenuInflater().inflate(R.menu.file_detail_activity_my_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.file_detail_activity_menu, menu);
        }

        SubMenu subMenu = menu.findItem(R.id.menu_overflow).getSubMenu();
        if (isExternalShared) {
            MenuItem miEnableExternalLink = subMenu.findItem(R.id.action_file_detail_enable_external_link);
            miEnableExternalLink.setTitle(R.string.jandi_copy_link);
        } else {
            subMenu.removeItem(R.id.action_file_detail_disable_external_link);
        }

        return true;
    }

    @ItemLongClick(R.id.lv_file_detail_comments)
    void onCommentLongClick(ResMessages.OriginalMessage item) {
        fileDetailPresenter.onLongClickComment(item);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.CommentLongTap);
    }

    @ItemClick(R.id.lv_file_detail_comments)
    void onCommentClick(ResMessages.OriginalMessage item) {
        hideSoftKeyboard();
        stickerViewModel.dismissStickerSelector(false);
    }

    @Click(R.id.iv_file_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        FileDetailActivity.this.stickerInfo = NULL_STICKER;
        dismissStickerPreview();
        setSendButtonSelected();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Sticker_cancel);
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }

        fileDetailPresenter.deleteComment(fileId, event.messageType, event.messageId, event.feedbackId);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.CommentLongTap_Delete);

    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }
        copyToClipboard(event.contentString);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.CommentLongTap_Copy);
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        fileDetailPresenter.checkSharedEntity(event.getId(), fileId);
    }

    private void setUpActionBar() {
        // Set up the action bar.
        Toolbar toolbar = ((Toolbar) findViewById(R.id.layout_search_bar));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_file_detail_download:
                download();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.FileSubMenu_Download);
                return true;
            case R.id.action_file_detail_share:
                clickShareButton();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.FileSubMenu_Share);
                return true;
            case R.id.action_file_detail_unshare:
                clickUnShareButton();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.FileSubMenu_UnShare);
                return true;
            case R.id.action_file_detail_delete:
                showDeleteFileDialog(fileId);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.FileSubMenu_Delete);
                return true;
            case R.id.menu_overflow:
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.FileSubMenu);
                return true;
            case R.id.action_file_detail_export:
                Permissions.getChecker()
                        .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .hasPermission(() -> onExportFile(fileMessage))
                        .noPermission(() -> {
                            requestPermission(REQ_STORAGE_PERMISSION_EXPORT, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }).check();
                break;
            case R.id.action_file_detail_enable_external_link:
                etComment.setSelection(etComment.getSelectionEnd());
                fileDetailPresenter.onCopyExternLink(fileMessage, isExternalShared);
                break;
            case R.id.action_file_detail_disable_external_link:
                fileDetailPresenter.onDisableExternLink(fileMessage);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void onExportFile(ResMessages.FileMessage fileMessage) {
        ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        fileDetailPresenter.onExportFile(fileMessage, progressDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        fileDetailPresenter.registClipboardListenerforMention();
        ActivityHelper.setOrientation(this);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        fileDetailPresenter.removeClipboardListenerforMention();
        ReadyCommentRepository.getRepository().upsertReadyComment(new ReadyComment(fileId, etComment.getText().toString()));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        dismissProgress();
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @AfterTextChange(R.id.et_message)
    void onCommentTextChange(Editable editable) {
        setSendButtonSelected();
    }

    @Click(R.id.et_message)
    void onMessageInputClick() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.MessageInputField);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void finishOnMainThread() {
        finish();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void loadSuccess(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                            boolean isSendAction, int selectMessageId) {
        this.fileMessage = fileMessage;
        shareEntities = fileMessage.shareEntities;

        drawFileDetail(fileMessage, commentMessages, isSendAction);

        if (selectMessageId > 0) {
            int position = fileDetailCommentListAdapter.findMessagePosition(selectMessageId);
            if (position >= 0) {
                // 헤더를 포함하기 때문에 +1 한다.
                lvFileDetailComments.smoothScrollToPosition(position + 1);
            }

            fileDetailCommentListAdapter.setSelectMessage(selectMessageId);
            fileDetailCommentListAdapter.notifyDataSetChanged();

        }

        // XXX what mean Mention VM?
        fileDetailPresenter.refreshMentionVM(this, fileMessage, etComment);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(FileDetailActivity.this, (dialog, which) -> finish());
    }

    /**
     * *********************************************************
     * 파일 공유
     * **********************************************************
     */
    void clickShareButton() {
        FileShareActivity_.intent(this)
                .extra("fileId", fileId)
                .startForResult(INTENT_RETURN_TYPE_SHARE);
    }

    @UiThread
    @Override
    public void showMoveDialog(int entityIdToBeShared) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FileDetailActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setMessage(getString(R.string.jandi_move_entity_after_share))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EntityManager entityManager = EntityManager.getInstance();
                        FormattedEntity entity = entityManager.getEntityById(entityIdToBeShared);

                        moveToMessageListActivity(entityIdToBeShared, entity.type,
                                entity.isUser() ? -1 : entityIdToBeShared, entity.isStarred);
                    }
                })
                .create()
                .show();
    }

    @UiThread
    @Override
    public void onShareMessageSucceed(int entityIdToBeShared, ResMessages.FileMessage fileMessage) {
        ColoredToast.show(this, getString(R.string.jandi_share_succeed, getSupportActionBar().getTitle()));
        mixpanelAnalytics.trackSharingFile(entityManager,
                entityManager.getEntityById(entityIdToBeShared).type,
                fileMessage);
        clearAdapter();
        fileDetailPresenter.getFileDetail(fileId, false, true, -1);
    }

    /**
     * *********************************************************
     * 파일 공유 해제
     * **********************************************************
     */
    void clickUnShareButton() {
        ArrayList<Integer> rawSharedEntities = new ArrayList<>();
        if (shareEntities != null) {

            Observable.from(shareEntities)
                    .map(integerWrapper -> integerWrapper.getShareEntity())
                    .collect(() -> rawSharedEntities, (integers, integer) -> integers.add(integer))
                    .subscribe();
        }
        FileUnshareActivity_.intent(this)
                .fileId(fileId)
                .sharedEntities(rawSharedEntities)
                .startForResult(INTENT_RETURN_TYPE_UNSHARE);
    }

    @UiThread
    @Override
    public void onUnShareMessageSucceed(int entityIdToBeUnshared, ResMessages.FileMessage fileMessage) {
        ColoredToast.show(this, getString(R.string.jandi_unshare_succeed, getSupportActionBar().getTitle()));
        mixpanelAnalytics.trackUnsharingFile(entityManager,
                entityManager.getEntityById(entityIdToBeUnshared).type,
                fileMessage);
        clearAdapter();
        fileDetailPresenter.getFileDetail(fileId, false, true, -1);
    }

    public void onEvent(SocketMessageStarEvent event) {
        int messageId = event.getMessageId();
        boolean starred = event.isStarred();

        LogUtil.e("isStarred", event.isStarred() + "");

        if (messageId == fileId) {
            fileHeadManager.updateStarred(starred);
        }
    }

    public void onEvent(ConfirmDeleteFileEvent event) {
        if (!isForeground) {
            return;
        }
        isFromDeleteAction = true;
        fileDetailPresenter.deleteFile(event.getFileId(), roomId);
    }

    public void onEvent(DeleteFileEvent event) {
        LogUtil.d("FileDetailActivity", "finishing ? " + isFinishing());
        if (isFinishing()) {
            return;
        }

        LogUtil.d("FileDetailActivity", "isFromDeleteAction ? " + isFromDeleteAction);
        if (isFromDeleteAction) {
            return;
        }

        if (fileId == event.getId()) {
            LogUtil.e("FileDetailActivity", "DeleteFileEvent");
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(ShareFileEvent event) {
        if (fileId == event.getId()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (fileId == event.getFileId()) {
            fileDetailPresenter.getFileDetail(fileId, false, false, -1);
        }
    }

    public void onEvent(MoveSharedEntityEvent event) {
        if (!isForeground) {
            return;
        }

        int entityId = event.getEntityId();

        EntityManager entityManager = EntityManager.getInstance();

        FormattedEntity entity = entityManager.getEntityById(entityId);

        int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC
                : entity.isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC
                : JandiConstants.TYPE_DIRECT_MESSAGE;

        boolean isStarred = entity.isStarred;
        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            if (entity.isPublicTopic() && entity.isJoined
                    || entity.isPrivateGroup()) {

                moveToMessageListActivity(entityId, entityType, entityId, isStarred);
            } else {
                fileDetailPresenter.joinAndMove(entity);
            }
        } else {
            moveToMessageListActivity(entityId, entityType, entityId, isStarred);
        }

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.TapSharedTopic);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void moveToMessageListActivity(int entityId, int entityType, int roomId, boolean isStarred) {
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(EntityManager.getInstance().getTeamId())
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .isFavorite(isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @UiThread
    @Override
    public void onDeleteFileSucceed(boolean isOk) {
        if (isOk) {
            CharSequence title = getSupportActionBar().getTitle();
            if (!TextUtils.isEmpty(title)) {
                ColoredToast.show(this, getString(R.string.jandi_delete_succeed, title));
            } else {
                ColoredToast.show(this, getString(R.string.jandi_delete_succeed, ""));
            }

            Intent data = new Intent();
            data.putExtra(MessageListFragment.EXTRA_FILE_DELETE, true);
            data.putExtra(MessageListFragment.EXTRA_FILE_ID, fileId);
            setResult(RESULT_OK, data);
            finish();
        } else {
            ColoredToast.showError(this, getString(R.string.err_delete_file));
        }
        isFromDeleteAction = false;
    }

    /**
     * *********************************************************
     * 댓글 작성 관련
     * **********************************************************
     */
    @Click(R.id.btn_send_message)
    void sendComment() {
        CharSequence text = etComment.getText();
        String comment = TextUtils.isEmpty(text) ? "" : text.toString().trim();
        hideSoftKeyboard();

        ResultMentionsVO mentions = fileDetailPresenter.getMentionInfo();

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            dismissStickerPreview();
            StickerRepository.getRepository()
                    .upsertRecentSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());

            fileDetailPresenter.sendCommentWithSticker(
                    fileId, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(),
                    mentions.getMessage(), mentions.getMentions());
            stickerInfo = NULL_STICKER;

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Sticker_Send);
        } else {
            fileDetailPresenter.sendComment(fileId, mentions.getMessage(), mentions.getMentions());
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Send);
        }

        etComment.setText("");
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void hideSoftKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showKeyboard() {
        inputMethodManager.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @Click(R.id.btn_message_sticker)
    void onStickerClick(View view) {
        boolean selected = view.isSelected();
        if (selected) {
            stickerViewModel.dismissStickerSelector(true);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Sticker);
        } else {

            boolean canDraw;
            if (SdkUtils.isMarshmallow()) {
                canDraw = Settings.canDrawOverlays(FileDetailActivity.this);
            } else {
                canDraw = true;
            }

            if (canDraw) {
                int keyboardHeight =
                        JandiPreference.getKeyboardHeight(getApplicationContext());
                stickerViewModel.showStickerSelector(keyboardHeight);
            } else {
                // Android M (23) 부터 적용되는 시나리오
                String packageName = JandiApplication.getContext().getPackageName();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
                startActivityForResult(intent, REQ_WINDOW_PERMISSION);
            }

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.Sticker);
        }
    }

    @Click(R.id.btn_show_mention)
    void onMentionClick() {
        BaseInputConnection inputConnection = new BaseInputConnection(etComment, true);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_AT));

        if (stickerViewModel.isShow()) {
            stickerViewModel.dismissStickerWindow();
        }

    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showStickerPreview() {
        vgStickerPreview.setVisibility(View.VISIBLE);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void loadSticker(StickerInfo stickerInfo) {
        StickerManager.getInstance()
                .loadStickerDefaultOption(
                        ivStickerPreview, stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void dismissStickerPreview() {
        vgStickerPreview.setVisibility(View.GONE);
    }

    /**
     * *********************************************************
     * 파일 연결 관련
     * **********************************************************
     */
    public void download() {
        fileDetailPresenter.onClickDownload(fileId, fileMessage);
    }

    @UiThread
    @Override
    public void startGoogleOrDropboxFileActivity(String fileUrl) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
    }

    private void initProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
        }

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showDownloadProgressDialog(String fileName) {
        initProgressDialog();
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();
    }

    @UiThread
    @Override
    public void dismissDownloadProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing() || !isForeground) {
            return;
        }
        progressDialog.dismiss();
    }

    public void onEventMainThread(FileDownloadStartEvent fileDownloadStartEvent) {
        if (!isForeground) {
            return;
        }

        initProgressDialog();

        fileDetailPresenter.downloadFile(fileDownloadStartEvent.getUrl(),
                fileDownloadStartEvent.getFileName(),
                fileDownloadStartEvent.getFileType(),
                fileDownloadStartEvent.getExt(),
                fileId);
    }

    @UiThread
    @Override
    public void onDownloadFileSucceed(File file, String fileType, ResMessages.FileMessage fileMessage,
                                      boolean execute) {
        mixpanelAnalytics.trackDownloadingFile(entityManager, fileMessage);
        try {
            if (execute) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), getFileType(file, fileType));
                startActivity(intent);
            }
            ColoredToast.show(FileDetailActivity.this, getString(R.string.jandi_file_downloaded_into, file.getPath()));
        } catch (ActivityNotFoundException e) {
            String rawString = getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(this, formatString);
        } catch (SecurityException e) {
            String rawString = getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(this, formatString);
        }
    }

    private String getFileType(File file, String fileType) {

        String fileName = file.getName();
        int idx = fileName.lastIndexOf(".");

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (idx >= 0) {
            return mimeTypeMap.getMimeTypeFromExtension(
                    fileName.substring(idx + 1, fileName.length()).toLowerCase());
        } else {
            return mimeTypeMap.getExtensionFromMimeType(fileType.toLowerCase());
        }
    }

    /**
     * 사용자 프로필 보기
     */
    public void onEvent(ShowProfileEvent event) {
        if (!isForeground) {
            return;
        }

        int userEntityId = event.userId;
        fileDetailPresenter.getProfile(userEntityId);
    }

    @UiThread
    @Override
    public void onGetProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {

        if (!isForeground) {
            return;
        }
        EntityManager entityManager = EntityManager.getInstance();
        moveToMessageListActivity(
                event.userId, JandiConstants.TYPE_DIRECT_MESSAGE, -1,
                entityManager.getEntityById(event.userId).isStarred);
    }

    @UiThread
    @Override
    public void showUserInfoDialog(FormattedEntity user) {
        MemberProfileActivity_.intent(this)
                .memberId(user.getId())
                .from(MemberProfileActivity.EXTRA_FROM_FILE_DETAIL)
                .start();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void copyToClipboard(String contentString) {
        if (TextUtils.isEmpty(contentString)) {
            contentString = "";
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void setExternalShared(boolean externalShared) {
        this.isExternalShared = externalShared;
        invalidateOptionsMenu();
    }

    @Override
    public void exportIntentFile(File result, String mimeType) {
        Intent target = new Intent(Intent.ACTION_SEND);
        Uri parse = Uri.parse(result.getAbsolutePath());
        target.setDataAndType(parse, mimeType);
        Bundle extras = new Bundle();
        extras.putParcelable(Intent.EXTRA_STREAM, Uri.fromFile(result));
        target.putExtras(extras);
        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
        }
    }

    @Override
    public void setFileMessage(ResMessages.FileMessage fileMessage) {
        this.fileMessage = fileMessage;
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void dismissMentionButton() {
        ivMention.setVisibility(View.GONE);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showMentionButton() {
        ivMention.setVisibility(View.VISIBLE);
    }

    public void showDeleteFileDialog(int fileId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(getString(R.string.jandi_file_delete_message))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_delete, (dialog, which) ->
                        EventBus.getDefault().post(new ConfirmDeleteFileEvent(fileId)))
                .create().show();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void drawFileWriterState(boolean isEnabled) {
        fileHeadManager.drawFileWriterState(isEnabled);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void drawFileDetail(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                               boolean isSendAction) {
        fileHeadManager.setFileInfo(fileMessage);

        if (TextUtils.equals(fileMessage.status, "archived")) {
            vgCommentLayout.setVisibility(View.GONE);

            getSupportActionBar().setTitle(R.string.jandi_deleted_file);
        }

        isDeleted = TextUtils.equals(fileMessage.status, "archived");

        isMyFile = fileMessage.writerId == EntityManager.getInstance().getMe().getId() ||
                fileDetailPresenter.isTeamOwner();

        isExternalShared = fileMessage.content.externalShared;

        invalidateOptionsMenu();

        fileDetailCommentListAdapter.clear();
        fileDetailCommentListAdapter.updateFileComments(commentMessages);
        fileDetailCommentListAdapter.notifyDataSetChanged();

        if (isSendAction) {
            lvFileDetailComments.setSelection(fileDetailCommentListAdapter.getCount());
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showManipulateMessageDialogFragment(ResMessages.OriginalMessage item, boolean isMine) {
        DialogFragment newFragment;
        if (item instanceof ResMessages.CommentMessage) {
            newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(
                    (ResMessages.CommentMessage) item, isMine);
        } else {
            if (!isMine) {
                return;
            }
            newFragment = ManipulateMessageDialogFragment.newInstanceByStickerCommentMessage(
                    (ResMessages.CommentStickerMessage) item, isMine);
        }
        newFragment.show(getSupportFragmentManager(), "dioalog");
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void setSendButtonSelected() {
        boolean visible = vgStickerPreview.getVisibility() == View.VISIBLE || etComment.length() > 0;
        btnSend.setEnabled(visible);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showProgress() {
        dismissProgress();
        progressWheel.show();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1)
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    fileDetailPresenter.onConfigurationChanged();
                    stickerViewModel.onConfigurationChanged();
                });
    }

    @UiThread
    @Override
    public void clearAdapter() {
        fileDetailCommentListAdapter.clear();
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showToast(String message) {
        ColoredToast.show(this, message);
    }

    @UiThread
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(this, message);
    }

    @Override
    public void onBackPressed() {
        if (!stickerViewModel.isShow()) {
            super.onBackPressed();
        } else {
            stickerViewModel.dismissStickerSelector(true);
        }
    }

    public void onEvent(SelectedMemberInfoForMensionEvent event) {
        if (!isForeground) {
            return;
        }
        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        MentionControlViewModel mentionControlViewModel = fileDetailPresenter.getMentionControlViewModel();
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    public void onEvent(MessageStarredEvent event) {
        if (!isForeground) {
            return;
        }

        int messageId = event.getMessageId();

        switch (event.getAction()) {
            case STARRED:
                fileDetailPresenter.registStarredComment(messageId);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.CommentLongTap_Star);
                break;
            case UNSTARRED:
                fileDetailPresenter.unregistStarredComment(messageId);
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.CommentLongTap_Unstar);
                break;
        }
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void modifyStarredInfo(int messageId, boolean isStarred) {
        int position = fileDetailCommentListAdapter.searchIndexOfMessages(messageId);
        fileDetailCommentListAdapter.modifyStarredStateByPosition(position, isStarred);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void updateFileStarred(boolean starred) {
        fileHeadManager.updateStarred(starred);
    }

    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void showUnsharedFileToast() {
        ColoredToast.showError(FileDetailActivity.this, getString(R.string.jandi_unshared_message));
    }

    @TargetApi(Build.VERSION_CODES.M)
    @UiThread(propagation = Propagation.REUSE)
    @Override
    public void requestPermission(int requestCode, String... permissions) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::download)
                .addRequestCode(REQ_STORAGE_PERMISSION_EXPORT)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, () -> onExportFile(fileMessage))
                .resultPermission(Permissions.createPermissionResult(requestCode,
                        permissions,
                        grantResults));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int entityId = data.getIntExtra("EntityId", -1);
            if (requestCode == INTENT_RETURN_TYPE_SHARE) {
                fileDetailPresenter.shareMessage(fileId, entityId);
            } else if (requestCode == INTENT_RETURN_TYPE_UNSHARE) {
                fileDetailPresenter.unShareMessage(fileId, entityId);
            }
        }
    }

    private void setEditTextTouchEvent() {
        etComment.setOnClickListener(v -> {
            if (stickerViewModel.isShow()) {
                stickerViewModel.dismissStickerSelector(true);
            }
        });
    }


}
