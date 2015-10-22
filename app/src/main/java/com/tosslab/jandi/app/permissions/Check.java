package com.tosslab.jandi.app.permissions;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.SdkUtils;

public class Check {
    private Permission permission;
    private HasPermission hasPermission;
    private NoPermission noPermission;

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

    public void check() {
        if (permission == null) {
            return;
        }
        String permissionString = permission.getPermission();
        if (SdkUtils.hasPermission(JandiApplication.getContext(), permissionString)) {
            if (hasPermission != null) {
                hasPermission.hasPermission();
            }
        } else {
            if (noPermission != null) {
                noPermission.noPermission();
            }
        }
    }
}
