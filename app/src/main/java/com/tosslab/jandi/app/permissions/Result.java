package com.tosslab.jandi.app.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class Result {
    private List<Integer> requestCodes;
    private Activity activity;
    private Map<Integer, Pair<Check.HasPermission, Check.NoPermission>> permissionsActor;
    private Map<Integer, String> permissionMapper;
    private NeverAskAgain neverAskAgain;

    public Result() {
        requestCodes = new ArrayList<>();
        permissionsActor = new HashMap<>();
        permissionMapper = new HashMap<>();
    }

    public Result activity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public Result addRequestCode(int requestCode) {
        requestCodes.add(requestCode);
        return this;
    }

    public Result addPermission(String permission,
                                Check.HasPermission hasPermission,
                                Check.NoPermission noPermission) {
        Integer key = requestCodes.get(requestCodes.size() - 1);
        permissionsActor.put(key, new Pair<>(hasPermission, noPermission));
        permissionMapper.put(key, permission);
        return this;
    }


    public Result addPermission(String permission,
                                Check.HasPermission hasPermission) {
        Integer key = requestCodes.get(requestCodes.size() - 1);
        permissionsActor.put(key, new Pair<>(hasPermission, null));
        permissionMapper.put(key, permission);
        return this;
    }

    public void resultPermission(OnRequestPermissionsResult result) {
        if (result == null) {
            return;
        }

        int requestCode = Observable.from(requestCodes)
                .filter(integer -> integer == result.getRequestCode())
                .firstOrDefault(-1)
                .toBlocking()
                .first();

        if (requestCode < 0) {
            return;
        }

        int permissionCount = result.getPermissions().length;

        Pair<Check.HasPermission, Check.NoPermission> actor = permissionsActor.get(requestCode);

        boolean denied = false;
        String deniedPermission = null;
        for (int idx = 0; idx < permissionCount; idx++) {
            int grantResult = result.getGrantResults()[idx];

            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                denied = true;
                deniedPermission = result.getPermissions()[idx];
                break;
            }
        }


        if (denied) {
            boolean neverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission);
            if (!neverAskAgain) {
                if (actor.second != null) {
                    actor.second.noPermission();
                }
            } else {
                if (this.neverAskAgain != null) {
                    this.neverAskAgain.denyPermanently();
                }
            }
        } else {
            if (actor.first != null) {
                actor.first.hasPermission();
            }

        }
    }

    public Result neverAskAgain(NeverAskAgain neverAskAgain) {
        this.neverAskAgain = neverAskAgain;
        return this;
    }

    public interface NeverAskAgain {
        void denyPermanently();
    }
}
