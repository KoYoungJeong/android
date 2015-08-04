package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.content.Context;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
public interface TopicDetailPresenter {

    void setView(View view);

    void onInit(Context context, int entityId);

    void onTopicInvite(Context context, int entityId);

    void onTopicDescriptionMove(Context context, int entityId);

    void onTopicStar(Context context, int entityId);

    void onTopicLeave(Context context, int entityId);

    void onTopicDelete(Context context, int entityId);

    void deleteTopic(Context context, int entityId);

    void onChangeTopicName(Context context, int entityId);

    void onConfirmChangeTopicName(Context context, int entityId, String topicName, int entityType);

    void updateTopicPushSubscribe(Context context, int teamId, int entityId, boolean pushOn);

    interface View {

        void leaveTopic();

        void setTopicName(String topicName);

        void setTopicDescription(String topicDescription);

        void setStarred(boolean isStarred);

        void setTopicMemberCount(int topicMemberCount);

        void showSuccessToast(String message);

        void showFailToast(String message);

        void setEnableTopicDelete(boolean owner);

        void showTopicDeleteDialog();

        void setTopicPushSwitch(boolean isPushOn);

        void showProgressWheel();

        void dismissProgressWheel();

        void showTopicNameChangeDialog(int entityId, String entityName, int entityType);

        void moveTopicDescriptionEdit();
    }
}
