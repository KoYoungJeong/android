package com.tosslab.jandi.app.ui.invites;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel_;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by tonyjs on 15. 5. 5..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class InviteUtilsTest {
    private TeamDomainInfoModel teamDomainInfoModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
        teamDomainInfoModel = TeamDomainInfoModel_.getInstance_(RuntimeEnvironment.application);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    //FIXME
    @Test
    public void testCheckInvitationDisabled() throws Exception {
        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
//        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
//                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, teamId);
//        assertThat(result.first, is(equalTo(InviteUtils.Result.SUCCESS)));
    }
}