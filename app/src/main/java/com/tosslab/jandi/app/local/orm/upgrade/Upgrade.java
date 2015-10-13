package com.tosslab.jandi.app.local.orm.upgrade;

import java.sql.SQLException;

public interface Upgrade {
    void onUpgrade() throws SQLException;
}
