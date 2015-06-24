package com.tosslab.jandi.app.ui.maintab.topic.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EFragment(R.layout.fragment_entity_popup)
public class EntityMenuDialogFragment extends DialogFragment {

    @FragmentArg
    int entityId;

    @ViewById(R.id.btn_entity_popup_starred)
    Button starredButton;

    @ViewById(R.id.btn_entity_popup_leave)
    Button leaveButton;

    @Bean
    EntityMenuDialogModel entityMenuDialogModel;

    @Bean
    EntityClientManager entityClientManager;
    private ProgressWheel progressWheel;

    @AfterViews
    void initView() {
        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);
        getDialog().setTitle(entity.getName());

        if (entity.isUser()) {
            if (!TextUtils.equals(entity.getUser().status, "enabled")) {
                starredButton.setVisibility(View.GONE);
            }
        }

        setStarredButtonText(entity.isStarred);

        if (entityMenuDialogModel.isDefaultTopic(entityId)) {
            leaveButton.setVisibility(View.GONE);
        } else {
            leaveButton.setVisibility(View.VISIBLE);
        }

        progressWheel = new ProgressWheel(getActivity());
        progressWheel.init();

    }

    public void setStarredButtonText(boolean isStarred) {
        if (isStarred) {
            starredButton.setText(R.string.jandi_unstarred);
        } else {
            starredButton.setText(R.string.jandi_starred);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }


    @Click(R.id.btn_entity_popup_starred)
    void onStarredClick() {
        showProgressWheel();
        requestStarred();

    }

    @Background
    void requestStarred() {
        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);
        try {
            if (entity.isStarred) {
                entityMenuDialogModel.requestUnstarred(entityId);
            } else {
                entityMenuDialogModel.requestStarred(entityId);
            }
            entity.isStarred = !entity.isStarred;

            entityMenuDialogModel.refreshEntities();

            if (entity.isStarred) {
                showToast(getString(R.string.jandi_message_starred));
            } else {
                showToast(getString(R.string.jandi_message_no_starred));
            }

            EventBus.getDefault().post(new RetrieveTopicListEvent());

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        } finally {
            dismissProgressWheel();
        }

        dismissOnUiThread();

    }

    @UiThread
    void dismissOnUiThread() {
        if (isVisible()) {
            dismiss();
        }
    }

    @UiThread
    void showToast(String message) {
        ColoredToast.show(getActivity(), message);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(getActivity(), message);
    }

    @Click(R.id.btn_entity_popup_leave)
    void onLeaveClick() {

        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);

        if (entity.isPublicTopic() || entity.isUser()) {
            showProgressWheel();
            leaveEntity(entityId, entity.isPublicTopic(), entity.isUser());
        } else {
            showPrivateTopicLeaveDialog(entityId, entity.getName());
        }
    }

    private void showPrivateTopicLeaveDialog(final int entityId, String entityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(entityName)
                .setMessage(R.string.jandi_message_leave_private_topic)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressWheel();
                        leaveEntity(entityId, false, false);
                    }
                }).create().show();
    }

    @Background
    void leaveEntity(int entityId, boolean publicTopic, boolean isUser) {
        try {
            FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);

            if (entity != null) {
                entityMenuDialogModel.leaveEntity(entity.isPublicTopic());
            }

            if (!isUser) {
                entityMenuDialogModel.requestLeaveEntity(entityId, publicTopic);
            } else {
                int memberId = EntityManager.getInstance(getActivity()).getMe().getId();
                entityMenuDialogModel.requestDeleteChat(memberId, entityId);
            }
            entityMenuDialogModel.refreshEntities();

            EventBus.getDefault().post(new RetrieveTopicListEvent());
        } catch (JandiNetworkException e) {
            showErrorToast(getString(R.string.err_entity_leave));
            e.printStackTrace();
        } catch (Exception e) {
            showErrorToast(getString(R.string.err_entity_leave));
            e.printStackTrace();
        } finally {
            dismissProgressWheel();
            dismissOnUiThread();
        }
    }

    @Click(R.id.btn_entity_popup_close)
    void onCloseClick() {
        dismiss();
    }

    @UiThread
    void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }
}
