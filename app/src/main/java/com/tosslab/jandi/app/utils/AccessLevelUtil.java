package com.tosslab.jandi.app.utils;


import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.room.TopicRoom;

import rx.Observable;

public class AccessLevelUtil {

    public static boolean hasAccessLevel(long targetId) {
        if (TeamInfoLoader.getInstance().isJandiBot(targetId)) {
            return true;
        } else {
            return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .filter(TopicRoom::isJoined)
                    .takeFirst(topicRoom -> topicRoom.getMembers().contains(targetId))
                    .map(topicRoom -> true)
                    .defaultIfEmpty(false)
                    .toBlocking().firstOrDefault(false);
        }
    }


    public static void showDialogUnabledAccessLevel(Activity activity) {
        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.common_authority_notauthorised_title)
                .setMessage(R.string.common_authority_notauthorised_body)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    public static void setTextOfLevel(Level level, TextView textView) {
        Resources resources = textView.getResources();
        switch (level) {
            case Guest:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_guest);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_guest));
                textView.setText(R.string.common_authority_title_associate);
                break;
            case Owner:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_owner);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_owner));
                textView.setText(R.string.common_authority_title_owner);
                break;
            case Admin:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_admin);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_admin));
                textView.setText(R.string.common_authority_title_manager);
                break;
            default:
            case Member:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_member);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_member));
                textView.setText(R.string.common_authority_title_member);
                break;
        }

    }
}
