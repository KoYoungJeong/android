package com.tosslab.jandi.app.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class DateComparatorUtil {
    public static boolean isSince5min(Date currentMessageTime, Date beforeMessageTime) {
        if (beforeMessageTime == null) {
            beforeMessageTime = new Date();
        }

        if (currentMessageTime == null) {
            currentMessageTime = new Date();
        }

        long beforeTime = beforeMessageTime.getTime();
        long currentTime = currentMessageTime.getTime();

        double diffTime = currentTime - beforeTime;
        if (diffTime / (1000l * 60l * 5) < 1d) {
            return true;
        }

        return false;
    }

    public static boolean isBefore30Days(Date time) {
        if (time == null) {
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date before30days = calendar.getTime();

        return time.before(before30days);

    }

    public static boolean isSameTime(Date time1, Date time2) {
        if (time1 == null) {
            return false;
        }

        if (time2 == null) {
            return false;
        }

        if (time1.getTime() / (1000l * 60l) == time2.getTime() / (1000l * 60l)) {
            return true;
        } else {
            return false;
        }
    }
}
