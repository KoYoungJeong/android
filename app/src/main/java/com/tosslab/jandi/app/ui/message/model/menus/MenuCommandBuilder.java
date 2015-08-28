package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MenuCommandBuilder {

    private AppCompatActivity activity;
    private EntityClientManager mEntityClientManager;
    private ChattingInfomations chattingInfomations;
    private Fragment fragment;

    public MenuCommandBuilder(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static MenuCommandBuilder init(AppCompatActivity activity) {
        return new MenuCommandBuilder(activity);
    }

    public MenuCommandBuilder with(EntityClientManager entityClientManager) {
        this.mEntityClientManager = entityClientManager;
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
            case R.id.action_entity_move_file_list:
                return new FileListCommand(activity, chattingInfomations);
            case R.id.action_entity_search:
                SearchMenuCommand command = SearchMenuCommand_.getInstance_(activity);
                command.setEntityId(chattingInfomations.entityId);
                return command;
            case R.id.action_entity_more:
                return new TopicDetailCommand(fragment, chattingInfomations.teamId, chattingInfomations.entityId);
        }
        return null;
    }

    public MenuCommandBuilder with(Fragment fragment) {
        this.fragment = fragment;
        return this;
    }
}
