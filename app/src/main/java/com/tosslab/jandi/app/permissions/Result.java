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
    private Map<Integer, Pair<HasPermission, NoPermission>> permissionsActor;
    private Map<Integer, String> permissionMapper;

    public Result() {
        requestCodes = new ArrayList<>();
        permissionsActor = new HashMap<>();
        permissionMapper = new HashMap<>();
    }

    public Result addRequestCode(int requestCode) {
        requestCodes.add(requestCode);
        return this;
    }

    public Result addPermission(String permission,
                                HasPermission hasPermission,
                                NoPermission noPermission) {
        Integer key = requestCodes.get(requestCodes.size() - 1);
        permissionsActor.put(key, new Pair<>(hasPermission, noPermission));
        permissionMapper.put(key, permission);
        return this;
    }

    public Result addPermission(String permission,
                                HasPermission hasPermission) {
        Integer key = requestCodes.get(requestCodes.size() - 1);
        permissionsActor.put(key, new Pair<>(hasPermission, null));
        permissionMapper.put(key, permission);
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

        Pair<HasPermission, NoPermission> actor = permissionsActor.get(requestCode);
        for (int idx = 0; idx < permissionCount; idx++) {
            int grantResult = result.getGrantResults()[idx];

            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                if (actor.second != null) {
                    actor.second.noPermission();
                }
                return;
            }
        }

        if (actor.first != null) {
            actor.first.hasPermission();
        }
    }
}
