package com.tosslab.jandi.app.utils;

import android.content.Context;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by justinygchoi on 14. 11. 10..
 */
public class BadgeUtils {

    public static synchronized void setBadge(Context context, int count) {
        ShortcutBadger.with(context).count(count);
    }

    public static void clearBadge(Context context) {
        ShortcutBadger.with(context).remove();
    }

}
