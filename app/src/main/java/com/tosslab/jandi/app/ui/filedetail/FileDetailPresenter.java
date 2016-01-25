package com.tosslab.jandi.app.ui.filedetail;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.KeyEvent;
import android.widget.EditText;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.ReadyCommentRepository;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.filedetail.domain.FileStarredInfo;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@EBean
public class FileDetailPresenter {

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

    protected List<Long> getSharedTopicIds(ResMessages.OriginalMessage fileDetail) {
        List<Long> sharedTopicIds = new ArrayList<>();

        EntityManager entityManager = EntityManager.getInstance();


        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
        Observable.from(fileMessage.shareEntities)
                .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                .filter(shareEntity -> {
                    FormattedEntity entity = entityManager.getEntityById(shareEntity);
                    return entity != EntityManager.UNKNOWN_USER_ENTITY && !entity.isUser();
                })
                .collect(() -> sharedTopicIds, List::add)
                .subscribe();

        return sharedTopicIds;
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean onLoadFromCache(long fileId, long selectMessageId) {
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
    public void getFileDetail(long fileId, boolean isSendAction, boolean showDialog, long selectMessageId) {
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
                    errorMessage = JandiApplication.getContext().getResources().getString(R.string.err_network);
                } else {
                    errorMessage = JandiApplication.getContext().getResources().getString(R.string.err_file_detail);
                }
                view.showToast(errorMessage);
            }
            view.finishOnMainThread();
        } catch (Exception e) {
            view.dismissProgress();
            view.showToast(JandiApplication.getContext().getResources().getString(R.string.err_file_detail));
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
    public void shareMessage(ResMessages.FileMessage fileMessage, long entityIdToBeShared) {
        view.showProgress();
        try {
            fileDetailModel.shareMessage(fileMessage.id, entityIdToBeShared);
            LogUtil.d("success to share message");

            fileDetailModel.trackFileShareSuccess(entityIdToBeShared, fileMessage.id);

            view.dismissProgress();
            view.onShareMessageSucceed(entityIdToBeShared, fileMessage);
            view.showMoveDialog(entityIdToBeShared);
        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileShareFail(errorCode);
            view.dismissProgress();
            view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_share));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            fileDetailModel.trackFileShareFail(-1);
            view.dismissProgress();
            view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_share));
        }
    }

    @Background
    public void unShareMessage(ResMessages.FileMessage fileMessage, long entityIdToBeUnshared) {
        view.showProgress();
        try {
            fileDetailModel.unshareMessage(fileMessage.id, entityIdToBeUnshared);
            LogUtil.d("success to unshare message");

            fileDetailModel.trackFileUnShareSuccess(entityIdToBeUnshared, fileMessage.id);

            view.dismissProgress();

            view.onUnShareMessageSucceed(entityIdToBeUnshared, fileMessage);

        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            fileDetailModel.trackFileUnShareFail(errorCode);
            view.dismissProgress();
            view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_unshare));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            fileDetailModel.trackFileUnShareFail(-1);
            view.dismissProgress();
            view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_unshare));
        }
    }

    @Background
    public void joinAndMove(FormattedEntity entity) {
        view.showProgress();

        try {
            EntityManager entityManager = EntityManager.getInstance();
            fileDetailModel.joinEntity(entity);

            MixpanelMemberAnalyticsClient
                    .getInstance(JandiApplication.getContext(), entityManager.getDistictId())
                    .trackJoinChannel();

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.refreshEntity();

            view.dismissProgress();

            view.moveToMessageListActivity(entity.getId(), entityType, entity.getId(), false);
        } catch (Exception e) {
            e.printStackTrace();
            view.dismissProgress();
        }
    }

    @Background
    public void sendComment(long fileId, String message, List<MentionObject> mentions) {
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
    void sendCommentWithSticker(long fileId, long stickerGroupId, String stickerId, String comment,
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
    public void deleteComment(long fileId, int messageType, long messageId, long feedbackId) {
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
     * @param topicId
     */
    @Background
    public void deleteFile(long fileId, long topicId) {
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

    public void checkSharedEntity(long eventId, ResMessages.FileMessage fileMessage) {
        if (fileMessage == null) {
            return;
        }

        long entityId;
        Iterator<ResMessages.OriginalMessage.IntegerWrapper> iterator = fileMessage.shareEntities.iterator();
        while (iterator.hasNext()) {
            entityId = iterator.next().getShareEntity();

            if (eventId == entityId) {
                view.finishOnMainThread();
                return;
            }
        }
    }

    public void onClickDownload(long fileId, ResMessages.FileMessage fileMessage) {
        if (fileMessage == null) {
            LogUtil.e("FileDetailPresenter", "fileMessage is empty.");
            view.showErrorToast(JandiApplication.getContext()
                    .getResources().getString(R.string.err_download));
            return;
        }

        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.PlaceholderType placeholderType =
                PlaceholderUtil.getPlaceholderType(content.serverUrl, content.icon);

        switch (placeholderType) {
            case Google:
            case Dropbox:
                String photoUrl = ImageUtil.getImageFileUrl(content.fileUrl);
                view.startGoogleOrDropboxFileActivity(photoUrl);
                return;
        }

        downloadFile(ImageUtil.getImageFileUrl(content.fileUrl),
                content.title,
                content.type,
                content.ext,
                fileId);
    }

    public void downloadFile(String url, String fileName, final String fileType, String ext,
                             long fileId) {
        Permissions.getChecker()
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .noPermission(() -> {
                    view.requestPermission(FileDetailActivity.REQ_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                })
                .hasPermission(() -> {
                    downloadFileImpl(url, fileName, fileType, ext, fileId);
                }).check();
    }

    private void downloadFileImpl(String url, String fileName, final String fileType, String ext,
                                  long fileId) {
        DownloadService.start(fileId, url, fileName, ext, fileType);
    }

    @Background
    public void getProfile(long userEntityId) {
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
                                 EditText editText) {

        List<Long> sharedTopicIds = getSharedTopicIds(fileMessage);

        if (mentionControlViewModel == null) {
            mentionControlViewModel = MentionControlViewModel.newInstance(activity,
                    editText,
                    sharedTopicIds,
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);

            ReadyComment readyComment = ReadyCommentRepository.getRepository().getReadyComment(fileMessage.id);
            mentionControlViewModel.setUpMention(readyComment.getText());
            mentionControlViewModel.setOnMentionShowingListener(isShowing -> {
                if (isShowing) {
                    view.dismissMentionButton();
                } else {
                    view.showMentionButton();
                }
            });

        } else {
            mentionControlViewModel.refreshMembers(sharedTopicIds);
        }

        if (mentionControlViewModel.getAllSelectableMembers().size() == 0) {
            view.dismissMentionButton();
        } else {
            view.showMentionButton();
        }
        registClipboardListenerforMention();
    }

    public void registClipboardListenerforMention() {
        removeClipboardListenerforMention();
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
    public void registStarredComment(long messageId) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.registStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, true);
            view.showToast(JandiApplication.getContext().getString(R.string.jandi_message_starred));
            view.modifyStarredInfo(messageId, true);
            EventBus.getDefault().post(new StarredInfoChangeEvent());

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void unregistStarredComment(long messageId) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.unregistStarredMessage(teamId, messageId);
            MessageRepository.getRepository().updateStarred(messageId, false);

            view.modifyStarredInfo(messageId, false);
            view.showToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
            EventBus.getDefault().post(new StarredInfoChangeEvent());

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }

    }

    @Background
    public void unregistStarredFileMessage(long fileId) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.unregistStarredMessage(teamId, fileId);
            MessageRepository.getRepository().updateStarred(fileId, false);

            view.updateFileStarred(false);
            view.showToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
            EventBus.getDefault().post(new StarredInfoChangeEvent());
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Background
    public void registStarredFileMessage(long fileId) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            fileDetailModel.registStarredMessage(teamId, fileId);
            view.updateFileStarred(true);
            view.showToast(JandiApplication.getContext().getString(R.string.jandi_message_starred));
            MessageRepository.getRepository().updateStarred(fileId, true);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }

    }

    public void changeStarredFileMessageState(long fileId, boolean starred) {
        // 클릭 여러번을 막기 위함
        starredPublishSubject.onNext(new FileStarredInfo(fileId, starred));
    }

    public boolean isTeamOwner() {
        return fileDetailModel.isTeamOwner();
    }

    public void onConfigurationChanged() {
        mentionControlViewModel.onConfigurationChanged();
    }

    public void onExportFile(ResMessages.FileMessage fileMessage, ProgressDialog progressDialog) {
        String downloadFilePath = fileDetailModel.getDownloadFilePath(fileMessage.content.title);
        String downloadUrl = fileDetailModel.getDownloadUrl(fileMessage.content.fileUrl);

        fileDetailModel.downloadFile(downloadUrl, downloadFilePath, (downloaded, total) -> {
            progressDialog.setProgress((int) (downloaded * 100 / total));
        }, (e, result) -> {
            progressDialog.dismiss();

            if (e == null && result != null) {
                view.exportIntentFile(result, fileMessage.content.type);
            } else {
                view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
            }
        });
    }

    public void onCopyExternLink(ResMessages.FileMessage fileMessage, boolean isExternalShared) {
        if (!isExternalShared) {
            if (NetworkCheckUtil.isConnected()) {
                enableExternalLink(fileMessage);
            } else {
                view.showCheckNetworkDialog();
            }
        } else {
            // 클립보드에 바로 복사 처리

            removeClipboardListenerforMention();
            StringBuffer externalLink = new StringBuffer(JandiConstantsForFlavors.SERVICE_BASE_URL).append("file/").append(fileMessage.content.externalCode);
            view.copyToClipboard(externalLink.toString());
            view.showToast(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link));
            registClipboardListenerforMention();
        }
    }

    @Background
    void enableExternalLink(ResMessages.FileMessage fileMessage) {
        try {
            view.showProgress();
            long teamId = fileDetailModel.getTeamId();
            ResMessages.FileMessage fileMessage2 = fileDetailModel.enableExternalLink(teamId, fileMessage.id);
            ResMessages.FileContent content = fileMessage2.content;
            fileDetailModel.updateExternalLink(content.fileUrl, content.externalShared, content.externalUrl, content.externalCode);
            view.setExternalShared(content.externalShared);
            view.setFileMessage(fileMessage2);
            StringBuffer externalLink = new StringBuffer(JandiConstantsForFlavors.SERVICE_BASE_URL).append("file/").append(content.externalCode);
            view.copyToClipboard(externalLink.toString());
            view.showToast(JandiApplication.getContext().getResources().getString(R.string.jandi_success_copy_clipboard_external_link));
            view.dismissProgress();
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
            view.dismissProgress();
        }
    }

    @Background
    public void onDisableExternLink(ResMessages.FileMessage fileMessage) {
        try {
            view.showProgress();
            long teamId = fileDetailModel.getTeamId();
            ResMessages.FileMessage fileMessage2 = fileDetailModel.disableExternalLink(teamId, fileMessage.id);
            ResMessages.FileContent content = fileMessage2.content;
            fileDetailModel.updateExternalLink(content.fileUrl, content.externalShared, content.externalUrl, content.externalCode);
            view.setExternalShared(content.externalShared);
            view.setFileMessage(fileMessage2);
            view.showToast(JandiApplication.getContext().getResources().getString(R.string.jandi_success_disable_external_link));
            view.dismissProgress();
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.showErrorToast(JandiApplication.getContext().getString(R.string.jandi_err_unexpected));
            view.dismissProgress();
        }
    }

    public void onMentionClick(int selectionStart, String message) {
        if (fileDetailModel.needSpace(selectionStart, message)) {
            view.inputText(KeyEvent.KEYCODE_SPACE);
        }
        view.inputText(KeyEvent.KEYCODE_AT);
    }

    public interface View {
        void drawFileWriterState(boolean isEnabled);

        void drawFileDetail(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                            boolean isSendAction);

        void loadSuccess(ResMessages.FileMessage fileMessage, List<ResMessages.OriginalMessage> commentMessages,
                         boolean isSendAction, long selectMessageId);

        void showCheckNetworkDialog();

        void showDeleteFileDialog(long fileId);

        void showUserInfoDialog(FormattedEntity user);

        void showMoveDialog(long entityIdToBeShared);

        void showManipulateMessageDialogFragment(ResMessages.OriginalMessage item, boolean isMine);

        void onShareMessageSucceed(long entityIdToBeShared, ResMessages.FileMessage fileMessage);

        void onUnShareMessageSucceed(long entityIdToBeUnshared, ResMessages.FileMessage fileMessage);

        void onDeleteFileSucceed(boolean isOk);

        void onDownloadFileSucceed(File file, String fileType, ResMessages.FileMessage fileMessage,
                                   boolean execute);

        void onGetProfileFailed();

        void setSendButtonSelected();

        void showProgress();

        void dismissProgress();

        void showDownloadProgressDialog(String fileName);

        void dismissDownloadProgressDialog();

        void clearAdapter();

        void showToast(String message);

        void showErrorToast(String message);

        void hideSoftKeyboard();

        void moveToMessageListActivity(long entityId, int entityType, long roomId, boolean isStarred);

        void startGoogleOrDropboxFileActivity(String fileUrl);

        void finishOnMainThread();

        void showKeyboard();

        void showStickerPreview();

        void loadSticker(StickerInfo stickerInfo);

        void dismissStickerPreview();

        void modifyStarredInfo(long messageId, boolean isStarred);

        void updateFileStarred(boolean starred);

        void showUnsharedFileToast();

        void requestPermission(int requestCode, String... permission);

        void copyToClipboard(String externalUrl);

        void setExternalShared(boolean externalShared);

        void exportIntentFile(File result, String type);

        void setFileMessage(ResMessages.FileMessage fileMessage2);

        void dismissMentionButton();

        void showMentionButton();

        void inputText(int keycodeSpace);
    }
}
