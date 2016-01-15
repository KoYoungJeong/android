package com.tosslab.jandi.app.ui.maintab.topic.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.entities.TopicFolderMoveCallEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EFragment
public class EntityMenuDialogFragment extends DialogFragment {

    @FragmentArg
    int entityId;

    @FragmentArg
    int folderId;

    TextView btnStarred;

    TextView btnLeave;

    TextView tvTitle;

    TextView btnMoveFolder;

    TextView btnNotification;

    @Bean
    EntityMenuDialogModel entityMenuDialogModel;

    @Bean
    EntityClientManager entityClientManager;
    private ProgressWheel progressWheel;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    void initView() {
        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);
        tvTitle.setText(entity.getName());

        boolean isBot = entityMenuDialogModel.isBot(entityId);
        boolean isDirectMessage = entity.isUser() || isBot;
        if (isDirectMessage) {
            if (!entity.isEnabled() || isBot) {
                btnStarred.setVisibility(View.GONE);
            }
            btnMoveFolder.setVisibility(View.GONE);
            btnNotification.setVisibility(View.GONE);
        } else {
            btnNotification.setVisibility(View.VISIBLE);

            final boolean isTopicPushOn = entity.isTopicPushOn;

            String notificationText = getActivity().getResources().getString(R.string.jandi_notification_off);
            if (!isTopicPushOn) {
                notificationText = getActivity().getResources().getString(R.string.jandi_notification_on);
            }
            btnNotification.setText(notificationText);

            btnNotification.setOnClickListener(v -> {
                entityMenuDialogModel.updateNotificationOnOff(entityId, !isTopicPushOn);
                dismiss();
            });
        }

        setStarredButtonText(entity.isStarred);

        if (entityMenuDialogModel.isDefaultTopic(entityId)) {
            btnLeave.setVisibility(View.GONE);
        } else {
            btnLeave.setVisibility(View.VISIBLE);
        }

        progressWheel = new ProgressWheel(getActivity());
    }

    public void setStarredButtonText(boolean isStarred) {
        if (isStarred) {
            btnStarred.setText(R.string.jandi_unstarred);
        } else {
            btnStarred.setText(R.string.jandi_starred);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate((R.layout.fragment_entity_popup), null);

        btnStarred = (TextView) view.findViewById(R.id.btn_entity_popup_starred);
        btnLeave = (TextView) view.findViewById(R.id.btn_entity_popup_leave);
        tvTitle = (TextView) view.findViewById(R.id.tv_popup_title);
        btnMoveFolder = (TextView) view.findViewById(R.id.btn_entity_popup_move_folder);
        btnNotification = (TextView) view.findViewById(R.id.btn_entity_popup_notification);

        btnStarred.setOnClickListener(v -> onStarredClick());
        btnLeave.setOnClickListener(v -> onLeaveClick());
        btnMoveFolder.setOnClickListener(v -> onMoveFolderClick());

        view.findViewById(R.id.btn_entity_popup_cancel).setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setView(view)
                .create();
    }

    void onStarredClick() {
        showProgressWheel();
        requestStarred();

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        AnalyticsValue.Screen category = entity.isUser() ? AnalyticsValue.Screen.MessageTab : AnalyticsValue.Screen.TopicsTab;
        AnalyticsValue.Action action = entity.isStarred ? AnalyticsValue.Action.TopicSubMenu_Unstar : AnalyticsValue.Action.TopicSubMenu_Star;
        AnalyticsUtil.sendEvent(category, action);
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

            dismissProgressWheel();

            if (entity.isStarred) {
                showToast(getString(R.string.jandi_message_starred));
            } else {
                showToast(getString(R.string.jandi_message_no_starred));
            }

            EventBus.getDefault().post(new RetrieveTopicListEvent());

            dismiss();
        } catch (RetrofitError e) {
            e.printStackTrace();
            dismissProgressWheel();
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
            dismissProgressWheel();
            dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismiss() {
        super.dismiss();
    }

    @UiThread
    void showToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    void onLeaveClick() {
        FormattedEntity entity = entityMenuDialogModel.getEntity(entityId);

        boolean bot = entityMenuDialogModel.isBot(entityId);
        if (entity.isPublicTopic() || entity.isUser() || bot) {
            showProgressWheel();
            leaveEntity(entityId, entity.isPublicTopic(), entity.isUser() || bot);
        } else {
            showPrivateTopicLeaveDialog(entityId, entity.getName());
        }
    }

    private void showPrivateTopicLeaveDialog(final int entityId, String entityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
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

            if (entity != EntityManager.UNKNOWN_USER_ENTITY) {
                entityMenuDialogModel.leaveEntity(entity.isPublicTopic());
            }

            if (!isUser) {
                entityMenuDialogModel.requestLeaveEntity(entityId, publicTopic);
            } else {
                int memberId = EntityManager.getInstance().getMe().getId();
                entityMenuDialogModel.requestDeleteChat(memberId, entityId);
            }
            entityMenuDialogModel.refreshEntities();

            if (entity.isUser()) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.TopicSubMenu_Leave);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicSubMenu_Leave);
            }

            dismissProgressWheel();

            EventBus.getDefault().post(new RetrieveTopicListEvent());

            dismiss();
        } catch (RetrofitError e) {
            showErrorToast(getString(R.string.err_entity_leave));
            e.printStackTrace();

            dismissProgressWheel();
            dismiss();
        } catch (Exception e) {
            showErrorToast(getString(R.string.err_entity_leave));
            e.printStackTrace();

            dismissProgressWheel();
            dismiss();
        }
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

    void onMoveFolderClick() {
        TopicFolderMoveCallEvent topicFolderMoveCallEvent = new TopicFolderMoveCallEvent();
        topicFolderMoveCallEvent.setTopicId(entityId);
        topicFolderMoveCallEvent.setFolderId(folderId);
        EventBus.getDefault().post(topicFolderMoveCallEvent);

        dismiss();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicSubMenu_Move);
    }
}
