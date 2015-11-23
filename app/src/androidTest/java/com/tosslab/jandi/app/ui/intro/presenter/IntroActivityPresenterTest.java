package com.tosslab.jandi.app.ui.intro.presenter;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

//import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tonyjs on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroActivityPresenterTest extends ApplicationTestCase<Application> {

    IntroActivityPresenter presenter;
    IntroActivityPresenter.View view;
    String occurred;

    public IntroActivityPresenterTest() {
        super(Application.class);
    }

    @Before
    public void init() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class).clearAllData();
        
        JandiApplication.setContext(context);

        presenter = IntroActivityPresenter_.getInstance_(context);
    }

    //FIXME IntroActivityModel.sleep(Thread.sleep) 으로 인해 타이밍 이슈가 있는 것에 대한 고민 필요
    @Test
    public void testCheckNewVersion() throws Exception {
        Context context = JandiApplication.getContext();

        IntroActivityPresenter.View mockView = mock(IntroActivityPresenter.View.class);
        presenter.setView(mockView);

        /** 네트워킹이 되지 않는 경우 **/
        // Given
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        // When
        presenter.checkNewVersion(context, false);

        // Then
        verify(mockView, timeout(3000)).moveToIntroTutorialActivity();

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우 **/
//        // Given
//        ResAccessToken resAccessToken = new ResAccessToken();
//
//        resAccessToken.setRefreshToken("111dsasd-bsdbsb-2214");
//
//        AccessTokenRepository.getRepository().upsertAccessToken(resAccessToken);
//
//        // When
//        presenter.checkNewVersion(context, false);
//
//        // Then
//        verify(mockView, timeout(3000)).showCheckNetworkDialog();

        /** 네트워킹이 되지 않는 경우, 로그인은 한 경우, 계정정보가 있는 경우 **/
//        // Given
//        ResAccountInfo resAccountInfo = new ResAccountInfo();
//        resAccountInfo.setId("13123");
//
//        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
//
//        // When
//        presenter.checkNewVersion(context, false);
//
//        // Then
//        verify(mockView, timeout(3000)).moveTeamSelectActivity();
    }
}