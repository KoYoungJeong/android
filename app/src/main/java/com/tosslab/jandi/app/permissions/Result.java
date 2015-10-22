package com.tosslab.jandi.app.permissions;

import android.content.pm.PackageManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class Result {
    private List<Integer> requestCodes;
    private Map<String, Pair<HasPermission, NoPermission>> permissionsActor;

    public Result() {
        requestCodes = new ArrayList<>();
        permissionsActor = new HashMap<>();
    }

    public Result addRequestCode(int requestCode) {
        requestCodes.add(requestCode);
        return this;
    }

    public Result addPermission(String permission,
                                HasPermission hasPermission,
                                NoPermission noPermission) {
        permissionsActor.put(permission, new Pair<>(hasPermission, noPermission));
        return this;
    }

    public Result addPermission(String permission,
                                HasPermission hasPermission) {
        permissionsActor.put(permission, new Pair<>(hasPermission, null));
        return this;
    }


    public void resultPermission(OnRequestPermissionsResult result) {
        if (result == null) {
            return;
        }

        Integer requestCode = Observable.from(requestCodes)
                .filter(integer -> integer == result.getRequestCode())
                .firstOrDefault(-1)
                .toBlocking()
                .first();

        if (requestCode < 0) {
            return;
        }

        int permissionCount = result.getPermissions().length;

        for (int idx = 0; idx < permissionCount; idx++) {
            String permission = result.getPermissions()[idx];
            int grantResult = result.getGrantResults()[idx];

            Pair<HasPermission, NoPermission> actor = permissionsActor.get(permission);
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                if (actor.first != null) {
                    actor.first.hasPermission();
                }
            } else {
                if (actor.second != null) {
                    actor.second.noPermission();
                }
            }
        }
    }
}
