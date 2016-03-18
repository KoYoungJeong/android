package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import setup.BaseInitUtil;

/**
 * Created by tee on 16. 3. 17..
 */
public class SetProfileSecondPagePresenterTest {
    private SetProfileSecondPagePresenter presenter;
    private SetProfileSecondPagePresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        presenter = SetProfileSecondPagePresenter_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(SetProfileSecondPagePresenter.View.class);
        presenter.setView(mockView);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testOnRequestProfile() {
        // given
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        // when
        presenter.requestProfile();

        Awaitility.await().until(() -> finish[0]);

        // then
        Mockito.verify(mockView).showProgressWheel();
        Mockito.verify(mockView).displayProfileInfos(Mockito.anyObject());
        Mockito.verify(mockView).dismissProgressWheel();
    }

    @Test
    public void testChooseEmail() {
        presenter.chooseEmail("dummy");
        Mockito.verify(mockView).showEmailChooseDialog(Mockito.anyObject(), Mockito.anyString());
    }

    @Test
    public void testSetEmail() {
        presenter.setEmail("dummy");
        Mockito.verify(mockView).setEmail(Mockito.anyObject(), Mockito.anyString());
    }

    @Test
    public void testUploadExtraInfo() {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        presenter.uploadExtraInfo("dummy", "dummy", "dummy", "dummy");

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(mockView).updateProfileSucceed();
    }

}