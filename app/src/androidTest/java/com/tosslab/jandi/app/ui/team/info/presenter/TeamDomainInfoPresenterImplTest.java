package com.tosslab.jandi.app.ui.team.info.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModelTest;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import rx.Observable;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(AndroidJUnit4.class)
public class TeamDomainInfoPresenterImplTest {

    private TeamDomainInfoPresenter presenter;
    private TeamDomainInfoPresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        presenter = TeamDomainInfoPresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = mock(TeamDomainInfoPresenter.View.class);
        presenter.setView(mockView);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testCheckEmailInfo() throws Exception {
        presenter.checkEmailInfo();
        verify(mockView, times(0)).finishView();
        verify(mockView, times(0)).showFailToast(anyString());

        BaseInitUtil.clear();
        presenter.checkEmailInfo();
        verify(mockView, times(1)).showFailToast(anyString());
        verify(mockView, times(1)).finishView();

    }

    @Test
    public void testCreateTeam() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        StringBuffer teamNameBuffer = new StringBuffer();
        Observable.range(0, 6)
                .map(integer -> ((char) (Math.random() * ('z' - 'a') + 'a')))
                .collect(() -> teamNameBuffer, (stringBuffer, aDouble) -> stringBuffer.append(aDouble))
                .subscribe();

        String teamName = teamNameBuffer.toString();

        presenter.createTeam(teamName, teamName);
        await().timeout(30, TimeUnit.SECONDS).until(() -> finish[0]);

        verify(mockView, times(1)).showProgressWheel();
        verify(mockView, times(1)).successCreateTeam(teamName);

        new Retrofit.Builder()
                .baseUrl(JandiConstantsForFlavors.SERVICE_INNER_API_URL)
                .build()
                .create(TeamDomainInfoModelTest.Team.class)
                .deleteTeam(TokenUtil.getRequestAuthentication(), AccountRepository.getRepository().getSelectedTeamId(), new TeamDomainInfoModelTest.ReqDeleteTeam());
    }
}