package com.tosslab.jandi.app.local.database;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiDatabaseOpenHelperTest {

    @Test
    public void testInit() throws Exception {
        JandiAccountDatabaseManager jandiAccountDatabaseManager = JandiAccountDatabaseManager.getInstance(Robolectric.application);

        assertTrue(true);

    }
}