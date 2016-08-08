package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class LeaveViewModel {

    private EntityClientManager entityClientManager;
    private Lazy<ChatApi> chatApi;

    @Inject
    public LeaveViewModel(Lazy<ChatApi> chatApi) {
        this.entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
        ;
        this.chatApi = chatApi;
    }

    public void leave(long entityId) {
        leaveEntityInBackground(entityId);
    }

    public boolean canLeaveRoom(long entityId) {
        return TeamInfoLoader.getInstance().isPublicTopic(entityId)
                || TeamInfoLoader.getInstance().isUser(entityId);
    }

    public void showPrivateTopicLeaveDialog(Context context, long entityId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(TeamInfoLoader.getInstance().getName(entityId))
                .setMessage(R.string.jandi_message_leave_private_topic)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_leave, (dialog, which) -> leaveEntityInBackground(entityId))
                .create()
                .show();
    }


    void leaveEntityInBackground(long entityId) {

        Observable.just(entityId)
                .concatMap(entityid -> {
                    try {
                        if (TeamInfoLoader.getInstance().isTopic(entityid)) {

                            if (TeamInfoLoader.getInstance().isPublicTopic(entityid)) {
                                entityClientManager.leaveChannel(entityid);
                            } else {
                                entityClientManager.leavePrivateGroup(entityid);
                            }
                        } else {
                            long memberId = TeamInfoLoader.getInstance().getMyId();
                            chatApi.get().deleteChat(memberId, entityid);
                        }
                        trackTopicLeaveSuccess(entityid);
                        return Observable.just(entityid);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(entityid -> {
                    EventBus.getDefault().post(new TopicLeaveEvent());
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();
                        trackTopicLeaveFail(errorCode);
                        e.printStackTrace();
                        leaveEntityFailed(JandiApplication.getContext().getString(R.string.err_entity_leave));
                    } else {
                        trackTopicLeaveFail(-1);
                        t.printStackTrace();
                        leaveEntityFailed(JandiApplication.getContext().getString(R.string.err_entity_leave));
                    }
                });
    }

    private void trackTopicLeaveSuccess(long entityId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicLeave)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, entityId)
                .build());

    }

    private void trackTopicLeaveFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicLeave)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

    void leaveEntityFailed(String errMessage) {
        ColoredToast.show(errMessage);
    }
}
