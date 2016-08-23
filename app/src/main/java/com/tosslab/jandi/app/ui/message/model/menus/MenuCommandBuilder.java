package com.tosslab.jandi.app.ui.message.model.menus;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MenuCommandBuilder {

    private AppCompatActivity activity;
    private Fragment fragment;
    private long teamId;
    private long entityId;

    public MenuCommandBuilder(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static MenuCommandBuilder init(AppCompatActivity activity) {
        return new MenuCommandBuilder(activity);
    }

    public MenuCommandBuilder teamId(long teamId) {
        this.teamId = teamId;
        return this;
    }

    public MenuCommandBuilder entityId(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public MenuCommand build(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return new HomeMenuCommand(activity);
            case R.id.action_entity_move_file_list:
                return new FileListCommand(activity, entityId);
            case R.id.action_entity_search:
                long memberId = -1l;
                // directMessage라면 roomId로 변환
                if (TeamInfoLoader.getInstance().isMember(entityId)) {
                    memberId = entityId;
                    Observable.from(TeamInfoLoader.getInstance().getDirectMessageRooms())
                            .takeFirst(room -> room.getCompanionId() == entityId)
                            .map(DirectMessageRoom::getId)
                            .firstOrDefault(-1L)
                            .subscribe(roomId -> {
                                entityId = roomId;
                            });
                }
                return new SearchMenuCommand(activity, entityId, memberId);
            case R.id.action_entity_more:
                return new TopicDetailCommand(fragment, teamId, entityId);
        }
        return null;
    }

    public MenuCommandBuilder with(Fragment fragment) {
        this.fragment = fragment;
        return this;
    }
}
