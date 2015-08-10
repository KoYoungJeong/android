package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.RobolectricGradleTestRunner;

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
@RunWith(RobolectricGradleTestRunner.class)
public class AccountRepositoryTest {


    private AccountRepository repository;
    private ResAccountInfo originAccountInfo;

    @Before
    public void setUp() throws Exception {

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(
                        BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);
        originAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        repository = AccountRepository.getRepository();
        repository.upsertAccountAllInfo(originAccountInfo);

    }

    @Test
    public void testUpsertAccountInfo() throws Exception {


        ResAccountInfo resAccountInfo = repository.getAccountInfo();

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


        repository.upsertAccountAllInfo(originAccountInfo);

        ResAccountInfo accountInfo2 = repository.getAccountInfo();

        assertThat(originAccountInfo.getEmails().size(), is(equalTo(accountInfo2.getEmails().size())));
    }

    @Test
    public void testUpsertSelectedTeamInfo() throws Exception {

        int teamId = originAccountInfo.getMemberships().iterator().next().getTeamId();

        repository.updateSelectedTeamInfo(teamId);

        assertThat(repository.getSelectedTeamId(), is(equalTo(teamId)));
        assertThat(repository.getSelectedTeamInfo().getTeamId(), is(equalTo(teamId)));


    }

    @Test
    public void testGetTeamInfo() throws Exception {
        ResAccountInfo.UserTeam team = originAccountInfo.getMemberships().iterator().next();
        int teamId = team.getTeamId();
        ResAccountInfo.UserTeam teamInfo = repository.getTeamInfo(teamId);

        assertThat(team.getTeamId(), is(equalTo(teamInfo.getTeamId())));
        assertThat(team.getMemberId(), is(equalTo(teamInfo.getMemberId())));
        assertThat(team.getName(), is(equalTo(teamInfo.getName())));
        assertThat(team.getStatus(), is(equalTo(teamInfo.getStatus())));
        assertThat(team.getTeamDomain(), is(equalTo(teamInfo.getTeamDomain())));

    }

    @Test
    public void testGetAccountEmails() throws Exception {

        List<ResAccountInfo.UserEmail> accountEmails = repository.getAccountEmails();

        assertThat(accountEmails.size(), is(equalTo(originAccountInfo.getEmails().size())));

        Iterator<ResAccountInfo.UserEmail> iterator = originAccountInfo.getEmails().iterator();
        assertThat(accountEmails.get(0).getId(), is(equalTo(iterator.next().getId())));

    }

    @Test
    public void testDeleteAccountInfo() throws Exception {

        repository.deleteAccountInfo();


        ResAccountInfo accountInfo = repository.getAccountInfo();

        assertThat(accountInfo, is(nullValue()));
    }

    @Test
    public void testGetAccountTeams() throws Exception {

        List<ResAccountInfo.UserTeam> accountTeams = repository.getAccountTeams();

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

        repository.updateSelectedTeamInfo(teamId);

        ResAccountInfo.UserTeam selectedTeamInfo = repository.getSelectedTeamInfo();

        assertThat(selectedTeamInfo.getTeamDomain(), is(equalTo(next.getTeamDomain())));
        assertThat(selectedTeamInfo.getTeamId(), is(equalTo(next.getTeamId())));

    }


    @Test
    public void testUpsertUserEmail() throws Exception {

        List<ResAccountInfo.UserEmail> accountEmails = repository.getAccountEmails();
        ResAccountInfo.UserEmail remove = accountEmails.remove(0);

        repository.upsertUserEmail(accountEmails);

        List<ResAccountInfo.UserEmail> newAccountEmails = repository.getAccountEmails();

        Observable.from(newAccountEmails)
                .filter(userEmail -> userEmail.getId().equals(remove.getId()))
                .subscribe(userEmail1 -> fail("expect : " + accountEmails +
                        "\bactual : " + newAccountEmails));

    }
}