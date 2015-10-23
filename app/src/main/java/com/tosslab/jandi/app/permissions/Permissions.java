package com.tosslab.jandi.app.permissions;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

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

    public static void requestPermission(AppCompatActivity activity, int requestCode, Permission permission) {

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

}
