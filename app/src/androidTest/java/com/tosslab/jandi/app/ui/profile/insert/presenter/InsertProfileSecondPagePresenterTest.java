package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.ui.profile.insert.dagger.InsertProfileSecondPageModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created by tee on 16. 3. 17..
 */
public class InsertProfileSecondPagePresenterTest {
    @Inject
    InsertProfileSecondPagePresenter presenter;
    private InsertProfileSecondPagePresenterImpl.View mockView;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        mockView = mock(InsertProfileSecondPagePresenterImpl.View.class);
        DaggerInsertProfileSecondPagePresenterTest_TestComponent.builder()
                .insertProfileSecondPageModule(new InsertProfileSecondPageModule(mockView))
                .build()
                .inject(this);
    }

    @Test
    public void testOnRequestProfile() {
        // given
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).displayProfileInfos(any());

        // when
        presenter.requestProfile();

        Awaitility.await().until(() -> finish[0]);

        // then
        Mockito.verify(mockView).displayProfileInfos(Mockito.anyObject());
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

    @Component(modules = InsertProfileSecondPageModule.class)
    interface TestComponent {
        void inject(InsertProfileSecondPagePresenterTest test);
    }

}