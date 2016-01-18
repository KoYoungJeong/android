package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public interface TopicDetailPresenter {

    void setView(View view);

    void onInit(Context context, int entityId);

    void onTopicInvite(Activity activity, int entityId);

    void onTopicDescriptionMove(int entityId);

    void onTopicStar(Context context, int entityId);

    void onAssignTopicOwner(int entityId);

    void onTopicLeave(Context context, int entityId);

    void onTopicDelete(int entityId);

    void deleteTopic(Context context, int entityId);

    void onChangeTopicName(int entityId);

    void onConfirmChangeTopicName(Context context, int entityId, String topicName, int entityType);

    void onAutoJoin(int entityId, boolean autoJoin);

    void onPushClick(Context context, int teamId, int entityId, boolean checked);

    interface View {

        void leaveTopic();

        void setTopicName(String topicName);

        void setTopicDescription(String topicDescription);

        void setStarred(boolean isStarred);

        void setTopicMemberCount(int topicMemberCount);

        void showSuccessToast(String message);

        void showFailToast(String message);

        void showTopicDeleteDialog();

        void setTopicPushSwitch(boolean isPushOn);

        void showProgressWheel();

        void dismissProgressWheel();

        void showTopicNameChangeDialog(int entityId, String entityName, int entityType);

        void showNeedToAssignTopicOwnerDialog(String topicName);

        void moveTopicDescriptionEdit();

        void setLeaveVisible(boolean owner, boolean defaultTopic);

        void setTopicAutoJoin(boolean autoJoin, boolean owner, boolean defaultTopic, boolean privateTopic);

        void setAssignTopicOwnerVisible(boolean owner);

        void moveToAssignTopicOwner();
        void showGlobalPushSetupDialog();
    }
}
