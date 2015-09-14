package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;

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
    InvitationViewModel invitationViewModel;

    @Bean
    EntityClientManager entityClientManager;

    private View view;

    @Override
    public void setView(View view) {

        this.view = view;
    }

    @Override
    public void onInit(Context context, int entityId) {
        String topicName = topicDetailModel.getTopicName(context, entityId);
        String topicDescription = topicDetailModel.getTopicDescription(context, entityId);
        int topicMemberCount = topicDetailModel.getTopicMemberCount(context, entityId);
        boolean isStarred = topicDetailModel.isStarred(context, entityId);
        boolean owner = topicDetailModel.isOwner(context, entityId);
        boolean isTopicPushSubscribe = topicDetailModel.isPushOn(context, entityId);

        boolean defaultTopic = topicDetailModel.isDefaultTopic(context, entityId);

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
        invitationViewModel.inviteMembersToEntity(activity, entityId);
    }

    @Override
    public void onTopicDescriptionMove(Context context, int entityId) {
        if (topicDetailModel.isOwner(context, entityId)) {
            view.moveTopicDescriptionEdit();
        }
    }

    @Background
    @Override
    public void onTopicStar(Context context, int entityId) {
        boolean isStarred = topicDetailModel.isStarred(context, entityId);

        try {

            if (isStarred) {
                entityClientManager.disableFavorite(entityId);

                topicDetailModel.trackTopicUnStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_starred_unstarred));
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOffStar);
            } else {
                entityClientManager.enableFavorite(entityId);

                topicDetailModel.trackTopicStarSuccess(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_message_starred));
                GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.TurnOnStar);
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
    public void onTopicDelete(Context context, int entityId) {
        if (!topicDetailModel.isOwner(context, entityId)) {
            return;
        }

        view.showTopicDeleteDialog();
    }

    @Background
    @Override
    public void deleteTopic(Context context, int entityId) {
        view.showProgressWheel();
        try {
            int entityType = topicDetailModel.getEntityType(context, entityId);
            topicDetailModel.deleteTopic(entityId, entityType);
            topicDetailModel.trackDeletingEntity(context, entityType);
            topicDetailModel.trackTopicDeleteSuccess(entityId);
            view.leaveTopic();
        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            topicDetailModel.trackTopicDeleteFail(errorCode);
            e.printStackTrace();
        } catch (Exception e) {
            topicDetailModel.trackTopicDeleteFail(-1);
            e.printStackTrace();
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Override
    public void onChangeTopicName(Context context, int entityId) {
        if (topicDetailModel.isOwner(context, entityId)) {
            String topicName = topicDetailModel.getTopicName(context, entityId);
            int entityType = topicDetailModel.getEntityType(context, entityId);
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
