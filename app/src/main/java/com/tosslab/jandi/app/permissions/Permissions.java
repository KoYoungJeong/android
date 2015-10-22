package com.tosslab.jandi.app.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.SdkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class Permissions {


    public static final int MAX_8BIT = 0xFF;

    public static Check getChecker() {
        return new Check();
    }

    public static Result getResult() {
        return new Result();
    }

    public static OnRequestPermissionsResult createPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        return new OnRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static void requestPermission(Activity activity, int requestCode, Permission permission) {

        if (activity == null || permission == null || requestCode > MAX_8BIT) {
            return;
        }

        activity.requestPermissions(new String[]{permission.getPermission()}, requestCode);
    }

    public static void requestPermission(Fragment fragment, int requestCode, Permission permission) {

        if (fragment == null || permission == null || requestCode > MAX_8BIT) {
            return;
        }

        fragment.requestPermissions(new String[]{permission.getPermission()}, requestCode);
    }

    public interface Permission {
        String getPermission();
    }

    public interface HasPermission {
        void hasPermission();
    }

    public interface NoPermission {
        void noPermission();
    }

    public static class OnRequestPermissionsResult {
        private final int requestCode;
        private final String[] permissions;
        private final int[] grantResults;

        public OnRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            this.requestCode = requestCode;
            this.permissions = permissions;
            this.grantResults = grantResults;
        }
    }

    public static class Result {
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
                    .filter(integer -> integer == result.requestCode)
                    .firstOrDefault(-1)
                    .toBlocking()
                    .first();

            if (requestCode < 0) {
                return;
            }

            int permissionCount = result.permissions.length;

            for (int idx = 0; idx < permissionCount; idx++) {
                String permission = result.permissions[idx];
                int grantResult = result.grantResults[idx];

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

    public static class Check {
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

}
