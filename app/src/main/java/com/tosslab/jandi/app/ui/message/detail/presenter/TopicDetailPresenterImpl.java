package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import de.greenrobot.event.EventBus;

@EBean
public class TopicDetailPresenterImpl implements TopicDetailPresenter {

    @Bean
    TopicDetailModel topicDetailModel;

    @Bean
    LeaveViewModel leaveViewModel;

    @Bean
    EntityClientManager entityClientManager;

    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInit(Context context, long entityId) {
        String topicName = topicDetailModel.getTopicName(entityId);
        String topicDescription = topicDetailModel.getTopicDescription(entityId);
        int topicMemberCount = topicDetailModel.getTopicMemberCount(entityId);
        boolean isStarred = topicDetailModel.isStarred(entityId);
        boolean owner = topicDetailModel.isOwner(entityId)
                || topicDetailModel.isTeamOwner();
        boolean isTopicPushSubscribe = topicDetailModel.isPushOn(entityId);

        boolean defaultTopic = topicDetailModel.isDefaultTopic(entityId);
        boolean privateTopic = topicDetailModel.isPrivateTopic(entityId);
        boolean autoJoin = topicDetailModel.isAutoJoin(entityId);

        if (TextUtils.isEmpty(topicDescription)) {
            if (owner) {
                topicDescription = context.getString(R.string.jandi_explain_topic_description);
            } else {
                topicDescription = context.getString(R.string.jandi_it_has_no_topic_description);
            }
        }

        view.setTopicName(topicName);
        view.setStarred(isStarred);
        view.setTopicDescription(topicDescription);
        view.setTopicMemberCount(topicMemberCount);
        view.setTopicAutoJoin(autoJoin, owner, defaultTopic, privateTopic);
        view.setTopicPushSwitch(isTopicPushSubscribe);
        view.setLeaveVisible(owner, defaultTopic);
        view.setAssignTopicOwnerVisible(owner);
    }

    @Override
    public void onTopicInvite(Activity activity, long entityId) {
        if (topicDetailModel.getTopicMemberCount(entityId) != topicDetailModel.getEnabledTeamMemberCount()) {
            MembersListActivity_.intent(activity)
                    .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .entityId(entityId)
                    .type(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC)
                    .start();
        } else {
            String message = activity.getString(R.string.warn_all_users_are_already_invited);
            view.showFailToast(message);
        }
    }

    @Override
    public void onTopicDescriptionMove(long entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            view.moveTopicDescriptionEdit();
        }
    }

    @Background
    @Override
    public void onTopicStar(Context context, long entityId) {
        boolean isStarred = topicDetailModel.isStarred(entityId);

        try {

            if (isStarred) {
                entityClientManager.disableFavorite(entityId);

                topicDetailModel.trackTopicUnStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_starred_unstarred));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.Off);
            } else {
                entityClientManager.enableFavorite(entityId);

                topicDetailModel.trackTopicStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_message_starred));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.On);
            }

            TopicRepository.getInstance().updateStarred(entityId, !isStarred);
            TeamInfoLoader.getInstance().refresh();

            view.setStarred(!isStarred);

        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            if (isStarred) {
                topicDetailModel.trackTopicUnStarFail(errorCode);
            } else {
                topicDetailModel.trackTopicStarFail(errorCode);
            }
        }
    }

    @Override
    public void onAssignTopicOwner(long entityId) {
        if (topicDetailModel.isStandAlone(entityId)) {
            String message = JandiApplication.getContext()
                    .getResources().getString(R.string.jandi_topic_inside_alone);
            view.showFailToast(message);
        } else {
            view.moveToAssignTopicOwner();
        }
    }

    @Override
    public void onTopicLeave(Context context, long entityId) {
        if (topicDetailModel.isOwner(entityId)
                && !(topicDetailModel.isStandAlone(entityId))) {
            String topicName = topicDetailModel.getTopicName(entityId);
            view.showNeedToAssignTopicOwnerDialog(topicName);
        } else {
            leaveViewModel.initData(context, entityId);
            leaveViewModel.leave();
        }
    }

    @Override
    public void onTopicDelete(long entityId) {
        if (!(topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner())) {
            return;
        }

        view.showTopicDeleteDialog();
    }

    @Background
    @Override
    public void deleteTopic(Context context, long entityId) {
        view.showProgressWheel();
        try {
            int entityType = topicDetailModel.getEntityType(entityId);
            topicDetailModel.deleteTopic(entityId, entityType);
            topicDetailModel.trackTopicDeleteSuccess(entityId);
            view.dismissProgressWheel();
            view.leaveTopic();
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            topicDetailModel.trackTopicDeleteFail(errorCode);
            e.printStackTrace();
            view.dismissProgressWheel();
        } catch (Exception e) {
            topicDetailModel.trackTopicDeleteFail(-1);
            e.printStackTrace();
            view.dismissProgressWheel();
        }
    }

    @Override
    public void onChangeTopicName(long entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            String topicName = topicDetailModel.getTopicName(entityId);
            int entityType = topicDetailModel.getEntityType(entityId);
            view.showTopicNameChangeDialog(entityId, topicName, entityType);
        }
    }

    @Background
    @Override
    public void onConfirmChangeTopicName(Context context, long entityId, String topicName, int entityType) {
        view.showProgressWheel();
        try {
            topicDetailModel.modifyTopicName(entityType, entityId, topicName);
            TopicRepository.getInstance().updateName(entityId, topicName);
            TeamInfoLoader.getInstance().refresh();

            view.setTopicName(topicName);

            topicDetailModel.trackChangingEntityName(context, entityId, entityType);
            TopicRepository.getInstance().updateName(entityId, topicName);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicInfoUpdateEvent(entityId));

        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();

            topicDetailModel.trackChangingEntityNameFail(errorCode);

            if (errorCode == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.showFailToast(context.getString(R.string.err_entity_duplicated_name));
            } else {
                view.showFailToast(context.getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            topicDetailModel.trackChangingEntityNameFail(-1);
            view.showFailToast(context.getString(R.string.err_entity_modify));
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Background
    void updateTopicPushSubscribe(long teamId, long entityId, boolean pushOn, boolean showGlobalPushAlert) {
        if (!showGlobalPushAlert) {
            view.showProgressWheel();
        }

        try {
            topicDetailModel.updatePushStatus(teamId, entityId, pushOn);
            TopicRepository.getInstance().updatePushSubscribe(entityId, pushOn);
            TeamInfoLoader.getInstance().refresh();
            view.dismissProgressWheel();
        } catch (RetrofitException e) {
            e.printStackTrace();

            view.dismissProgressWheel();

            if (e.getStatusCode() >= 500) {
                view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (!showGlobalPushAlert) {
                view.dismissProgressWheel();
            }

        }
    }

    @Background
    @Override
    public void onAutoJoin(long entityId, boolean autoJoin) {

        if (!topicDetailModel.isOwner(entityId) && !topicDetailModel.isTeamOwner()) {
            // 수정권한 없음
            onInit(JandiApplication.getContext(), entityId);
            return;
        }

        if (topicDetailModel.isPrivateTopic(entityId)) {
            // private topic == true 이면 그외의 값은 의미 없음
            onInit(JandiApplication.getContext(), entityId);
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_auto_join_cannot_be_private_topic));
            return;
        }

        if (topicDetailModel.isDefaultTopic(entityId)) {
            // 기본 토픽  == true 의미 없음..
            onInit(JandiApplication.getContext(), entityId);
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_auto_join_cannot_be_default_topic));
            return;
        }

        view.showProgressWheel();
        try {
            topicDetailModel.updateAutoJoin(entityId, autoJoin);
            view.dismissProgressWheel();
            TopicRepository.getInstance().updateAutoJoin(entityId, autoJoin);
            TeamInfoLoader.getInstance().refresh();
            onInit(JandiApplication.getContext(), entityId);
        } catch (RetrofitException e) {
            e.printStackTrace();
            view.dismissProgressWheel();

            if (e.getStatusCode() >= 500) {
                view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            }

        }
    }

    @Override
    public void onPushClick(long teamId, long entityId, boolean checked) {
        boolean onGlobalPush = topicDetailModel.isOnGlobalPush();
        if (checked && !onGlobalPush) {
            view.showGlobalPushSetupDialog();
        }

        updateTopicPushSubscribe(teamId, entityId, checked, !onGlobalPush);
    }

}
