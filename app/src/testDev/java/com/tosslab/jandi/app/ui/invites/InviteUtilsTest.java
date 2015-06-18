package com.tosslab.jandi.app.ui.invites;

import android.util.Pair;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.member.TeamInfoActivity;
import com.tosslab.jandi.app.ui.member.TeamInfoActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel_;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by tonyjs on 15. 5. 5..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class InviteUtilsTest {
    private TeamDomainInfoModel teamDomainInfoModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        teamDomainInfoModel = TeamDomainInfoModel_.getInstance_(Robolectric.application);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    //FIXME
    @Test
    public void testCheckInvitationDisabled() throws Exception {
        int teamId = JandiAccountDatabaseManager.getInstance(Robolectric.application)
                .getUserTeams().get(0).getTeamId();
//        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
//                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, teamId);
//        assertThat(result.first, is(equalTo(InviteUtils.Result.SUCCESS)));
    }
}