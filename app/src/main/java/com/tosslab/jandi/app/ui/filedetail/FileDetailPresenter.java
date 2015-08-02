package com.tosslab.jandi.app.ui.filedetail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;

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

    public List<Integer> getSharedTopicIds(Context context, ResMessages.OriginalMessage fileDetail) {
        List<Integer> sharedTopicIds = new ArrayList<>();

        EntityManager entityManager = EntityManager.getInstance(context);

        for (int entity : fileDetail.shareEntities) {
            FormattedEntity formattedEntity = entityManager.getEntityById(entity);
            if (formattedEntity.isPublicTopic() || formattedEntity.isPrivateGroup()) {
                sharedTopicIds.add(formattedEntity.getId());
            }
        }
        return sharedTopicIds;
    }

    public void setView(View view) {
        this.view = view;
    }

    /**
     * *********************************************************
     * 파일 상세 출력 관련
     * **********************************************************
     */
    @Background
    public void getFileDetail(int fileId, boolean isSendAction, boolean showDialog) {
        LogUtil.d("try to get file detail having ID, " + fileId);

        if (showDialog) {
            view.showProgress();
        }
        try {
            ResFileDetail resFileDetail = fileDetailModel.getFileDetailInfo(fileId);

            for (ResMessages.OriginalMessage messageDetail : resFileDetail.messageDetails) {
                if (messageDetail instanceof ResMessages.FileMessage) {
                    fileDetailModel.setFileMessage((ResMessages.FileMessage) messageDetail);
                    break;
                }
            }

            Collections.sort(resFileDetail.messageDetails,
                    (lhs, rhs) -> lhs.createTime.compareTo(rhs.createTime));

            view.dismissProgress();

            view.onGetFileDetailSucceed(resFileDetail, isSendAction);

            boolean enableUserFromUploader = fileDetailModel.isEnableUserFromUploder(resFileDetail);
            view.drawFileWriterState(enableUserFromUploader);
        } catch (RetrofitError e) {
            LogUtil.e("fail to get file detail.", e);
            view.dismissProgress();
            String errorMessage;
            if (e.getResponse() != null
                    && e.getResponse().getStatus() == 403) {
                errorMessage = activity.getResources().getString(R.string.jandi_unshared_message);
            } else {
                if (e.getCause() instanceof ConnectionNotFoundException) {
                    errorMessage = activity.getResources().getString(R.string.err_network);
                } else {
                    errorMessage = activity.getResources().getString(R.string.err_file_detail);
                }
            }
            view.showToast(errorMessage);
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
        boolean isMine = fileDetailModel.isMyComment(item.writerId);
        view.showManipulateMessageDialogFragment(item, isMine);
    }

    public void onClickShare() {
        final List<FormattedEntity> unSharedEntities = fileDetailModel.getUnsharedEntities();
        view.initShareListDialog(unSharedEntities);
    }

    public void onClickUnShare() {
        final List<Integer> shareEntities = fileDetailModel.getFileMessage().shareEntities;
        view.initUnShareListDialog(shareEntities);
    }

    @Background
    public void shareMessage(int fileId, int entityIdToBeShared) {
        view.showProgress();
        try {
            fileDetailModel.shareMessage(fileId, entityIdToBeShared);
            LogUtil.d("success to share message");
            view.dismissProgress();
            view.onShareMessageSucceed(entityIdToBeShared, fileDetailModel.getFileMessage());
            view.showMoveDialog(entityIdToBeShared);
        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_share));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
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

            view.dismissProgress();

            view.onUnShareMessageSucceed(entityIdToBeUnshared, fileDetailModel.getFileMessage());

        } catch (RetrofitError e) {
            LogUtil.e("fail to send message", e);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_unshare));
        } catch (Exception e) {
            LogUtil.e("fail to send message", e);
            view.dismissProgress();
            view.showErrorToast(activity.getResources().getString(R.string.err_unshare));
        }
    }

    @Background
    public void joinAndMove(FormattedEntity entityId) {
        view.showProgress();

        try {
            EntityManager entityManager = EntityManager.getInstance(activity);
            fileDetailModel.joinEntity(entityId);

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, entityManager.getDistictId())
                    .trackJoinChannel();

            int entityType = JandiConstants.TYPE_PUBLIC_TOPIC;

            fileDetailModel.refreshEntity();

            view.dismissProgress();

            view.moveToMessageListActivity(entityId.getId(), entityType, entityManager.getTeamId(), false);
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

            getFileDetail(fileId, true, true);
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
    void sendCommentWithSticker(int fileId, int stickerGroupId, String stickerId, String comment, List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageCommentWithSticker(fileId, stickerGroupId, stickerId, comment, mentions);

            view.dismissProgress();

            getFileDetail(fileId, true, true);
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

            getFileDetail(fileId, false, true);
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
    public void deleteFile(int fileId) {
        view.showProgress();
        try {
            fileDetailModel.deleteFile(fileId);
            LogUtil.d("success to delete file");
            view.dismissProgress();
            view.onDeleteFileSucceed(true);
        } catch (RetrofitError e) {
            LogUtil.e("delete file failed", e);
            view.dismissProgress();
            view.onDeleteFileSucceed(false);
        } catch (Exception e) {
            view.dismissProgress();
            view.onDeleteFileSucceed(false);
        }
    }

    public void checkSharedEntity(int eventId) {
        final ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage();
        if (fileMessage == null) {
            return;
        }
        int size = fileMessage.shareEntities.size();

        int entityId;
        for (int idx = 0; idx < size; ++idx) {
            entityId = fileMessage.shareEntities.get(idx);

            if (eventId == entityId) {
                view.finishOnMainThread();
                return;
            }
        }
    }

    @Background
    public void onClickDownload(ProgressDialog progressDialog) {
        ResMessages.FileMessage fileMessage = fileDetailModel.getFileMessage();
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

        String fileName = content.fileUrl.replace(" ", "%20");

        view.showDownloadProgressDialog(fileName);

        downloadFile(BitmapUtil.getFileUrl(content.fileUrl), content.name, content.type, progressDialog);
    }

    @Background
    public void downloadFile(String url, String fileName, final String fileType, ProgressDialog progressDialog) {
        try {
            File result = fileDetailModel.download(url, fileName, fileType, progressDialog);

            if (fileDetailModel.isMediaFile(fileType)) {
                fileDetailModel.addGallery(result, fileType);
            }

            view.dismissDownloadProgressDialog();
            view.onDownloadFileSucceed(result, fileType, fileDetailModel.getFileMessage());
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

        List<Integer> sharedTopicIds = getSharedTopicIds(
                activity.getApplicationContext(), fileMessage);

        if (mentionControlViewModel == null) {
            mentionControlViewModel = new MentionControlViewModel(activity,
                    searchMemberListView, editText, fileCommentListView, sharedTopicIds);
        }
        mentionControlViewModel.clear();
    }

    public ResultMentionsVO getMentionInfo() {
        return mentionControlViewModel.getMentionInfoObject();
    }

    public MentionControlViewModel getMentionControlViewModel() {
        return mentionControlViewModel;
    }

    public void registStarredFile(int teamId, int messageId) {
        fileDetailModel.registStarredFile(teamId, messageId);
    }

    public void unregistStarredFile(int teamId, int messageId) {
        fileDetailModel.unregistStarredFile(teamId, messageId);
    }

    public interface View {
        void drawFileWriterState(boolean isEnabled);

        void drawFileDetail(ResFileDetail resFileDetail, boolean isSendAction);

        void onGetFileDetailSucceed(ResFileDetail resFileDetail, boolean isSendAction);

        void showDeleteFileDialog(int fileId);

        void showUserInfoDialog(FormattedEntity user);

        void showMoveDialog(int entityIdToBeShared);

        void showManipulateMessageDialogFragment(ResMessages.OriginalMessage item, boolean isMine);

        void initShareListDialog(List<FormattedEntity> unSharedEntities);

        void initUnShareListDialog(List<Integer> shareEntitiesIds);

        void onShareMessageSucceed(int entityIdToBeShared, ResMessages.FileMessage fileMessage);

        void onUnShareMessageSucceed(int entityIdToBeUnshared, ResMessages.FileMessage fileMessage);

        void onDeleteFileSucceed(boolean isOk);

        void onDownloadFileSucceed(File file, String fileType, ResMessages.FileMessage fileMessage);

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

        void moveToMessageListActivity(int entityId, int entityType, int teamId, boolean isStarred);

        void startGoogleOrDropboxFileActivity(String fileUrl);

        void finishOnMainThread();

        void showKeyboard();

        void showStickerPreview();

        void loadSticker(StickerInfo stickerInfo);

        void dismissStickerPreview();


    }
}


