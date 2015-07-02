package com.tosslab.jandi.app.ui.filedetail;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.entities.MoveSharedEntityEvent;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFileEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.database.sticker.JandiStickerDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListFragment;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.sticker.StickerViewModel;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@OptionsMenu(R.menu.file_detail_activity_menu)
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAnalyticsActivity {

    private static final StickerInfo NULL_STICKER = new StickerInfo();
    @Extra
    public int fileId;

    @Bean
    FileDetailModel fileDetailModel;
    @Bean
    FileDetailPresenter fileDetailPresenter;

    @Bean
    StickerViewModel stickerViewModel;

    @Bean
    KeyboardHeightModel keyboardHeightModel;

    private ResMessages.FileMessage mResFileDetail;
    private EntityManager mEntityManager;

    private boolean isMyFile;
    private boolean isDeleted = true;
    private boolean isForeground;
    private StickerInfo stickerInfo = NULL_STICKER;

    @AfterViews
    public void initForm() {

        setUpActionBar();

        mEntityManager = EntityManager.getInstance(FileDetailActivity.this);

        stickerViewModel.setOnStickerClick(new StickerViewModel.OnStickerClick() {
            @Override
            public void onStickerClick(int groupId, String stickerId) {
                StickerInfo oldSticker = stickerInfo;
                stickerInfo = new StickerInfo();
                stickerInfo.setStickerGroupId(groupId);
                stickerInfo.setStickerId(stickerId);
                fileDetailPresenter.showStickerPreview();

                if (oldSticker.getStickerGroupId() != stickerInfo.getStickerGroupId() || !TextUtils.equals(oldSticker.getStickerId(), stickerInfo.getStickerId())) {
                    fileDetailPresenter.loadSticker(stickerInfo);
                }
                fileDetailPresenter.setSendButtonSelected(true);
            }
        });

        JandiPreference.setKeyboardHeight(FileDetailActivity.this, 0);
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

        return true;
    }

    @Click(R.id.iv_file_detail_preview_sticker_close)
    void onStickerPreviewClose() {
        FileDetailActivity.this.stickerInfo = NULL_STICKER;
        fileDetailPresenter.dismissStickerPreview();
        fileDetailPresenter.setSendButtonSelected(false);

    }

    @ItemLongClick(R.id.list_file_detail_comments)
    void onCommentLongClick(ResMessages.OriginalMessage item) {
        if (item == null) {
            return;
        }
        boolean isMine = fileDetailModel.isMyComment(item.writerId);

        DialogFragment newFragment = null;
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

    public void onEvent(RequestDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }
        DialogFragment newFragment = DeleteMessageDialogFragment.newInstance(event, true);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    // 삭제 확인
    public void onEvent(ConfirmDeleteMessageEvent event) {
        if (!isForeground) {
            return;
        }
        deleteComment(event.messageType, event.messageId, event.feedbackId);
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        if (!isForeground) {
            return;
        }
        fileDetailPresenter.copyToClipboard(event.contentString);
    }

    @Background
    void deleteComment(int messageType, int messageId, int feedbackId) {
        fileDetailPresenter.showProgressWheel();
        try {
            if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                fileDetailModel.deleteStickerComment(messageId, MessageItem.TYPE_STICKER_COMMNET);
            } else {
                fileDetailModel.deleteComment(messageId, feedbackId);
            }

            getFileDetail(false, true);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }


    private void setUpActionBar() {
        // Set up the action bar.

        Toolbar toolbar = ((Toolbar) findViewById(R.id.layout_search_bar));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                return true;
            case R.id.action_file_detail_share:
                clickShareButton();
                return true;
            case R.id.action_file_detail_unshare:
                clickUnshareButton();
                return true;
            case R.id.action_file_detail_delete:
                fileDetailPresenter.showDeleteFileDialog(fileId);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        getFileDetail(false, true);
        trackGaFileDetail(mEntityManager);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Override
    protected void onStop() {
        fileDetailPresenter.dismissProgressWheel();
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @AfterTextChange(R.id.et_file_detail_comment)
    void onCommentTextChange(Editable editable) {
        int inputLength = editable.length();
        fileDetailPresenter.setSendButtonSelected(inputLength > 0);
    }

    /**
     * *********************************************************
     * 파일 상세 출력 관련
     * **********************************************************
     */
    @Background
    void getFileDetail(boolean isSendAction, boolean showDialog) {
        if (showDialog) {
            fileDetailPresenter.showProgressWheel();
        }
        LogUtil.d("try to get file detail having ID, " + fileId);
        try {
            ResFileDetail resFileDetail = fileDetailModel.getFileDetailInfo(fileId);

            for (ResMessages.OriginalMessage messageDetail : resFileDetail.messageDetails) {
                if (messageDetail instanceof ResMessages.FileMessage) {
                    mResFileDetail = (ResMessages.FileMessage) messageDetail;
                    break;
                }
            }

            Collections.sort(resFileDetail.messageDetails, (lhs, rhs) -> lhs.createTime.compareTo(rhs.createTime));

            getFileDetailSucceed(resFileDetail, isSendAction);

            boolean enableUserFromUploder = fileDetailModel.isEnableUserFromUploder(resFileDetail);
            fileDetailPresenter.drawFileWriterState(enableUserFromUploder);

        } catch (RetrofitError e) {
            LogUtil.e("fail to get file detail.", e);

            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.SERVICE_UNAVAILABLE) {
                getFileDetailFailed(getString(R.string.jandi_unshared_message));
            } else {
                getFileDetailFailed(getString(R.string.err_file_detail));
                e.printStackTrace();
            }
            finishOnMainThread();
        } catch (Exception e) {
            getFileDetailFailed(getString(R.string.err_file_detail));
            finishOnMainThread();
            e.printStackTrace();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }

    }

    @UiThread
    void finishOnMainThread() {
        finish();
    }

    @UiThread
    void getFileDetailSucceed(ResFileDetail resFileDetail, boolean isSendAction) {

        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {

                isDeleted = TextUtils.equals(fileDetail.status, "archived");

                isMyFile = fileDetail.writerId == EntityManager.getInstance(FileDetailActivity.this).getMe().getId();

                invalidateOptionsMenu();
                break;
            }
        }

        fileDetailPresenter.drawFileDetail(resFileDetail, isSendAction);
    }

    @UiThread
    void getFileDetailFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
    }

    /**
     * *********************************************************
     * 파일 공유
     * **********************************************************
     */
    void clickShareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_shared);
        dialog.setView(view);
        final AlertDialog cdpSelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 entity를 추출
        final List<FormattedEntity> unSharedEntities = fileDetailModel.getUnsharedEntities(mResFileDetail.shareEntities);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, unSharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cdpSelectDialog != null)
                    cdpSelectDialog.dismiss();
                shareMessageInBackground(unSharedEntities.get(i).getEntity().id);
            }
        });
    }

    @Background
    public void shareMessageInBackground(int entityIdToBeShared) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.shareMessage(fileId, entityIdToBeShared);
            LogUtil.d("success to share message");
            shareMessageSucceed(entityIdToBeShared);
            showMoveDialog(entityIdToBeShared);
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("fail to send message", e);
            shareMessageFailed();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("fail to send message", e);
            shareMessageFailed();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }

    @UiThread
    void showMoveDialog(int entityIdToBeShared) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FileDetailActivity.this);
        builder.setMessage(getString(R.string.jandi_move_entity_after_share))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FormattedEntity entity = EntityManager.getInstance(FileDetailActivity.this)
                                .getEntityById(entityIdToBeShared);

                        MessageListV2Activity_.intent(FileDetailActivity.this)
                                .teamId(EntityManager.getInstance(FileDetailActivity.this).getTeamId())
                                .entityId(entityIdToBeShared)
                                .entityType(entity.type)
                                .roomId(entity.isUser() ? -1 : entityIdToBeShared)
                                .isFavorite(entity.isStarred)
                                .teamId(entity.getEntity().teamId)
                                .start();
                    }
                })
                .create()
                .show();

    }

    @UiThread
    public void shareMessageSucceed(int entityIdToBeShared) {
        ColoredToast.show(this, getString(R.string.jandi_share_succeed, getSupportActionBar().getTitle()));
        trackSharingFile(mEntityManager,
                mEntityManager.getEntityById(entityIdToBeShared).type,
                mResFileDetail);
        fileDetailPresenter.clearAdapter();
        getFileDetail(false, true);
    }

    @UiThread
    public void shareMessageFailed() {
        ColoredToast.showError(this, getString(R.string.err_share));
    }

    /**
     * *********************************************************
     * 파일 공유 해제
     * **********************************************************
     */
    void clickUnshareButton() {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        View view = getLayoutInflater().inflate(R.layout.dialog_select_cdp, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.jandi_title_cdp_to_be_unshared);
        dialog.setView(view);
        final AlertDialog entitySelectDialog = dialog.show();

        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        // 현재 이 파일을 share 하지 않는 CDP를 추출
        List<Integer> shareEntitiesIds = mResFileDetail.shareEntities;
        final List<FormattedEntity> sharedEntities = mEntityManager.retrieveGivenEntities(shareEntitiesIds);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, sharedEntities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (entitySelectDialog != null)
                    entitySelectDialog.dismiss();
                unshareMessageInBackground(sharedEntities.get(i).getEntity().id);
            }
        });
    }

    @Background
    public void unshareMessageInBackground(int entityIdToBeUnshared) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.unshareMessage(fileId, entityIdToBeUnshared);
            LogUtil.d("success to unshare message");
            trackUnsharingFile(mEntityManager,
                    mEntityManager.getEntityById(entityIdToBeUnshared).type,
                    mResFileDetail);
            fileDetailPresenter.unshareMessageSucceed(entityIdToBeUnshared);
            getFileDetail(false, true);
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("fail to send message", e);
            unshareMessageFailed();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("fail to send message", e);
            unshareMessageFailed();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }

    @UiThread
    public void unshareMessageFailed() {
        ColoredToast.showError(this, getString(R.string.err_unshare));
    }

    public void onEvent(ConfirmDeleteFileEvent event) {
        if (!isForeground) {
            return;
        }
//        deleteFileInBackground(event.getFileId());
    }

    public void onEvent(DeleteFileEvent event) {

        if (fileId == event.getId()) {
            getFileDetail(false, false);
        }
    }

    public void onEvent(ShareFileEvent event) {
        if (fileId == event.getId()) {
            getFileDetail(false, false);
        }
    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (fileId == event.getId()) {
            getFileDetail(false, false);
        }
    }

    public void onEvent(MoveSharedEntityEvent event) {

        if (!isForeground) {
            return;
        }

        int entityId = event.getEntityId();

        EntityManager entityManager = EntityManager.getInstance(FileDetailActivity.this);

        FormattedEntity entity = entityManager.getEntityById(entityId);

        int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : entity.isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;

        int teamId = entityManager.getTeamId();
        boolean isStarred = entity.isStarred;
        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            if (entity.isPublicTopic() && entity.isJoined
                    || entity.isPrivateGroup()) {

                moveMessageList(entityId, entityType, teamId, isStarred);
            } else {
                joinAndMove(entity);
            }
        } else {
            moveMessageList(entityId, entityType, teamId, isStarred);
        }

    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (mResFileDetail != null) {
            int size = mResFileDetail.shareEntities.size();

            int entityId;
            int eventId = event.getId();
            for (int idx = 0; idx < size; ++idx) {
                entityId = mResFileDetail.shareEntities.get(idx);

                if (eventId == entityId) {
                    finish();
                    return;
                }
            }
        }
    }

    private void moveMessageList(int entityId, int entityType, int teamId, boolean isStarred) {
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityId : -1)
                .isFromPush(false)
                .isFavorite(isStarred)
                .start();
    }

    @Background
    void joinAndMove(FormattedEntity entityId) {
        fileDetailPresenter.showProgressWheel();

        try {
            EntityManager entityManager = EntityManager.getInstance(FileDetailActivity.this);
            fileDetailModel.joinEntity(entityId);
            MixpanelMemberAnalyticsClient
                    .getInstance(FileDetailActivity.this, entityManager.getDistictId())
                    .trackJoinChannel();

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.refreshEntity();

            moveMessageList(entityId.getId(), entityType, entityManager.getTeamId(), false);

        } catch (RetrofitError e) {
            e.printStackTrace();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }

//    /**
//     * 파일 삭제
//     *
//     * @param fileId
//     */
//    @Background
//    public void deleteFileInBackground(int fileId) {
//        fileDetailPresenter.showProgressWheel();
//        try {
//            fileDetailModel.deleteFile(fileId);
//            LogUtil.d("success to delete file");
//            deleteFileDone(true);
//        } catch (JandiNetworkException e) {
//            LogUtil.e("delete file failed", e);
//            deleteFileDone(false);
//        } catch (Exception e) {
//            deleteFileDone(false);
//        } finally {
//            fileDetailPresenter.dismissProgressWheel();
//        }
//    }

    @UiThread
    public void deleteFileDone(boolean isOk) {
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
    }

    @Click(R.id.btn_message_sticker)
    void onStickerClick(View view) {
        boolean selected = view.isSelected();

        if (selected) {
            stickerViewModel.dismissStickerSelector();
        } else {
            int keyboardHeight = JandiPreference.getKeyboardHeight(FileDetailActivity.this.getApplicationContext());
            if (keyboardHeight > 0) {
                fileDetailPresenter.hideKeyboard();
                stickerViewModel.showStickerSelector(keyboardHeight);
                if (keyboardHeightModel.getOnKeyboardShowListener() == null) {
                    keyboardHeightModel.setOnKeyboardShowListener(isShow -> {
                        if (isShow) {
                            stickerViewModel.dismissStickerSelector();
                        }
                    });
                }
            } else {
                initKeyboardHeight();
            }
        }
    }

    private void initKeyboardHeight() {
        EditText etMessage = fileDetailPresenter.getSendEditTextView();
        keyboardHeightModel.setOnKeyboardHeightCaptureListener(() -> {
            onStickerClick(findViewById(R.id.btn_message_sticker));
            keyboardHeightModel.setOnKeyboardHeightCaptureListener(null);
        });

        etMessage.requestFocus();
        fileDetailPresenter.showKeyboard();
    }


    /**
     * *********************************************************
     * 댓글 작성 관련
     * **********************************************************
     */
    @Click(R.id.btn_file_detail_send_comment)
    void onSendComment() {
        String comment = fileDetailPresenter.getCommentText().trim();
        fileDetailPresenter.hideSoftKeyboard();

        if (stickerInfo != null && stickerInfo != NULL_STICKER) {
            fileDetailPresenter.dismissStickerPreview();
            JandiStickerDatabaseManager.getInstance(FileDetailActivity.this.getApplicationContext()).upsertRecentSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId());

            sendCommentWithSticker(stickerInfo.getStickerGroupId(), stickerInfo.getStickerId(), comment);
            stickerInfo = NULL_STICKER;
        } else if (!TextUtils.isEmpty(comment)) {
            sendComment(comment);
        }
    }

    @Background
    void sendCommentWithSticker(int stickerGroupId, String stickerId, String comment) {
        fileDetailPresenter.showProgressWheel();
        try {

            fileDetailModel.sendMessageCommentWithSticker(fileId, stickerGroupId, stickerId, comment);

            getFileDetail(true, true);

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }

    }


    @Background
    public void sendComment(String message) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.sendMessageComment(fileId, message);

            getFileDetail(true, true);
            LogUtil.d("success to send message");
        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }

    }


    /**
     * *********************************************************
     * 파일 연결 관련
     * **********************************************************
     */
    public void download() {

        MimeTypeUtil.PlaceholderType placeholderType = PlaceholderUtil.getPlaceholderType(mResFileDetail.content.serverUrl, mResFileDetail.content.icon);

        switch (placeholderType) {
            case Google:
            case Dropbox:
                String photoUrl = BitmapUtil.getFileUrl(mResFileDetail.content.fileUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl)));
                return;
        }


        String fileName = mResFileDetail.content.fileUrl.replace(" ", "%20");

        final ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();

        downloadInBackground(BitmapUtil.getFileUrl(mResFileDetail.content.fileUrl), mResFileDetail.content.name, mResFileDetail.content.type, progressDialog);
    }

    public void onEvent(FileDownloadStartEvent fileDownloadStartEvent) {

        if (!isForeground) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileDownloadStartEvent.getFileName());
        progressDialog.show();

        downloadInBackground(fileDownloadStartEvent.getUrl(), fileDownloadStartEvent.getFileName(), fileDownloadStartEvent.getFileType(), progressDialog);
    }

    @Background
    public void downloadInBackground(String url, String fileName, final String fileType, ProgressDialog progressDialog) {
        try {
            File result = fileDetailModel.download(url, fileName, fileType, progressDialog);
            trackDownloadingFile(mEntityManager, mResFileDetail);

            if (fileDetailModel.isMediaFile(fileType)) {
                fileDetailModel.addGallery(result, fileType);
            }

            fileDetailPresenter.downloadDone(result, fileType, progressDialog);
        } catch (Exception e) {
            LogUtil.e("Download failed", e);
            fileDetailPresenter.showFailToast(getString(R.string.err_download));
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필 보기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    public void onEvent(RequestUserInfoEvent event) {

        if (!isForeground) {
            return;
        }
        int userEntityId = event.userId;
        getProfileInBackground(userEntityId);
    }

    @Background
    void getProfileInBackground(int userEntityId) {
        try {
            ResLeftSideMenu.User user = fileDetailModel.getUserProfile(userEntityId);
            fileDetailPresenter.showUserInfoDialog(new FormattedEntity(user));
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("get profile failed", e);
            getProfileFailed();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("get profile failed", e);
            getProfileFailed();
        }
    }

    @Override
    public void onBackPressed() {
        if (!stickerViewModel.isShowStickerSelector()) {
            super.onBackPressed();
        } else {
            stickerViewModel.dismissStickerSelector();
        }
    }

    @UiThread
    void getProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {

        if (!isForeground) {
            return;
        }

        EntityManager entityManager = EntityManager.getInstance(FileDetailActivity.this);
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .roomId(-1)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

}
