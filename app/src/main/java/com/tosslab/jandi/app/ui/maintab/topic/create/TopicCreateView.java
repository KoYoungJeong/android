package com.tosslab.jandi.app.ui.maintab.topic.create;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class TopicCreateView {

    public static final int TITLE_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 300;
    @RootContext
    Activity activity;


    @ViewById(R.id.et_topic_create_title)
    EditText tvTitle;

    @ViewById(R.id.et_topic_create_description)
    EditText tvTopicDescription;

    @ViewById(R.id.img_topic_create_private_check)
    ImageView privateCheckView;

    @ViewById(R.id.img_topic_create_public_check)
    ImageView publicCheckView;

    @ViewById(R.id.tv_topic_create_name_count)
    TextView tvTitleCount;

    @ViewById(R.id.tv_topic_create_description_count)
    TextView tvDescriptionCount;

    private ProgressWheel progressWheel;


    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(activity);
    }

    @AfterViews
    void initView() {
        setTopicType(true);
    }

    public String getTopicTitle() {
        return tvTitle.getText().toString();
    }

    public void setTopicType(boolean isPublic) {
        if (isPublic) {
            publicCheckView.setSelected(true);
            privateCheckView.setSelected(false);

            publicCheckView.setVisibility(View.VISIBLE);
            privateCheckView.setVisibility(View.INVISIBLE);

        } else {
            publicCheckView.setSelected(false);
            privateCheckView.setSelected(true);

            publicCheckView.setVisibility(View.INVISIBLE);
            privateCheckView.setVisibility(View.VISIBLE);

        }
    }

    public boolean isPublicSelected() {
        return publicCheckView.isSelected();
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    public void createTopicFailed(int err_entity_duplicated_name) {
        ColoredToast.showError(activity, activity.getString(err_entity_duplicated_name));

    }

    @UiThread
    public void createTopicSuccess(int teamId, int entityId, String topicTitle, boolean publicSelected) {

        ColoredToast.show(activity, activity.getString(R.string.jandi_message_create_entity, topicTitle));

        int entityType;
        if (publicSelected) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        }

        MessageListV2Activity_.intent(activity)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .teamId(teamId)
                .roomId(entityId)
                .entityType(entityType)
                .entityId(entityId)
                .isFavorite(false)
                .start();

        activity.finish();

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(activity, null);
    }

    public void setTitleCount(int length) {
        tvTitleCount.setText(String.format("%d/%d", length, TITLE_MAX_LENGTH));
    }

    public void setDescriptionCount(int length) {
        tvDescriptionCount.setText(String.format("%d/%d", length, DESCRIPTION_MAX_LENGTH));
    }

    public String getTopicDescriptionText() {
        return tvTopicDescription.getText().toString().trim();
    }
}
