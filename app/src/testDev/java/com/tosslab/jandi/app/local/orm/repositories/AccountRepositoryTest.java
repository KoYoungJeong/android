package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;

import java.util.Iterator;
import java.util.List;

import rx.Observable;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class AccountRepositoryTest {


    private ResAccountInfo originAccountInfo;

    @Before
    public void setUp() throws Exception {

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(
                        BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);
        originAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        AccountRepository.getRepository().upsertAccountAllInfo(originAccountInfo);

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testUpsertAccountInfo() throws Exception {


        ResAccountInfo resAccountInfo = AccountRepository.getRepository().getAccountInfo();

        assertThat(resAccountInfo, is(notNullValue()));
        assertThat(resAccountInfo.getId(), is(originAccountInfo.getId()));
        assertThat(resAccountInfo.getMemberships().size(), is(originAccountInfo.getMemberships().size()));
        assertThat(resAccountInfo.getEmails().size(), is(originAccountInfo.getEmails().size()));

        assertThat(resAccountInfo.getThumbnailInfo().getLargeThumbnailUrl(),
                is(originAccountInfo.getThumbnailInfo().getLargeThumbnailUrl()));
        assertThat(resAccountInfo.getThumbnailInfo().getMediumThumbnailUrl(),
                is(originAccountInfo.getThumbnailInfo().getMediumThumbnailUrl()));
        assertThat(resAccountInfo.getThumbnailInfo().getSmallThumbnailUrl(),
                is(originAccountInfo.getThumbnailInfo().getSmallThumbnailUrl()));


        AccountRepository.getRepository().upsertAccountAllInfo(originAccountInfo);

        ResAccountInfo accountInfo2 = AccountRepository.getRepository().getAccountInfo();

        assertThat(originAccountInfo.getEmails().size(), is(equalTo(accountInfo2.getEmails().size())));
    }

    @Test
    public void testUpsertSelectedTeamInfo() throws Exception {

        int teamId = originAccountInfo.getMemberships().iterator().next().getTeamId();

        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        assertThat(AccountRepository.getRepository().getSelectedTeamId(), is(equalTo(teamId)));
        assertThat(AccountRepository.getRepository().getSelectedTeamInfo().getTeamId(), is(equalTo(teamId)));


    }

    @Test
    public void testGetTeamInfo() throws Exception {
        ResAccountInfo.UserTeam team = originAccountInfo.getMemberships().iterator().next();
        int teamId = team.getTeamId();
        ResAccountInfo.UserTeam teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);

        assertThat(team.getTeamId(), is(equalTo(teamInfo.getTeamId())));
        assertThat(team.getMemberId(), is(equalTo(teamInfo.getMemberId())));
        assertThat(team.getName(), is(equalTo(teamInfo.getName())));
        assertThat(team.getStatus(), is(equalTo(teamInfo.getStatus())));
        assertThat(team.getTeamDomain(), is(equalTo(teamInfo.getTeamDomain())));

    }

    @Test
    public void testGetAccountEmails() throws Exception {

        List<ResAccountInfo.UserEmail> accountEmails = AccountRepository.getRepository().getAccountEmails();

        assertThat(accountEmails.size(), is(equalTo(originAccountInfo.getEmails().size())));

        Iterator<ResAccountInfo.UserEmail> iterator = originAccountInfo.getEmails().iterator();
        assertThat(accountEmails.get(0).getId(), is(equalTo(iterator.next().getId())));

    }

    @Test
    public void testDeleteAccountInfo() throws Exception {

        AccountRepository.getRepository().deleteAccountInfo();


        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        assertThat(accountInfo, is(nullValue()));
    }

    @Test
    public void testGetAccountTeams() throws Exception {

        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();

        assertThat(accountTeams.size(), is(equalTo(originAccountInfo.getMemberships().size())));

        Observable.from(accountTeams)
                .filter(userTeam -> Observable.from(originAccountInfo.getMemberships())
                        .filter(userTeam1 -> userTeam.getTeamId() == userTeam1.getTeamId())
                        .map(userTeam2 -> false)
                        .toBlocking()
                        .first())
                .subscribe(userTeam3 -> fail("Cannot find " + userTeam3.getName()));

    }

    @Test
    public void testGetSelectedTeamId() throws Exception {

        ResAccountInfo.UserTeam next = originAccountInfo.getMemberships().iterator().next();
        int teamId = next.getTeamId();

        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        assertThat(selectedTeamInfo.getTeamDomain(), is(equalTo(next.getTeamDomain())));
        assertThat(selectedTeamInfo.getTeamId(), is(equalTo(next.getTeamId())));

    }


    @Test
    public void testUpsertUserEmail() throws Exception {

        List<ResAccountInfo.UserEmail> accountEmails = AccountRepository.getRepository().getAccountEmails();
        ResAccountInfo.UserEmail remove = accountEmails.remove(0);

        AccountRepository.getRepository().upsertUserEmail(accountEmails);

        List<ResAccountInfo.UserEmail> newAccountEmails = AccountRepository.getRepository().getAccountEmails();

        Observable.from(newAccountEmails)
                .filter(userEmail -> userEmail.getId().equals(remove.getId()))
                .subscribe(userEmail1 -> fail("expect : " + accountEmails +
                        "\bactual : " + newAccountEmails));

    }
}