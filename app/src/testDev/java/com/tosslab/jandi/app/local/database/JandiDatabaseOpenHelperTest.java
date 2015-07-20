package com.tosslab.jandi.app.local.database;


import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiDatabaseOpenHelperTest {

    @Test
    public void testInit() throws Exception {
        AccountRepository repository = AccountRepository.getRepository();

        assertTrue(true);

    }
}