package com.tosslab.jandi.app.ui.maintab.topic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
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
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EFragment(R.layout.fragment_entity_popup)
public class EntityMenuDialogFragment extends DialogFragment {

    private static final Logger logger = Logger.getLogger(EntityMenuDialogFragment.class);

    @FragmentArg
    int entityId;

    @ViewById(R.id.btn_entity_popup_starred)
    Button starredButton;

    @ViewById(R.id.btn_entity_popup_leave)
    Button leaveButton;

    @Bean
    EntityMenuDialogModel entityMenuDialogModel;
    private ProgressWheel progressWheel;

    @AfterViews
    void initView() {
        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);
        getDialog().setTitle(entity.getName());
        setStarredButtonText(entity.isStarred);

        progressWheel = new ProgressWheel(getActivity());
        progressWheel.init();

//        if (entity.isPrivateGroup()) {
//            holdLeaveButton();
//        }
    }

    @Background
    void holdLeaveButton() {

        int count = 3;

        String text;
        if (isVisible()) {
            text = String.format("%s (%d)", getString(R.string.jandi_action_leave), count);
            setLeaveButton(text, false);
        }
        while (count != 0) {

            try {
                Thread.sleep(1000);
                if (!isVisible()) {
                    return;
                }
            } catch (InterruptedException e) {
            }

            --count;
            if (isVisible()) {
                text = String.format("%s (%d)", getString(R.string.jandi_action_leave), count);
                setLeaveButton(text, false);
            } else {
                return;
            }
        }

        if (isVisible()) {
            text = getString(R.string.jandi_action_leave);
            setLeaveButton(text, true);
        }

    }

    @UiThread
    void setLeaveButton(String text, boolean enabled) {
        leaveButton.setText(text);
        leaveButton.setEnabled(enabled);
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
                showToast(getString(R.string.jandi_message_no_starred));
            } else {
                entityMenuDialogModel.requestStarred(entityId);
                showToast(getString(R.string.jandi_message_starred));
            }
            entity.isStarred = !entity.isStarred;

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

        if (entity.isPublicTopic()) {
            showProgressWheel();
            leaveEntity(entityId, entity.isPublicTopic());
        } else {
            showPrivateTopicLeaveDialog(entityId, entity.getName());
            dismissOnUiThread();
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
                        leaveEntity(entityId, false);
                    }
                }).create().show();
    }

    @Background
    void leaveEntity(int entityId, boolean publicTopic) {
        try {
            entityMenuDialogModel.requestLeaveEntity(entityId, publicTopic);
            entityMenuDialogModel.refreshEntities();
            EventBus.getDefault().post(new RetrieveTopicListEvent());
        } catch (JandiNetworkException e) {
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
        dismissProgressWheel();
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
