package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

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

        if (TextUtils.isEmpty(topicDescription)) {
            if (owner) {
                topicDescription = context.getString(R.string.jandi_plz_input_topic_description);
            } else {
                topicDescription = context.getString(R.string.jandi_it_has_no_topic_description);
            }
        }

        view.setTopicName(topicName);
        view.setStarred(isStarred);
        view.setTopicDescription(topicDescription);
        view.setTopicMemberCount(topicMemberCount);
        view.setEnableTopicDelete(owner);
    }

    @Override
    public void onTopicInvite(Context context, int entityId) {
        invitationViewModel.initData(context, entityId);
        invitationViewModel.invite();
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
            } else {
                entityClientManager.enableFavorite(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_message_starred));
            }

            EntityManager.getInstance(context).getEntityById(entityId).isStarred = !isStarred;

            view.setStarred(!isStarred);

        } catch (RetrofitError e) {

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
            view.leaveTopic();
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
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

            topicDetailModel.trackChangingEntityName(context, entityType);
            EntityManager.getInstance(context).getEntityById(entityId).getEntity().name = topicName;
            EventBus.getDefault().post(new TopicInfoUpdateEvent(entityId));

        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.showFailToast(context.getString(R.string.err_entity_duplicated_name));
            } else {
                view.showFailToast(context.getString(R.string.err_entity_modify));
            }
        } catch (Exception e) {
            view.showFailToast(context.getString(R.string.err_entity_modify));
        } finally {
            view.dismissProgressWheel();
        }
    }

}
