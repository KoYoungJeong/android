package com.tosslab.jandi.app.team.onlinestatus;

import java.util.HashSet;

/**
 * Created by tee on 2017. 4. 17..
 */

public class OnlineStatus {
    private static OnlineStatus onlineStatus = null;

    private HashSet<Long> onlineMemberSet;

    private OnlineStatus() {
        onlineMemberSet = new HashSet<>();
    }

    public static OnlineStatus getInstance() {
        if (onlineStatus == null) {
            synchronized (OnlineStatus.class) {
                if (onlineStatus == null) {
                    onlineStatus = new OnlineStatus();
                }
            }
        }
        return onlineStatus;
    }

    public void setOnlineMember(long memberId) {
        if (onlineMemberSet != null) {
            onlineMemberSet.add(memberId);
        }
    }

    public void setOfflineMember(long memberId) {
        if (onlineMemberSet != null) {
            if (onlineMemberSet.contains(memberId)) {
                onlineMemberSet.remove(memberId);
            }
        }
    }

    public boolean isOnlineMember(long memberId) {
        if (onlineMemberSet != null) {
            return onlineMemberSet.contains(memberId);
        } else {
            return false;
        }
    }

}