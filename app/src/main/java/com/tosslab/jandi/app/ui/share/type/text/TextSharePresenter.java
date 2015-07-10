package com.tosslab.jandi.app.ui.share.type.text;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Spinner;

import com.iangclifton.android.floatlabel.FloatLabel;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.share.type.adapter.ShareEntityAdapter;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class TextSharePresenter {

    @ViewById(R.id.spinner_share_text_entity)
    Spinner entitySpinner;

    @ViewById(R.id.txt_share_text)
    FloatLabel floatLabel;

    @RootContext
    Context context;

    private String subject;
    private String text;
    private List<EntityInfo> entityInfos;
    private ShareEntityAdapter shareEntityAdapter;
    private ProgressWheel progressWheel;

    public void setText(String subject, String text) {

        this.subject = subject;
        this.text = text;
        if (floatLabel != null) {
            floatLabel.getEditText().setText(text);
        }
    }

    void initObject(Activity activity) {
        shareEntityAdapter = new ShareEntityAdapter(context);
        progressWheel = new ProgressWheel(activity);
    }

    @AfterViews
    void initView() {
        EditText editText = floatLabel.getEditText();
        editText.setMaxLines(4);

        StringBuffer buffer = new StringBuffer();
        if (!TextUtils.isEmpty(subject)) {
            buffer.append(subject).append("\n");
        }

        if (!TextUtils.isEmpty(text)) {
            buffer.append(text);
        }

        editText.setText(text);

        for (EntityInfo entityInfo : entityInfos) {
            shareEntityAdapter.add(entityInfo);
        }

        entitySpinner.setAdapter(shareEntityAdapter);

    }

    public void setEntityInfos(List<EntityInfo> entityInfos) {
        this.entityInfos = entityInfos;
    }

    public String getMessageText() {
        return floatLabel.getEditText().getText().toString();
    }

    public EntityInfo getSelectedEntityInfo() {
        return shareEntityAdapter.getItem(entitySpinner.getSelectedItemPosition());
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        progressWheel.show();
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    public void showErrorMessage(String message) {
        ColoredToast.showError(context, message);
    }

    @UiThread
    public void showSuccessMessage(String message) {
        ColoredToast.show(context, message);
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
                .roomId(entityType != JandiConstants.TYPE_DIRECT_MESSAGE ? entityType : entity.getEntityId())
                .entityId(entity.getEntityId())
                .entityType(entityType)
                .isFavorite(starredEntity)
                .start();
    }
}
