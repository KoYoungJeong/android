package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.Iterator;

import me.leolin.shortcutbadger.ShortcutBadger;
import rx.Observable;

/**
 * Created by justinygchoi on 14. 11. 10..
 */
public class BadgeUtils {

    public static synchronized void setBadge(Context context, int count) {
        ShortcutBadger.with(context).count(count);
    }

    public static int getTotalUnreadCount(ResLeftSideMenu resLeftSideMenu) {

        int totalUnread = 0;

        Iterator<ResLeftSideMenu.AlarmInfo> alarmInfoIterator = Observable.from(resLeftSideMenu.alarmInfos)
                .filter(alarmInfo -> {

                    if (TextUtils.equals(alarmInfo.entityType, "chat")) {
                        return true;
                    } else {

                        ResLeftSideMenu.Entity entity = Observable.from(resLeftSideMenu.joinEntities)
                                .filter(joinEntity -> alarmInfo.entityId == joinEntity.id)
                                .toBlocking()
                                .firstOrDefault(new ResLeftSideMenu.Entity() {
                                });

                        return entity.id != 0;
                    }

                }).toBlocking()
                .getIterator();

        while (alarmInfoIterator.hasNext()) {
            totalUnread += alarmInfoIterator.next().alarmCount;
        }

        return totalUnread;
    }

    public static void clearBadge(Context context) {
        ShortcutBadger.with(context).remove();
    }

}
