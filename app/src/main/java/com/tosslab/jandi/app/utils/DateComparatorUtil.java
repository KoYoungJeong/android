package com.tosslab.jandi.app.utils;

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
}
