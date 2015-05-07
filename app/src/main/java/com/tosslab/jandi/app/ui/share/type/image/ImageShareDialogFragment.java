package com.tosslab.jandi.app.ui.share.type.image;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.model.ShareModel;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EFragment(R.layout.fragment_share_image)
public class ImageShareDialogFragment extends DialogFragment {


    private static final Logger logger = Logger.getLogger(ImageShareDialogFragment.class);

    @FragmentArg
    String uriString;


    @Bean
    ShareModel shareModel;

    @Bean
    ImageSharePresenter imageSharePresenter;

    private File imageFile;

    @AfterInject
    void initObject() {

        List<EntityInfo> entities = shareModel.getEntityInfos();

        imageSharePresenter.setEntityInfos(entities);
    }

    @AfterViews
    void initView() {
        String imagePath = shareModel.getImagePath(uriString);

        if (TextUtils.isEmpty(imagePath)) {
            getActivity().finish();
            return;
        }

        if (imagePath.startsWith("https://") || imagePath.startsWith("http://")) {
            String downloadDir = GoogleImagePickerUtil.getDownloadPath();
            String downloadName = GoogleImagePickerUtil.getWebImageName();
            downloadImage(imagePath, downloadDir, downloadName);
        } else {
            this.imageFile = new File(imagePath);
            imageSharePresenter.bindImage(this.imageFile);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.jandi_share_to_jandi);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }

    @Background
    void downloadImage(String path, String downloadDir, String downloadName) {

        imageSharePresenter.showProgressBar();
        try {
            Log.d("INFO", "Download Path " + downloadDir + "/" + downloadName);

            File file = GoogleImagePickerUtil.downloadFile(getActivity(), null, path, downloadDir, downloadName);
            Log.d("INFO", "Downloaded Path " + file.getAbsolutePath());
            imageFile = file;
            imageSharePresenter.bindImage(file);
        } catch (Exception e) {
        } finally {
            imageSharePresenter.dismissPrgoressBar();
        }
    }

    @Click(R.id.btn_share_text_send)
    void onSendClick() {

        EntityInfo selectedEntity = imageSharePresenter.getSelectedEntity();

        ProgressDialog uploadProgress = imageSharePresenter.getUploadProgress(getActivity(), imageFile.getParentFile().getAbsolutePath(), imageFile.getName());

        String titleText = imageSharePresenter.getTitleText().trim();
        String commentText = imageSharePresenter.getCommentText().trim();
        uploadFile(selectedEntity, imageFile, titleText, commentText, uploadProgress);
    }

    @Background
    void uploadFile(EntityInfo selectedEntity, File imageFile, String titleText, String commentText, ProgressDialog uploadProgress) {

        boolean isPublicTopic = selectedEntity.isPublicTopic();

        try {
            JsonObject result = shareModel.uploadFile(imageFile, titleText, commentText, selectedEntity, uploadProgress, isPublicTopic);
            if (result.get("code") == null) {

                logger.error("Upload Success : " + result);
                imageSharePresenter.showSuccessToast(getString(R.string.jandi_file_upload_succeed));
                int entityType = 0;
                shareModel.trackUploadingFile(entityType, result);
            } else {
                logger.error("Upload Fail : Result : " + result);
                imageSharePresenter.showFailToast(getString(R.string.err_file_upload_failed));
            }

            finishOnUiThread();
        } catch (Exception e) {
            logger.error("Upload Error : ", e);
            imageSharePresenter.showFailToast(getString(R.string.err_file_upload_failed));
        } finally {
            imageSharePresenter.dismissDialog(uploadProgress);
        }

    }

    @UiThread
    void finishOnUiThread() {
        getActivity().finish();
    }


}
