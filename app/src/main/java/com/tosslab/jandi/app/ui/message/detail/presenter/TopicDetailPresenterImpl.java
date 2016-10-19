package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.message.detail.model.LeaveViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicDelete;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicNameChange;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicStar;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicUnStar;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TopicDetailPresenterImpl implements TopicDetailPresenter {

    private TopicDetailModel topicDetailModel;
    private LeaveViewModel leaveViewModel;
    private View view;

    @Inject
    public TopicDetailPresenterImpl(View view, TopicDetailModel topicDetailModel, LeaveViewModel leaveViewModel) {
        this.topicDetailModel = topicDetailModel;
        this.leaveViewModel = leaveViewModel;
        this.view = view;
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInit(long entityId) {
        TopicRoom topic = topicDetailModel.getTopic(entityId);
        if (topic == null || topic.getId() <= 0) {
            return;
        }

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
                topicDescription = JandiApplication.getContext().getString(R.string.jandi_explain_topic_description);
            } else {
                topicDescription = JandiApplication.getContext().getString(R.string.jandi_it_has_no_topic_description);
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
    public void onTopicDescriptionMove(long entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            view.moveTopicDescriptionEdit();
        }
    }

    @Override
    public void onTopicStar(long entityId) {
        Observable.just(topicDetailModel.isStarred(entityId))
                .concatMap(isStarred -> {
                    try {
                        if (isStarred) {
                            topicDetailModel.updateTopicStatus(entityId, false);
                            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.Off);
                        } else {
                            topicDetailModel.updateTopicStatus(entityId, true);
                            SprinklrTopicStar.sendLog(entityId);
                            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicDescription, AnalyticsValue.Action.Star, AnalyticsValue.Label.On);
                        }
                        TopicRepository.getInstance().updateStarred(entityId, !isStarred);
                        TeamInfoLoader.getInstance().refresh();
                        return Observable.just(isStarred);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isStarred -> {
                    if (isStarred) {
                        view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_starred_unstarred));
                    } else {
                        view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_message_starred));
                    }
                    view.setStarred(!isStarred);

                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();
                        if (topicDetailModel.isStarred(entityId)) {
                            SprinklrTopicUnStar.sendFailLog(errorCode);
                        } else {
                            SprinklrTopicStar.sendFailLog(errorCode);
                        }
                    }
                });

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
            if (leaveViewModel.canLeaveRoom(entityId)) {
                leaveViewModel.leave(entityId);
            } else {
                leaveViewModel.showPrivateTopicLeaveDialog(context, entityId);
            }
        }
    }

    @Override
    public void onTopicDelete(long entityId) {
        if (!(topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner())) {
            return;
        }

        view.showTopicDeleteDialog();
    }

    @Override
    public void deleteTopic(Context context, long entityId) {
        view.showProgressWheel();
        Observable.just(entityId)
                .concatMap(entityid -> {
                    try {
                        int entityType = topicDetailModel.getEntityType(entityId);
                        topicDetailModel.deleteTopic(entityId, entityType);
                        SprinklrTopicDelete.sendLog(entityId);
                        return Observable.just(entityid);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();
                    view.leaveTopic();
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();
                        SprinklrTopicDelete.sendFailLog(errorCode);
                    } else {
                        SprinklrTopicDelete.sendFailLog(-1);
                    }
                });
    }

    @Override
    public void onChangeTopicName(long entityId) {
        if (topicDetailModel.isOwner(entityId) || topicDetailModel.isTeamOwner()) {
            String topicName = topicDetailModel.getTopicName(entityId);
            int entityType = topicDetailModel.getEntityType(entityId);
            view.showTopicNameChangeDialog(entityId, topicName, entityType);
        }
    }

    @Override
    public void onConfirmChangeTopicName(Context context, long entityId, String topicName, int entityType) {
        view.showProgressWheel();

        Observable.just(new Object())
                .concatMap(it -> {
                    try {
                        topicDetailModel.modifyTopicName(entityType, entityId, topicName);
                        return Observable.just(topicName);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                        return Observable.error(e);
                    }

                })
                .doOnNext(it -> {
                    TopicRepository.getInstance().updateName(entityId, topicName);
                    TeamInfoLoader.getInstance().refresh();
                    SprinklrTopicNameChange.sendLog(entityId);
                    EventBus.getDefault().post(new TopicInfoUpdateEvent(entityId));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> {
                    view.setTopicName(name);
                    view.dismissProgressWheel();
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();

                        SprinklrTopicNameChange.sendFailLog(errorCode);
                        if (errorCode == JandiConstants.NetworkError.DUPLICATED_NAME) {
                            view.showFailToast(context.getString(R.string.err_entity_duplicated_name));
                        } else {
                            view.showFailToast(context.getString(R.string.err_entity_modify));
                        }
                    } else {
                        SprinklrTopicNameChange.sendFailLog(-1);
                        view.showFailToast(context.getString(R.string.err_entity_modify));
                    }
                });
    }

    void updateTopicPushSubscribe(long teamId, long entityId, boolean pushOn, boolean showGlobalPushAlert) {
        if (!showGlobalPushAlert) {
            view.showProgressWheel();
        }

        Observable.just(new Object())
                .concatMap(it -> {
                    try {
                        topicDetailModel.updatePushStatus(teamId, entityId, pushOn);
                        return Observable.just(it);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }

                })
                .doOnNext(it -> {
                    TopicRepository.getInstance().updatePushSubscribe(entityId, pushOn);
                    TeamInfoLoader.getInstance().refresh();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();

                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getStatusCode() >= 500) {
                            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
                        }
                    }
                });
    }

    @Override
    public void onAutoJoin(long entityId, boolean autoJoin) {

        if (!topicDetailModel.isOwner(entityId) && !topicDetailModel.isTeamOwner()) {
            // 수정권한 없음
            onInit(entityId);
            return;
        }

        if (topicDetailModel.isPrivateTopic(entityId)) {
            // private topic == true 이면 그외의 값은 의미 없음
            onInit(entityId);
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_auto_join_cannot_be_private_topic));
            return;
        }

        if (topicDetailModel.isDefaultTopic(entityId)) {
            // 기본 토픽  == true 의미 없음..
            onInit(entityId);
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_auto_join_cannot_be_default_topic));
            return;
        }

        view.showProgressWheel();

        Observable.just(Pair.create(entityId, autoJoin))
                .concatMap(pair -> {
                    try {
                        topicDetailModel.updateAutoJoin(pair.first, pair.second);
                        return Observable.just(pair);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                })
                .doOnNext(it -> {
                    TopicRepository.getInstance().updateAutoJoin(it.first, it.second);
                    TeamInfoLoader.getInstance().refresh();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();
                    onInit(it.first);
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        view.dismissProgressWheel();

                        if (e.getStatusCode() >= 500) {
                            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
                        }
                    }
                });

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
