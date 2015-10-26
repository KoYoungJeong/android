package com.tosslab.jandi.app.permissions;

public class Permissions {

    public static Check getChecker() {
        return new Check();
    }

    public static Result getResult() {
        return new Result();
    }

    public static OnRequestPermissionsResult createPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        return new OnRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
