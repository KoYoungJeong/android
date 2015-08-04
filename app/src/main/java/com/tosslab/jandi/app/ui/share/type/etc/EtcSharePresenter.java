package com.tosslab.jandi.app.ui.share.type.etc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.model.ShareModel;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Steve SeongUg Jung on 15. 8. 4..
 */
@EBean
public class EtcSharePresenter {

    @Bean
    ShareModel shareModel;
    private View view;

    public void onInitObject() {
        List<EntityInfo> entities = shareModel.getEntityInfos();

        view.setEntityInfos(entities);

    }

    public void setView(View view) {
        this.view = view;
    }

    public void onInitFile(String uriString) {


        String imagePath = shareModel.getImagePath(uriString);
        if (TextUtils.isEmpty(imagePath)) {
            view.finishOnMainThread();
            return;
        }

        if (imagePath.startsWith("https://") || imagePath.startsWith("http://")) {
            view.finishOnMainThread();
            return;
        }

        view.setTitle(new File(imagePath).getName());
    }

    public void onSendFile(Activity activity, EntityInfo selectedEntity, String title, String comment,
                           String uriString) {
        String filePath = shareModel.getImagePath(uriString);

        File uploadFile = new File(filePath);

        ProgressDialog uploadProgress = view.getUploadProgress(uploadFile.getParentFile().getAbsolutePath(), uploadFile.getName());

        uploadFile(activity, selectedEntity, uploadFile, title, comment, uploadProgress);
    }

    @Background
    void uploadFile(Activity activity, EntityInfo selectedEntity, File imageFile, String titleText, String commentText, ProgressDialog uploadProgress) {

        boolean isPublicTopic = selectedEntity.isPublicTopic();

        try {

            JsonObject result = shareModel.uploadFile(imageFile, titleText, commentText, selectedEntity, uploadProgress, isPublicTopic);
            if (result.get("code") == null) {

                view.showSuccessToast(activity.getString(R.string.jandi_file_upload_succeed));
                int entityType = 0;
                shareModel.trackUploadingFile(entityType, result);
            } else {
                view.showFailToast(activity.getString(R.string.err_file_upload_failed));
            }

            view.finishOnMainThread();
        } catch (ExecutionException e) {
            if (activity != null) {
                view.showFailToast(activity.getString(R.string.jandi_canceled));
            }
        } catch (Exception e) {
            view.showFailToast(activity.getString(R.string.err_file_upload_failed));
        } finally {
            view.dismissDialog(uploadProgress);
        }

    }

    interface View {

        void setEntityInfos(List<EntityInfo> entities);

        void finishOnMainThread();

        ProgressDialog getUploadProgress(String absolutePath, String name);

        void showSuccessToast(String message);

        void showFailToast(String message);

        void dismissDialog(ProgressDialog uploadProgress);

        void setTitle(String title);
    }
}
