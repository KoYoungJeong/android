package com.tosslab.jandi.app.utils;


import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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
            if (TeamInfoLoader.getInstance().getMyLevel() == Level.Guest) {
                return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                        .filter(TopicRoom::isJoined)
                        .takeFirst(topicRoom -> topicRoom.getMembers().contains(targetId))
                        .map(topicRoom -> true)
                        .defaultIfEmpty(false)
                        .toBlocking().firstOrDefault(false);
            } else {
                return true;
            }
        }
    }


    public static void showDialogUnabledAccessLevel(Activity activity) {
        String title = activity.getString(R.string.common_authority_notauthorised_title);
        String message = activity.getString(R.string.common_authority_notauthorised_body);

        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(Html.fromHtml(String.format("<b>%s</b><br/><br/>%s", title, message)))
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

    public static void setTextOfLevelInNav(Level level, TextView textView) {
        Resources resources = textView.getResources();
        switch (level) {
            case Guest:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_guest_nav);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_guest_nav));
                textView.setText(R.string.common_authority_title_associate);
                break;
            case Owner:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_owner_nav);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_owner_nav));
                textView.setText(R.string.common_authority_title_owner);
                break;
            case Admin:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_admin_nav);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_admin_nav));
                textView.setText(R.string.common_authority_title_manager);
                break;
            default:
            case Member:
                textView.setBackgroundResource(R.drawable.bg_user_level_team_member_nav);
                textView.setTextColor(resources.getColor(R.color.jandi_text_level_team_member_nav));
                textView.setText(R.string.common_authority_title_member);
                break;
        }

        textView.setPaintFlags(textView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public static void setTextOfLevelInProfile(Level level, TextView textView) {
        Resources resources = textView.getResources();
        switch (level) {
            case Guest:
                textView.setText(R.string.common_authority_title_associate);
                break;
            case Owner:
                textView.setText(R.string.common_authority_title_owner);
                break;
            case Admin:
                textView.setText(R.string.common_authority_title_manager);
                break;
            default:
            case Member:
                textView.setText(R.string.common_authority_title_member);
                break;
        }
    }
}
