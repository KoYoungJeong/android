package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MenuCommandBuilder {

    private AppCompatActivity activity;
    private JandiEntityClient mJandiEntityClient;
    private ChattingInfomations chattingInfomations;

    public MenuCommandBuilder(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static MenuCommandBuilder init(AppCompatActivity activity) {
        return new MenuCommandBuilder(activity);
    }

    public MenuCommandBuilder with(JandiEntityClient jandiEntityClient) {
        this.mJandiEntityClient = jandiEntityClient;
        return this;
    }

    public MenuCommandBuilder with(ChattingInfomations chattingInfomations) {
        this.chattingInfomations = chattingInfomations;
        return this;
    }

    public MenuCommand build(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                return new HomeMenuCommand(activity);
            case R.id.action_entity_starred:
                FavoriteTriggerCommand favoriteTriggerCommand = FavoriteTriggerCommand_.getInstance_(activity);
                favoriteTriggerCommand.initData(activity, mJandiEntityClient, chattingInfomations);
                return favoriteTriggerCommand;
            case R.id.action_entity_move_file_list:
                return new FileListCommand(activity, chattingInfomations);
            case R.id.action_entity_invite:
            case R.id.action_my_entity_invite:
                InviteCommand invitecommand = InviteCommand_.getInstance_(activity);
                invitecommand.initData(activity, mJandiEntityClient, chattingInfomations);
                return invitecommand;
            case R.id.action_my_entity_rename:
                return new ModifyEntityCommand(activity, chattingInfomations);
            case R.id.action_my_entity_delete:
                return new DeleteTopicCommand(activity);
            case R.id.action_entity_leave:
            case R.id.action_my_entity_leave:
                LeaveEntityCommand leaveentitycommand = LeaveEntityCommand_.getInstance_(activity);
                leaveentitycommand.initData(activity, mJandiEntityClient, chattingInfomations);
                return leaveentitycommand;
            case R.id.action_entity_members:
            case R.id.action_my_entity_members:
                TopicParticipantCommand topicParticipantCommand = TopicParticipantCommand_.getInstance_(activity);
                topicParticipantCommand.setEntity(chattingInfomations.entityId);
                return topicParticipantCommand;
        }
        return null;
    }

}
