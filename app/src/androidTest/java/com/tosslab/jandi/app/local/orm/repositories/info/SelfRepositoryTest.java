package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class SelfRepositoryTest {
    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Test
    public void testIsMe() throws Exception {
        assertThat(SelfRepository.getInstance().isMe(TeamInfoLoader.getInstance().getMyId())).isTrue();
        assertThat(SelfRepository.getInstance().isMe(-1)).isFalse();
    }
}