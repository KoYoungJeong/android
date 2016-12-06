package com.tosslab.jandi.app.local.orm.upgrade;

import java.util.concurrent.Callable;

public class RealmUpgradeChecker {
    private Callable<Long> minimumVersion;
    private Runnable upgrade;

    private RealmUpgradeChecker(Callable<Long> minimumVersion, Runnable upgrade) {
        this.minimumVersion = minimumVersion;
        this.upgrade = upgrade;
    }

    public static RealmUpgradeChecker create(Callable<Long> minimumVersion, Runnable upgrade) {
        return new RealmUpgradeChecker(minimumVersion, upgrade);
    }

    public void run(long oldVersion) {

        if (minimumVersion == null || upgrade == null) {
            return;
        }

        long call;
        try {
            call = minimumVersion.call();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (oldVersion < call) {
            upgrade.run();
        }
    }
}
