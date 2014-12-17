package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MenuCommandBuilder {

    private Activity activity;
    private JandiEntityClient mJandiEntityClient;
    private ChattingInfomations chattingInfomations;

    public MenuCommandBuilder(Activity activity) {
        this.activity = activity;
    }

    public static MenuCommandBuilder init(Activity activity) {
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
                FavoriteTriggerCommand_ favoriteTriggerCommand_ = FavoriteTriggerCommand_.getInstance_(activity);
                favoriteTriggerCommand_.initData(activity, mJandiEntityClient, chattingInfomations);
                return favoriteTriggerCommand_;
            case R.id.action_entity_move_file_list:
                return new FileListCommand(activity, chattingInfomations);
            case R.id.action_entity_invite:
            case R.id.action_my_entity_invite:
                InviteCommand_ inviteCommand_ = InviteCommand_.getInstance_(activity);
                inviteCommand_.initData(activity, mJandiEntityClient, chattingInfomations);
                return inviteCommand_;
            case R.id.action_my_entity_rename:
                return new ModifyEntityCommand(activity, chattingInfomations);
            case R.id.action_my_entity_delete:
                return new DeleteTopicCommand(activity);
            case R.id.action_entity_leave:
            case R.id.action_my_entity_leave:
                LeaveEntityCommand_ leaveEntityCommand_ = LeaveEntityCommand_.getInstance_(activity);
                leaveEntityCommand_.initData(activity, mJandiEntityClient, chattingInfomations);
                return leaveEntityCommand_;
        }
        return null;
    }

}
