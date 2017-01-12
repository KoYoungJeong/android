package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicCreate;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class TopicCreatePresenterImpl implements TopicCreatePresenter {

    TopicCreateModel topicCreateModel;
    private View view;

    @Inject
    TopicCreatePresenterImpl(View view, TopicCreateModel topicCreateModel) {
        this.topicCreateModel = topicCreateModel;
        this.view = view;
    }

    @Override
    public void onCreateTopic(String topicTitle, String topicDescriptionText, boolean isPublic, boolean isAutojoin) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        if (topicCreateModel.invalideTitle(topicTitle)) {
            return;
        }

        view.showProgressWheel();

        Observable.defer(() -> {
            try {
                return Observable.just(topicCreateModel.createTopic(topicTitle, isPublic, topicDescriptionText, isAutojoin));
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        }).doOnNext(topic -> {

            topicCreateModel.addTopic(topic);
            EventBus.getDefault().post(new RetrieveTopicListEvent());

            SprinklrTopicCreate.sendLog(topic.getId());

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();
                    long teamId = TeamInfoLoader.getInstance().getTeamId();
                    view.createTopicSuccess(teamId, it.getId(), topicTitle, isPublic);
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        view.dismissProgressWheel();
                        int errorCode = e.getResponseCode();
                        SprinklrTopicCreate.sendFailLog(errorCode);
                        if (e.getStatusCode() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                            view.createTopicFailed(R.string.err_entity_duplicated_name);
                        } else {
                            view.createTopicFailed(R.string.err_entity_create);
                        }
                    } else {
                        SprinklrTopicCreate.sendFailLog(-1);
                        view.dismissProgressWheel();
                        view.createTopicFailed(R.string.err_entity_create);
                    }
                });

    }

}
