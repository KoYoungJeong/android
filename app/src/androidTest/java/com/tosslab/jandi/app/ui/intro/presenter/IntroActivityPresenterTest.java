package com.tosslab.jandi.app.ui.intro.presenter;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import setup.BaseInitUtil;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class IntroActivityPresenterTest {

    @Inject
    IntroActivityPresenter presenter;
    private IntroActivityPresenter.View mockView;

    @Before
    public void init() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        BaseInitUtil.clear();

        JandiApplication.setContext(context);
        mockView = mock(IntroActivityPresenter.View.class);

        DaggerIntroTestComponent.builder()
                .introModule(new IntroModule(mockView))
                .build()
                .inject(this);
    }

    @Test
    public void testCheckNewVersion() throws Exception {
        Context context = JandiApplication.getContext();

        /** 잔디가 점검중인 경우 **/
        {
            // Given

            ResConfig mockResConfig = mock(ResConfig.class);
            ResConfig.Maintenance mockMaintenance = mock(ResConfig.Maintenance.class);
            mockMaintenance.status = true;

            mockResConfig.maintenance = mockMaintenance;

            IntroActivityModel mockModel = mock(IntroActivityModel.class);
            when(mockModel.isNetworkConnected()).thenReturn(true);

            when(mockModel.getConfigInfo()).thenReturn(mockResConfig);

            presenter.model = mockModel;

            // When
            presenter.checkNewVersion(false);

            // Then
            verify(mockView, timeout(3000)).showMaintenanceDialog();

            reset(mockView);
        }

        /** 업데이트가 필요한 경우 **/
        {
            // Given
            ResConfig mockResConfig2 = mock(ResConfig.class);
            ResConfig.Versions mockVersions = mock(ResConfig.Versions.class);
            mockVersions.android = 3;
            mockResConfig2.versions = mockVersions;

            IntroActivityModel mockModel2 = mock(IntroActivityModel.class);
            when(mockModel2.isNetworkConnected()).thenReturn(true);

            when(mockModel2.getInstalledAppVersion()).thenReturn(2);
            when(mockModel2.getConfigInfo()).thenReturn(mockResConfig2);

            presenter.model = mockModel2;

            // When
            presenter.checkNewVersion(false);

            // Then
            verify(mockView, timeout(3000)).showUpdateDialog();
            reset(mockView);
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
            // When
            presenter.checkNewVersion(false);

            // Then
            verify(mockView, timeout(3000)).moveToSignHomeActivity();
            reset(mockView);
            reset(mockModel);
        }

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우, 계정정보가 있는 경우 **/
        {

            when(mockModel.hasMigration()).thenReturn(true);
            presenter.model = mockModel;

            // When
            presenter.checkNewVersion(false);

            // Then
            verify(mockView, timeout(3000)).moveTeamSelectActivity();
            reset(mockView);
            reset(mockModel);
        }

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우, 계정보가 있는 경우, 선택한 팀이 있는 경우 **/
        {
            // Given

            when(mockModel.hasSelectedTeam()).thenReturn(true);

            presenter.model = mockModel;

            // When
            presenter.checkNewVersion(false);

            // Then
            verify(mockView, timeout(3000)).moveToMainActivity(anyBoolean());
            reset(mockView);
            reset(mockModel);
        }
    }
}