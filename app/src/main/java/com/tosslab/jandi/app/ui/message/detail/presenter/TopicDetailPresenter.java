package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.content.Context;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public interface TopicDetailPresenter {

    void setView(View view);

    void onInit(long entityId);

    void onTopicDescriptionMove(long entityId);

    void onTopicStar(long entityId);

    void onAssignTopicOwner(long entityId);

    void onTopicLeave(Context context, long entityId);

    void onTopicDelete(long entityId);

    void deleteTopic(Context context, long entityId);

    void onChangeTopicName(long entityId);

    void onConfirmChangeTopicName(Context context, long entityId, String topicName, int entityType);

    void onAutoJoin(long entityId, boolean autoJoin);

    void onPushClick(long teamId, long entityId, boolean checked);

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

        void showTopicNameChangeDialog(long entityId, String entityName, int entityType);

        void showNeedToAssignTopicOwnerDialog(String topicName);

        void moveTopicDescriptionEdit();

        void setLeaveVisible(boolean owner, boolean defaultTopic);

        void setTopicAutoJoin(boolean autoJoin, boolean owner, boolean defaultTopic, boolean privateTopic);

        void setAssignTopicOwnerVisible(boolean owner);

        void moveToAssignTopicOwner();
        void showGlobalPushSetupDialog();
    }
}
