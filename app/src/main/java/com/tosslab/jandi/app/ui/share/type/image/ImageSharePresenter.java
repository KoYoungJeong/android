package com.tosslab.jandi.app.ui.share.type.image;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.iangclifton.android.floatlabel.FloatLabel;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.share.type.adapter.ShareEntityAdapter;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 14..
 */
@EBean
public class ImageSharePresenter {

    @RootContext
    Context context;

    @ViewById(R.id.spinner_share_image_entity)
    Spinner entitySpinner;

    @ViewById(R.id.img_share_image)
    ImageView shareImageView;

    @ViewById(R.id.txt_share_image_title)
    FloatLabel titleText;

    @ViewById(R.id.txt_share_image_comment)
    FloatLabel commentText;


    private ShareEntityAdapter shareEntityAdapter;
    private List<EntityInfo> entities;

    @AfterInject
    void initObject() {
        shareEntityAdapter = new ShareEntityAdapter(context);
    }

    @AfterViews
    void initViews() {

        titleText.getEditText().setImeOptions(EditorInfo.IME_ACTION_NEXT);

        for (EntityInfo entity : entities) {
            shareEntityAdapter.add(entity);
        }

        entitySpinner.setAdapter(shareEntityAdapter);

    }

    public void setEntityInfos(List<EntityInfo> entities) {
        this.entities = entities;
    }

    @UiThread
    public void bindImage(File imagePath) {

        titleText.getEditText().setText(imagePath.getName());

        Glide.with(context)
                .load(imagePath)
                .centerCrop()
                .crossFade()
                .into(shareImageView);
    }

    @UiThread
    public void dismissIncrementProgressDialog(ProgressDialog downloadProgress) {
        if (downloadProgress != null && downloadProgress.isShowing()) {
            downloadProgress.dismiss();
        }
    }

    public EntityInfo getSelectedEntity() {

        return shareEntityAdapter.getItem(entitySpinner.getSelectedItemPosition());

    }

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(context, message);
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(context, message);
    }

    @UiThread
    public void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public String getTitleText() {

        return titleText.getEditText().getText().toString();

    }

    public String getCommentText() {

        return commentText.getEditText().getText().toString();
    }

    @UiThread
    public void moveEntity(int teamId, EntityInfo entity, boolean starredEntity) {

        int entityType;

        if (entity.isPrivateTopic()) {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (entity.isPublicTopic()) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
        }

        MainTabActivity_.intent(context)
                .start();

        MessageListV2Activity_.intent(context)
                .teamId(teamId)
                .entityId(entity.getEntityId())
                .entityType(entityType)
                .isFavorite(starredEntity)
                .start();
    }

}
