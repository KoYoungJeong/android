package com.tosslab.jandi.app.ui.filedetail;

import android.util.Log;

import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;

@EBean
public class FileDetailPresenter {

    public static final String TAG = FileDetailActivity.TAG;

    @Bean
    FileDetailModel fileDetailModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void init(long fileId, boolean withProgress) {
        if (withProgress) {
            view.showProgress();
        }

        ResFileDetail fileDetail = null;
        boolean isNetworkConnected = fileDetailModel.isNetworkdConneted();
        if (isNetworkConnected) {
            try {
                fileDetail = fileDetailModel.getFileDetailFromServer(fileId);
                fileDetailModel.saveFileDetailInfo(fileDetail);
                fileDetail = fileDetailModel.getFileDetailFromCache(fileId);
            } catch (RetrofitError e) {
                LogUtil.e(TAG, Log.getStackTraceString(e));
                fileDetail = fileDetailModel.getFileDetailFromCache(fileId);
            }
        } else {
            fileDetail = fileDetailModel.getFileDetailFromCache(fileId);
        }

        if (fileDetail == null || fileDetail.messageCount <= 0) {
            if (withProgress) {
                view.dismissProgress();
            }

            if (isNetworkConnected) {
                view.showUnexpectedErrorToast();
            } else {
                view.showCheckNetworkDialog();
            }
            return;
        }

        List<ResMessages.OriginalMessage> messages = fileDetail.messageDetails;
        for (int i = 0; i < messages.size(); i++) {
            ResMessages.OriginalMessage message = messages.get(i);
            String name = message.getClass().getName();
            LogUtil.d("tony", name);
            if (message instanceof ResMessages.FileMessage) {
                LogUtil.e("tony", "position - " + i);
            }
        }
        // 파일의 상세정보는 API 로 부터 받아온 리스트의 마지막에 있다.
        int fileDetailPosition = messages.size() - 1;
        ResMessages.FileMessage fileMessage =
                (ResMessages.FileMessage) messages.get(fileDetailPosition);
        if (fileMessage.content != null) {
            boolean isImageFile = fileDetailModel.isImageFile(fileMessage.content);
            boolean isDeletedFile = fileDetailModel.isDeletedFile(fileMessage.status);
            boolean isMyFile = fileDetailModel.isMyFile(fileMessage.writerId);
            boolean isExternalSharedFile = fileMessage.content.externalShared;
            List<Long> sharedTopicIds = fileDetailModel.getSharedTopicIds(fileMessage);
            view.bindFileDetail(fileMessage, sharedTopicIds,
                    isMyFile, isDeletedFile, isImageFile, isExternalSharedFile);
            messages.remove(fileDetailPosition);
        }

        fileDetailModel.sortByDate(messages);
        view.bindComments(messages);

        if (withProgress) {
            view.dismissProgress();
        }
    }

    @Background
    public void sendCommentWithSticker(long fileId, long stickerGroupId,
                                       String stickerId, String comment,
                                       List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageCommentWithSticker(
                    fileId, stickerGroupId, stickerId, comment, mentions);

            init(fileId, false);

            view.dismissProgress();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
        }
    }

    @Background
    public void sendComment(long fileId, String message, List<MentionObject> mentions) {
        view.showProgress();
        try {
            fileDetailModel.sendMessageComment(fileId, message, mentions);
            view.dismissProgress();

            init(fileId, false);
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
            view.dismissProgress();
        }
    }

    public void onShareAction(int entityId, ResMessages.FileMessage fileMessage) {

    }

    public interface View {
        void showProgress();

        void dismissProgress();

        void showUnexpectedErrorToast();

        void showCheckNetworkDialog();

        void showKeyboard();

        void hideKeyboard();

        void refreshClipboardListenerForMention();

        void bindFileDetail(ResMessages.FileMessage fileMessage, List<Long> sharedTopicIds,
                            boolean isMyFile, boolean isDeletedFile,
                            boolean isImageFile, boolean isExternalShared);

        void bindComments(List<ResMessages.OriginalMessage> fileComments);


    }

}
