package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class HumanRepositoryTest {
    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Test
    public void testAddHuman() throws Exception {
        // Given
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        Human human = getHuman(teamId);

        // When
        HumanRepository.getInstance().addHuman(teamId, human);

        // Then
        Human human1 = HumanRepository.getInstance().getHuman(1);
        assertThat(human1).isNotNull();
        assertThat(human1.getName()).isEqualTo("test");
        assertThat(human1.getPhotoUrl()).isEqualTo("photoUrl");
        assertThat(human1.getInitialInfo()).isNotNull();
        assertThat(human1.getInitialInfo().getTeamId()).isEqualTo(teamId);

    }

    @NonNull
    private Human getHuman(long teamId) {
        Human human = new Human();
        human.setId(1);
        human.setTeamId(teamId);
        human.setName("test");
        human.setPhotoUrl("photoUrl");
        InitialInfo initialInfo = new InitialInfo();
        initialInfo.setTeamId(teamId);
        human.setInitialInfo(initialInfo);
        return human;
    }
}