package com.tosslab.jandi.app.utils;


import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import rx.Observable;

public class AccessLevelUtil {

    public static boolean hasAccessLevel(long targetId) {

        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .takeFirst(topicRoom -> topicRoom.getMembers().contains(targetId))
                .map(topicRoom -> true)
                .defaultIfEmpty(false)
                .toBlocking().firstOrDefault(false);

    }


    public static void showDialogUnabledAccessLevel(Activity activity) {
        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle("접근 권한 없음요")
                .setMessage("물어봐줘, 어드민")
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }
}
