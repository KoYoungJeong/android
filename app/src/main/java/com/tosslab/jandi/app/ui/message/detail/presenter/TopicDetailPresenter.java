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

    interface View {

        void setTopicName(String topicName);

        void setTopicDescription(String topicDescription);

        void setStarred(boolean isStarred);

        void setTopicMemberCount(int topicMemberCount);

        void showSuccessToast(String message);
    }
}
