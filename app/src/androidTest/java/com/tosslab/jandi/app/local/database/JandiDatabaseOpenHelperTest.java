package com.tosslab.jandi.app.local.database;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiDatabaseOpenHelperTest {

    @Test
    public void testInit() throws Exception {
        JandiDatabaseManager jandiDatabaseManager = JandiDatabaseManager.getInstance(Robolectric.application);
        SQLiteDatabase readableDatabase = jandiDatabaseManager.getReadableDatabase();

        assertTrue(true);

        System.out.println(readableDatabase.getPath());

    }
}