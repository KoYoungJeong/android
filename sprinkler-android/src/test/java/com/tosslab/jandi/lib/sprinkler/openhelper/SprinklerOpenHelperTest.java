package com.tosslab.jandi.lib.sprinkler.openhelper;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.lib.sprinkler.domain.TrackId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 *
 */
@Config(manifest = "sprinkler-android/src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklerOpenHelperTest {


    @Test
    public void testTrackerId() throws Exception {
        ConnectionSource connectionSource = new AndroidConnectionSource(
                new SprinklerOpenHelper(Robolectric.application));

        Dao<TrackId, Long> dao = DaoManager.createDao(connectionSource, TrackId.class);

        TrackId data = new TrackId();
        data.setAccountId(1);
        data.setMemberId(101);
        data.setToken("1 : 101 : token1");
        int index = dao.create(data);

        List<TrackId> trackIds = dao.queryForAll();

        assertTrue(index > 0);
        assertThat(trackIds, is(notNullValue()));
        assertThat(trackIds.size(), is(1));
    }
}