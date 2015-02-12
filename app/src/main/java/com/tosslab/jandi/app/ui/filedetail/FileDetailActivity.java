package com.tosslab.jandi.app.ui.filedetail;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.DeleteMessageDialogFragment;
import com.tosslab.jandi.app.dialogs.ManipulateMessageDialogFragment;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.events.files.ConfirmDeleteFile;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.message.v2.MessageListFragment;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;

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
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@OptionsMenu(R.menu.file_detail_activity_menu)
@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends BaseAnalyticsActivity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);
    @Extra
    public int fileId;

    @Bean
    FileDetailModel fileDetailModel;
    @Bean
    FileDetailPresenter fileDetailPresenter;

    private ResMessages.FileMessage mResFileDetail;
    private Context mContext;
    private EntityManager mEntityManager;

    private boolean isMyFile;
    private boolean isDeleted = true;

    @AfterViews
    public void initForm() {
        mContext = getApplicationContext();

        setUpActionBar();

        mEntityManager = EntityManager.getInstance(FileDetailActivity.this);

        getFileDetail(false);
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

    @ItemLongClick(R.id.list_file_detail_comments)
    void onCommentLongClick(ResMessages.CommentMessage item) {
        if (item == null) {
            return;
        }
        boolean isMine = fileDetailModel.isMyComment(item.writerId);
        DialogFragment newFragment = ManipulateMessageDialogFragment.newInstanceByCommentMessage(item, isMine);
        newFragment.show(getFragmentManager(), "dioalog");
    }

    public void onEvent(RequestDeleteMessageEvent event) {
        DialogFragment newFragment = DeleteMessageDialogFragment.newInstance(event);
        newFragment.show(getFragmentManager(), "dialog");
    }

    // 삭제 확인
    public void onEvent(ConfirmDeleteMessageEvent event) {
        deleteComment(event.messageType, event.messageId, event.feedbackId);
    }

    public void onEvent(ConfirmCopyMessageEvent event) {
        fileDetailPresenter.copyToClipboard(event.contentString);
    }

    @Background
    void deleteComment(int messageType, int messageId, int feedbackId) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.deleteComment(messageId, feedbackId);
            getFileDetail(false);
        } catch (JandiNetworkException e) {
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }


    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
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
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        trackGaFileDetail(mEntityManager);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        fileDetailPresenter.dismissProgressWheel();
        super.onStop();
    }

    @Override
    public void finish() {
//        setResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
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
    void getFileDetail(boolean isSendAction) {
        fileDetailPresenter.showProgressWheel();
        log.debug("try to get file detail having ID, " + fileId);
        try {
            ResFileDetail resFileDetail = fileDetailModel.getFileDetailInfo(fileId);
            for (ResMessages.OriginalMessage messageDetail : resFileDetail.messageDetails) {
                if (messageDetail instanceof ResMessages.FileMessage) {
                    mResFileDetail = (ResMessages.FileMessage) messageDetail;
                    break;
                }
            }

            Collections.sort(resFileDetail.messageDetails, (lhs, rhs) -> lhs.updateTime.compareTo(rhs.updateTime));

            getFileDetailSucceed(resFileDetail, isSendAction);

        } catch (JandiNetworkException e) {
            log.error("fail to get file detail.", e);
            getFileDetailFailed(getString(R.string.err_file_detail));
            finishOnMainThread();
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

                if (TextUtils.equals(fileDetail.status, "archived")) {
                    isDeleted = true;
                } else {
                    isDeleted = false;
                }

                if (fileDetail.writerId == EntityManager.getInstance(FileDetailActivity.this).getMe().getId()) {
                    isMyFile = true;
                } else {
                    isMyFile = false;
                }

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
        try {
            fileDetailModel.shareMessage(fileId, entityIdToBeShared);
            log.debug("success to share message");
            shareMessageSucceed(entityIdToBeShared);
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
            shareMessageFailed();
        }
    }

    @UiThread
    public void shareMessageSucceed(int entityIdToBeShared) {
        ColoredToast.show(this, getString(R.string.jandi_share_succeed));
        trackSharingFile(mEntityManager,
                mEntityManager.getEntityById(entityIdToBeShared).type,
                mResFileDetail);
        fileDetailPresenter.clearAdapter();
        getFileDetail(false);
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
        try {
            fileDetailModel.unshareMessage(fileId, entityIdToBeUnshared);
            log.debug("success to unshare message");
            trackUnsharingFile(mEntityManager,
                    mEntityManager.getEntityById(entityIdToBeUnshared).type,
                    mResFileDetail);
            fileDetailPresenter.unshareMessageSucceed(entityIdToBeUnshared);
            getFileDetail(false);
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
            unshareMessageFailed();
        }
    }

    @UiThread
    public void unshareMessageFailed() {
        ColoredToast.showError(this, getString(R.string.err_unshare));
    }

    public void onEvent(ConfirmDeleteFile event) {
        deleteFileInBackground(event.getFileId());
    }

    /**
     * 파일 삭제
     *
     * @param fileId
     */
    @Background
    public void deleteFileInBackground(int fileId) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.deleteFile(fileId);
            log.debug("success to delete file");
            deleteFileDone(true);
        } catch (JandiNetworkException e) {
            log.error("delete file failed", e);
            deleteFileDone(false);
        } finally {
            fileDetailPresenter.dismissProgressWheel();
        }
    }

    @UiThread
    public void deleteFileDone(boolean isOk) {
        if (isOk) {
            CharSequence title = getActionBar().getTitle();
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

    /**
     * *********************************************************
     * 댓글 작성 관련
     * **********************************************************
     */

    @Click(R.id.btn_file_detail_send_comment)
    void sendComment() {
        String comment = fileDetailPresenter.getCommentText();
        fileDetailPresenter.hideSoftKeyboard();

        if (comment.length() > 0) {
            sendCommentInBackground(comment);
        }
    }


    @Background
    public void sendCommentInBackground(String message) {
        fileDetailPresenter.showProgressWheel();
        try {
            fileDetailModel.sendMessageComment(fileId, message);

            getFileDetail(true);
            log.debug("success to send message");
        } catch (JandiNetworkException e) {
            log.error("fail to send message", e);
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
        String serverUrl = (mResFileDetail.content.serverUrl.equals("root"))
                ? JandiConstantsForFlavors.SERVICE_ROOT_URL
                : mResFileDetail.content.serverUrl;
        String fileName = mResFileDetail.content.fileUrl.replace(" ", "%20");

        final ProgressDialog progressDialog = new ProgressDialog(FileDetailActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileName);
        progressDialog.show();

        downloadInBackground(serverUrl + fileName, mResFileDetail.content.name, mResFileDetail.content.type, progressDialog);
    }

    public void onEvent(FileDownloadStartEvent fileDownloadStartEvent) {
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
            log.error("Download failed", e);
            ColoredToast.showError(mContext, getString(R.string.err_download));
        }
    }

    /**
     * *********************************************************
     * 사용자 프로필 보기
     * TODO Background 는 공통으로 빼고 Success, Fail 리스너를 둘 것.
     * **********************************************************
     */
    public void onEvent(RequestUserInfoEvent event) {
        int userEntityId = event.userId;
        getProfileInBackground(userEntityId);
    }

    @Background
    void getProfileInBackground(int userEntityId) {
        try {
            ResLeftSideMenu.User user = fileDetailModel.getUserProfile(userEntityId);
            fileDetailPresenter.showUserInfoDialog(new FormattedEntity(user));
        } catch (JandiNetworkException e) {
            log.error("get profile failed", e);
            getProfileFailed();
        } catch (Exception e) {
            log.error("get profile failed", e);
            getProfileFailed();
        }
    }


    @UiThread
    void getProfileFailed() {
        ColoredToast.showError(this, getString(R.string.err_profile_get_info));
        finish();
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance(FileDetailActivity.this);
        MessageListV2Activity_.intent(FileDetailActivity.this)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

}
