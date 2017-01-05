package com.tosslab.jandi.app.permissions;

import android.app.Activity;

import com.tosslab.jandi.app.utils.SdkUtils;

public class Check {
    private Permission permission;
    private HasPermission hasPermission;
    private NoPermission noPermission;
    private Activity activity;

    public Check permission(Permission permission) {
        this.permission = permission;
        return this;
    }

    public Check hasPermission(HasPermission hasPermission) {
        this.hasPermission = hasPermission;
        return this;
    }

    public Check noPermission(NoPermission noPermission) {
        this.noPermission = noPermission;
        return this;
    }

    public Check activity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public void check() {
        if (permission == null) {
            return;
        }
        String permissionString = permission.getPermission();
        if (SdkUtils.hasPermission(permissionString)) {
            if (hasPermission != null) {
                hasPermission.hasPermission();
            }
        } else {
            if (noPermission != null) {
                noPermission.noPermission();
            }
        }
    }

    public interface HasPermission {
        void hasPermission();
    }

    public interface NoPermission {
        void noPermission();
    }

}
