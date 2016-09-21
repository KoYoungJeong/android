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
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicLeave;

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

        int msgId;

        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);
        if (topic.getMemberCount() > 1) {
            msgId = R.string.jandi_message_leave_private_topic;
        } else {
            msgId = R.string.jandi_message_leave_private_topic_when_only_you;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(topic.getName())
                .setMessage(msgId)
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
                        SprinklrTopicLeave.sendLog(entityId);
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
                        SprinklrTopicLeave.sendFailLog(errorCode);
                        e.printStackTrace();
                        leaveEntityFailed(JandiApplication.getContext().getString(R.string.err_entity_leave));
                    } else {
                        SprinklrTopicLeave.sendFailLog(-1);
                        t.printStackTrace();
                        leaveEntityFailed(JandiApplication.getContext().getString(R.string.err_entity_leave));
                    }
                });
    }

    void leaveEntityFailed(String errMessage) {
        ColoredToast.show(errMessage);
    }
}
