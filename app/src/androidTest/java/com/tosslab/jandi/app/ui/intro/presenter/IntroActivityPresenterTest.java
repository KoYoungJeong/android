package com.tosslab.jandi.app.ui.intro.presenter;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tonyjs on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroActivityPresenterTest {

    @Before
    public void init() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        BaseInitUtil.clear();

        JandiApplication.setContext(context);
    }

    @Test
    public void testCheckNewVersion() throws Exception {
        Context context = JandiApplication.getContext();

        /** 잔디가 점검중인 경우 **/
        {
            // Given
            IntroActivityPresenter.View mockView = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter = IntroActivityPresenter_.getInstance_(context);
            presenter.setView(mockView);

            ResConfig mockResConfig = mock(ResConfig.class);
            ResConfig.Maintenance mockMaintenance = mock(ResConfig.Maintenance.class);
            mockMaintenance.status = true;

            mockResConfig.maintenance = mockMaintenance;

            IntroActivityModel mockModel = mock(IntroActivityModel.class);
            when(mockModel.isNetworkConnected()).thenReturn(true);

            when(mockModel.getConfigInfo()).thenReturn(mockResConfig);

            presenter.model = mockModel;

            // When
            presenter.checkNewVersion(context, false);

            // Then
            verify(mockView, timeout(3000)).showMaintenanceDialog();
        }

        /** 업데이트가 필요한 경우 **/
        {
            // Given
            IntroActivityPresenter.View mockView2 = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter2 = IntroActivityPresenter_.getInstance_(context);
            presenter2.setView(mockView2);

            ResConfig mockResConfig2 = mock(ResConfig.class);
            ResConfig.Versions mockVersions = mock(ResConfig.Versions.class);
            mockVersions.android = 3;
            mockResConfig2.versions = mockVersions;

            IntroActivityModel mockModel2 = mock(IntroActivityModel.class);
            when(mockModel2.isNetworkConnected()).thenReturn(true);

            when(mockModel2.getInstalledAppVersion()).thenReturn(2);
            when(mockModel2.getConfigInfo()).thenReturn(mockResConfig2);

            presenter2.model = mockModel2;

            // When
            presenter2.checkNewVersion(context, false);

            // Then
            verify(mockView2, timeout(3000)).showUpdateDialog();
        }
    }

    /**
     * checkNewVersion 네트워킹이 되지 않는 경우
     **/
    @Test
    public void testCheckNewVersionWithoutNetworking() throws Exception {
        Context context = JandiApplication.getContext();

        IntroActivityModel mockModel = mock(IntroActivityModel.class);

        when(mockModel.isNetworkConnected()).thenReturn(false);
        {
            // Given
            IntroActivityPresenter.View mockView = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter = IntroActivityPresenter_.getInstance_(context);
            presenter.setView(mockView);

            // When
            presenter.checkNewVersion(context, false);

            // Then
            verify(mockView, timeout(3000)).moveToIntroTutorialActivity();
        }

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우 **/
        {
            // Given
            IntroActivityPresenter.View mockView2 = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter2 = IntroActivityPresenter_.getInstance_(context);
            presenter2.setView(mockView2);

            when(mockModel.isNeedLogin()).thenReturn(false);
            presenter2.model = mockModel;

            // When
            presenter2.checkNewVersion(context, false);

            // Then
            verify(mockView2, timeout(3000)).showCheckNetworkDialog();
        }

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우, 계정정보가 있는 경우 **/
        {
            // Given
            IntroActivityPresenter.View mockView3 = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter3 = IntroActivityPresenter_.getInstance_(context);
            presenter3.setView(mockView3);

            when(mockModel.hasMigration()).thenReturn(true);
            presenter3.model = mockModel;

            // When
            presenter3.checkNewVersion(context, false);

            // Then
            verify(mockView3, timeout(3000)).moveTeamSelectActivity();
        }

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우, 계정보가 있는 경우, 선택한 팀이 있는 경우 **/
        {
            // Given
            IntroActivityPresenter.View mockView4 = mock(IntroActivityPresenter.View.class);
            IntroActivityPresenter presenter4 = IntroActivityPresenter_.getInstance_(context);
            presenter4.setView(mockView4);

            when(mockModel.hasSelectedTeam()).thenReturn(true);

            presenter4.model = mockModel;

            // When
            presenter4.checkNewVersion(context, false);

            // Then
            verify(mockView4, timeout(3000)).moveToMainActivity();
        }
    }
}