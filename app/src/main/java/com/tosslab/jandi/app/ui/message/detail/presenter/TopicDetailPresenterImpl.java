package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
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
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
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
    public void onInit(Context context, int entityId) {
        String topicName = topicDetailModel.getTopicName(entityId);
        String topicDescription = topicDetailModel.getTopicDescription(entityId);
        int topicMemberCount = topicDetailModel.getTopicMemberCount(entityId);
        boolean isStarred = topicDetailModel.isStarred(entityId);
        boolean owner = topicDetailModel.isOwner(entityId)
                || topicDetailModel.isTeamOwner();
        boolean isTopicPushSubscribe = topicDetailModel.isPushOn(entityId);

        boolean defaultTopic = topicDetailModel.isDefaultTopic(entityId);

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
        view.setTopicPushSwitch(isTopicPushSubscribe);
        view.setLeaveVisible(owner, defaultTopic);
    }

    @Override
    public void onTopicInvite(Activity activity, int entityId) {
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
    public void onTopicDescriptionMove(int entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            view.moveTopicDescriptionEdit();
        }
    }

    @Background
    @Override
    public void onTopicStar(Context context, int entityId) {
        boolean isStarred = topicDetailModel.isStarred(entityId);

        try {

            if (isStarred) {
                entityClientManager.disableFavorite(entityId);

                topicDetailModel.trackTopicUnStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_starred_unstarred));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOffStar);
            } else {
                entityClientManager.enableFavorite(entityId);

                topicDetailModel.trackTopicStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_message_starred));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOnStar);
            }

            EntityManager.getInstance().getEntityById(entityId).isStarred = !isStarred;

            view.setStarred(!isStarred);

        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            if (isStarred) {
                topicDetailModel.trackTopicUnStarFail(errorCode);
            } else {
                topicDetailModel.trackTopicStarFail(errorCode);
            }
        }
    }

    @Override
    public void onTopicLeave(Context context, int entityId) {
        leaveViewModel.initData(context, entityId);
        leaveViewModel.leave();
    }

    @Override
    public void onTopicDelete(int entityId) {
        if (!(topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner())) {
            return;
        }

        view.showTopicDeleteDialog();
    }

    @Background
    @Override
    public void deleteTopic(Context context, int entityId) {
        view.showProgressWheel();
        try {
            int entityType = topicDetailModel.getEntityType(entityId);
            topicDetailModel.deleteTopic(entityId, entityType);
            topicDetailModel.trackDeletingEntity(context, entityType);
            topicDetailModel.trackTopicDeleteSuccess(entityId);
            view.dismissProgressWheel();
            view.leaveTopic();
        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
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
    public void onChangeTopicName(int entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            String topicName = topicDetailModel.getTopicName(entityId);
            int entityType = topicDetailModel.getEntityType(entityId);
            view.showTopicNameChangeDialog(entityId, topicName, entityType);
        }
    }

    @Background
    @Override
    public void onConfirmChangeTopicName(Context context, int entityId, String topicName, int entityType) {
        view.showProgressWheel();
        try {
            topicDetailModel.modifyTopicName(entityType, entityId, topicName);

            view.setTopicName(topicName);

            topicDetailModel.trackChangingEntityName(context, entityId, entityType);
            EntityManager.getInstance().getEntityById(entityId).getEntity().name = topicName;
            EventBus.getDefault().post(new TopicInfoUpdateEvent(entityId));

        } catch (RetrofitError e) {
            Response response = e.getResponse();
            int errorCode = response != null ? response.getStatus() : -1;

            topicDetailModel.trackChangingEntityNameFail(errorCode);

            if (response != null && response.getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
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
    @Override
    public void updateTopicPushSubscribe(Context context, int teamId, int entityId, boolean pushOn) {
        view.showProgressWheel();

        try {
            topicDetailModel.updatePushStatus(teamId, entityId, pushOn);
            EntityManager.getInstance().getEntityById(entityId).isTopicPushOn = pushOn;
            view.dismissProgressWheel();
        } catch (RetrofitError e) {
            e.printStackTrace();

            view.dismissProgressWheel();

            if (e.getCause() instanceof ConnectionNotFoundException) {
                view.showFailToast(context.getString(R.string.err_network));
            }

        } catch (Exception e) {
            e.printStackTrace();

            view.dismissProgressWheel();

        }
    }

}
