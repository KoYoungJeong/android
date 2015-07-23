package com.tosslab.jandi.app.local.orm;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class OrmDatabaseHelperTest {

    private OrmDatabaseHelper helper;
    private Dao<ResMessages.StickerContent, ?> dao;

    @Before
    public void setUp() throws Exception {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        dao = helper.getDao(ResMessages.StickerContent.class);

    }

    @Test
    public void testPrepareStickerContent() throws Exception {

        List<ResMessages.StickerContent> query = dao.queryBuilder().query();
        assertThat(query.size(), is(equalTo(26)));
    }
}