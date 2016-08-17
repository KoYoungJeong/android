package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.team.TeamInfoLoader;

import junit.framework.Assert;

import org.junit.Test;

import setup.BaseInitUtil;

/**
 * Created by tee on 2016. 8. 17..
 */
@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialPollInfoRepositoryTest {

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Test
    public void getVotableCount() throws Exception {
        Assert.assertEquals(InitialPollInfoRepository.getInstance().getVotableCount(),
                TeamInfoLoader.getInstance().getPollBadge());
    }

}


