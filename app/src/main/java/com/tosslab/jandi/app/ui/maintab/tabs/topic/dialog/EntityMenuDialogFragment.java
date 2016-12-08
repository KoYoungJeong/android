package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog.model.EntityMenuDialogModel;
import com.tosslab.jandi.app.ui.settings.main.SettingsActivity;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity;
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


/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EFragment
public class EntityMenuDialogFragment extends DialogFragment {

    @FragmentArg
    long entityId;

    @FragmentArg
    long roomId;

    @FragmentArg
    long folderId;

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

        Room room = TeamInfoLoader.getInstance().getRoom(roomId);

        if (room == null) {
            dismiss();
            return;
        }

        tvTitle.setText(TeamInfoLoader.getInstance().getName(entityId));

        boolean isDirectMessage = roomId != entityId;
        if (isDirectMessage) {
            if (!TeamInfoLoader.getInstance().isEnabled(entityId)) {
                btnStarred.setVisibility(View.GONE);
            }
            btnMoveFolder.setVisibility(View.GONE);
            btnNotification.setVisibility(View.GONE);
        } else {
            btnNotification.setVisibility(View.VISIBLE);

            final boolean isTopicPushOn = TeamInfoLoader.getInstance().isPushSubscribe(entityId);

            String notificationText = getActivity().getResources().getString(R.string.jandi_notification_off);
            if (!isTopicPushOn) {
                notificationText = getActivity().getResources().getString(R.string.jandi_notification_on);
            }
            btnNotification.setText(notificationText);

            btnNotification.setOnClickListener(v -> {
                if (!entityMenuDialogModel.isGlobalPushOff() && !isTopicPushOn) {
                    showGlobalPushSetupDialog();
                } else {
                    entityMenuDialogModel.updateNotificationOnOff(entityId, !isTopicPushOn);
                }
                dismiss();
            });
        }

        setStarredButtonText(TeamInfoLoader.getInstance().isStarred(roomId));

        Level level = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).getLevel();
        if (entityMenuDialogModel.isDefaultTopic(entityId) || level == Level.Guest) {
            btnLeave.setVisibility(View.GONE);
        } else {
            btnLeave.setVisibility(View.VISIBLE);
        }

        progressWheel = new ProgressWheel(getActivity());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showGlobalPushSetupDialog() {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.jandi_explain_global_push_off)
                .setNegativeButton(R.string.jandi_close, null)
                .setPositiveButton(R.string.jandi_go_to_setting, (dialog, which) -> {
                    movePushSettingActivity();
                })
                .create()
                .show();
    }

    private void movePushSettingActivity() {
        SettingsActivity.startActivity(getActivity());
        startActivity(new Intent(getActivity(), SettingPushActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
    }

    @Background
    void requestStarred() {
        boolean starred = TeamInfoLoader.getInstance().isStarred(roomId);

        try {
            if (starred) {
                entityMenuDialogModel.requestUnstarred(entityId);
            } else {
                entityMenuDialogModel.requestStarred(entityId);
            }
            boolean isUser = roomId != entityId;
            if (isUser) {
                HumanRepository.getInstance().updateStarred(entityId, !starred);
            } else {
                TopicRepository.getInstance().updateStarred(roomId, !starred);
            }
            TeamInfoLoader.getInstance().refresh();

            dismissProgressWheel();

            if (!starred) {
                showToast(getString(R.string.jandi_message_starred));
            } else {
                showToast(getString(R.string.jandi_message_no_starred));
            }

            EventBus.getDefault().post(new RetrieveTopicListEvent());

            dismiss();

            starred = TeamInfoLoader.getInstance().isStarred(roomId);
            AnalyticsValue.Screen category = roomId !=
                    entityId ? AnalyticsValue.Screen.MessageTab : AnalyticsValue.Screen.TopicsTab;
            AnalyticsValue.Action action = AnalyticsValue.Action.TopicSubMenu_Star;
            AnalyticsValue.Label label = starred ? AnalyticsValue.Label.Star : AnalyticsValue.Label.Unstar;
            AnalyticsUtil.sendEvent(category, action, label);
        } catch (RetrofitException e) {
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onLeaveClick() {
        long myId = TeamInfoLoader.getInstance().getMyId();
        int memberCount = TeamInfoLoader.getInstance().getTopicMemberCount(entityId);
        boolean isPublicTopic = TeamInfoLoader.getInstance().isPublicTopic(entityId);
        boolean isTopicOwner = TeamInfoLoader.getInstance().isTopicOwner(entityId, myId);
        boolean isJandiBot = entityMenuDialogModel.isJandiBot(entityId);
        String name = TeamInfoLoader.getInstance().getName(entityId);

        if (roomId == entityId // 토픽이고
                && isTopicOwner // 내가 토픽 오너
                && memberCount > 1 // 1명초과 토픽에 있는 경우
                ) {
            onAssignToTopicOwner(name);
        } else {
            onLeaveEntity(name, isPublicTopic, roomId != entityId, isJandiBot);
        }

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onLeaveEntity(String userName, boolean isPublicTopic, boolean isUser, boolean isBot) {
        if (isPublicTopic || isUser || isBot) {
            showProgressWheel();
            leaveEntity(entityId, isPublicTopic, isUser || isBot);
        } else {
            showPrivateTopicLeaveDialog(entityId, userName);
        }
    }

    private void onAssignToTopicOwner(String topicName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(topicName);
        builder.setMessage(R.string.jandi_need_to_assign_topic_owner);
        builder.setPositiveButton(R.string.jandi_confirm, null);
        builder.create().show();
    }

    private void showPrivateTopicLeaveDialog(final long entityId, String entityName) {
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
    void leaveEntity(long entityId, boolean publicTopic, boolean isUser) {
        try {

            if (!isUser) {
                entityMenuDialogModel.requestLeaveEntity(entityId, publicTopic);
            } else {
                long memberId = TeamInfoLoader.getInstance().getMyId();
                entityMenuDialogModel.requestDeleteChat(memberId, entityId);
            }
            if (!isUser) {
                TopicRepository.getInstance().deleteTopic(entityId);
            } else {
                ChatRepository.getInstance().updateChatOpened(roomId, false);
            }
            TeamInfoLoader.getInstance().refresh();

            if (TeamInfoLoader.getInstance().isUser(entityId)) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MessageTab, AnalyticsValue.Action.TopicSubMenu_Leave);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicSubMenu_Leave);
            }

            dismissProgressWheel();

            EventBus.getDefault().post(new RetrieveTopicListEvent());

            dismiss();
        } catch (RetrofitException e) {
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
