package com.tosslab.jandi.app.ui.passcode.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.ui.passcode.model.PassCodeModel;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tonyjs on 16. 2. 25..
 */
@RunWith(AndroidJUnit4.class)
public class PassCodePresenterTest {

    PassCodePresenter presenter;
    PassCodePresenter.View view;

    @Before
    public void setUp() throws Exception {
        view = mock(PassCodePresenter.View.class);
        presenter = new PassCodePresenter(view, new PassCodeModel());
    }

    @Test
    public void testOnPassCodeInput() throws Exception {
        {
            // Given
            int passCode = 1;
            int mode = PassCodeActivity.MODE_TO_SAVE_PASSCODE;
            // When
            presenter.onPassCodeInput(passCode, mode);
            // Then
            verify(view).checkPassCode(anyInt());
        }

        {
            int mode = PassCodeActivity.MODE_TO_SAVE_PASSCODE;
            for (int i = 0; i < 4; i++) {
                int passCode = (int) (Math.random() * 10);
                presenter.onPassCodeInput(passCode, mode);
            }
            verify(view, atLeast(4)).checkPassCode(anyInt());
            verify(view, atMost(1)).clearPassCodeCheckerWithDelay();
            verify(view, atMost(1)).showNeedToValidateText();
        }

        {
            // Given
            String passCode = "1234";
            JandiPreference.setPassCode(JandiApplication.getContext(), passCode);
            int mode = PassCodeActivity.MODE_TO_UNLOCK;

            // When
            presenter.onPassCodeInput(1, mode);
            presenter.onPassCodeInput(2, mode);
            presenter.onPassCodeInput(3, mode);
            presenter.onPassCodeInput(4, mode);

            // Then
            verify(view, atLeast(4)).checkPassCode(anyInt());
            verify(view, atMost(1)).showSuccess();
        }
    }

}