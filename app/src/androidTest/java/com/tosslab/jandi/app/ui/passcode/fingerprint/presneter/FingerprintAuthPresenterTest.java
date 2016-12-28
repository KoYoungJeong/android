package com.tosslab.jandi.app.ui.passcode.fingerprint.presneter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.passcode.fingerprint.dagger.FingerprintAuthModule;
import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@Ignore
@RunWith(AndroidJUnit4.class)
public class FingerprintAuthPresenterTest {

    @Inject
    FingerprintAuthPresenter presenter;
    private FingerprintAuthView view;

    @Before
    public void setup() throws Exception {
        view = mock(FingerprintAuthView.class);
        DaggerFingerprintAuthPresenterTest_Component.builder()
                .fingerprintAuthModule(new FingerprintAuthModule(view))
                .build()
                .inject(this);
    }

    @Test
    public void testOnStartListeningFingerprint() throws Exception {
        // Given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(view).startListening(anyObject());

        // When
        presenter.onStartListeningFingerprint();

        // Then
        verify(view).startListening(anyObject());
    }

    @Test
    public void testOnAuthenticationError() throws Exception {
        // When
        int errorCode = anyInt();
        String errString = anyString();

        presenter.onAuthenticationError(errorCode, errString);

        // Then
        verify(view).showAuthenticationError(errorCode, errString);
    }

    @Test
    public void testOnAuthenticationFailed() throws Exception {
        // When
        presenter.onAuthenticationFailed();

        // Then
        verify(view).showFingerprintAuthFailed();
    }

    @Test
    public void testOnAuthenticationHelp() throws Exception {
        // When
        int helpCode = anyInt();
        String helpString = anyString();

        presenter.onAuthenticationHelp(helpCode, helpString);

        // Then
        verify(view).showAuthenticationHelpDialog(helpString);
    }

    @Test
    public void testOnAuthenticationSucceeded() throws Exception {
        // When
        presenter.onAuthenticationSucceeded(null);

        // Then
        verify(view).setFingerprintAuthSuccess();
    }

    @dagger.Component(modules = FingerprintAuthModule.class)
    public interface Component {
        void inject(FingerprintAuthPresenterTest test);
    }
}