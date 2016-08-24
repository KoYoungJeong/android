package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by tee on 2016. 8. 17..
 */
@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialPollInfoRepositoryTest {

    public static final int STANDARD_VALUE = 10;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

        OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        Dao<InitialInfo.Poll, ?> dao = helper.getDao(InitialInfo.Poll.class);
        InitialInfo.Poll data = new InitialInfo.Poll();
        data.setVotableCount(STANDARD_VALUE);
        dao.createOrUpdate(data);

        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void plusVotableCount() throws Exception {
        boolean success = InitialPollInfoRepository.getInstance().increaseVotableCount();
        assertThat(success).isTrue();

        int votableCount = InitialPollInfoRepository.getInstance().getVotableCount();
        assertThat(votableCount).isEqualTo(STANDARD_VALUE + 1);
    }

    @Test
    public void minusVotableCount() throws Exception {
        boolean success = InitialPollInfoRepository.getInstance().decreaseVotableCount();
        assertThat(success).isTrue();

        int votableCount = InitialPollInfoRepository.getInstance().getVotableCount();
        assertThat(votableCount).isEqualTo(STANDARD_VALUE - 1);
    }

    @Test
    public void getVotableCount() throws Exception {
        assertThat(InitialPollInfoRepository.getInstance().getVotableCount())
                .isEqualTo(TeamInfoLoader.getInstance().getPollBadge())
                .isEqualTo(STANDARD_VALUE);
    }


}


