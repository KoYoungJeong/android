package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class HumanRepositoryTest {

    private static InitialInfo initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();

    }

    @Test
    public void testGetHuman() throws Exception {
        long myId = initializeInfo.getSelf().getId();
        assertThat(HumanRepository.getInstance().isHuman(myId)).isTrue();
        assertThat(HumanRepository.getInstance().isHuman(0)).isFalse();
    }

    @Test
    public void testAddHuman() throws Exception {
        // Given
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        Human human = getHuman();

        // When
        HumanRepository.getInstance().addHuman(teamId, human);

        // Then
        Human human1 = HumanRepository.getInstance().getHuman(1);
        assertThat(human1).isNotNull();
        assertThat(human1.getName()).isEqualTo("test");
        assertThat(human1.getPhotoUrl()).isEqualTo("photoUrl");

    }

    @NonNull
    private Human getHuman() {
        Human human = new Human();
        human.setId(1);
        human.setTeamId(teamId);
        human.setName("test");
        human.setPhotoUrl("photoUrl");
        InitialInfo initialInfo = new InitialInfo();
        initialInfo.setTeamId(teamId);
        return human;
    }

    @Test
    public void testIsHuman() throws Exception {
        {
            assertThat(HumanRepository.getInstance().isHuman(getHuman().getId())).isFalse();
        }

        {
            HumanRepository.getInstance().addHuman(teamId, getHuman());
            assertThat(HumanRepository.getInstance().isHuman(getHuman().getId())).isTrue();
        }

    }

    @Test
    public void testGetMemberCount() throws Exception {
        int memberCount = HumanRepository.getInstance().getMemberCount(teamId);

        assertThat(memberCount).isGreaterThanOrEqualTo(1);

        HumanRepository.getInstance().addHuman(teamId, getHuman());

        assertThat(HumanRepository.getInstance().getMemberCount(teamId))
                .isGreaterThan(memberCount);
    }

    @Test
    public void testUpdateStatus() throws Exception {
        HumanRepository.getInstance().addHuman(teamId, getHuman());

        String status = "disabled";
        HumanRepository.getInstance().updateStatus(getHuman().getId(), status);

        Human human = HumanRepository.getInstance().getHuman(getHuman().getId());
        assertThat(human.getStatus()).isEqualToIgnoringCase(status);

    }

    @Test
    public void testUpdatePhotoUrl() throws Exception {
        HumanRepository.getInstance().addHuman(teamId, getHuman());
        String photoUrl = "http://";
        HumanRepository.getInstance().updatePhotoUrl(getHuman().getId(), photoUrl);
        assertThat(HumanRepository.getInstance().getHuman(getHuman().getId()).getPhotoUrl())
                .isEqualToIgnoringCase(photoUrl);
    }

    @Test
    public void testUpdateHuman() throws Exception {
        Human human = getHuman();
        HumanRepository.getInstance().addHuman(teamId, human);
        human.setName("name2");
        HumanRepository.getInstance().updateHuman(human);

        Human human1 = HumanRepository.getInstance().getHuman(getHuman().getId());
        assertThat(human1.getName()).isEqualToIgnoringCase(human.getName());
    }

    @Test
    public void testUpdateStarred() throws Exception {
        HumanRepository.getInstance().addHuman(teamId, getHuman());
        HumanRepository.getInstance().updateStarred(getHuman().getId(), true);

        assertThat(HumanRepository.getInstance().getHuman(getHuman().getId()).isStarred())
                .isTrue();
    }
}