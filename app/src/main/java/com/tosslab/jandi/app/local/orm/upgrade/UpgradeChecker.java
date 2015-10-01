package com.tosslab.jandi.app.local.orm.upgrade;

import java.sql.SQLException;

public class UpgradeChecker {
    private MinimumVersion minimumVersion;
    private Upgrade upgrade;

    private UpgradeChecker(MinimumVersion minimumVersion, Upgrade upgrade) {
        this.minimumVersion = minimumVersion;
        this.upgrade = upgrade;
    }

    public static UpgradeChecker create(MinimumVersion minimumVersion, Upgrade upgrade) {
        return new UpgradeChecker(minimumVersion, upgrade);
    }

    public void run(int oldVersion) {

        if (minimumVersion == null || upgrade == null) {
            return;
        }

        if (oldVersion <= minimumVersion.getMinimumVersion()) {
            try {
                upgrade.onUpgrade();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
