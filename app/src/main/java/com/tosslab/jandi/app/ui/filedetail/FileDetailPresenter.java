package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.filedetail.domain.FileStarredInfo;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.file.FileSizeUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailPresenter {

    @RootContext
    Activity activity;

    @Bean
    FileDetailModel fileDetailModel;

    private MentionControlViewModel mentionControlViewModel;

    private View view;
    private PublishSubject<FileStarredInfo> starredPublishSubject;

    @AfterInject
    void initObject() {
        starredPublishSubject = PublishSubject.create();

        starredPublishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .subscribe(fileStarredInfo -> {
                    if (fileStarredInfo.isStarred()) {
                        registStarredFileMessage(fileStarredInfo.getFileId());
                    } else {
                        unregistStarredFileMessage(fileStarredInfo.getFileId());
                    }
                }, Throwable::printStackTrace);
    }

    public List<Integer> getSharedTopicIds(ResMessages.OriginalMessage fileDetail) {
        List<Integer> sharedTopicIds = new ArrayList<>();

        EntityManager entityManager = EntityManager.getInstance();

        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
        for (ResMessages.OriginalMessage.IntegerWrapper entity : fileMessage.shareEntities) {
            FormattedEntity formattedEntity = entityManager.getEntityById(entity.getShareEntity());
            if (formattedEntity != EntityManager.UNKNOWN_USER_ENTITY && !formattedEntity.isUser()) {
                sharedTopicIds.add(formattedEntity.getId());
            }
        }
        return sharedTopicIds;
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean onLoadFromCache(int fileId, int selectMessageId) {
        List<FileDetail> fileDetail = fileDetailModel.getFileDetail(fileId);
        if (fileDetail != null && !fileDetail.isEmpty()) {
            ResMessages.FileMessage file = fileDetail.get(0).getFile();

            ResFileDetail fakeFile = new ResFileDetail();
            fakeFile.messageDetails = new ArrayList<>();
            fakeFile.messageDetails.add(file);

            for (FileDetail detail : fileDetail) {
                ResMessages.OriginalMessage message = null;
                if (detail.getComment() != null) {
                    message = detail.getComment();
                } else if (detail.getSticker() != null) {
                    message = detail.getSticker();
                }

                if (message != null) {
                    fakeFile.messageDetails.add(message);
                }
            }

            ResMessages.FileMessage fileMessage = fileDetailModel.extractFileMssage(fakeFile.messageDetails);
            List<ResMessages.OriginalMessage> commentMessages =
                    fileDetailModel.extractCommentMessage(fakeFile.messageDetails);

            view.loadSuccess(fileMessage, commentMessages, false, selectMessageId);

            return true;
        }

        return false;

    }

    /**
     * 파일 상세 출력 관련
     */
    @Background
    public void getFileDetail(int fileId, boolean isSendAction, boolean showDialog, int selectMessageId) {
        LogUtil.d("try to get file detail having ID, " + fileId);

        if (showDialog) {
            view.showProgress();
        }
        try {
            ResFileDetail resFileDetail = fileDetailModel.getFileDetailInfo(fileId);

            LogUtil.d("FileDetailActivity", resFileDetail.toString());

            ResMessages.FileMessage fileMessage = fileDetailModel.extractFileMssage(resFileDetail.messageDetails);

            if (fileMessage == null) {
                throw new NullPointerException("File Message doens't conains");
            }

            // 저장하기
            fileDetailModel.saveFileDetailInfo(resFileDetail);

            List<ResMessages.OriginalMessage> commentMessages =
                    fileDetailModel.extractCommentMessage(resFileDetail.messageDetails);

            view.dismissProgress();

            view.loadSuccess(fileMessage, commentMessages, isSendAction, selectMessageId);

        } catch (RetrofitError e) {
            LogUtil.e("fail to get file detail.", e);
            view.dismissProgress();
            String errorMessage;
            if (e.getResponse() != null
                    && e.getResponse().getStatus() == 403) {
                view.showUnsharedFileToast();
            } else {
                if (e.getCause() instanceof ConnectionNotFoundException) {
                    errorMessage = activity.getResources().getString(R.string.err_network);
                } else {
                    errorMessage = activity.getResources().getString(R.string.err_file_detail);
                }
                view.showToast(errorMessage);
            }
            view.finishOnMainThread();
        } catch (Exception e) {
            view.dismissProgress();
            view.showToast(activity.getResources().getString(R.string.err_file_detail));
            view.finishOnMainThread();
        }

    }

    public void onLongClickComment(ResMessages.OriginalMessage item) {
        if (item == null) {
            return;
        }
        boolean isMine = fileDetailModel.isMyComment(item.writerId) || fileDetailModel.isTeamOwner();
        view.showManipulateMessageDialogFragment(item, isMine);
    }

    @Background
    public void shareMessage(int fileId, int entityIdToBeShared) {
        view.showProgress();
        try {
            fileDetailModel.shareMessage(fileId, entityIdToBeShared);
            LogUtil.d("success to share message");

            fileDetailModel.trackFileShareSuccess(entityIdToBeShared, fileId);

            view.dismissProgress();
            view.onShareMessageSucceed(entityIdToBeShared, fileDetailModel.getFileMessage(fileId));
            view.showMoveDialog(entityIdToBeShared);
        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileShareFail(errorCode);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_share));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            fileDetailModel.trackFileShareFail(-1);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_share));
        }
    }

    @Background
    public void unShareMessage(int fileId, int entityIdToBeUnshared) {
        view.showProgress();
        try {
            fileDetailModel.unshareMessage(fileId, entityIdToBeUnshared);
            LogUtil.d("success to unshare message");

            fileDetailModel.trackFileUnShareSuccess(entityIdToBeUnshared, fileId);

            view.dismissProgress();

            view.onUnShareMessageSucceed(entityIdToBeUnshared,
                    fileDetailModel.getFileMessage(fileId));

        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileUnShareFail(errorCode);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_unshare));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            fileDetailModel.trackFileUnShareFail(-1);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_unshare));
        }
    }

    @Background
    public void joinAndMove(FormattedEntity entityId) {
        view.showProgress();

        try {
            EntityManager entityManager = EntityManager.getInstance();
            fileDetailModel.joinEntity(entityId);

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, entityManager.getDistictId())
                    .trackJoinChannel();

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.refreshEntity();

            view.dismissProgress();

            view.moveToMessageListActivity(entityId.getId(), entityType, entityId.getId(), false);
        } catch (Exception e) {
            e.printStackTrace();
            view.dismissProgress();
        }
    }

    @Background
    public void sendComment(int fileId, String message, List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageComment(fileId, message, mentions);
            view.dismissProgress();

            getFileDetail(fileId, true, true, -1);
            LogUtil.d("success to send message");
        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            view.dismissProgress();
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            view.dismissProgress();
        }
    }

    @Background
    void sendCommentWithSticker(int fileId, int stickerGroupId, String stickerId, String comment,
                                List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageCommentWithSticker(
                    fileId, stickerGroupId, stickerId, comment, mentions);

            view.dismissProgress();

            getFileDetail(fileId, true, true, -1);
        } catch (RetrofitError e) {
            view.dismissProgress();
            e.printStackTrace();
        } catch (Exception e) {
            view.dismissProgress();
            e.printStackTrace();
        }
    }

    @Background
    public void deleteComment(int fileId, int messageType, int messageId, int feedbackId) {
        view.showProgress();
        try {
            if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
                fileDetailModel.deleteStickerComment(messageId, MessageItem.TYPE_STICKER_COMMNET);
            } else {
                fileDetailModel.deleteComment(messageId, feedbackId);
            }

            view.dismissProgress();

            getFileDetail(fileId, false, true, -1);
        } catch (RetrofitError e) {
            view.dismissProgress();
        } catch (Exception e) {
            view.dismissProgress();
        }
    }

    /**
     * 파일 삭제
     *
     * @param fileId
     */
    @Background
    public void deleteFile(int fileId, int topicId) {
        view.showProgress();
        try {
            fileDetailModel.deleteFile(fileId);
            LogUtil.d("success to delete file");

            fileDetailModel.trackFileDeleteSuccess(topicId, fileId);

            view.dismissProgress();
            view.onDeleteFileSucceed(true);
        } catch (RetrofitError e) {
            LogUtil.e("delete file failed", e);

            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileDeleteFail(errorCode);

            view.dismissProgress();
            view.onDeleteFileSucceed(false);
        } catch (Exception e) {
            fileDetailModel.trackFileDeleteFail(-1);

            view.dismissProgress();
            view.onDeleteFileSucceed(false);
        }
    }

    public void checkSharedEntity(int eventId, int fileId) {
        final ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage(fileId);
        if (fileMessage == null) {
            return;
        }
        int size = fileMessage.shareEntities.size();

        int entityId;
        Iterator<ResMessages.OriginalMessage.IntegerWrapper> iterator = fileMessage.shareEntities.iterator();
        while (iterator.hasNext()) {
            entityId = iterator.next().getShareEntity();

            if (eventId == entityId) {
                view.finishOnMainThread();
                return;
            }
        }
    }

    public void onClickDownload(ProgressDialog progressDialog, int fileId) {
        ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage(fileId);
        if (fileMessage == null) {
            return;
        }

        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.PlaceholderType placeholderType =
                PlaceholderUtil.getPlaceholderType(content.serverUrl, content.icon);

        switch (placeholderType) {
            case Google:
            case Dropbox:
                String photoUrl = BitmapUtil.getFileUrl(content.fileUrl);
                view.startGoogleOrDropboxFileActivity(photoUrl);
                return;
        }


        downloadFile(BitmapUtil.getFileUrl(content.fileUrl),
                content.title,
                content.type,
                content.ext,
                progressDialog, fileId, false);
    }

    @Background
    public void downloadFile(String url, String fileName, final String fileType, String ext,
                             ProgressDialog progressDialog, int fileId, boolean execute) {

        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .noPermission(() -> {
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                })
                .hasPermission(() -> {
                    downloadFileImpl(url, fileName, fileType, ext, progressDialog, fileId, execute);
                }).check();
    }

    private void downloadFileImpl(String url, String fileName, final String fileType, String ext,
                                  ProgressDialog progressDialog, int fileId, boolean execute) {
        String dialogText = FileSizeUtil.getDownloadFileName(fileName, ext);

        view.showDownloadProgressDialog(dialogText);

        try {
            File result = fileDetailModel.download(url, fileName, ext, progressDialog);

            if (fileDetailModel.isMediaFile(fileType)) {
                fileDetailModel.addGallery(result, fileType);
            }

            fileDetailModel.trackFileDownloadSuccess(fileId);

            view.dismissDownloadProgressDialog();
            view.onDownloadFileSucceed(result, fileType, fileDetailModel.getFileMessage(fileId),
                    execute);
        } catch (Exception e) {
            LogUtil.e("Download failed", e);
            view.dismissDownloadProgressDialog();
            view.showErrorToast(activity.getResources().getString(R.string.err_download));
        }
    }

    @Background
    public void getProfile(int userEntityId) {
        try {
            ResLeftSideMenu.User user = fileDetailModel.getUserProfile(userEntityId);
            view.showUserInfoDialog(new FormattedEntity(user));
        } catch (RetrofitError e) {
            LogUtil.e("get profile failed", e);
            view.onGetProfileFailed();
        } catch (Exception e) {
            LogUtil.e("get profile failed", e);
            view.onGetProfileFailed();
        }
    }

    public void refreshMentionVM(Activity activity, ResMessages.OriginalMessage fileMessage,
                                 RecyclerView searchMemberListView,
                                 EditText editText, ListView fileCommentListView) {

        List<Integer> sharedTopicIds = getSharedTopicIds(fileMessage);

        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(activity,
                    editText,
                    sharedTopicIds,
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
            registClipboardListenerforMention();
        }
        mentionControlViewModel.clear();
    }

    public void registClipboardListenerforMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }

    public void removeClipboardListenerforMention() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
    }

    public ResultMentionsVO getMentionInfo() {
        if (mentionControlViewModel != null) {
            return mentionControlViewModel.getMentionInfoObject();
        } else {
            return new ResultMentionsVO("", new ArrayList<>());
        }
    }

    public MentionControlViewModel getMentionControlViewModel() {
        return mentionControlViewModel;
    }

    @Background
    public void registStarredComment(int messageId) {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.registStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, true);
            view.showToast(activity.getString(R.string.jandi_message_starred));
            view.modifyStarredInfo(messageId, true);
            EventBus.getDefault().post(new StarredInfoChangeEvent());

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void unregistStarredComment(int messageId) {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.unregistStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);

            view.modifyStarredInfo(messageId, false);
            view.showToast(activity.getString(R.string.jandi_unpinned_message));
            EventBus.getDefault().post(new StarredInfoChangeEvent());

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }

    }

    @Background
    public void unregistStarredFileMessage(int fileId) {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.unregistStarredMessage(teamId, fileId);
            MessageRepository.getRepository().updateStarred(fileId, false);

            view.updateFileStarred(false);
            view.showToast(activity.getString(R.string.jandi_unpinned_message));
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void registStarredFileMessage(int fileId) {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.registStarredMessage(teamId, fileId);
            view.updateFileStarred(true);
            view.showToast(activity.getString(R.string.jandi_message_starred));
            MessageRepository.getRepository().updateStarred(fileId, true);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }

    }

    public void changeStarredFileMessageState(int fileId, boolean starred) {
        // 클릭 여러번을 막기 위함
        starredPublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    public boolean isTeamOwner() {
        return fileDetailModel.isTeamOwner();
    }

    public void onConfigurationChanged() {
        mentionControlViewModel.onConfigurationChanged();
    }

    public interface View {
        void drawFileWriterState(boolean isEnabled);

        void drawFileDetail(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                            boolean isSendAction);

        void loadSuccess(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                         boolean isSendAction, int selectMessageId);

        void showCheckNetworkDialog();

        void showDeleteFileDialog(int fileId);

        void showUserInfoDialog(FormattedEntity user);

        void showMoveDialog(int entityIdToBeShared);

        void showManipulateMessageDialogFragment(ResMessages.OriginalMessage item, boolean isMine);

        void onShareMessageSucceed(int entityIdToBeShared, ResMessages.FileMessage fileMessage);

        void onUnShareMessageSucceed(int entityIdToBeUnshared, ResMessages.FileMessage fileMessage);

        void onDeleteFileSucceed(boolean isOk);

        void onDownloadFileSucceed(File file, String fileType, ResMessages.FileMessage fileMessage,
                                   boolean execute);

        void onGetProfileFailed();

        void setSendButtonSelected(boolean selected);

        void showProgress();

        void dismissProgress();

        void showDownloadProgressDialog(String fileName);

        void dismissDownloadProgressDialog();

        void clearAdapter();

        void showToast(String message);

        void showErrorToast(String message);

        void hideSoftKeyboard();

        void moveToMessageListActivity(int entityId, int entityType, int roomId, boolean isStarred);

        void startGoogleOrDropboxFileActivity(String fileUrl);

        void finishOnMainThread();

        void showKeyboard();

        void showStickerPreview();

        void loadSticker(StickerInfo stickerInfo);

        void dismissStickerPreview();

        void modifyStarredInfo(int messageId, boolean isStarred);

        void updateFileStarred(boolean starred);

        void showUnsharedFileToast();

        void requestPermission(int requestCode, String... permission);
    }
}
